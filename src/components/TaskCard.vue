<template>
  <div class="task-card-inner">
    <!-- 任务标题和完成状态 -->
    <div class="task-title-section">
      <input 
        type="checkbox"
        class="task-checkbox"
        :checked="task.completed"
        @click.stop
      />
      <h3 class="task-title">{{ task.title }}</h3>
      <span v-if="task.completed" class="completed-tag">{{ $t('taskManagement.completed') }}</span>
    </div>

    <!-- 任务描述 -->
    <p v-if="task.description" class="task-description">{{ truncateText(task.description, 100) }}</p>

    <!-- 任务元数据 -->
    <div class="task-meta">
      <!-- 截止日期 -->
      <div class="meta-item date" v-if="task.endDate">
        <i class="bi-calendar2"></i>
        <span>{{ formatDate(task.endDate) }}</span>
      </div>

      <!-- 分类标签 -->
      <div class="meta-item category" v-if="task.category">
        <span 
          class="category-badge"
          :style="{ backgroundColor: getCategoryColor ? getCategoryColor(task.category) : '#6c757d' }"
        >
          {{ getCategoryName ? getCategoryName(task.category) : task.category }}
        </span>
      </div>

      <!-- 优先级标签 -->
      <div class="meta-item priority" v-if="task.priority">
        <span class="priority-badge" :class="`priority-${task.priority}`">
          {{ task.priority === 'high' ? '高' : task.priority === 'medium' ? '中' : '低' }}
        </span>
      </div>
    </div>

    <!-- 任务进度信息 -->
    <div class="task-progress-section">
      <!-- 待办事项数量 -->
      <div class="todo-count" v-if="task.todos && task.todos.length > 0">
        <i class="bi-list-task"></i>
        <span>{{ getCompletedTodosCount ? getCompletedTodosCount(task) : 0 }}/{{ task.todos.length }} {{ $t('taskManagement.todos') }}</span>
      </div>

      <!-- 任务进度条 -->
      <div class="task-progress" v-if="task.todos && task.todos.length > 0">
        <div class="progress-info">
          <span class="progress-text">{{ getTaskProgress ? getTaskProgress(task) : 0 }}%</span>
        </div>
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: (getTaskProgress ? getTaskProgress(task) : 0) + '%' }"
            :class="{
              'completed': getTaskProgress && getTaskProgress(task) === 100,
              'in-progress': getTaskProgress && getTaskProgress(task) > 0 && getTaskProgress(task) < 100,
              'not-started': getTaskProgress && getTaskProgress(task) === 0
            }"
          ></div>
        </div>
      </div>
      
      <!-- 显示关联的待办事项列表（简化版） -->
      <div class="associated-todos" v-if="task.todos && task.todos.length > 0 && getCompletedTodosCount">
        <div class="todo-list-mini">
          <div 
            v-for="(todoId, index) in task.todos.slice(0, 3)" 
            :key="index" 
            class="todo-item-mini"
            :class="{ 'completed': isTodoCompleted(todoId) }"
          >
            <i class="bi-check-circle-fill" v-if="isTodoCompleted(todoId)"></i>
            <i class="bi-circle" v-else></i>
            <span class="todo-text-mini">{{ getTodoText(todoId) || `待办事项 ${index + 1}` }}</span>
          </div>
          <div v-if="task.todos.length > 3" class="todo-more">
            +{{ task.todos.length - 3 }} 更多
          </div>
        </div>
      </div>
    </div>

    <!-- 时间使用情况 -->
    <div class="time-usage-section" v-if="task.estimatedTime || task.actualTime">
      <div class="time-info">
        <span class="time-label">{{ $t('taskManagement.timeUsage') }}</span>
        <span class="time-data">
          {{ task.actualTime || 0 }}/{{ task.estimatedTime || 0 }}h
        </span>
      </div>
      <div v-if="task.estimatedTime" class="time-progress-bar">
        <div 
          class="time-progress-fill" 
          :style="{ width: task.estimatedTime ? Math.min(100, (task.actualTime || 0) / task.estimatedTime * 100) : 0 + '%' }"
          :class="{ 'over-budget': task.estimatedTime && task.actualTime > task.estimatedTime }"
        ></div>
      </div>
    </div>

    <!-- 状态标签 -->
    <div class="task-status-badges">
      <span v-if="isOverdue && isOverdue(task)" class="status-badge overdue">
        {{ $t('taskManagement.overdue') }}
      </span>
      <span v-else-if="isDueSoon && isDueSoon(task)" class="status-badge due-soon">
        {{ $t('taskManagement.dueSoon') }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TaskCard',
  props: {
    task: {
      type: Object,
      required: true
    },
    getCategoryName: {
      type: Function,
      default: null
    },
    getCategoryColor: {
      type: Function,
      default: null
    },
    formatDate: {
      type: Function,
      default: null
    },
    isOverdue: {
      type: Function,
      default: null
    },
    isDueSoon: {
      type: Function,
      default: null
    },
    getTaskProgress: {
      type: Function,
      default: null
    },
    getCompletedTodosCount: {
      type: Function,
      default: null
    }
  },
  methods: {
    truncateText(text, maxLength) {
      if (text.length <= maxLength) {
        return text;
      }
      return text.substring(0, maxLength) + '...';
    },
    
    // 检查todo是否已完成
    isTodoCompleted(todoId) {
      if (!this.getCompletedTodosCount || !this.task) {
        return false;
      }
      
      // 尝试从store中查找特定todo的完成状态
      const allTodoLists = this.$store.getters.todoLists;
      
      // 遍历所有待办事项列表查找匹配的todo
      for (const todoList of Object.values(allTodoLists)) {
        const matchedTodo = todoList.find(todo => 
          todo.id === todoId || 
          (typeof todoId === 'string' && todoId.includes('_') && 
           todo.text === todoId.split('_')[1])
        );
        
        if (matchedTodo) {
          return matchedTodo.checked;
        }
      }
      
      // 如果未找到，使用任务的整体完成状态作为后备判断
      const completedCount = this.getCompletedTodosCount(this.task);
      const totalCount = this.task.todos ? this.task.todos.length : 0;
      
      return totalCount > 0 && completedCount === totalCount;
    },
    
    // 获取todo文本内容
    getTodoText(todoId) {
      // 尝试从store中查找特定todo的文本内容
      const allTodoLists = this.$store.getters.todoLists;
      
      // 遍历所有待办事项列表查找匹配的todo
      for (const todoList of Object.values(allTodoLists)) {
        const matchedTodo = todoList.find(todo => 
          todo.id === todoId || 
          (typeof todoId === 'string' && todoId.includes('_') && 
           todo.text === todoId.split('_')[1])
        );
        
        if (matchedTodo) {
          return matchedTodo.text;
        }
      }
      
      // 如果未找到，返回空字符串，让UI显示默认文本
      return '';
    }
  }
};
</script>

