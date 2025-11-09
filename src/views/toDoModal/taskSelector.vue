<template>
  <!-- 简化版任务选择器 -->
  <div class="task-selector-container">
    <!-- 点击区域 -->
    <div class="task-selector-trigger" @click="toggleDropdown">
      <i class="bi bi-person-workspace"></i>
      <span v-if="task" class="selected-task-text">{{ task }}</span>
    </div>
    
    <!-- 下拉菜单 -->
    <div v-show="showDropdown" class="dropdown-menu-task-selector">
      <div v-for="task in taskOptions" :key="task" class="dropdown-item" @click="selectTask(task)">
        <i class="bi bi-tag-fill"></i>
        <span>{{ task }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "taskSelector",
  props: {
    task: {
      type: String,
      default: ""
    }
  },
  computed: {
    // 从store获取所有任务标题作为选项
    taskOptions() {
      const tasks = this.$store.getters.tasks || {};
      return Object.values(tasks)
        .map(task => task.title)
        .filter(title => title && title.trim() !== "")
        .sort();
    }
  },
  data() {
    return {
      selectedTask: this.task,
      showDropdown: false
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
  },
  beforeUnmount() {
    // 组件卸载前移除事件监听
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    selectTask(task) {
      this.selectedTask = task;
      this.$emit('update:task', this.selectedTask);
      this.$emit('task-selected', this.selectedTask);
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
</style>