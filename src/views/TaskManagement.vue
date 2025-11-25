<template>
  <div class="task-management-container">
    <!-- 页面头部 -->
    <div class="task-management-header">
      <div class="header-left">
        <h1 class="page-title">{{ $t('taskManagement.title') }}</h1>
        <p class="page-subtitle">{{ $t('taskManagement.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <div class="search-container">
          <i class="bi-search search-icon"></i>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('taskManagement.searchTasks')"
            class="search-input"
          />
        </div>
        <button 
          class="btn btn-primary primary-action"
          @click="openCreateTaskModal"
        >
          <i class="bi-plus"></i> {{ $t('taskManagement.newTask') }}
        </button>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="task-toolbar">
      <!-- 分类过滤器 -->
      <div class="filter-section">
        <label class="filter-label">{{ $t('taskManagement.filterByCategory') }}</label>
        <select 
          v-model="selectedCategory"
          class="form-control category-filter-select"
        >
          <option value="">{{ $t('taskManagement.allCategories') }}</option>
          <option v-for="category in allCategories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>

      <!-- 排序选项 -->
      <div class="sort-section">
        <label class="filter-label">{{ $t('taskManagement.sortBy') }}</label>
        <select 
          v-model="sortBy"
          class="form-control sort-select"
          @change="sortTasks"
        >
          <option value="dueDate">{{ $t('taskManagement.dueDate') }}</option>
          <option value="priority">{{ $t('taskManagement.priority') }}</option>
          <option value="createdAt">{{ $t('taskManagement.createdDate') }}</option>
        </select>
        <button 
          class="btn sort-order-btn"
          @click="toggleSortOrder"
          :title="sortOrder === 'asc' ? $t('taskManagement.sortDescending') : $t('taskManagement.sortAscending')"
        >
          <i class="bi-sort-up" v-if="sortOrder === 'asc'"></i>
          <i class="bi-sort-down" v-else></i>
        </button>
      </div>

      <!-- 视图切换 -->
      <div class="view-section">
        <label class="filter-label">{{ $t('taskManagement.view') }}</label>
        <div class="view-toggle">
          <button 
            class="btn view-btn"
            :class="{ active: viewMode === 'card' }"
            @click="viewMode = 'card'"
          :title="$t('taskManagement.cardView')"
          >
            <i class="bi-grid"></i>
          </button>
          <button 
            class="btn view-btn"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
            :title="$t('taskManagement.listView')"
          >
            <i class="bi-list"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="task-stats">
      <div class="stat-card">
        <div class="stat-icon"><i class="bi-check-circle"></i></div>
        <div class="stat-content">
          <div class="stat-number">{{ completedTasksCount }}</div>
          <div class="stat-label">{{ $t('taskManagement.completed') }}</div>
        </div>
      </div>
      <div class="stat-card" :class="{ 'stat-overdue': overdueTasksCount > 0 }">
        <div class="stat-icon" :class="{ 'stat-icon-overdue': overdueTasksCount > 0 }"><i class="bi-exclamation-circle"></i></div>
        <div class="stat-content">
          <div class="stat-number">{{ overdueTasksCount }}</div>
          <div class="stat-label">{{ $t('taskManagement.overdue') }}</div>
        </div>
      </div>
      <div class="stat-card" :class="{ 'stat-due-soon': dueSoonTasksCount > 0 }">
        <div class="stat-icon" :class="{ 'stat-icon-due-soon': dueSoonTasksCount > 0 }"><i class="bi-clock"></i></div>
        <div class="stat-content">
          <div class="stat-number">{{ dueSoonTasksCount }}</div>
          <div class="stat-label">{{ $t('taskManagement.dueSoon') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><i class="bi-calendar"></i></div>
        <div class="stat-content">
          <div class="stat-number">{{ totalTasksCount }}</div>
          <div class="stat-label">{{ $t('taskManagement.totalTasks') }}</div>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div 
      class="task-list"
      :class="{ 'list-view': viewMode === 'list' }"
    >
      <div 
        v-for="task in sortedAndFilteredTasks"
        :key="task.id"
        class="task-card"
        :class="{
          'completed': task.completed,
          'overdue': !task.completed && isOverdue(task),
          'due-soon': !task.completed && isDueSoon(task)
        }"
        :style="{ borderLeft: `4px solid ${task.color || '#2196F3'}` }"
        @click="openTaskDetails(task.id)"
      >
        <div class="task-card-header">
          <div class="task-title-container">
            <input 
              type="checkbox"
              class="task-checkbox"
              :checked="task.completed"
              @click.stop="toggleTaskCompletion(task.id)"
            />
            <h3 class="task-title">{{ task.title }}</h3>
          </div>
          <div class="task-actions">
            <button 
              class="btn task-action-btn"
              @click.stop="openTaskDetails(task.id)"
              :title="$t('taskManagement.editTask')"
            >
              <i class="bi-pencil"></i>
            </button>
            <button 
              class="btn task-action-btn"
              @click.stop="openTaskKanban(task.id)"
              :title="$t('taskManagement.viewKanban')"
            >
              K
            </button>
            <button 
              class="btn task-action-btn danger"
              @click.stop="deleteTask(task.id)"
              :title="$t('taskManagement.deleteTask')"
            >
              <i class="bi-trash"></i>
            </button>
          </div>
        </div>
        
        <div class="task-card-body">
          <p v-if="task.description" class="task-description">{{ truncateText(task.description, 120) }}</p>
          
          <div class="task-meta">
            <div class="meta-item">
              <i class="bi-calendar2"></i>
              <span>{{ formatDate(task.endDate) }}</span>
            </div>
            <div class="meta-item" v-if="task.category">
              <span 
                class="category-badge"
                :style="{ backgroundColor: getCategoryColor(task.category) }"
              >
                {{ getCategoryName(task.category) }}
              </span>
            </div>
            <div class="meta-item" v-if="task.priority">
              <span class="priority-badge" :class="`priority-${task.priority}`">
                {{ $t(`taskManagement.${task.priority}`) }}
              </span>
            </div>
          </div>
          
          <div class="task-progress" v-if="task.todos && task.todos.length > 0">
            <div class="progress-info">
              <span class="progress-text">{{ getTaskProgress(task) }}%</span>
              <span class="progress-label">{{ $t('taskManagement.completed') }}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: getTaskProgress(task) + '%' }"></div>
            </div>
            <div class="todos-count">
              {{ getCompletedTodosCount(task) }}/{{ task.todos.length }} {{ $t('taskManagement.todosCompleted') }}
            </div>
          </div>
          
          <!-- Milestone 进度 -->
          <div class="milestone-progress" v-if="task.milestones && task.milestones.length > 0">
            <div class="progress-info">
              <span class="progress-text">{{ getMilestoneProgress(task) }}%</span>
              <span class="progress-label">{{ $t('taskManagement.milestones') }}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill milestone-progress-fill" :style="{ width: getMilestoneProgress(task) + '%' }"></div>
            </div>
            <div class="milestones-count">
              {{ getCompletedMilestonesCount(task) }}/{{ task.milestones.length }} {{ $t('taskManagement.milestonesCompleted') }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="sortedAndFilteredTasks.length === 0" class="empty-state">
        <div class="empty-icon-container">
          <i class="bi-calendar-check"></i>
        </div>
        <h3 class="empty-title">{{ $t('taskManagement.noTasksFound') }}</h3>
        <p class="empty-description">{{ $t('taskManagement.emptyStateDescription') }}</p>
        <button 
          class="btn btn-primary"
          @click="openCreateTaskModal"
        >
          <i class="bi-plus"></i> {{ $t('taskManagement.createFirstTask') }}
        </button>
      </div>
    </div>
    
    <!-- 创建/编辑任务模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingTask ? $t('taskManagement.editTask') : $t('taskManagement.createTask') }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveTask">
            <div class="form-group">
              <label for="taskTitle">{{ $t('taskManagement.taskTitle') }} <span class="required">*</span></label>
              <input 
                id="taskTitle" 
                v-model="currentTask.title" 
                type="text" 
                class="form-control" 
                required
                :placeholder="$t('taskManagement.enterTaskTitle')"
              >
            </div>
            <div class="form-group">
              <label for="taskDescription">{{ $t('taskManagement.description') }}</label>
              <textarea 
                id="taskDescription" 
                v-model="currentTask.description" 
                class="form-control"
                rows="4"
                :placeholder="$t('taskManagement.enterDescription')"
              ></textarea>
            </div>
            <div class="form-row">
              <div class="form-group col-md-6">
                <label for="startDate">{{ $t('taskManagement.startDate') }} <span class="required">*</span></label>
                <input 
                  id="startDate" 
                  v-model="currentTask.startDate" 
                  type="date" 
                  class="form-control" 
                  required
                >
              </div>
              <div class="form-group col-md-6">
                <label for="endDate">{{ $t('taskManagement.endDate') }} <span class="required">*</span></label>
                <input 
                  id="endDate" 
                  v-model="currentTask.endDate" 
                  type="date" 
                  class="form-control" 
                  required
                >
              </div>
            </div>
            <div class="form-row">
              <div class="form-group col-md-6">
                <label for="taskCategory">{{ $t('taskManagement.category') }}</label>
                <select 
                  id="taskCategory" 
                  v-model="currentTask.category" 
                  class="form-control"
                  required
                >
                  <option value="" disabled selected>{{ $t('taskManagement.selectCategory') }}</option>
                  <option v-for="category in allCategories" :key="category.id" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
              </div>
              <div class="form-group col-md-6">
                <label for="taskPriority">{{ $t('taskManagement.priority') }}</label>
                <select 
                  id="taskPriority" 
                  v-model="currentTask.priority" 
                  class="form-control"
                >
                  <option value="low">{{ $t('taskManagement.low') }}</option>
                  <option value="medium">{{ $t('taskManagement.medium') }}</option>
                  <option value="high">{{ $t('taskManagement.high') }}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label for="taskColor">{{ $t('taskManagement.taskColor') }}</label>
              <div class="color-picker-container">
                <input 
                  id="taskColor" 
                  v-model="currentTask.color" 
                  type="color" 
                  class="form-control color-input"
                >
                <div class="color-swatches">
                  <div 
                    v-for="color in ['#2196F3', '#4CAF50', '#FF9800', '#F44336', '#9C27B0', '#00BCD4', '#FFC107', '#795548']" 
                    :key="color"
                    class="color-swatch"
                    :style="{ backgroundColor: color }"
                    :class="{ active: currentTask.color === color }"
                    @click="currentTask.color = color"
                    :title="color"
                  ></div>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>{{ $t('taskManagement.milestones') }}</label>
              <div class="milestones-container">
                <div 
                  v-for="(milestone, index) in currentTask.milestones" 
                  :key="index"
                  class="milestone-item"
                >
                  <div class="milestone-content">
                    <input 
                      type="checkbox" 
                      v-model="milestone.completed"
                      class="milestone-checkbox"
                    >
                    <input 
                      type="text" 
                      v-model="milestone.title"
                      class="milestone-input"
                      :placeholder="$t('taskManagement.milestonePlaceholder')"
                    >
                  </div>
                  <button 
                    type="button"
                    class="btn btn-danger btn-sm milestone-delete"
                    @click="removeMilestone(index)"
                  >
                    <i class="bi-trash"></i>
                  </button>
                </div>
                <button 
                  type="button"
                  class="btn btn-secondary btn-sm milestone-add"
                  @click="addMilestone"
                >
                  <i class="bi-plus"></i> {{ $t('taskManagement.addMilestone') }}
                </button>
              </div>
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input 
                  v-model="currentTask.completed" 
                  type="checkbox" 
                  class="form-check-input"
                >
                {{ $t('taskManagement.markAsCompleted') }}
              </label>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" @click="closeModal">
                {{ $t('taskManagement.cancel') }}
              </button>
              <button type="submit" class="btn btn-primary">
                {{ $t('taskManagement.save') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
  </template>
  
  <style scoped>
  .color-picker-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .color-input {
    height: 40px;
    width: 100%;
    border: 1px solid #ced4da;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .color-swatches {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .color-swatch {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s ease;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .color-swatch:hover {
    transform: scale(1.1);
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
  }
  
  .color-swatch.active {
    border-color: #333;
    transform: scale(1.1);
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
  }
  
  @media (max-width: 768px) {
    .color-swatches {
      justify-content: center;
    }
    
    .color-swatch {
      width: 28px;
      height: 28px;
    }
  }
  /* Milestone 相关样式 */
  .milestones-container {
    margin-top: 10px;
  }
  
  .milestone-item {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    padding: 8px;
    background-color: #f8f9fa;
    border-radius: 4px;
    border: 1px solid #e9ecef;
  }
  
  .milestone-content {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 8px;
  }
  
  .milestone-checkbox {
    margin: 0;
  }
  
  .milestone-input {
    flex: 1;
    padding: 6px 10px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    font-size: 14px;
    background-color: white;
  }
  
  .milestone-input:focus {
    outline: none;
    border-color: #2196F3;
    box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.25);
  }
  
  .milestone-delete {
    padding: 4px 8px;
    margin-left: 8px;
  }
  
  .milestone-add {
    width: 100%;
    margin-top: 8px;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
  
  /* 任务卡片中的 Milestone 进度样式 */
  .milestone-progress {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #e9ecef;
  }
  
  .milestone-progress .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }
  
  .milestone-progress .progress-label {
    font-size: 13px;
    color: #6c757d;
    font-weight: 500;
  }
  
  .milestone-progress-fill {
    background-color: #9C27B0;
  }
  
  .milestones-count {
    font-size: 12px;
    color: #6c757d;
    margin-top: 4px;
    text-align: right;
  }
  
  @media (max-width: 768px) {
    .milestone-item {
      flex-direction: column;
      align-items: stretch;
      gap: 8px;
    }
    
    .milestone-delete {
      margin-left: 0;
      width: 100%;
    }
    
    .milestone-content {
      flex-direction: row;
    }
  }
  </style>

<script>
import moment from 'moment';
import taskRepository from '../repositories/taskRepository';

export default {
  name: 'TaskManagement',
  data() {
    return {
      showModal: false,
      editingTask: null,
      selectedCategory: '',
      searchQuery: '',
      sortBy: 'dueDate',
      sortOrder: 'asc',
      viewMode: 'card',
      currentTask: {
      title: '',
      description: '',
      startDate: '',
      endDate: '',
      category: '',
      priority: 'medium',
      completed: false,
      todos: [],
      color: '#2196F3',
      milestones: []
    },
    };
  },
  computed: {
    tasks() {
      return this.$store.getters.tasks;
    },
    taskCategories() {
      return this.$store.getters.taskCategories;
    },
    allCategories() {
      // 如果没有从store获取到分类数据，提供一些默认分类
      if (!this.taskCategories || this.taskCategories.length === 0) {
        return [
          { id: 'knowledge', name: this.$t('taskManagement.knowledge'), color: '#4CAF50' },
          { id: 'skill', name: this.$t('taskManagement.skill'), color: '#2196F3' },
          { id: 'certificate', name: this.$t('taskManagement.certificate'), color: '#FF9800' },
        ];
      }
      return this.taskCategories;
    },
    filteredTasks() {
      let allTasks = Object.values(this.tasks);
      
      // 按分类过滤
      if (this.selectedCategory && this.selectedCategory !== '') {
        allTasks = allTasks.filter(task => task.category === this.selectedCategory);
      }
      
      // 按搜索关键词过滤
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        allTasks = allTasks.filter(task => 
          task.title.toLowerCase().includes(query) || 
          (task.description && task.description.toLowerCase().includes(query))
        );
      }
      
      return allTasks;
    },
    sortedAndFilteredTasks() {
      return this.sortTasks([...this.filteredTasks]);
    },
    // 统计数据
    completedTasksCount() {
      return Object.values(this.tasks).filter(task => task.completed).length;
    },
    overdueTasksCount() {
      return Object.values(this.tasks).filter(task => !task.completed && this.isOverdue(task)).length;
    },
    dueSoonTasksCount() {
      return Object.values(this.tasks).filter(task => !task.completed && this.isDueSoon(task)).length;
    },
    totalTasksCount() {
      return Object.keys(this.tasks).length;
    },
  },
  // 搜索逻辑已在computed属性中实现，无需额外的watch监听
  mounted() {
    // 加载任务数据
    this.$store.dispatch('loadTasks');
    this.$store.dispatch('loadTaskCategories');
  },
  methods: {
    // 添加milestone
    addMilestone() {
      // 确保milestones是数组，在Vue 3中直接赋值即可保持响应式
      if (!this.currentTask.milestones) {
        this.currentTask.milestones = [];
      }
      
      // 添加新的里程碑对象
      this.currentTask.milestones.push({
        title: '',
        completed: false
      });
    },
    
    // 删除milestone
    removeMilestone(index) {
      // 确保milestones数组存在
      if (!this.currentTask.milestones) return;
      
      // 在Vue 3中，splice操作会被正确检测为响应式变更
      this.currentTask.milestones.splice(index, 1);
    },
    
    // 打开创建任务模态框
    openCreateTaskModal() {
      this.editingTask = null;
      this.currentTask = {
      title: '',
      description: '',
      startDate: moment().format('YYYY-MM-DD'),
      endDate: moment().add(1, 'week').format('YYYY-MM-DD'),
      category: '',
      priority: 'medium',
      completed: false,
      todos: [],
      color: '#2196F3',
      milestones: []
    };
      this.showModal = true;
    },
    
    // 关闭模态框
    closeModal() {
      this.showModal = false;
      this.editingTask = null;
    },
    
    // 保存任务
    saveTask() {
      // 验证日期
      if (moment(this.currentTask.startDate).isAfter(moment(this.currentTask.endDate))) {
        // 由于notifications模块没有showToast方法，使用浏览器原生alert作为替代
        alert(this.$t('taskManagement.invalidDateRange'));
        return;
      }
      
      if (this.editingTask) {
        // 更新现有任务
        // 确保milestones数组存在，即使为空
        const milestones = Array.isArray(this.currentTask.milestones) ? this.currentTask.milestones : [];
        const updates = {
          ...this.currentTask,
          updatedAt: new Date().toISOString(),
          milestones: milestones
        };
        
        this.$store.commit('updateTask', {
          taskId: this.editingTask,
          updates: updates,
        });
        taskRepository.update(this.editingTask, updates);
        // 由于notifications模块没有showToast方法，暂时省略通知
      } else {
        // 创建新任务
        const taskId = moment().format('YYYYMMDDTHHmmssS');
        // 确保milestones数组存在，即使为空
        const milestones = Array.isArray(this.currentTask.milestones) ? this.currentTask.milestones : [];
        const newTask = {
          ...this.currentTask,
          id: taskId,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          milestones: milestones
        };
        
        this.$store.commit('addTask', newTask);
        taskRepository.update(taskId, newTask);
        // 由于notifications模块没有showToast方法，暂时省略通知
      }
      
      this.closeModal();
    },
    
    // 打开任务详情
    openTaskDetails(taskId) {
      this.editingTask = taskId;
      this.currentTask = { ...this.tasks[taskId] };
      this.showModal = true;
    },
    
    // 打开任务看板
    openTaskKanban(taskId) {
      console.log('Opening task kanban for taskId:', taskId);
      // Set the current task ID in the store - use the correct mutation from mainStore
      this.$store.commit('setCurrentTaskId', taskId);
      
      // Update the showTaskKanban state - use the correct mutation from tasks store
      this.$store.commit('showTaskKanban', true);
      
      // Get the task from store
      const task = this.tasks[taskId];
      console.log('Found task:', task);
      
      // Set active task using the correct mutation from tasks store
      if (task) {
        this.$store.commit('setActiveTask', taskId);
        
        // Find related todos from todoLists
        const relatedTodos = [];
        console.log('Scanning todoLists for taskId:', taskId);
        
        Object.entries(this.$store.state.todoLists || {}).forEach(([listId, todos]) => {
          if (todos && Array.isArray(todos)) {
            const taskTodos = todos.filter(todo => 
              todo && (todo.task === taskId || listId === taskId)
            );
            if (taskTodos.length > 0) {
              console.log(`Found ${taskTodos.length} todos in list ${listId}`);
              relatedTodos.push(...taskTodos);
            }
          }
        });
        
        console.log('Total related todos found:', relatedTodos.length);
        
        // Also check if task has todos property as backup
        if (task.todos && Array.isArray(task.todos)) {
          console.log(`Task has ${task.todos.length} todos in its own property`);
        }
      } else {
        console.error('Task not found in store');
      }
      
      this.$store.commit('showTaskManagement', false);
    },
    
    // 删除任务
    deleteTask(taskId) {
      if (confirm(this.$t('taskManagement.confirmDeleteTask'))) {
        this.$store.commit('removeTask', taskId);
        taskRepository.remove(taskId);
        // 由于notifications模块没有showToast方法，暂时省略通知
      }
    },
    
    // 切换任务完成状态
    toggleTaskCompletion(taskId) {
      const task = this.tasks[taskId];
      const updatedTask = { ...task, completed: !task.completed };
      
      this.$store.commit('updateTask', {
        taskId,
        updates: updatedTask,
      });
      taskRepository.update(taskId, updatedTask);
    },
    
    // 检查任务是否逾期
    isOverdue(task) {
      return moment(task.endDate).isBefore(moment(), 'day');
    },
    
    // 检查任务是否即将到期（7天内）
    isDueSoon(task) {
      const today = moment();
      const endDate = moment(task.endDate);
      return endDate.diff(today, 'days') >= 0 && endDate.diff(today, 'days') <= 7;
    },
    
    // 获取任务进度
    getTaskProgress(task) {
      if (!task.todos || task.todos.length === 0) {
        return task.completed ? 100 : 0;
      }
      
      // 实际应用中，应该根据关联的todo完成状态计算进度
      // 这里简化处理，假设每个todo的权重相同
      const completedTodos = this.getCompletedTodosCount(task);
      return Math.round((completedTodos / task.todos.length) * 100);
    },
    
    // 获取已完成的待办事项数量
    getCompletedTodosCount(task) {
      if (!task.todos || task.todos.length === 0) {
        return 0;
      }
      
      let completedCount = 0;
      const allTodoLists = this.$store.getters.todoLists;
      
      // 遍历任务关联的所有todo ID
      task.todos.forEach(todoId => {
        // 遍历所有待办事项列表
        Object.values(allTodoLists).forEach(todoList => {
          // 在当前列表中查找匹配的todo
          const matchedTodo = todoList.find(todo => 
            todo.id === todoId || 
            (typeof todoId === 'string' && todoId.includes('_') && 
             todo.text === todoId.split('_')[1])
          );
          
          // 如果找到且已完成，增加计数
          if (matchedTodo && matchedTodo.checked) {
            completedCount++;
          }
        });
      });
      
      return completedCount;
    },
    
    // 获取milestone进度
    getMilestoneProgress(task) {
      if (!task.milestones || task.milestones.length === 0) return 0;
      const completedMilestones = task.milestones.filter(milestone => milestone && milestone.completed).length;
      return Math.round((completedMilestones / task.milestones.length) * 100);
    },
    
    // 获取已完成的milestones数量
    getCompletedMilestonesCount(task) {
      if (!task.milestones) return 0;
      return task.milestones.filter(milestone => milestone && milestone.completed).length;
    },
    
    // 获取分类名称
    getCategoryName(categoryId) {
      const category = this.allCategories.find(cat => cat.id === categoryId);
      return category ? category.name : '';
    },
    
    // 获取分类颜色
    getCategoryColor(categoryId) {
      const category = this.allCategories.find(cat => cat.id === categoryId);
      return category ? category.color : '#6c757d';
    },
    
    // 获取特定分类的任务数量
    getTasksCountByCategory(categoryId) {
      return Object.values(this.tasks).filter(task => task.category === categoryId).length;
    },
    
    // 格式化日期
    formatDate(date) {
      return moment(date).format('MMM D, YYYY');
    },
    
    // 排序任务
    sortTasks(tasks) {
      return tasks.sort((a, b) => {
        let comparison = 0;
        
        switch (this.sortBy) {
          case 'dueDate':
            comparison = moment(a.endDate).valueOf() - moment(b.endDate).valueOf();
            break;
          case 'priority': {
            const priorityOrder = { urgent: 4, high: 3, medium: 2, low: 1 };
            comparison = priorityOrder[b.priority] - priorityOrder[a.priority];
            break;
          }
          case 'createdAt':
            comparison = new Date(a.createdAt) - new Date(b.createdAt);
            break;
        }
        
        return this.sortOrder === 'asc' ? comparison : -comparison;
      });
    },
    
    // 切换排序顺序
    toggleSortOrder() {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    },
    
    // 截断文本
    truncateText(text, maxLength) {
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
  },
};
</script>

<style scoped>
/* 基础样式 */
.task-management-container {
  padding: 20px 20px 20px 80px;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 响应式内边距调整 */
@media (max-width: 768px) {
  .task-management-container {
    padding: 15px;
  }
}

/* 页面头部 */
.task-management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}

.header-left .page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #212529;
}

.header-left .page-subtitle {
  margin: 5px 0 0 0;
  color: #6c757d;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-container {
  position: relative;
  width: 300px;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  color: #007bff !important;
  font-size: 18px !important;
  z-index: 100 !important;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 12px 12px 12px 50px !important;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  background-color: white;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.primary-action {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
}

/* 工具栏 */
  .task-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 25px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .category-filter-select {
    width: 200px;
    padding: 8px 12px;
    border: 1px solid #ced4da;
    border-radius: 6px;
    font-size: 14px;
    background-color: white;
    cursor: pointer;
  }

  .category-filter-select:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  }

.filter-section,
.sort-section,
.view-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 500;
  color: #495057;
  white-space: nowrap;
}

.task-categories-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.2s;
}

.category-filter-btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.category-filter-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.category-color-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.task-count {
  font-size: 12px;
  opacity: 0.7;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.sort-order-btn {
  padding: 8px;
  background: white;
  border: 1px solid #ced4da;
  border-radius: 6px;
  color: #6c757d;
}

.view-toggle {
  display: flex;
  background: white;
  border: 1px solid #ced4da;
  border-radius: 6px;
  overflow: hidden;
}

.view-btn {
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: #6c757d;
  transition: all 0.2s;
}

.view-btn:hover {
  background: #f8f9fa;
}

.view-btn.active {
  background: #007bff;
  color: white;
}

/* 统计概览 */
.task-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #007bff;
  color: white;
  border-radius: 50%;
  font-size: 20px;
}

.stat-content .stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #212529;
}