<style scoped>
.task-card-inner {
  width: 100%;
}

.task-title-section {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.task-checkbox {
  margin-top: 4px;
  width: 16px;
  height: 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.task-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #212529;
  line-height: 1.4;
  flex-grow: 1;
  word-break: break-word;
}

.completed-tag {
  background-color: #28a745;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.task-description {
  margin: 0 0 12px 0;
  color: #6c757d;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #6c757d;
}

.meta-item.date {
  gap: 4px;
}

.category-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
  font-weight: 500;
}

.priority-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.priority-high {
  background-color: #f8d7da;
  color: #dc3545;
}

.priority-medium {
  background-color: #fff3cd;
  color: #ffc107;
}

.priority-low {
  background-color: #d1ecf1;
  color: #17a2b8;
}

/* 任务进度部分 */
.task-progress-section {
  margin-bottom: 12px;
}

.todo-count {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6c757d;
}

/* 任务进度部分 */
.task-progress-section {
  margin-bottom: 12px;
}

.todo-count {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6c757d;
}

.task-progress {
  margin-bottom: 8px;
  border-top: 1px solid #e9ecef;
  padding-top: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
  color: #495057;
}

.progress-text {
  font-weight: 600;
}

.progress-bar {
  height: 8px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-fill.completed {
  background-color: #28a745;
}

.progress-fill.in-progress {
  background-color: #007bff;
}

.progress-fill.not-started {
  background-color: #6c757d;
}

.associated-todos {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.todo-list-mini {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-item-mini {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  padding: 4px 0;
}

.todo-item-mini.completed {
  color: #6c757d;
  text-decoration: line-through;
}

.todo-text-mini {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-more {
  font-size: 12px;
  color: #6c757d;
  font-style: italic;
  margin-top: 4px;
}

/* 时间使用部分 */
.time-usage-section {
  margin-bottom: 12px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.time-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.time-label {
  color: #6c757d;
}

.time-data {
  font-weight: 600;
  color: #495057;
}

.time-progress-bar {
  height: 6px;
  background-color: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.time-progress-fill {
  height: 100%;
  background-color: #17a2b8;
  transition: width 0.3s ease;
  border-radius: 3px;
}

.time-progress-fill.over-budget {
  background-color: #dc3545;
}

.task-status-badges {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-badge.overdue {
  background-color: #f8d7da;
  color: #dc3545;
}

.status-badge.due-soon {
  background-color: #fff3cd;
  color: #ffc107;
}

/* 深色主题样式 */
.dark-theme .task-title {
  color: #c9d1d9;
}

.dark-theme .task-description {
  color: #8b949e;
}

.dark-theme .meta-item {
  color: #8b949e;
}

.dark-theme .task-progress {
  border-top-color: #21262d;
}

.dark-theme .progress-bar {
  background-color: #21262d;
}

.dark-theme .priority-high {
  background-color: rgba(220, 53, 69, 0.2);
  color: #f8d7da;
}

.dark-theme .priority-medium {
  background-color: rgba(255, 193, 7, 0.2);
  color: #fff3cd;
}

.dark-theme .priority-low {
  background-color: rgba(23, 162, 184, 0.2);
  color: #d1ecf1;
}

.dark-theme .status-badge.overdue {
  background-color: rgba(220, 53, 69, 0.2);
  color: #f8d7da;
}

.dark-theme .status-badge.due-soon {
  background-color: rgba(255, 193, 7, 0.2);
  color: #fff3cd;
}
</style>