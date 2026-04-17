<template>
  <div class="task-kanban-container">
    <!-- 页面头部 -->
    <div class="kanban-header">
      <div class="header-left">
        <i 
          class="bi-arrow-left cursor-pointer" 
          @click="goBackToTaskManagement"
          title="返回"
          style="font-size: 1.5rem; color: #6c757d;" 
        ></i>
        <h1 class="page-title">{{ $t('taskManagement.kanbanView') }}{{ currentTask && currentTask.title ? ' - ' + currentTask.title : '' }}</h1>
      </div>
      <div class="header-actions">
        <!-- <div class="search-container">
          <i class="bi-search search-icon"></i>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('taskManagement.searchTasks')"
            class="search-input"
          />
        </div> -->
        <!-- 移除新建任务按钮，保持纯查看模式 -->
        <!-- <button 
          class="btn btn-primary primary-action"
          @click="openCreateTaskModal"
        >
          <i class="bi-plus"></i> {{ $t('taskManagement.newTask') }}
        </button> -->
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="statistics-section">
      <!-- 加载状态 -->
      <div class="loading-container" v-if="loading">
        <div class="loading-spinner"></div>
        <p>加载统计数据中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div class="error-container" v-else-if="dashboardData && !dashboardData.dashboardStats">
        <i class="bi-exclamation-triangle"></i>
        <p>无法加载统计数据</p>
      </div>
      
      <!-- 无数据状态 -->
      <div class="no-data-container" v-else-if="dashboardData && dashboardData.dashboardStats.length === 0">
        <i class="bi-info-circle"></i>
        <p>暂无统计数据</p>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stat-card" v-else v-for="stat in dashboardData?.dashboardStats" :key="stat.id">
        <h3>{{ stat.title }}</h3>
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-change" :class="stat.footer?.type" v-if="stat.footer">
          <i :class="stat.footer.icon" v-if="stat.footer.icon"></i> 
          {{ stat.footer.text.replace('{}', stat.footer.diff || stat.value) }}
        </div>
      </div>
    </div>
    
    <!-- GitHub风格提交图 -->
      <div class="commit-graph-container">
        <h3 class="commit-graph-title">任务完成统计</h3>
        <div class="commit-graph">
          <!-- 星期标签和网格内容 -->
          <div class="commit-content-wrapper">
            <!-- 星期标签 -->
            <div class="commit-week-labels">
              <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']" 
                   :key="day" 
                   class="commit-week-label">
                {{ day }}
              </div>
            </div>
            
            <!-- 网格内容区域 -->
            <div class="commit-grid-content">
              <!-- 主要网格内容 -->
              <div class="commit-cells">
                <div v-for="date in getDateRange()" 
                     :key="date.getTime()"
                     class="commit-cell"
                     :class="`commit-level-${getCommitLevel(getCompletedTasksByDate(date))}`"
                     :data-tooltip="getCommitTooltip(date, getCompletedTasksByDate(date))"
                     :title="getCommitTooltip(date, getCompletedTasksByDate(date))">
                </div>
              </div>
              
              <!-- 月份标签 -->
              <div class="commit-month-labels">
                <div v-for="(month, index) in getMonthLabels()" 
                     :key="index"
                     class="commit-month-label"
                     :style="{ gridColumn: month.position }">
                  {{ month.name }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- 图例 -->
          <div class="commit-legend">
            <span class="commit-legend-text">更少</span>
            <div v-for="level in [0, 1, 2, 3, 4]" 
                 :key="level"
                 class="commit-legend-cell"
                 :class="`commit-level-${level}`">
            </div>
            <span class="commit-legend-text">更多</span>
            <!-- 未完成任务标记 -->
            <div class="commit-legend-cell commit-level-5" title="有未完成任务"></div>
            <span class="commit-legend-text">未完成</span>
          </div>
        </div>
      </div>

    <!-- 里程碑统计表格 - 单独一行 -->
    <div class="milestones-section full-width-section">
      <div class="section-header">
        <h3>里程碑统计</h3>
        <div class="milestones-summary">
          <span class="milestones-count">共 {{ totalMilestonesCount }} 个里程碑</span>
          <span class="milestones-completion">平均完成率: {{ averageMilestoneCompletionRate }}%</span>
        </div>
      </div>
      <div class="milestones-table-container">
        <table class="milestones-table">
          <thead>
            <tr>
              <th>里程碑名称</th>
              <th>总任务数</th>
              <th>已完成</th>
              <th>待处理</th>
              <th>完成率</th>
              <th>进度</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="milestone in milestoneStats" :key="milestone.name">
              <td class="milestone-name">{{ milestone.name }}</td>
              <td class="milestone-total">{{ milestone.totalTasks }}</td>
              <td class="milestone-completed">{{ milestone.completedTasks }}</td>
              <td class="milestone-pending">{{ milestone.pendingTasks }}</td>
              <td class="milestone-rate">{{ milestone.completionRate }}%</td>
              <td class="milestone-progress">
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :class="{
                      'progress-low': milestone.completionRate < 30,
                      'progress-medium': milestone.completionRate >= 30 && milestone.completionRate < 70,
                      'progress-high': milestone.completionRate >= 70
                    }"
                    :style="{ width: milestone.completionRate + '%' }"
                  ></div>
                </div>
              </td>
            </tr>
            <tr v-if="milestoneStats.length === 0">
              <td colspan="6" class="no-milestones">
                暂无里程碑数据
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 统计图表区域 - 响应式布局 -->
    <div class="stats-charts-container">
      <!-- 任务完成趋势 -->
      <div class="trend-section stats-chart-item">
        <div class="section-header">
          <h3>任务完成趋势</h3>
          <div class="trend-select-with-icon">
            <i :class="{
              'fas fa-calendar-day': selectedTrend === 'week_daily',
              'fas fa-calendar-week': selectedTrend === 'week_weekly',
              'fas fa-calendar-alt': selectedTrend === 'month_monthly',
              'fas fa-calendar-check': selectedTrend === 'quarter_quarterly',
              'fas fa-chart-line': true
            }" class="trend-icon"></i>
            <select class="trend-select" v-model="selectedTrend">
              <option value="week_daily">每天</option>
              <option value="week_weekly">每周</option>
              <option value="month_monthly">每月</option>
              <option value="quarter_quarterly">每季度</option>
            </select>
          </div>
        </div>
        <div class="trend-chart-container">
          <svg class="trend-chart" viewBox="0 0 800 300">
            <defs>
              <linearGradient id="trendGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#10b981;stop-opacity:0.2" />
                <stop offset="100%" style="stop-color:#10b981;stop-opacity:0" />
              </linearGradient>
            </defs>
            
            <!-- 网格线 -->
            <line x1="50" y1="100" x2="750" y2="100" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            <line x1="50" y1="150" x2="750" y2="150" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            <line x1="50" y1="200" x2="750" y2="200" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            
            <!-- X轴 -->
            <line x1="50" y1="250" x2="750" y2="250" stroke="#e5e7eb" stroke-width="1.5"/>
            <!-- Y轴 -->
            <line x1="50" y1="50" x2="50" y2="250" stroke="#e5e7eb" stroke-width="1.5"/>
            
            <!-- 填充区域 -->
            <polygon 
              v-if="trendPoints"
              :points="getAreaPoints(trendPoints)"
              fill="url(#trendGradient)"
            />
            
            <!-- X轴标签 - 由后端提供 -->
            <text v-for="item in trendXAxisLabels" :key="'tx-' + item.index"
                  :x="item.x" 
                  y="270" 
                  :text-anchor="item.anchor" 
                  class="axis-label">
              {{ item.text }}
            </text>
            
            <!-- 动态折线 -->
            <polyline 
              :points="trendPoints" 
              fill="none" 
              stroke="#10b981" 
              stroke-width="3" 
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            
            <!-- 动态数据点 -->
            <template v-for="(point, index) in trendChartDataPoints" :key="'tp-' + index">
              <circle 
                :cx="point.x" 
                :cy="point.y" 
                r="4" 
                fill="white"
                stroke="#10b981"
                stroke-width="2"
                class="chart-point"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <animate 
                  attributeName="r" 
                  values="4;7;4" 
                  dur="0.8s" 
                  :begin="`${index * 0.1}s`"
                  fill="freeze"
                />
              </circle>
            </template>
            <!-- 数据值标签 -->
            <template v-for="(p, i) in trendLabeledPoints" :key="'tl-' + i">
              <text 
                :x="p.x" 
                :y="p.y - 14" 
                text-anchor="middle" 
                class="data-label"
                :style="{ animationDelay: `${i * 0.1 + 0.4}s` }"
              >
                {{ p.value }}
              </text>
            </template>
          </svg>
        </div>
      </div>

      <!-- 完成时间统计 -->
      <div class="time-stats-section stats-chart-item">
        <div class="section-header">
          <h3>完成时间统计</h3>
          <select class="trend-select" v-model="timeStatsPeriod">
            <option value="7">近7天</option>
            <option value="14">近14天</option>
            <option value="30">近30天</option>
          </select>
        </div>
        <div class="trend-chart-container">
          <svg class="trend-chart" viewBox="0 0 800 300">
            <defs>
              <linearGradient id="timeGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#3b82f6;stop-opacity:0.2" />
                <stop offset="100%" style="stop-color:#3b82f6;stop-opacity:0" />
              </linearGradient>
            </defs>
            
            <!-- 网格线 -->
            <line x1="50" y1="100" x2="750" y2="100" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            <line x1="50" y1="150" x2="750" y2="150" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            <line x1="50" y1="200" x2="750" y2="200" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4,4"/>
            
            <!-- X轴 -->
            <line x1="50" y1="250" x2="750" y2="250" stroke="#e5e7eb" stroke-width="1.5"/>
            <!-- Y轴 -->
            <line x1="50" y1="50" x2="50" y2="250" stroke="#e5e7eb" stroke-width="1.5"/>
            
            <!-- 填充区域 -->
            <polygon 
              v-if="timeStatsPoints"
              :points="getAreaPoints(timeStatsPoints)"
              fill="url(#timeGradient)"
            />
            
            <!-- X轴标签 -->
            <text v-for="item in timeXAxisLabels" :key="'tsx-' + item.index"
                  :x="item.x" 
                  y="270" 
                  :text-anchor="item.anchor" 
                  class="axis-label">
              {{ item.text }}
            </text>
            
            <!-- 动态折线 -->
            <polyline 
              :points="timeStatsPoints" 
              fill="none" 
              stroke="#3b82f6" 
              stroke-width="3" 
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            
            <!-- 动态数据点 -->
            <template v-for="(point, index) in timeStatsChartDataPoints" :key="'ts-' + index">
              <circle 
                :cx="point.x" 
                :cy="point.y" 
                r="4" 
                fill="white"
                stroke="#3b82f6"
                stroke-width="2"
                class="chart-point"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <animate 
                  attributeName="r" 
                  values="4;7;4" 
                  dur="0.8s" 
                  :begin="`${index * 0.1}s`"
                  fill="freeze"
                />
              </circle>
            </template>
            <!-- 数据值标签 -->
            <template v-for="(p, i) in timeLabeledPoints" :key="'tsl-' + i">
              <text 
                :x="p.x" 
                :y="p.y - 14" 
                text-anchor="middle" 
                class="data-label"
                :style="{ animationDelay: `${i * 0.1 + 0.4}s` }"
              >
                {{ p.value }}h
              </text>
            </template>
          </svg>
        </div>
      </div>
    </div>

    <!-- 最近任务 -->
    <div class="recent-tasks-section">
      <div class="section-header">
        <h3>最近任务</h3>
        <a href="#" class="view-all-link">查看全部</a>
      </div>
      <div class="task-list">
        <template v-if="getRecentTodosLimited().length > 0">
          <div 
            v-for="(task, index) in getRecentTodosLimited()" 
            :key="task.id || index"
            class="task-item expanded"
          >
            <input 
              type="checkbox" 
              :id="`recent-task-${index}`" 
              class="task-checkbox"
              :checked="!!task.completed"
              disabled
            >
            <div class="task-content">
              <label 
                :for="`recent-task-${index}`" 
                class="task-label"
                :title="task.title"
              >
                {{ task.title }}
              </label>
              <div class="task-meta">
                <span class="task-date">{{ formatDate(task.createdAt) }}</span>
                <span class="task-time">{{ formatTaskTime(task) || '' }}</span>
                <span class="task-status" :class="task.completed ? 'completed' : 'pending'">
                  {{ task.completed ? '已完成' : '待处理' }}
                </span>
                <span v-if="task.desc && task.desc.trim() !== ''" class="task-parent">
                  内容: {{ task.desc }}
                </span>
                <span v-if="task.subtasks && task.subtasks.length > 0 && task.subtasks.some(subtask => subtask && subtask.trim() !== '')" class="task-subtasks">
                  子任务: {{ task.subtasks.filter(subtask => subtask && subtask.trim() !== '').join(', ') }}
                </span>
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <!-- 默认模拟数据，确保页面始终有内容显示 -->
          <div class="task-item">
            <input type="checkbox" id="mock-task1" class="task-checkbox">
            <div class="task-content">
              <label for="mock-task1" class="task-label">完成Q3季度报告</label>
              <div class="task-meta">
                <span class="task-date">2024-01-15</span>
                <span class="task-time">14:30</span>
                <span class="task-list-id">列表: 工作</span>
                <span class="task-status pending">待处理</span>
              </div>
            </div>
          </div>
          <div class="task-item">
            <input type="checkbox" id="mock-task2" class="task-checkbox">
            <div class="task-content">
              <label for="mock-task2" class="task-label">更新项目文档</label>
              <div class="task-meta">
                <span class="task-date">2024-01-14</span>
                <span class="task-time">09:15</span>
                <span class="task-list-id">列表: 项目</span>
                <span class="task-status pending">待处理</span>
              </div>
            </div>
          </div>
          <div class="task-item">
            <input type="checkbox" id="mock-task3" class="task-checkbox">
            <div class="task-content">
              <label for="mock-task3" class="task-label">准备周会演示</label>
              <div class="task-meta">
                <span class="task-date">2024-01-13</span>
                <span class="task-list-id">列表: 工作</span>
                <span class="task-status pending">待处理</span>
              </div>
            </div>
          </div>
          <div class="task-item">
            <input type="checkbox" id="mock-task4" class="task-checkbox">
            <div class="task-content">
              <label for="mock-task4" class="task-label">评审团队代码</label>
              <div class="task-meta">
                <span class="task-date">2024-01-12</span>
                <span class="task-list-id">列表: 开发</span>
                <span class="task-status pending">待处理</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 创建/编辑任务模态框已移除，保持纯查看模式 -->
  </div>