.stat-content .stat-label {
  color: #6c757d;
  font-size: 14px;
}

/* 逾期任务统计卡片样式 */
.stat-card.stat-overdue {
  border: 2px solid #dc3545;
  background-color: #fdf2f2;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.15);
}

.stat-icon-overdue {
  background-color: #dc3545;
  color: white;
  font-size: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card.stat-overdue .stat-content .stat-number {
  color: #dc3545;
  font-size: 28px;
  font-weight: 700;
}

.stat-card.stat-overdue .stat-content .stat-label {
  color: #dc3545;
  font-weight: 500;
}

/* 即将到期任务统计卡片样式 */
.stat-card.stat-due-soon {
  border: 2px solid #ffc107;
  background-color: #fffdf0;
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.15);
}

.stat-icon-due-soon {
  background-color: #ffc107;
  color: white;
  font-size: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card.stat-due-soon .stat-content .stat-number {
  color: #856404;
  font-size: 28px;
  font-weight: 700;
}

.stat-card.stat-due-soon .stat-content .stat-label {
  color: #856404;
  font-weight: 500;
}

/* 任务列表 */
.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.task-list.list-view {
  grid-template-columns: 1fr;
}

.task-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.task-card.completed {
  opacity: 0.7;
  border-left-color: #28a745;
}

.task-card.overdue {
  border-left-color: #dc3545;
  border-left-width: 6px;
  background-color: #fdf2f2;
  box-shadow: 0 2px 12px rgba(220, 53, 69, 0.15);
}

.task-card.overdue .task-title {
  color: #dc3545;
  font-weight: 600;
}

.task-card.due-soon {
  border-left-color: #ffc107;
  border-left-width: 6px;
  background-color: #fffdf0;
  box-shadow: 0 2px 12px rgba(255, 193, 7, 0.15);
}

.task-card.due-soon .task-title {
  color: #856404;
  font-weight: 600;
}

.task-card.completed .task-title {
  text-decoration: line-through;
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.task-title-container {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-grow: 1;
}

.task-checkbox {
  margin-top: 4px;
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.task-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #212529;
  line-height: 1.4;
}

.task-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.task-card:hover .task-actions {
  opacity: 1;
}

.task-action-btn {
  padding: 6px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #6c757d;
  font-size: 14px;
  transition: all 0.2s;
}

.task-action-btn:hover {
  background: #f8f9fa;
}

.task-action-btn.danger:hover {
  background: #f8d7da;
  color: #dc3545;
}

.task-description {
  margin-bottom: 15px;
  color: #6c757d;
  line-height: 1.5;
  font-size: 14px;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6c757d;
}

.category-badge {
  padding: 2px 8px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.priority-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.priority-low {
  background: #d1ecf1;
  color: #0c5460;
}

.priority-medium {
  background: #fff3cd;
  color: #856404;
}

.priority-high {
  background: #ffeaa7;
  color: #856404;
}

.priority-urgent {
  background: #f8d7da;
  color: #721c24;
}

.task-progress {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f1f3f5;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #212529;
}

.progress-label {
  font-size: 12px;
  color: #6c757d;
}

.progress-bar {
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: #007bff;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.todos-count {
  font-size: 12px;
  color: #6c757d;
  text-align: right;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 20px 20px; /* 进一步减少上下内边距，确保无数据时页面不会过长 */
  grid-column: 1 / -1;
}

.empty-icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 120px;
  height: 120px;
  background: #f8f9fa;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.empty-icon-container i {
  font-size: 48px;
  color: #adb5bd;
}

.empty-title {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 500;
  color: #495057;
}

.empty-description {
  margin: 0 0 20px 0;
  color: #6c757d;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  animation: fadeIn 0.2s;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #212529;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: #f8f9fa;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex-grow: 1;
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #495057;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

input[type="text"],
input[type="date"],
textarea,
select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

/* 搜索框样式已在头部样式中定义 */

input[type="text"]:focus,
input[type="date"]:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
  margin-bottom: 0;
}

.form-check-input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

/* 按钮样式 */
.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.btn-outline-secondary {
  background-color: white;
  color: #6c757d;
  border: 1px solid #6c757d;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  color: white;
}

/* 分类徽章样式 */
.category-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  text-align: center;
  min-width: 60px;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .task-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section,
  .sort-section,
  .view-section {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .task-management-header {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }
  
  .header-actions {
    flex-direction: column;
  }
  
  .search-container {
    width: 100%;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .task-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .task-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .task-management-container {
    padding: 15px;
  }
  
  .task-stats {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    margin: 15px;
    max-height: calc(100vh - 30px);
  }
}

/* 深色主题样式 */
.dark-theme .task-management-container {
  color: #c9d1d9;
}

.dark-theme .task-management-header {
  border-bottom-color: #30363d;
}

.dark-theme .header-left .page-title {
  color: #c9d1d9;
}

.dark-theme .header-left .page-subtitle {
  color: #8b949e;
}

.dark-theme .search-input {
  background: #0d1117;
  border-color: #30363d;
  color: #c9d1d9;
}

.dark-theme .search-input:focus {
  border-color: #007bff;
}

.dark-theme .search-icon {
  color: #8b949e;
}

.dark-theme .task-toolbar {
  background: #0d1117;
  border-color: #30363d;
}

.dark-theme .filter-label {
  color: #c9d1d9;
}

.dark-theme .category-filter-btn {
  background: #161b22;
  border-color: #30363d;
  color: #c9d1d9;
}

.dark-theme .category-filter-btn:hover {
  background: #21262d;
  border-color: #484f58;
}

.dark-theme .sort-select {
  background: #161b22;
  border-color: #30363d;
  color: #c9d1d9;
}

.dark-theme .sort-order-btn {
  background: #161b22;
  border-color: #30363d;
  color: #8b949e;
}

.dark-theme .view-toggle {
  background: #161b22;
  border-color: #30363d;
}

.dark-theme .view-btn {
  color: #8b949e;
}

.dark-theme .view-btn:hover {
  background: #21262d;
}

.dark-theme .stat-card {
  background: #161b22;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.dark-theme .stat-content .stat-number {
  color: #c9d1d9;
}

.dark-theme .stat-content .stat-label {
  color: #8b949e;
}

.dark-theme .task-card {
  background: #161b22;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark-theme .task-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

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
  background: #21262d;
}

.dark-theme .todos-count {
  color: #8b949e;
}

.dark-theme .empty-state .empty-icon-container {
  background: #161b22;
}

.dark-theme .empty-title {
  color: #c9d1d9;
}

.dark-theme .empty-description {
  color: #8b949e;
}

.dark-theme .modal-content {
  background: #161b22;
  color: #c9d1d9;
}

.dark-theme .modal-header {
  border-bottom-color: #30363d;
}

.dark-theme .modal-header h3 {
  color: #c9d1d9;
}

.dark-theme .close-btn {
  color: #8b949e;
}

.dark-theme .close-btn:hover {
  background: #21262d;
}

.dark-theme input[type="text"],
.dark-theme input[type="date"],
.dark-theme textarea,
.dark-theme select {
  background: #0d1117;
  border-color: #30363d;
  color: #c9d1d9;
}

.dark-theme input[type="text"]:focus,
.dark-theme input[type="date"]:focus,
.dark-theme textarea:focus,
.dark-theme select:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.2);
}

.dark-theme label {
  color: #c9d1d9;
}

.dark-theme .form-actions {
  border-top-color: #30363d;
}
</style>