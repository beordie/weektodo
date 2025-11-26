<template>
  <!-- 里程碑选择器 -->
  <div class="milestone-selector-container">
    <!-- 点击区域 -->
    <div class="milestone-selector-trigger" @click="toggleDropdown" v-if="selectedTask">
      <i class="bi bi-flag"></i>
      <span v-if="milestone" class="selected-milestone-text">{{ milestone }}</span>
    </div>
    
    <!-- 下拉菜单 -->
    <div v-show="showDropdown && selectedTask" class="dropdown-menu-milestone-selector">
      <div class="dropdown-item" @click="clearMilestone">
        <i class="bi bi-x-circle"></i>
        <span>{{ $t('taskManagement.clearMilestone') || 'Clear Milestone' }}</span>
      </div>
      <div v-for="milestone in milestoneOptions" :key="milestone.title || milestone" class="dropdown-item" @click="selectMilestone(milestone)">
        <i class="bi bi-flag-fill"></i>
        <span>{{ milestone.title || milestone }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "milestoneSelector",
  props: {
    task: {
      type: String,
      default: ""
    },
    milestone: {
      type: String,
      default: ""
    }
  },
  computed: {
    // 当前选中的任务
    selectedTask() {
      return this.task;
    },
    // 根据当前选中的任务获取相关里程碑选项
    milestoneOptions() {
      if (!this.selectedTask) return [];
      
      const tasks = this.$store.getters.tasks || {};
      const currentTask = Object.values(tasks).find(t => t.title === this.selectedTask);
      
      if (currentTask && currentTask.milestones && Array.isArray(currentTask.milestones)) {
        // 保持兼容，仍然使用任务层面的milestones数组
        return currentTask.milestones;
      }
      
      return [];
    }
  },
  data() {
    return {
      showDropdown: false
    };
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
    selectMilestone(milestone) {
      const milestoneTitle = typeof milestone === 'object' && milestone.title 
        ? milestone.title 
        : milestone;
      
      this.$emit('milestone-selected', { 
        task: this.selectedTask, 
        milestone: milestoneTitle 
      });
      this.showDropdown = false;
    },
    clearMilestone() {
      this.$emit('milestone-selected', { 
        task: this.selectedTask, 
        milestone: "" 
      });
      this.showDropdown = false;
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    handleClickOutside(event) {
      // 判断点击是否发生在组件外部
      if (this.showDropdown && !event.target.closest('.milestone-selector-container')) {
        this.showDropdown = false;
      }
    }
  }
};
</script>

<style scoped>
.milestone-selector-container {
  position: relative;
  display: inline-block;
}

.milestone-selector-trigger {
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

.milestone-selector-trigger:hover {
  background-color: #f8f9fa;
  border-radius: 5%;
}

.selected-milestone-text {
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

.dropdown-menu-milestone-selector {
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

.dropdown-item:first-child {
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 2px;
  font-style: italic;
}
</style>