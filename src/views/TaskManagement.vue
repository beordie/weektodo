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
      <div class="stat-card">
        <div class="stat-icon"><i class="bi-clock"></i></div>
        <div class="stat-content">
          <div class="stat-number">{{ completedTasksTotalHours }}</div>
          <div class="stat-label">{{ $t('taskManagement.completedHours') }}</div>
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
          'completed': task.completed === 1,
          'overdue': task.completed !== 1 && isOverdue(task),
          'due-soon': task.completed !== 1 && isDueSoon(task)
        }"
        :style="{ borderLeft: `4px solid ${task.color || '#2196F3'}` }"
        @click="openTaskDetails(task.id)"
      >
        <div class="task-card-header">
          <div class="task-title-container">
            <!-- 根据任务完成状态显示不同图标 -->
            <div 
              class="task-status-icon"
              @click.stop="toggleTaskCompletion(task.id)"
              :class="{ 'completed': task.completed === 1 }"
            >
              <!-- 使用项目现有的done.svg图标表示已完成任务 -->
              <img v-if="task.completed === 1" src="/img/done.svg" alt="已完成" width="24" height="24" class="done-icon"/>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9E9E9E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
              </svg>
            </div>
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
                {{ getPriorityName(task.priority) }}
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
          <div class="milestone-progress" v-if="task.milestoneCounter && task.milestoneCounter.total > 0">
            <div class="progress-info">
              <span class="progress-text">{{ getMilestoneProgress(task) }}%</span>
              <span class="progress-label">{{ $t('taskManagement.milestones') }}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill milestone-progress-fill" :style="{ width: getMilestoneProgress(task) + '%' }"></div>
            </div>
            <div class="milestones-count">
              {{ getCompletedMilestonesCount(task) }}/{{ task.milestoneCounter.total }} {{ $t('taskManagement.milestonesCompleted') }}
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
                  <option 
                    v-for="priority in localPriorities" 
                    :key="priority.id" 
                    :value="priority.id"
                  >
                    {{ priority.name }}
                  </option>
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
                      :checked="milestone.completed === 1"
                      @change="milestone.completed = $event.target.checked ? 1 : 0"
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
                  :checked="currentTask.completed === 1"
                  @change="currentTask.completed = $event.target.checked ? 1 : 0"
                  type="checkbox" 
                  class="form-check-input"
                >
                {{ $t('taskManagement.markAsCompleted') }}
              </label>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">
              {{ $t('taskManagement.cancel') }}
            </button>
            <button type="button" class="btn btn-primary" @click="saveTask">
              {{ $t('taskManagement.save') }}
            </button>
          </div>
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
import configAPI from '../helpers/api/configAPI';
import taskAPI from '../helpers/api/taskAPI';

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
    
    // 本地存储API获取的分类数据
    localCategories: [],
    // 本地存储API获取的优先级配置
    localPriorities: [],
    // 从API获取的仪表盘数据
    dashboardData: null,
    // 从API获取的任务数据
    apiTasks: [],
    // 加载状态
    loadingDashboardData: false,
    loadingTasks: false
    };
  },
  computed: {
    tasks() {
      // 使用从API获取的任务数据，不再从store获取
      return this.apiTasks;
    },
    taskCategories() {
      // 只使用本地API获取的分类数据，不再从store获取
      return this.localCategories.length > 0 ? this.localCategories : [];
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
      // 直接返回从API获取的任务数据，因为过滤已经在后端完成
      return this.apiTasks;
    },
    sortedAndFilteredTasks() {
      // 直接返回从API获取的任务数据，因为排序已经在后端完成
      return this.apiTasks;
    },
    // 统计数据
    completedTasksCount() {
      return this.dashboardData ? this.dashboardData.completedTasks : 0;
    },
    overdueTasksCount() {
      return this.dashboardData ? this.dashboardData.overdueTasks : 0;
    },
    dueSoonTasksCount() {
      return this.dashboardData ? this.dashboardData.upcomingTasks : 0;
    },
    totalTasksCount() {
      return this.dashboardData ? this.dashboardData.totalTasks : 0;
    },
    // 所有已完成任务的总时间（小时）
    completedTasksTotalHours() {
      return this.dashboardData ? this.dashboardData.totalTodoTime : 0;
    },
  },
  watch: {
    // 监听过滤和排序参数变化，重新加载任务数据
    selectedCategory() {
      this.loadTasksFromAPI();
    },
    searchQuery() {
      this.loadTasksFromAPI();
    },
    sortBy() {
      this.loadTasksFromAPI();
    },
    sortOrder() {
      this.loadTasksFromAPI();
    },
    // 监听 store 中的 showTaskManagement 状态变化，确保每次从菜单点击进入时重新加载数据
    '$store.getters.showTaskManagement'(newValue, oldValue) {
      if (newValue && !oldValue) {
        // 当组件从隐藏变为显示时，重新加载所有数据
        this.loadTasksFromAPI();
        this.loadDashboardData();
      }
    }
  },
  mounted() {
    // 从后端API获取任务数据
    this.loadTasksFromAPI();
    
    // 从后端API获取分类数据
    this.loadCategoriesFromAPI();
    
    // 从后端API获取优先级配置
    this.loadPrioritiesFromAPI();
    
    // 从后端API获取仪表盘数据
    this.loadDashboardData();
  },
  activated() {
    // 当组件被激活时(每次从菜单点击进入)重新加载所有数据
    this.loadTasksFromAPI();
    this.loadDashboardData();
  },
  methods: {
    // 从API加载任务数据，传递过滤和排序参数
    loadTasksFromAPI() {
      this.loadingTasks = true;
      
      // 准备请求参数
      const params = {
        title: this.searchQuery,
        category: this.selectedCategory,
        // 转换前端字段名与后端对应
        sortBy: this.sortBy === 'dueDate' ? 'endDate' : this.sortBy,
        sortOrder: this.sortOrder
      };
      
      taskAPI.getAllTasks(params)
        .then(response => {
          if (response) {
            // 将API返回的任务数据存储到本地变量
            this.apiTasks = response;
            // 手动触发视图更新
            this.$forceUpdate();
          } else {
            console.warn('⚠️ TaskManagement: API返回的任务数据格式不正确');
          }
        })
        .catch(error => {
          console.error('❌ TaskManagement: Failed to load tasks from API:', error);
          // 如果API调用失败，使用空数组
          this.apiTasks = [];
        })
        .finally(() => {
          this.loadingTasks = false;
        });
    },
    // 从API加载仪表盘数据
    loadDashboardData() {
      this.loadingDashboardData = true;
      taskAPI.getTaskDashboardData()
        .then(response => {
          if (response && response.dashboardStats) {
            // 将新的DashboardResponse格式转换为前端期望的旧格式
            const dashboardStats = response.dashboardStats;
            this.dashboardData = {
              completedTasks: dashboardStats.find(stat => stat.id === 'completed')?.value || 0,
              overdueTasks: dashboardStats.find(stat => stat.id === 'overdue_tasks')?.value || 0,
              upcomingTasks: dashboardStats.find(stat => stat.id === 'upcoming_tasks')?.value || 0,
              totalTasks: dashboardStats.find(stat => stat.id === 'total_tasks')?.value || 0,
              totalTodoTime: dashboardStats.find(stat => stat.id === 'total_todo_time')?.value || 0
            };
            // 手动触发视图更新
            this.$forceUpdate();
          } else {
            console.warn('⚠️ TaskManagement: API返回的数据格式不正确');
          }
        })
        .catch(error => {
          console.error('❌ TaskManagement: Failed to load dashboard data from API:', error);
        })
        .finally(() => {
          this.loadingDashboardData = false;
        });
    },
    
     // 从API加载分类数据
     loadCategoriesFromAPI() {
       configAPI.getCategories()
         .then(response => {
           if (response && response.categories) {
             // 直接将获取到的分类数据保存到本地变量
             this.localCategories = response.categories;
             // 手动触发视图更新
             this.$forceUpdate();
           } else {
             console.warn('⚠️ TaskManagement: API返回的数据格式不正确，没有categories字段');
           }
         })
         .catch(error => {
           console.error('❌ TaskManagement: Failed to load categories from API:', error);
           // 如果API调用失败，直接提供默认分类数据，不再从store获取
           this.localCategories = [];
           // 手动触发视图更新
           this.$forceUpdate();
         });
     },
     
     // 从API加载优先级配置
     loadPrioritiesFromAPI() {
       configAPI.getPriorities()
         .then(response => {
           if (response && response.priorities) {
             // 直接将获取到的优先级配置保存到本地变量
             this.localPriorities = response.priorities;
             // 手动触发视图更新
             this.$forceUpdate();
           } else {
             console.warn('⚠️ TaskManagement: API返回的数据格式不正确，没有priorities字段');
           }
         })
         .catch(error => {
           console.error('❌ TaskManagement: Failed to load priorities from API:', error);
           // 如果API调用失败，使用空数组
           this.localPriorities = [];
           // 手动触发视图更新
           this.$forceUpdate();
         });
     },
    // 添加milestone
    addMilestone() {
      // 确保milestones是数组，在Vue 3中直接赋值即可保持响应式
      if (!this.currentTask.milestones) {
        this.currentTask.milestones = [];
      }
      
      // 添加新的里程碑对象
      this.currentTask.milestones.push({
        title: '',
        completed: 0
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
    async saveTask() {
      // 验证日期
      if (moment(this.currentTask.startDate).isAfter(moment(this.currentTask.endDate))) {
        // 由于notifications模块没有showToast方法，使用浏览器原生alert作为替代
        alert(this.$t('taskManagement.invalidDateRange'));
        return;
      }
      
      try {
        if (this.editingTask) {
          // 更新现有任务
          // 确保milestones数组存在，即使为空
          const milestones = Array.isArray(this.currentTask.milestones) ? this.currentTask.milestones : [];
          const updates = {
            ...this.currentTask,
            updatedAt: new Date().toISOString(),
            milestones: milestones,
            // 将boolean值转换为整数：false -> 0, true -> 1
            completed: this.currentTask.completed ? 1 : 0
          };
          
          // 调用API更新任务
          await taskAPI.updateTask(this.editingTask, updates);
          // 由于notifications模块没有showToast方法，暂时省略通知
        } else {
          // 确保milestones数组存在，即使为空
          const milestones = Array.isArray(this.currentTask.milestones) ? this.currentTask.milestones : [];
          const newTask = {
            ...this.currentTask,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            milestones: milestones,
            // 将boolean值转换为整数：false -> 0, true -> 1
            completed: this.currentTask.completed ? 1 : 0
          };
          
          // 调用API创建任务
          await taskAPI.createTask(newTask);
          // 由于notifications模块没有showToast方法，暂时省略通知
        }
        
        // 保存成功后重新加载任务列表和仪表盘数据
        this.loadTasksFromAPI();
        this.loadDashboardData();
        this.closeModal();
      } catch (error) {
        console.error('保存任务失败:', error);
        alert(this.$t('taskManagement.saveTaskFailed'));
      }
    },
    
    // 打开任务详情
    async openTaskDetails(taskId) {
      try {
        this.editingTask = taskId;
        // 从API获取任务详情
        const taskDetails = await taskAPI.getTaskById(taskId);
        this.currentTask = taskDetails;
        this.showModal = true;
      } catch (error) {
        console.error('获取任务详情失败:', error);
        this.$message.error('获取任务详情失败');
      }
    },
    
    // 打开任务看板
    openTaskKanban(taskId) {
      console.log('Opening task kanban for taskId:', taskId);
      // Set the current task ID in the store - use the correct mutation from mainStore
      this.$store.commit('setCurrentTaskId', taskId);
      
      // Update the showTaskKanban state - use the correct mutation from tasks store
      this.$store.commit('showTaskKanban', true);
      
      // Hide task management page
      this.$store.commit('showTaskManagement', false);
      
      // Kanban page will fetch data from backend based on taskId
    },
    
    // 删除任务
    deleteTask(taskId) {
      if (confirm(this.$t('taskManagement.confirmDeleteTask'))) {        
        // 调用API删除任务
        taskAPI.deleteTask(taskId)
          .then(() => {
            // 删除成功，可以添加成功提示
            console.log('Task deleted successfully');
            // 重新加载任务列表和仪表盘数据以显示最新状态
            this.loadTasksFromAPI();
            this.loadDashboardData();
          })
          .catch(error => {
            // 删除失败，恢复本地状态并显示错误信息
            console.error('Failed to delete task:', error);
            // 这里可以添加错误提示，例如使用toast组件
            alert(this.$t('taskManagement.deleteTaskError'));
            
            // 重新加载任务列表和仪表盘数据以恢复正确的状态
            this.loadTasksFromAPI();
            this.loadDashboardData();
          });
      }
    },
    
    // 切换任务完成状态
    toggleTaskCompletion(taskId) {
      // 查找当前任务
      const task = this.tasks.find(t => t.id === taskId);
      if (!task) return;
      
      const updatedTask = { ...task, completed: task.completed === 1 ? 0 : 1 };
      
      // 调用API更新任务状态
      taskAPI.updateTask(taskId, updatedTask)
        .then(() => {
          console.log('Task completion status updated successfully');
          // 重新加载任务列表和仪表盘数据以显示最新状态
          this.loadTasksFromAPI();
          this.loadDashboardData();
        })
        .catch(error => {
          console.error('Failed to update task completion status:', error);
        });
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
          if (matchedTodo && matchedTodo.checked === 1) {
            completedCount++;
          }
        });
      });
      
      return completedCount;
    },
    
    // 获取milestone进度
    getMilestoneProgress(task) {
      if (!task.milestoneCounter || task.milestoneCounter.total === 0) return 0;
      return Math.round((task.milestoneCounter.done / task.milestoneCounter.total) * 100);
    },
    
    // 获取已完成的milestones数量
    getCompletedMilestonesCount(task) {
      return task.milestoneCounter ? task.milestoneCounter.done : 0;
    },
    
    // 获取分类名称
    getCategoryName(categoryId) {
      const category = this.allCategories.find(cat => cat.id === categoryId);
      return category ? category.name : '';
    },
    
    // 获取优先级名称
    getPriorityName(priorityId) {
      const priority = this.localPriorities.find(p => p.id === priorityId);
      return priority ? priority.name : '';
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
      // 直接使用数字优先级进行排序，假设数值越大优先级越高
      comparison = b.priority - a.priority;
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
  flex-shrink: 0; /* 防止图标在空间不足时收缩，保持圆形 */
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

.task-status-icon {
  margin-top: 4px;
  width: 20px;
  height: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

.task-status-icon:hover {
  transform: scale(1.1);
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

.priority-1 {
  background: #d1ecf1;
  color: #0c5460;
}

.priority-2 {
  background: #fff3cd;
  color: #856404;
}

.priority-3 {
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
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
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
  overflow-y: auto;
  flex-grow: 1;
}

.modal-footer {
  padding: 12px 24px;
  background: none;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
  background: none;
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