</template>

<style scoped>
/* 统计卡片区域样式 */
.statistics-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 加载状态样式 */
.loading-container {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  color: #6c757d;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态样式 */
.error-container {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  color: #dc3545;
  background-color: #fff5f5;
  border-radius: 8px;
  border: 1px solid #ffebee;
}

.error-container i {
  font-size: 36px;
  margin-bottom: 15px;
}

/* 无数据状态样式 */
.no-data-container {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #dee2e6;
}

.no-data-container i {
  font-size: 36px;
  margin-bottom: 15px;
}

/* 统计卡片样式 */
.stat-card {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.stat-change {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
}

.stat-change i {
  margin-right: 4px;
}

/* footer type 样式 */
.stat-change.increase {
  background-color: #d4edda;
  color: #155724;
}

.stat-change.decrease {
  background-color: #f8d7da;
  color: #721c24;
}

.stat-change.normal {
  background-color: #d1ecf1;
  color: #0c5460;
}

.stat-change.overdue {
  background-color: #f8f9fa;
  color: #6c757d;
}

/* 统计图表容器响应式布局 */
.stats-charts-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计图表项 - 与热力图风格一致 */
.stats-chart-item {
  background: rgba(255, 255, 255, 0.98);
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.stats-chart-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #34d399 0%, #10b981 50%, #059669 100%);
}

/* 图表区域头部 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2328;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header h3::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #34d399 0%, #10b981 100%);
  border-radius: 2px;
}

/* 选择器样式 */
.trend-select-with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-icon {
  font-size: 14px;
  color: #57606a;
}

.trend-select {
  padding: 8px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: #f6f8fa;
  color: #1f2328;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.trend-select:hover {
  border-color: #10b981;
  background: #dcfce7;
}

.trend-select:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

/* 图表容器 */
.trend-chart-container {
  position: relative;
  min-height: 320px;
  margin-top: 15px;
  background: linear-gradient(180deg, rgba(52, 211, 153, 0.03) 0%, transparent 100%);
  border-radius: 8px;
  padding: 10px;
}

.trend-chart {
  width: 100%;
  max-width: 100%;
  height: auto;
  overflow: visible;
}

.trend-chart polyline {
  animation: lineDrawIn 1s ease-out forwards;
  stroke-dasharray: 2000;
  stroke-dashoffset: 2000;
}

.trend-chart circle {
  animation: pointPopIn 0.5s ease-out forwards;
  transform-origin: center;
  opacity: 0;
}

.trend-chart .chart-point {
  animation: pointPopIn 0.5s ease-out forwards;
  transform-origin: center;
  opacity: 0;
  transition: all 0.2s ease;
}

.trend-chart .chart-point:hover {
  cursor: pointer;
  filter: drop-shadow(0 0 12px rgba(0,0,0,0.3));
}

.trend-chart .chart-point:hover + .data-label,
.trend-chart .chart-point:hover ~ .data-label {
  font-weight: 700;
  transform: scale(1.1);
}

.trend-chart circle:nth-child(1) { animation-delay: 0.1s; }
.trend-chart circle:nth-child(2) { animation-delay: 0.2s; }
.trend-chart circle:nth-child(3) { animation-delay: 0.3s; }
.trend-chart circle:nth-child(4) { animation-delay: 0.4s; }
.trend-chart circle:nth-child(5) { animation-delay: 0.5s; }
.trend-chart circle:nth-child(6) { animation-delay: 0.6s; }
.trend-chart circle:nth-child(7) { animation-delay: 0.7s; }
.trend-chart circle:nth-child(8) { animation-delay: 0.8s; }

.trend-chart polygon {
  animation: areaFadeIn 1s ease-out forwards;
  animation-delay: 0.3s;
  opacity: 0;
  transform-origin: bottom;
}

.trend-chart .data-label {
  animation: labelFadeIn 0.5s ease-out forwards;
  animation-delay: 0.5s;
  opacity: 0;
  transform-origin: center;
  transition: all 0.2s ease;
}

@keyframes lineDrawIn {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes pointPopIn {
  0% {
    opacity: 0;
    transform: scale(0);
  }
  50% {
    transform: scale(1.5);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes areaFadeIn {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes labelFadeIn {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 坐标轴和标签样式 */
.axis-label {
  font-size: 12px;
  fill: #57606a;
  font-weight: 500;
}

.data-label {
  font-size: 11px;
  fill: #1f2328;
  font-weight: 600;
}

/* 统计图表项暗黑主题适配 */
.dark-theme .stats-chart-item {
  background: rgba(22, 27, 34, 0.98);
  border: 1px solid rgba(48, 54, 61, 0.8);
}

.dark-theme .stats-chart-item::before {
  background: linear-gradient(90deg, #3fb950 0%, #2ea043 50%, #238636 100%);
}

.dark-theme .section-header h3 {
  color: #e6edf3;
}

.dark-theme .section-header h3::before {
  background: linear-gradient(180deg, #3fb950 0%, #2ea043 100%);
}

.dark-theme .trend-icon {
  color: #8b949e;
}

.dark-theme .trend-select {
  border-color: rgba(48, 54, 61, 0.8);
  background: #21262d;
  color: #e6edf3;
}

.dark-theme .trend-select:hover {
  border-color: #3fb950;
  background: rgba(63, 185, 80, 0.15);
}

.dark-theme .trend-select:focus {
  border-color: #3fb950;
  box-shadow: 0 0 0 3px rgba(63, 185, 80, 0.25);
}

.dark-theme .trend-chart-container {
  background: linear-gradient(180deg, rgba(63, 185, 80, 0.05) 0%, transparent 100%);
}

.dark-theme .axis-label {
  fill: #8b949e;
}

.dark-theme .data-label {
  fill: #e6edf3;
}

/* 任务项扩展样式 */
.task-item.expanded {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: background-color 0.2s;
}

.task-item.expanded:hover {
  background-color: #e9ecef;
}

.task-content {
  flex: 1;
  margin-left: 12px;
}

.task-label {
  display: block;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  cursor: default;
  color: #333;
}

/* GitHub风格提交图样式 */
.commit-graph-container {
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.commit-graph-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #1f2328;
  display: flex;
  align-items: center;
  gap: 8px;
}

.commit-graph-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #34d399 0%, #10b981 100%);
  border-radius: 2px;
}

.commit-graph {
  display: flex;
  flex-direction: column;
}

/* 星期标签和网格内容的水平布局 */
.commit-content-wrapper {
  display: flex;
  align-items: flex-start;
  width: 100%;
  min-width: min-content;
}

/* 星期标签 - 垂直排列在左侧，与热力图行完美对齐 */
.commit-week-labels {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-right: 12px;
  padding-top: 2px;
  height: fit-content;
}

.commit-week-label {
  font-size: 11px;
  color: #57606a;
  text-align: right;
  padding-right: 6px;
  line-height: 16px;
  height: 16px;
  min-height: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

/* 网格内容区域 - 包含任务格子和月份标签 */
.commit-grid-content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* 主要网格内容 - 52周 x 7天 */
.commit-cells {
  display: grid;
  grid-template-columns: repeat(52, 1fr);
  grid-template-rows: repeat(7, 16px);
  gap: 5px;
  width: 100%;
  height: auto;
  overflow-x: visible;
}

.commit-cell {
  min-width: 16px;
  min-height: 16px;
  max-width: 50px;
  max-height: 50px;
  width: 16px;
  height: 16px;
  border-radius: 4px;
  background-color: #f6f8fa;
  position: relative;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  aspect-ratio: 1;
}

/* 不同提交级别的颜色 - 更柔和的渐变色 */
.commit-level-0 { 
  background-color: #f6f8fa; 
}
.commit-level-1 { 
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
}
.commit-level-2 { 
  background: linear-gradient(135deg, #86efac 0%, #4ade80 100%);
}
.commit-level-3 { 
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}
.commit-level-4 { 
  background: linear-gradient(135deg, #15803d 0%, #166534 100%);
}
.commit-level-5 { 
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
}

/* 悬停效果 - 更流畅的动画 */
.commit-cell:hover {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Tooltip样式 - 更美观 */
.commit-cell:hover::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(-8px);
  background: #1f2328;
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 12px;
  white-space: pre;
  max-width: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  pointer-events: none;
  overflow: visible;
  font-weight: 500;
  line-height: 1.4;
}

/* Tooltip箭头 */
.commit-cell:hover::before {
  content: '';
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #1f2328;
  z-index: 1000;
  margin-bottom: -4px;
}

/* 月份标签 - 水平分布在网格下方 */
.commit-month-labels {
  display: grid;
  grid-template-columns: repeat(52, 1fr);
  gap: 0;
  margin-top: 8px;
}

.commit-month-label {
  font-size: 11px;
  color: #57606a;
  text-align: left;
  font-weight: 600;
  grid-column: span 4;
  margin-left: 0;
  transform: translateX(-25%);
  padding-top: 2px;
}

/* 图例 - 更美观的样式 */
.commit-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  font-size: 12px;
  color: #57606a;
  padding: 12px 16px;
  background: #f6f8fa;
  border-radius: 8px;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.commit-legend-cell {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  transition: transform 0.2s ease;
}

.commit-legend-cell:hover {
  transform: scale(1.15);
}

.commit-legend-text {
  margin: 0 6px;
  font-weight: 500;
}

/* 暗黑主题适配 */
.dark-theme .commit-graph-container {
  background: rgba(22, 27, 34, 0.98);
  border: 1px solid rgba(48, 54, 61, 0.8);
}

.dark-theme .commit-graph-title {
  color: #e6edf3;
}

.dark-theme .commit-week-label {
  color: #8b949e;
}

.dark-theme .commit-cell {
  background-color: #161b22;
}

.dark-theme .commit-level-0 { 
  background-color: #161b22; 
}
.dark-theme .commit-level-1 { 
  background: linear-gradient(135deg, #0e4429 0%, #006d32 100%);
}
.dark-theme .commit-level-2 { 
  background: linear-gradient(135deg, #006d32 0%, #26a641 100%);
}
.dark-theme .commit-level-3 { 
  background: linear-gradient(135deg, #26a641 0%, #39d353 100%);
}
.dark-theme .commit-level-4 { 
  background: linear-gradient(135deg, #39d353 0%, #56d364 100%);
}
.dark-theme .commit-level-5 { 
  background: linear-gradient(135deg, #6e1a1a 0%, #8b1f1f 100%);
}

.dark-theme .commit-month-label {
  color: #8b949e;
}

.dark-theme .commit-legend {
  background: #21262d;
  color: #8b949e;
}

.dark-theme .commit-cell:hover::after {
  background: #e6edf3;
  color: #1f2328;
}

.dark-theme .commit-cell:hover::before {
  border-top-color: #e6edf3;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .commit-graph-container {
    padding: 16px;
  }
  
  .commit-cells {
    grid-template-columns: repeat(26, 1fr);
    grid-template-rows: repeat(7, 12px);
    gap: 4px;
  }
  
  .commit-month-labels {
    grid-template-columns: repeat(26, 1fr);
  }
  
  .commit-cell {
    min-width: 12px;
    min-height: 12px;
    width: 12px;
    height: 12px;
  }
  
  .commit-week-labels {
    gap: 4px;
  }
  
  .commit-week-label {
    font-size: 10px;
    line-height: 12px;
    height: 12px;
    min-height: 12px;
  }
  
  .commit-month-label {
    font-size: 10px;
    grid-column: span 2;
    margin-left: -3px;
  }
  
  .commit-legend {
    padding: 10px 12px;
    font-size: 11px;
  }
  
  .commit-legend-cell {
    width: 12px;
    height: 12px;
  }
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #6c757d;
}

.task-meta span {
  padding: 2px 8px;
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.05);
}

.task-status {
  font-weight: 500;
}

.task-status.completed {
  color: #28a745;
  background-color: rgba(40, 167, 69, 0.1);
}

.task-status.pending {
  color: #ffc107;
  background-color: rgba(255, 193, 7, 0.1);
}

/* 宽屏时并排显示 */
@media (min-width: 1200px) {
  .stats-charts-container {
    flex-direction: row;
    gap: 30px;
  }
  
  .stats-chart-item {
    flex: 1;
    min-width: 0;
  }
  
  .trend-chart-container {
    width: 100%;
    overflow-x: auto;
  }
  
  .trend-chart {
    width: 100%;
    max-width: 100%;
    height: auto;
  }
}

/* 确保图表容器有足够的高度 */
.trend-chart-container {
  position: relative;
  min-height: 320px;
  margin-top: 15px;
}
</style>

<script>
import moment from 'moment';
import configAPI from '../helpers/api/configAPI';
import taskAPI from '../helpers/api/taskAPI';
import todoAPI from '../helpers/api/todoAPI';

export default {
  name: 'TaskKanban',
  components: {},
  
  // 监控任务相关数据变化
  watch: {
    // 监听看板显示状态变化，当看板打开时重新加载数据
    showTaskKanban(newValue, oldValue) {
      console.log('🔍 看板显示状态变化:', oldValue, '→', newValue);
      if (newValue && !oldValue && this.currentTaskId) {
        console.log('📥 看板已打开，重新加载数据...');
        this.loadDashboardData();
        this.fetchMilestoneStats();
        this.fetchTrendKanban();
        this.fetchRecentTodos();
      }
    },
    selectedTrend() {
      this.fetchTrendKanban();
    },
    timeStatsPeriod() {
      this.fetchTimeKanban();
    },
    currentTaskId(newValue) {
      if (newValue && this.showTaskKanban) {
        this.loadDashboardData();
        this.fetchMilestoneStats();
        this.fetchTrendKanban();
        this.fetchTimeKanban();
        this.fetchRecentTodos();
      }
    }
  },
  
  // 生命周期钩子 - 组件挂载时加载任务数据
  mounted() {
      // 从后端API获取分类数据
      this.loadCategoriesFromAPI();
      // 不在这里调用loadDashboardData，避免与watch监听器冲突
      // 当showTaskKanban状态变化时，watch监听器会自动调用loadDashboardData
  },
  
  data() {
    return {
      showModal: false,
      editingTask: null,
      searchQuery: '',
      // 移除拖拽相关状态变量，保持纯查看模式
      // dragTaskId: null,
      // dragSourceColumn: null,
      timeStatsPeriod: '7', // 默认显示近7天的完成时间统计
      selectedTrend: 'week_daily', // 默认趋势类型：七天内每天对比
      dashboardData: null, // 存储从API获取的看板数据
      loading: false, // 加载状态
      trendLabels: [],
      trendStats: [],
      timeKanbanLabels: [],
      timeKanbanValues: [],
      recentTodosFromAPI: [], // 最近待办项（从后端接口获取）
      currentTask: {
        title: '',
        description: '',
        startDate: '',
        endDate: '',
        category: '',
        priority: 'medium',
        status: 'todo',
        completed: false,
        todos: [],
        milestones: []
      },
      localCategories: [], // 本地存储API返回的分类数据
      // 里程碑统计相关状态变量
      milestoneStats: [], // 里程碑统计数据数组
      totalMilestonesCount: 0, // 总里程碑数
      averageMilestoneCompletionRate: 0, // 平均完成率
      milestoneStatsLoading: false, // 里程碑统计数据加载状态
      todoCreationStats: [], // 待办事项创建统计数据数组
      todoStatsLoading: false // 待办事项统计数据加载状态
    };
  },
  computed: {
      currentTaskId() {
        return this.$store.getters.currentTaskId;
      },
      showTaskKanban() {
        return this.$store.getters.showTaskKanban;
      },
      tasks() {
        // 如果有currentTaskId，只返回该任务对象
        if (this.currentTaskId) {
          return this.$store.getters.tasks[this.currentTaskId] || null;
        }
        // 没有currentTaskId时返回null
        return null;
      },
      taskCategories() {
        // 调试日志：检查localCategories数据
        console.log('🔍 taskCategories计算属性调用:');
        console.log('  localCategories:', this.localCategories);
        console.log('  localCategories.length:', this.localCategories.length);
        // 只使用本地API获取的分类数据，不再从store获取
        // 如果localCategories为空，返回空数组
        const result = this.localCategories.length > 0 ? this.localCategories : [];
        console.log('  返回的分类数据:', result);
        return result;
      },
      trendLabelIndices() {
        const total = Array.isArray(this.trendLabels) ? this.trendLabels.length : 0;
        if (total <= 10) {
          return Array.from({ length: total }, (_, i) => i);
        }
        const step = Math.ceil(total / 8);
        const indices = [];
        for (let i = 0; i < total; i += step) {
          indices.push(i);
        }
        if (indices[indices.length - 1] !== total - 1) {
          indices.push(total - 1);
        }
        return indices;
      },
      timeLabelIndices() {
        const total = Array.isArray(this.timeStatsLabels) ? this.timeStatsLabels.length : 0;
        if (total <= 10) {
          return Array.from({ length: total }, (_, i) => i);
        }
        const step = Math.ceil(total / 8);
        const indices = [];
        for (let i = 0; i < total; i += step) {
          indices.push(i);
        }
        if (indices[indices.length - 1] !== total - 1) {
          indices.push(total - 1);
        }
        return indices;
      },
      trendXAxisLabels() {
        const total = Array.isArray(this.trendLabels) ? this.trendLabels.length : 0;
        if (total <= 0) return [];
        const maxTicks = Math.min(8, total);
        const indices = [];
        for (let k = 0; k < maxTicks; k++) {
          const idx = Math.round((k * (total - 1)) / (maxTicks - 1));
          if (!indices.includes(idx)) indices.push(idx);
        }
        const startX = 60;
        const endX = 740;
        const step = maxTicks > 1 ? (endX - startX) / (maxTicks - 1) : 0;
        return indices.map((i, pos) => {
          const isFirst = pos === 0;
          const isLast = pos === indices.length - 1;
          const anchor = isLast ? 'end' : (isFirst ? 'start' : 'middle');
          const xBase = startX + pos * step;
          const x = isLast ? xBase - 4 : (isFirst ? xBase + 4 : xBase);
          return { index: i, x, text: this.trendLabels[i], anchor };
        });
      },
      timeXAxisLabels() {
        const total = Array.isArray(this.timeStatsLabels) ? this.timeStatsLabels.length : 0;
        if (total <= 0) return [];
        const maxTicks = Math.min(8, total);
        const indices = [];
        for (let k = 0; k < maxTicks; k++) {
          const idx = Math.round((k * (total - 1)) / (maxTicks - 1));
          if (!indices.includes(idx)) indices.push(idx);
        }
        const startX = 60;
        const endX = 740;
        const step = maxTicks > 1 ? (endX - startX) / (maxTicks - 1) : 0;
        return indices.map((i, pos) => {
          const isFirst = pos === 0;
          const isLast = pos === indices.length - 1;
          const anchor = isLast ? 'end' : (isFirst ? 'start' : 'middle');
          const xBase = startX + pos * step;
          const x = isLast ? xBase - 4 : (isFirst ? xBase + 4 : xBase);
          return { index: i, x, text: this.timeStatsLabels[i], anchor };
        });
      },
      trendLabeledPoints() {
        const pts = Array.isArray(this.trendChartDataPoints) ? this.trendChartDataPoints : [];
        const total = pts.length;
        if (total <= 10) return pts;
        const step = Math.ceil(total / 8);
        const res = [];
        for (let i = 0; i < total; i += step) res.push(pts[i]);
        if (res[res.length - 1] !== pts[total - 1]) res.push(pts[total - 1]);
        return res;
      },
      timeLabeledPoints() {
        const pts = Array.isArray(this.timeStatsChartDataPoints) ? this.timeStatsChartDataPoints : [];
        const total = pts.length;
        if (total <= 10) return pts;
        const step = Math.ceil(total / 8);
        const res = [];
        for (let i = 0; i < total; i += step) res.push(pts[i]);
        if (res[res.length - 1] !== pts[total - 1]) res.push(pts[total - 1]);
        return res;
      },
      // 计算所有已完成任务的总时间（小时）
      completedTasksTotalHours() {
        // 从getRecentTodos中获取所有已完成且包含时间信息的任务
        const completedTasks = this.getRecentTodos().filter(todo => 
          todo.completed && todo.time && todo.time.start && todo.time.end
        );
        
        // 计算总时长
        const totalHours = completedTasks.reduce((total, task) => {
          // 解析开始和结束时间
          const startTime = moment(task.time.start, "HH:mm");
          const endTime = moment(task.time.end, "HH:mm");
          
          // 计算时间差（以小时为单位）
          const duration = moment.duration(endTime.diff(startTime));
          let hours = duration.asHours();
          
          // 累加时间差到总时长
          return total + hours;
        }, 0);
        
        // 返回保留一位小数的小时数
        return totalHours.toFixed(1) + 'h';
      },
      todoLists() {
        return this.$store.getters.todoLists;
      },
      // 从getRecentTodos方法获取的数据
      recentTodos() {
        return this.getRecentTodos();
      },
      // 已完成待办事项数量
      completedTodosCount() {
        return this.recentTodos.filter(todo => todo.completed).length;
      },
      // 待处理待办事项数量
      pendingTodosCount() {
        return this.recentTodos.filter(todo => !todo.completed).length;
      },
      
      // 今日完成待办事项数量
      todayCompletedTodosCount() {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        return this.recentTodos.filter(todo => {
          if (!todo.completed) return false;
          // 这里简化处理，实际应用中应该有completedAt字段
          const createdAt = new Date(todo.createdAt);
          return createdAt >= today;
        }).length;
      },

      
      // 获取趋势图表数据（由后端提供）
      trendData() {
        if (Array.isArray(this.trendStats) && this.trendStats.length > 0) {
          return this.trendStats;
        }
        return this.selectedTrend === 'quarter_quarterly' 
          ? [0, 0, 0, 0] 
          : [0, 0, 0, 0, 0, 0, 0];
      },
      
      // 时间统计数据（由后端提供），根据period长度返回
      timeStatsData() {
        if (Array.isArray(this.timeKanbanValues) && this.timeKanbanValues.length > 0) {
          return this.timeKanbanValues;
        }
        const len = Math.min(parseInt(this.timeStatsPeriod), 30);
        return Array(len).fill(0);
      },
      // 生成趋势图的坐标点
      trendPoints() {
        const data = this.trendData;
        const maxValue = Math.max(...data, 1); // 避免除以0
        const pointCount = data.length;
        
        // 根据数据点数量计算合适的间距
        const getXCoordinate = (index) => {
          // 对于不同数量的数据点，使用不同的起始位置和间距
          if (pointCount === 4) { // 季度趋势
            // 4个数据点时，从200开始，间距150
            return 200 + index * 150;
          } else { // 7个数据点（日、周、月趋势）
            // 7个数据点时，从100开始，间距100
            return 100 + index * 100;
          }
        };
        
        return data.map((value, index) => {
          const x = getXCoordinate(index);
          // 计算Y坐标，确保有最小值，防止点太靠近底部
          const y = 250 - Math.max(10, (value / maxValue) * 180);
          return `${x},${y}`;
        }).join(' ');
      },
      // 生成趋势图的数据点
      trendChartDataPoints() {
        const data = this.trendData;
        const maxValue = Math.max(...data, 1);
        const pointCount = data.length;
        
        // 根据数据点数量计算合适的间距
        const getXCoordinate = (index) => {
          if (pointCount === 4) { // 季度趋势
            return 200 + index * 150;
          } else { // 7个数据点
            return 100 + index * 100;
          }
        };
        
        return data.map((value, index) => {
          const x = getXCoordinate(index);
          const y = 250 - Math.max(10, (value / maxValue) * 180);
          return { x, y, value };
        });
      },
      // 生成日期标签
      timeStatsLabels() {
        if (Array.isArray(this.timeKanbanLabels) && this.timeKanbanLabels.length > 0) {
          return this.timeKanbanLabels;
        }
        const labels = [];
        for (let i = Math.min(parseInt(this.timeStatsPeriod), 30) - 1; i >= 0; i--) {
          const date = moment().subtract(i, 'days');
          labels.push(date.format('MM/DD'));
        }
        return labels;
      },
      
      // 生成最近7天的具体日期标签（用于趋势图表的x轴）
      weeklyDateLabels() {
        const labels = [];
        const now = new Date();
        
        // 生成最近7天的日期标签（MM/DD格式）
        for (let i = 6; i >= 0; i--) {
          const date = new Date(now);
          date.setDate(date.getDate() - i);
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          labels.push(`${month}/${day}`);
        }
        
        return labels;
      },

      // 最大时间值（用于计算图表比例）
      maxTimeStatsValue() {
        const max = Math.max(...this.timeStatsData, 1);
        // 向上取整到最近的整数
        return Math.ceil(max);
      },

      // 生成折线图的坐标点
      timeStatsPoints() {
        const data = this.timeStatsData;
        const maxValue = Math.max(...data, 1);
        const pointCount = data.length;
        
        // 根据数据点数量计算合适的间距（与trendPoints保持一致）
        const getXCoordinate = (index) => {
          // 对于不同数量的数据点，使用不同的起始位置和间距
          if (pointCount === 4) { // 季度趋势
            // 4个数据点时，从200开始，间距150
            return 200 + index * 150;
          } else { // 7个数据点（日、周、月趋势）
            // 7个数据点时，从100开始，间距100
            return 100 + index * 100;
          }
        };
        
        return data.map((value, index) => {
          const x = getXCoordinate(index);
          // 计算Y坐标，确保有最小值，防止点太靠近底部
          const y = 250 - Math.max(10, (value / maxValue) * 180);
          return `${x},${y}`;
        }).join(' ');
      },
      
      // 生成时间统计图表的数据点，与trendChartDataPoints保持一致的结构
      timeStatsChartDataPoints() {
        const data = this.timeStatsData;
        const maxValue = Math.max(...data, 1);
        const pointCount = data.length;
        
        // 根据数据点数量计算合适的间距（与trendChartDataPoints保持一致）
        const getXCoordinate = (index) => {
          if (pointCount === 4) { // 季度趋势
            return 200 + index * 150;
          } else { // 7个数据点
            return 100 + index * 100;
          }
        };
        
        return data.map((value, index) => {
          const x = getXCoordinate(index);
          const y = 250 - Math.max(10, (value / maxValue) * 180);
          return { x, y, value };
        });
      },
    allCategories() {
      // 如果没有从store获取到分类数据，提供一些默认分类
      if (!this.taskCategories || this.taskCategories.length === 0) {
        return [
          { id: 'work', name: this.$t('taskManagement.work') },
          { id: 'personal', name: this.$t('taskManagement.personal') },
          { id: 'health', name: this.$t('taskManagement.health') },
          { id: 'shopping', name: this.$t('taskManagement.shopping') },
          { id: 'other', name: this.$t('taskManagement.other') }
        ];
      }
      return this.taskCategories;
    },
    filteredTasks() {
      // 当tasks为null时返回空数组
      if (!this.tasks) {
        return [];
      }
      
      // 处理单个任务对象
      let allTasks = Array.isArray(this.tasks) ? this.tasks : [this.tasks];
      
      // 按搜索关键词过滤
      if (this.searchQuery && allTasks.length > 0) {
        const query = this.searchQuery.toLowerCase();
        allTasks = allTasks.filter(task => 
          task && task.title && (task.title.toLowerCase().includes(query) || 
          (task.description && task.description.toLowerCase().includes(query)))
        );
      }
      
      return allTasks;
    },
    todoTasks() {
      return this.filteredTasks.filter(task => task.status === 'todo' && !task.completed);
    },
    inProgressTasks() {
      return this.filteredTasks.filter(task => task.status === 'inProgress' && !task.completed);
    },
    reviewTasks() {
      return this.filteredTasks.filter(task => task.status === 'review' && !task.completed);
    },
    doneTasks() {
      return this.filteredTasks.filter(task => task.status === 'done' || task.completed);
    },
    // 计算任务完成百分比
    completionPercentage() {
      const totalTasks = this.filteredTasks.length;
      if (totalTasks === 0) return 0;
      return Math.round((this.doneTasks.length / totalTasks) * 100);
    },
    // 按分类统计待办事项数量
    todoStatsByCategory() {
      const stats = {};
      this.filteredTasks.forEach(task => {
        const categoryName = this.getCategoryName(task.category) || '';
        if (!stats[categoryName]) {
          stats[categoryName] = 0;
        }
        stats[categoryName]++;
      });
      
      return Object.keys(stats).map(category => ({
        name: category,
        count: stats[category]
      }));
    },
    // 时间分配统计
    timeAllocationStats() {
      const totalTasks = this.filteredTasks.length;
      if (totalTasks === 0) return [];
      
      return [
        {
          type: 'done',
          label: this.$t('taskManagement.completed'),
          percentage: Math.round((this.doneTasks.length / totalTasks) * 100),
          color: '#28a745'
        },
        {
          type: 'inProgress',
          label: this.$t('taskManagement.inProgress'),
          percentage: Math.round((this.inProgressTasks.length / totalTasks) * 100),
          color: '#007bff'
        },
        {
          type: 'review',
          label: this.$t('taskManagement.review'),
          percentage: Math.round((this.reviewTasks.length / totalTasks) * 100),
          color: '#ffc107'
        },
        {
          type: 'todo',
          label: this.$t('taskManagement.toDo'),
          percentage: Math.round((this.todoTasks.length / totalTasks) * 100),
          color: '#6c757d'
        }
      ];
    },
    
    // 总耗时统计
    totalTimeSpent() {
      // 根据任务优先级和状态计算估计耗时
      let totalHours = 0;
      
      this.filteredTasks.forEach(task => {
        // 基础时间（小时）：根据优先级设置不同的基础时间
        let baseHours = 2; // 默认为2小时
        
        switch (task.priority) {
          case 'urgent':
            baseHours = 4;
            break;
          case 'high':
            baseHours = 3;
            break;
          case 'medium':
            baseHours = 2;
            break;
          case 'low':
            baseHours = 1;
            break;
        }
        
        // 如果有嵌套的待办事项，增加时间
        if (task.todos && task.todos.length > 0) {
          baseHours += task.todos.length * 0.5; // 每个嵌套待办增加0.5小时
        }
        
        // 如果任务已完成，使用实际时间（模拟）
        if (task.completed || task.status === 'done') {
          // 模拟实际完成时间为基础时间的80%-120%
          totalHours += baseHours * (0.8 + Math.random() * 0.4);
        } else {
          // 未完成任务使用估计时间
          totalHours += baseHours;
        }
      });
      
      // 如果没有任务，返回默认值
      if (totalHours === 0) return '125h';
      
      // 格式化小时数（四舍五入到整数）
      return `${Math.round(totalHours)}h`;
    }
  },
  methods: {
    // 从API加载分类数据
    loadCategoriesFromAPI() {
      console.log('🔍 开始从API加载分类数据');
      configAPI.getCategories()
        .then(response => {
          console.log('✅ 成功获取分类数据:', response);
          if (response && response.categories) {
            // 直接将获取到的分类数据保存到本地变量
            this.localCategories = response.categories;
            console.log('任务分类已从API加载到本地:', this.localCategories);
            // 手动触发视图更新
            this.$forceUpdate();
            console.log('视图已强制更新');
          } else {
            console.warn('⚠️ API返回的数据格式不正确，没有categories字段');
          }
        })
        .catch(error => {
          console.error('❌ Failed to load categories from API:', error);
          // 如果API调用失败，直接提供默认分类数据，不再从store获取
          console.log('⚠️ API调用失败，使用默认分类数据');
          this.localCategories = [
            { id: 'work', name: this.$t('taskManagement.work'), color: '#007bff' },
            { id: 'personal', name: this.$t('taskManagement.personal'), color: '#28a745' },
            { id: 'health', name: this.$t('taskManagement.health'), color: '#dc3545' },
            { id: 'shopping', name: this.$t('taskManagement.shopping'), color: '#ffc107' },
            { id: 'other', name: this.$t('taskManagement.other'), color: '#6c757d' }
          ];
          // 手动触发视图更新
          this.$forceUpdate();
        });
    },
    
    // 获取里程碑统计数据
    fetchMilestoneStats() {
      console.log('🔍 开始从API获取里程碑统计数据，taskId:', this.currentTaskId);
      
      // 检查currentTaskId是否存在
      if (!this.currentTaskId) {
        console.warn('⚠️ 没有选中的任务ID，无法获取里程碑统计数据');
        return;
      }
      
      this.milestoneStatsLoading = true;
      taskAPI.getMilestoneStatistics(this.currentTaskId)
        .then(response => {
          console.log('📊 里程碑统计数据已从API获取:', response);
          if (response) {
            // 适配后端返回的数据格式：{ summary: { total, averageCompletionRate }, milestones: [...] }
            this.milestoneStats = (response.milestones || []).map(milestone => ({
              ...milestone,
              // 转换字段名以适配前端表格
              totalTasks: milestone.totalTodos,
              completedTasks: milestone.completedTodos,
              pendingTasks: milestone.pendingTodos
            }));
            this.totalMilestonesCount = response.summary?.total || 0;
            this.averageMilestoneCompletionRate = response.summary?.averageCompletionRate || 0;
          }
        })
        .catch(error => {
          console.error('❌ 获取里程碑统计数据失败:', error);
          // 发生错误时重置数据
          this.milestoneStats = [];
          this.totalMilestonesCount = 0;
          this.averageMilestoneCompletionRate = 0;
        })
        .finally(() => {
          this.milestoneStatsLoading = false;
        });
    },
    
    // 获取待办事项创建统计数据
    fetchTodoCreationStats() {
      console.log('🔍 开始从API获取待办事项创建统计数据，taskId:', this.currentTaskId);
      
      // 检查currentTaskId是否存在
      if (!this.currentTaskId) {
        console.warn('⚠️ 没有选中的任务ID，无法获取待办事项创建统计数据');
        return;
      }
      
      this.todoStatsLoading = true;
      taskAPI.getTodoCreationStats(this.currentTaskId)
        .then(response => {
          console.log('📊 待办事项创建统计数据已从API获取:', response);
          if (response && Array.isArray(response)) {
            this.todoCreationStats = response;
          }
        })
        .catch(error => {
          console.error('❌ 获取待办事项创建统计数据失败:', error);
          // 发生错误时重置数据
          this.todoCreationStats = [];
        })
        .finally(() => {
          this.todoStatsLoading = false;
        });
    },
    
    // 从后端API获取看板数据
    loadDashboardData() {
      console.log('🔍 开始从API获取看板数据，taskId:', this.currentTaskId);
      
      // 检查currentTaskId是否存在
      if (!this.currentTaskId) {
        console.warn('⚠️ 没有选中的任务ID，无法获取看板数据');
        this.loading = false;
        return;
      }
      
      this.loading = true;
      taskAPI.getTaskDashboardDataByTaskId(this.currentTaskId)
        .then(response => {
          console.log('📊 看板数据已从API获取:', response);
          this.dashboardData = response;
          this.loading = false;
        })
        .catch(error => {
          console.error('❌ 获取看板数据失败:', error);
          this.loading = false;
          // 可以在这里添加错误处理逻辑，例如显示错误提示
        });
        
      // 获取待办事项创建统计数据
      this.fetchTodoCreationStats();
      // 获取任务完成趋势看板数据
      this.fetchTrendKanban();
      // 获取完成时间统计看板数据
      this.fetchTimeKanban();
    },
    fetchTrendKanban() {
      if (!this.currentTaskId) {
        return;
      }
      taskAPI.getTaskTrendKanban(this.currentTaskId, this.selectedTrend)
        .then(response => {
          if (response && Array.isArray(response.values)) {
            this.trendStats = response.values;
            this.trendLabels = Array.isArray(response.labels) ? response.labels : [];
          } else {
            this.trendStats = [];
            this.trendLabels = [];
          }
        })
        .catch(() => {
          this.trendStats = [];
          this.trendLabels = [];
        });
    },
    // 获取最近待办项（分页接口）
    fetchRecentTodos() {
      const params = { page: 0, size: 10, sortBy: 'create', sortOrder: 'desc' };
      if (this.currentTaskId) {
        params.taskId = this.currentTaskId;
      }
      todoAPI.getAllPaged(params)
        .then(response => {
          if (Array.isArray(response)) {
            // 适配后端Todo结构到当前UI结构
            this.recentTodosFromAPI = response.map(t => ({
              id: t.id,
              title: t.text,
              desc: t.description,
              createdAt: t.createdAt,
              completed: t.checked === 1,
              time: t.time ? { start: t.time.start, end: t.time.end } : null,
              subtasks: Array.isArray(t.subTodos) ? t.subTodos.map(st => st.text).filter(Boolean) : []
            }));
          } else {
            this.recentTodosFromAPI = [];
          }
        })
        .catch(() => {
          this.recentTodosFromAPI = [];
        });
    },
    fetchTimeKanban() {
      if (!this.currentTaskId) {
        return;
      }
      taskAPI.getTaskTimeKanban(this.currentTaskId, this.timeStatsPeriod)
        .then(response => {
          if (response && Array.isArray(response.values)) {
            this.timeKanbanValues = response.values;
            this.timeKanbanLabels = Array.isArray(response.labels) ? response.labels : [];
          } else {
            this.timeKanbanValues = [];
            this.timeKanbanLabels = [];
          }
        })
        .catch(() => {
          this.timeKanbanValues = [];
          this.timeKanbanLabels = [];
        });
    },
    // 解析任务的时间信息并格式化为显示字符串
    formatTaskTime(task) {
      // 检查任务是否有有效的时间信息
      if (!task || !task.time || typeof task.time !== 'object' || !task.time.start || !task.time.end) {
        console.warn('任务时间信息不完整:', task);
        return '--';
      }
      
      try {
        // 解析开始和结束时间, 时间格式是 HH:mm
        const startTime = new Date(`2000-01-01T${task.time.start}`);
        const endTime = new Date(`2000-01-01T${task.time.end}`);
        
        // 格式化时间为小时:分钟格式
        const startFormatted = startTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        const endFormatted = endTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        
        // 返回格式化的时间范围
        return `${startFormatted} - ${endFormatted}`;
      } catch (error) {
        console.error('解析任务时间失败:', error);
        // 解析失败时，尝试返回创建时间
        if (task && task.createdAt) {
          try {
            return new Date(task.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
          } catch (e) {
            console.error('解析创建时间失败:', e);
          }
        }
        return null;
      }
    },
    
    // 获取逾期任务数量
    getOverdueTasksCount() {
      return this.filteredTasks.filter(task => this.isOverdue(task)).length;
    },
    
    // 获取最近待办项
    getRecentTodos() {
      // 优先使用后端recent接口数据
      if (Array.isArray(this.recentTodosFromAPI) && this.recentTodosFromAPI.length > 0) {
        return this.recentTodosFromAPI;
      }
      // 兼容旧数据结构
      if (this.dashboardData && this.dashboardData.todos) {
        return this.dashboardData.todos;
      }
      return [];
    },
    
    // 获取最近待办项（限制为10条）
    getRecentTodosLimited() {
      // 调用getRecentTodos()并限制只返回前10条任务
      return this.getRecentTodos().slice(0, 10);
    },
    
    // 获取最近一年的日期范围（52周，每周7天）
    getDateRange() {
      const dates = [];
      const today = new Date();
      
      // 从今天往前推52周
      for (let week = 51; week >= 0; week--) {
        for (let day = 0; day < 7; day++) {
          const date = new Date(today);
          date.setDate(today.getDate() - (week * 7 + (6 - day)));
          dates.push(date);
        }
      }
      
      return dates;
    },
    
    // 根据日期获取当天完成的任务数量
    getCompletedTasksByDate(date) {
      // 如果API数据还没加载，返回0
      if (!this.todoCreationStats || this.todoCreationStats.length === 0) {
        return 0;
      }
      
      const targetDate = new Date(date);
      targetDate.setHours(0, 0, 0, 0);
      
      // 计算一年前的今天
      const oneYearAgo = new Date();
      oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
      oneYearAgo.setHours(0, 0, 0, 0);
      
      // 计算目标日期距离一年前的天数差
      const timeDiff = targetDate.getTime() - oneYearAgo.getTime();
      const dayDiff = Math.floor(timeDiff / (1000 * 3600 * 24));
      
      // 如果日期超出了近一年的范围，返回0
      if (dayDiff < 0 || dayDiff >= 365) {
        return 0;
      }
      
      // 从API返回的数组中获取对应日期的任务数量
      return this.todoCreationStats[dayDiff] || 0;
    },
    
    // 根据任务数量确定提交级别（颜色深浅）
    getCommitLevel(count) {
      if (count === -1) return 5; // 有未完成任务，返回红色级别
      if (count === 0) return 0;
      if (count === 1) return 1;
      if (count >= 2 && count <= 3) return 2;
      if (count >= 4 && count <= 5) return 3;
      return 4; // 5个以上任务
    },
    
    // 获取月份标签（根据实际日期计算位置）
    getMonthLabels() {
      const dateRange = this.getDateRange();
      const monthLabels = [];
      let currentMonth = -1;
      
      // 遍历日期范围，找到每个月的第一个出现位置
      for (let i = 0; i < dateRange.length; i += 7) { // 按周遍历
        const date = dateRange[i];
        const month = date.getMonth();
        
        if (month !== currentMonth) {
          // 计算列位置（每7天一行，共52周）
          const weekIndex = Math.floor(i / 7);
          
          monthLabels.push({
            name: `${month + 1}月`,
            position: `${weekIndex + 1}`
          });
          
          currentMonth = month;
        }
      }
      
      return monthLabels;
    },
    
    // 计算填充区域的点
    getAreaPoints(points) {
      if (!points || points.trim() === '') return '';
      
      const pointsArray = points.split(' ');
      if (pointsArray.length < 2) return '';
      
      // 获取第一个点的 x 坐标和最后一个点的 x 坐标
      const firstPoint = pointsArray[0].split(',');
      const lastPoint = pointsArray[pointsArray.length - 1].split(',');
      
      // 构建填充区域的点：原始点 + 右下角 + 左下角
      let areaPoints = points;
      areaPoints += ` ${lastPoint[0]},250`;
      areaPoints += ` ${firstPoint[0]},250`;
      
      return areaPoints;
    },
    
    // 获取提交提示信息
    getCommitTooltip(date, count) {
      // 添加日期和任务数量
      const targetDate = new Date(date);
      const formattedDate = targetDate.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      return `${formattedDate}\n${count}个任务`;
    },
    
    // 获取上周同期的任务统计数据
    getLastWeekStats() {
      const now = new Date();
      const lastWeekStart = new Date(now);
      lastWeekStart.setDate(now.getDate() - 14); // 两周前
      
      const lastWeekEnd = new Date(now);
      lastWeekEnd.setDate(now.getDate() - 7); // 一周前
      
      const allTodos = this.getRecentTodos();
      
      return {
        total: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return createdAt >= lastWeekStart && createdAt < lastWeekEnd;
        }).length,
        completed: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return todo.completed && createdAt >= lastWeekStart && createdAt < lastWeekEnd;
        }).length,
        pending: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return !todo.completed && createdAt >= lastWeekStart && createdAt < lastWeekEnd;
        }).length,
        weeklyNew: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return createdAt >= lastWeekStart && createdAt < lastWeekEnd;
        }).length
      };
    },
    
    // 获取本周的任务统计数据
    getThisWeekStats() {
      const now = new Date();
      const weekStart = new Date(now);
      weekStart.setDate(now.getDate() - 7); // 一周前
      
      const allTodos = this.getRecentTodos();
      
      return {
        total: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return createdAt >= weekStart;
        }).length,
        completed: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return todo.completed && createdAt >= weekStart;
        }).length,
        pending: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return !todo.completed && createdAt >= weekStart;
        }).length,
        weeklyNew: allTodos.filter(todo => {
          const createdAt = new Date(todo.createdAt);
          return createdAt >= weekStart;
        }).length
      };
    },
    
    // 获取昨日完成的任务数量
    getYesterdayCompletedCount() {
      const now = new Date();
      const yesterdayStart = new Date(now);
      yesterdayStart.setDate(now.getDate() - 1);
      yesterdayStart.setHours(0, 0, 0, 0);
      
      const yesterdayEnd = new Date(now);
      yesterdayEnd.setDate(now.getDate() - 1);
      yesterdayEnd.setHours(23, 59, 59, 999);
      
      const allTodos = this.getRecentTodos();
      
      return allTodos.filter(todo => {
        if (!todo.completed) return false;
        // 简化处理，使用createdAt作为完成时间
        const createdAt = new Date(todo.createdAt);
        return createdAt >= yesterdayStart && createdAt <= yesterdayEnd;
      }).length;
    },
    
    // 切换任务完成状态（已禁用，保持纯查看模式）
    toggleTaskCompletion() {
      // 看板页面为纯查看模式，禁用任务修改功能
      console.log('任务修改功能已禁用');
    },
    
    // 返回任务管理页面
    goBack() {
      this.$store.commit('showTaskManagement', true);
      this.$store.commit('showTaskKanban', false);
    },
    
    // 返回任务管理页面（新方法）
    goBackToTaskManagement() {
      this.$store.commit('showTaskKanban', false);
      this.$store.commit('showTaskManagement', true);
    },
    
    // 打开创建任务模态框（已禁用，保持纯查看模式）
    openCreateTaskModal() {
      // 看板页面为纯查看模式，禁用任务创建功能
      console.log('任务创建功能已禁用');
    },
    
    // 打开任务详情（已禁用，保持纯查看模式）
    openTaskDetails() {
      // 看板页面为纯查看模式，禁用任务详情查看功能
      console.log('任务详情查看功能已禁用');
    },
    
    // 关闭模态框（已禁用，保持纯查看模式）
    closeModal() {
      // 看板页面为纯查看模式，模态框已移除
      this.showModal = false;
      this.editingTask = null;
    },
    
    // 保存任务（已禁用，保持纯查看模式）
    saveTask() {
      // 看板页面为纯查看模式，禁用任务保存功能
      console.log('任务保存功能已禁用');
    },
    
    // 拖拽相关方法（已禁用，保持纯查看模式）
    handleDragStart(event) {
      // 看板页面为纯查看模式，禁用拖拽功能
      event.preventDefault();
      console.log('拖拽功能已禁用');
    },
    
    handleDrop(event) {
      event.preventDefault();
      // 看板页面为纯查看模式，禁用拖拽放置功能
      console.log('拖拽放置功能已禁用');
    },
    
    // 获取分类名称
    getCategoryName(categoryId) {
      const category = this.taskCategories.find(cat => cat.id === categoryId);
      return category ? category.name : '';
    },
    
    // 获取分类颜色
    getCategoryColor(categoryId) {
      const category = this.taskCategories.find(cat => cat.id === categoryId);
      return category ? category.color : '#6c757d';
    },
    
    // 获取列表名称
    getListName(listId) {
      // 简化实现，实际应用中可能需要从store或其他地方获取真实的列表名称
      if (!listId) return '未分类';
      
      // 尝试从列表ID中提取有意义的名称
      const nameMatch = listId.match(/[a-zA-Z]+/);
      if (nameMatch) {
        return nameMatch[0].charAt(0).toUpperCase() + nameMatch[0].slice(1);
      }
      
      // 如果无法提取，返回ID的前几位或默认名称
      return listId.length > 10 ? listId.substring(0, 10) + '...' : listId;
    },
    
    // 格式化日期
    formatDate(date) {
      return moment(date).format('MMM D, YYYY');
    },
    
    // 检查任务是否逾期
    isOverdue(task) {
      return moment(task.endDate).isBefore(moment(), 'day');
    },
    
    // 检查任务是否即将到期（3天内）
    isDueSoon(task) {
      const today = moment();
      const endDate = moment(task.endDate);
      return endDate.diff(today, 'days') >= 0 && endDate.diff(today, 'days') <= 3;
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
      console.log('allTodoLists:', allTodoLists);
      
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
    
    // 获取圆环图的颜色
    getCompletionColor() {
      const percentage = this.completionPercentage;
      if (percentage >= 75) return '#28a745';
      if (percentage >= 50) return '#28a745';
      if (percentage >= 25) return '#ffc107';
      return '#dc3545';
    },
    
    // 获取圆环图的虚线数组
    getCircleDasharray() {
      const radius = 80;
      const circumference = 2 * Math.PI * radius;
      const progress = this.completionPercentage / 100;
      const dasharray = `${progress * circumference} ${circumference - progress * circumference}`;
      return dasharray;
    },
    
    // 获取柱状图高度
    getBarHeight(count) {
      const maxCount = Math.max(...this.todoStatsByCategory.map(item => item.count), 1);
      return (count / maxCount) * 100;
    }
  }
};
</script>

<style scoped>
/* 基础样式 */
  .task-kanban-container {
    padding: 20px 20px 20px 80px;
    width: 100%;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }
  
  /* 任务时间样式 */
  .task-meta .task-time {
    margin-left: 8px;
    color: #6c757d;
    font-size: 14px;
    font-weight: 400;
  }

.trend-chart-container {
  width: 100%;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.axis-label {
  font-size: 12px;
  fill: #6c757d;
  font-weight: 500;
}
.data-label {
  font-size: 12px;
  fill: #212529;
  pointer-events: none;
}

/* 完成时间统计样式 */
.time-stats-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

/* 全宽区域样式 */
.full-width-section {
  width: 100%;
  margin-bottom: 20px;
}

/* 里程碑统计样式 */
.milestones-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  width: 100%;
}

/* 确保里程碑统计表不会被压缩 */
.milestones-table-container {
  overflow-x: auto;
  min-width: 600px; /* 设置最小宽度确保表格列不会被过度压缩 */
}

.milestones-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.milestones-section h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #212529;
}

.milestones-summary {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #6c757d;
}

.milestones-summary .milestones-count,
.milestones-summary .milestones-completion {
  font-weight: 500;
}

.milestones-table-container {
  overflow-x: auto;
}

.milestones-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.milestones-table th,
.milestones-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.milestones-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.milestones-table tbody tr:hover {
  background-color: #f8f9fa;
}

.milestone-name {
  font-weight: 500;
  color: #212529;
}

.milestone-total,
.milestone-completed,
.milestone-pending,
.milestone-rate {
  font-weight: 500;
  text-align: center;
}

.milestone-completed {
  color: #28a745;
}

.milestone-pending {
  color: #ffc107;
}

.milestone-progress {
  padding: 8px 15px;
}

.progress-bar {
  height: 8px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-low {
  background-color: #dc3545;
}

.progress-medium {
  background-color: #ffc107;
}

.progress-high {
  background-color: #28a745;
}

.no-milestones {
  text-align: center !important;
  color: #6c757d;
  font-style: italic;
  padding: 30px !important;
}

/* 数据值标签样式 */
.data-value-label {
  font-size: 12px;
  fill: #28a745;
  font-weight: 600;
}

/* 最近任务样式 */
.recent-tasks-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.view-all-link {
  font-size: 14px;
  color: #007bff;
  text-decoration: none;
  transition: color 0.2s ease;
}

.view-all-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

.task-list {
  border-top: 1px solid #e9ecef;
  padding-top: 16px;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f8f9fa;
  transition: background-color 0.2s ease;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item:hover {
  background-color: #f8f9fa;
}

.task-checkbox {
  width: 18px;
  height: 18px;
  margin-right: 12px;
  cursor: pointer;
}

.task-label {
  font-size: 14px;
  color: #495057;
  cursor: pointer;
  flex: 1;
}

/* 看板区域样式 */
.kanban-section {
  margin-top: 30px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #212529;
}

.dark-theme .section-title {
  color: #c9d1d9;
}

/* 响应式内边距调整 */
@media (max-width: 768px) {
  .task-kanban-container {
    padding: 15px;
  }
}

/* 页面头部 */
.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.back-btn {
  background: transparent;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: #6c757d;
}

.back-btn:hover {
  background-color: #f8f9fa;
}

.header-left .page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #212529;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-container {
  position: relative;
  width: 300px;
}

.search-input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.primary-action {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
}

/* 看板区域 */
.kanban-board {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 20px;
}

.kanban-board::-webkit-scrollbar {
  height: 8px;
}

.kanban-board::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.kanban-board::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.kanban-board::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 看板列 */
.kanban-column {
  min-width: 300px;
  max-width: 300px;
  background-color: #f8f9fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  animation: fadeIn 0.3s ease-in-out;
}

.column-header {
  padding: 15px;
  border-bottom: 1px solid #e9ecef;
  border-radius: 8px 8px 0 0;
  background-color: #f1f3f4;
}

.column-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-icon {
  color: #6c757d;
  font-size: 16px;
}

.column-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #212529;
}

.task-count {
  font-size: 14px;
  background-color: #dee2e6;
  color: #495057;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: auto;
}

.column-content {
  padding: 15px;
  min-height: 200px;
  max-height: calc(100vh - 250px);
  overflow-y: auto;
  flex: 1;
}

.column-content::-webkit-scrollbar {
  width: 6px;
}

.column-content::-webkit-scrollbar-track {
  background: transparent;
}

.column-content::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.column-content::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* 任务卡片 */
.task-card {
  background: white;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.task-card.overdue {
  border-left-color: #dc3545;
}

.task-card.due-soon {
  border-left-color: #ffc107;
}

.task-card.completed {
  opacity: 0.7;
  background-color: #f8f9fa;
  border-left-color: #28a745;
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
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 600px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

.modal-header {
  padding: 20px 20px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
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
  background-color: #f8f9fa;
}

.modal-body {
  padding: 20px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group.col-md-6 {
  flex: 1;
  margin-bottom: 0;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #495057;
}

.required {
  color: #dc3545;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-control:focus {
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

/* 深色主题样式 */
.dark-theme .task-kanban-container {
  color: #c9d1d9;
}

.dark-theme .kanban-header {
  border-bottom-color: #30363d;
}

.dark-theme .header-left .page-title {
  color: #c9d1d9;
}

.dark-theme .back-btn {
  color: #8b949e;
}

.dark-theme .back-btn:hover {
  background-color: #21262d;
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

.dark-theme .kanban-column {
  background-color: #161b22;
}

.dark-theme .column-header {
  background-color: #0d1117;
  border-bottom-color: #30363d;
}

.dark-theme .column-icon {
  color: #8b949e;
}

.dark-theme .column-title {
  color: #c9d1d9;
}

.dark-theme .task-count {
  background-color: #21262d;
  color: #c9d1d9;
}

.dark-theme .task-card {
  background: #21262d;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.dark-theme .task-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);
}

.dark-theme .task-card.completed {
  background-color: #161b22;
  opacity: 0.7;
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

/* 深色主题 - 全宽区域 */
.dark-theme .full-width-section {
  width: 100%;
}

/* 深色主题 - 统计卡片 */
.dark-theme .stat-card {
  background-color: #21262d;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.dark-theme .stat-card h3 {
  color: #8b949e;
}

.dark-theme .stat-value {
  color: #c9d1d9;
}

/* 深色主题 - 任务完成趋势 */
.dark-theme .trend-section {
  background-color: #21262d;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.dark-theme .section-header h3 {
  color: #c9d1d9;
}

.dark-theme .trend-select {
  background-color: #0d1117;
  border-color: #30363d;
  color: #c9d1d9;
}

.dark-theme .trend-select:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.2);
}

.dark-theme .axis-label {
  fill: #8b949e;
}

/* 深色主题 - 最近任务 */
.dark-theme .recent-tasks-section {
  background-color: #21262d;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.dark-theme .view-all-link {
  color: #58a6ff;
}

.dark-theme .view-all-link:hover {
  color: #388bfd;
}

.dark-theme .task-list {
  border-top-color: #30363d;
}

.dark-theme .task-item {
  border-bottom-color: #161b22;
}

.dark-theme .task-item:hover {
  background-color: #161b22;
}

.dark-theme .task-label {
  color: #c9d1d9;
}
</style>
