<template>
  <!-- 简化版任务选择器 -->
  <div class="task-selector-container">
    <!-- 点击区域 -->
    <div class="task-selector-trigger" @click="toggleDropdown">
      <i class="bi bi-person-workspace"></i>
      <span v-if="taskId" class="selected-task-text" :title="task">{{ truncateTitle(task) }}</span>
    </div>
    
    <!-- 下拉菜单 -->
    <div v-show="showDropdown" class="dropdown-menu-task-selector">
      <div v-for="task in taskOptions" :key="task.title" class="dropdown-item" @click="selectTask(task)">
        <div class="task-color-indicator" :style="{ backgroundColor: task.color || '#2196F3' }"></div>
        <span :title="task.title">{{ truncateTitle(task.title) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import taskAPI from '../../helpers/api/taskAPI';

export default {
  name: "taskSelector",
  props: {
    taskId: {
      type: String,
      default: ""
    }
  },
  computed: {
    // 从本地数据获取所有任务作为选项
    taskOptions() {
      return this.tasks
        .filter(task => task.title && task.title.trim() !== "")
        .sort((a, b) => a.title.localeCompare(b.title));
    },
    // 根据taskId获取对应的任务标题
    task() {
      if (!this.taskId) return '';
      const selectedTask = this.tasks.find(task => task.id === this.taskId);
      return selectedTask ? selectedTask.title : '';
    }
  },
  data() {
    return {
      selectedTask: this.task,
      showDropdown: false,
      tasks: [],
      loading: false,
      error: null
    };
  },
  watch: {
    task(newTask) {
      this.selectedTask = newTask;
    }
  },
  mounted() {
    // 添加点击外部关闭下拉菜单的事件监听
    document.addEventListener('click', this.handleClickOutside);
    // 调用API获取所有任务
    this.loadTasks();
  },
  beforeUnmount() {
    // 组件卸载前移除事件监听
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    // 调用API获取所有任务
    loadTasks() {
      this.loading = true;
      this.error = null;
      
      taskAPI.getAllTasks()
        .then(response => {
          if (response) {
            this.tasks = response;
          } else {
            console.warn('⚠️ TaskSelector: API返回的任务数据格式不正确');
            this.tasks = [];
          }
        })
        .catch(error => {
          console.error('❌ TaskSelector: Failed to load tasks from API:', error);
          this.error = error;
          this.tasks = [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    selectTask(task) {
      this.selectedTask = task.id;
      this.$emit('task-selected', { taskId: task.id, color: task.color, task: task.title });
      this.showDropdown = false;
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
      console.log('toggleDropdown called, showDropdown:', this.showDropdown);
    },
    handleClickOutside(event) {
      // 判断点击是否发生在组件外部
      if (this.showDropdown && !event.target.closest('.task-selector-container')) {
        this.showDropdown = false;
        console.log('Clicked outside, closing dropdown');
      }
    },
    truncateTitle(str) {
      if (!str) return '';
      return str.length > 5 ? str.slice(0, 5) + '…' : str;
    }
  }
};
</script>

<style scoped>
.task-selector-container {
  position: relative;
  display: inline-block;
}

.task-selector-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 5px;
  
  background-color: transparent;
  color: #495057;
  width: auto;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.task-selector-trigger:hover {
  background-color: #f8f9fa;
  border-radius: 5%;
}

.selected-task-text {
  margin-left: 4px;
  margin-right: 4px;
  font-size: 14px;
  color: #495057;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}

.dropdown-menu-task-selector {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 2px;
  z-index: 1000;
  min-width: 150px;
  background-color: white;
  border: 1px solid rgba(0,0,0,0.15);
  border-radius: 4px;
  padding: 4px 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  max-height: 200px;
  overflow-y: auto;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

.dropdown-item i {
  margin-right: 8px;
  color: #6c757d;
}

.task-color-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  border: 1px solid rgba(0,0,0,0.1);
}
</style>
