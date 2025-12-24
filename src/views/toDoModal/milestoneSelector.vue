<template>
  <!-- 里程碑选择器 -->
  <div class="milestone-selector-container">
    <!-- 点击区域 -->
    <div class="milestone-selector-trigger" @click="toggleDropdown" v-if="selectedTask">
      <i class="bi bi-flag"></i>
      <span v-if="milestoneId" class="selected-milestone-text">{{ milestone }}</span>
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
import taskAPI from '../../helpers/api/taskAPI';

export default {
  name: "milestoneSelector",
  props: {
    taskId: {
      type: String,
      default: ""
    },
    milestoneId: {
      type: String,
      default: ""
    }
  },
  computed: {
    // 当前选中的任务
    selectedTask() {
      return this.taskId;
    },
    // 根据当前选中的任务获取相关里程碑选项
    milestoneOptions() {
      return this.milestones || [];
    },
    // 根据milestoneId获取对应的里程碑标题
    milestone() {
      if (!this.milestoneId) return '';
      const selectedMilestone = this.milestones.find(milestone => milestone.id === this.milestoneId);
      return selectedMilestone ? selectedMilestone.title : '';
    }
  },
  data() {
    return {
      showDropdown: false,
      milestones: [],
      loading: false,
      error: null,
      taskIdMap: new Map() // 用于存储任务标题到ID的映射
    };
  },
  watch: {
    // 当任务ID变化时重新获取里程碑
    taskId(newTaskId, oldTaskId) {
      if (newTaskId !== oldTaskId) {
        this.loadMilestones();
      }
    },
    // 当里程碑ID变化时更新显示
    milestoneId() {
      // 里程碑ID变化时不需要重新加载里程碑列表
    }
  },
  mounted() {
    // 添加点击外部关闭下拉菜单的事件监听
    document.addEventListener('click', this.handleClickOutside);
    // 初始加载里程碑
    this.loadMilestones();
  },
  beforeUnmount() {
    // 组件卸载前移除事件监听
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    // 调用API获取里程碑
    loadMilestones() {
      if (!this.selectedTask) {
        this.milestones = [];
        return;
      }
      
      this.loading = true;
      this.error = null;
      
      // 直接使用传入的taskId获取里程碑
      taskAPI.getMilestonesByTaskId(this.selectedTask)
        .then(milestones => {
          if (milestones) {
            this.milestones = milestones;
          } else {
            console.warn('⚠️ MilestoneSelector: API返回的里程碑数据格式不正确');
            this.milestones = [];
          }
        })
        .catch(error => {
          console.error('❌ MilestoneSelector: Failed to load milestones from API:', error);
          this.error = error;
          this.milestones = [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    selectMilestone(milestone) {
      this.$emit('milestone-selected', { 
        milestoneId: milestone.id || null,
        milestone: milestone.title || ""
      });
      this.showDropdown = false;
    },
    clearMilestone() {
      this.$emit('milestone-selected', { 
        milestoneId: null, 
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