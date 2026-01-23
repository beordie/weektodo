import { request } from './request';

/**
 * 任务相关API
 */
export const taskAPI = {
  /**
   * 获取所有任务，支持查询和排序
   * @param {Object} params - 查询和排序参数
   * @param {string} params.title - 任务标题模糊查询
   * @param {string} params.category - 任务分类精确查询
   * @param {string} params.sortBy - 排序字段（endDate, createdAt, priority）
   * @param {string} params.sortOrder - 排序方向（asc, desc）
   * @returns {Promise} - 返回任务列表的Promise
   */
  getAllTasks(params = {}) {
    // 构建查询参数
    const queryParams = new URLSearchParams();
    if (params.title) queryParams.append('title', params.title);
    if (params.category) queryParams.append('category', params.category);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.sortOrder) queryParams.append('sortOrder', params.sortOrder);
    
    // 构建完整的请求URL
    const url = `/tasks${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return request(url);
  },
  
  /**
   * 获取任务整体大盘数据
   * @returns {Promise} - 返回任务大盘数据的Promise
   */
  getTaskDashboardData() {
    return request('/tasks/dashboard');
  },

  /**
   * 根据任务ID获取任务看板数据
   * @param {string} taskId - 任务ID
   * @returns {Promise} - 返回任务看板数据的Promise
   */
  getTaskDashboardDataByTaskId(taskId) {
    return request(`/tasks/${taskId}/dashboard`);
  },
  
  /**
   * 根据ID获取任务
   * @param {string} id - 任务ID
   * @returns {Promise} - 返回任务对象的Promise
   */
  getTaskById(id) {
    return request(`/tasks/${id}`);
  },
  
  /**
   * 创建任务
   * @param {object} task - 任务对象
   * @returns {Promise} - 返回创建结果的Promise
   */
  createTask(task) {
    return request('/tasks', {
      method: 'POST',
      body: JSON.stringify(task)
    });
  },
  
  /**
   * 更新任务
   * @param {string} id - 任务ID
   * @param {object} task - 更新的任务对象
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateTask(id, task) {
    return request(`/tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(task)
    });
  },
  
  /**
   * 删除任务
   * @param {string} id - 任务ID
   * @returns {Promise} - 返回删除结果的Promise
   */
  deleteTask(id) {
    return request(`/tasks/${id}`, {
      method: 'DELETE'
    });
  },
  
  /**
   * 获取任务的所有里程碑
   * @param {string} taskId - 任务ID
   * @returns {Promise} - 返回里程碑列表的Promise
   */
  getMilestonesByTaskId(taskId) {
    return request(`/tasks/${taskId}/milestones`);
  },
  
  /**
   * 获取任务的特定里程碑
   * @param {string} taskId - 任务ID
   * @param {string} milestoneId - 里程碑ID
   * @returns {Promise} - 返回特定里程碑的Promise
   */
  getMilestoneByTaskIdAndId(taskId, milestoneId) {
    return request(`/tasks/${taskId}/milestones/${milestoneId}`);
  },
  
  /**
   * 为任务创建里程碑
   * @param {string} taskId - 任务ID
   * @param {object} milestone - 里程碑对象
   * @returns {Promise} - 返回创建结果的Promise
   */
  createMilestone(taskId, milestone) {
    return request(`/tasks/${taskId}/milestones`, {
      method: 'POST',
      body: JSON.stringify(milestone)
    });
  },
  
  /**
   * 更新任务的里程碑
   * @param {string} taskId - 任务ID
   * @param {string} milestoneId - 里程碑ID
   * @param {object} updates - 更新的内容
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateMilestone(taskId, milestoneId, updates) {
    return request(`/tasks/${taskId}/milestones/${milestoneId}`, {
      method: 'PUT',
      body: JSON.stringify(updates)
    });
  },
  
  /**
   * 删除任务的里程碑
   * @param {string} taskId - 任务ID
   * @param {string} milestoneId - 里程碑ID
   * @returns {Promise} - 返回删除结果的Promise
   */
  deleteMilestone(taskId, milestoneId) {
    return request(`/tasks/${taskId}/milestones/${milestoneId}`, {
      method: 'DELETE'
    });
  },

  /**
   * 获取任务的里程碑统计数据
   * @param {string} taskId - 任务ID
   * @returns {Promise} - 返回里程碑统计数据的Promise
   */
  getMilestoneStatistics(taskId) {
    return request(`/tasks/${taskId}/milestones/statistics`);
  },

  /**
   * 获取任务的所有待办事项
   * @param {string} taskId - 任务ID
   * @returns {Promise} - 返回待办事项列表的Promise
   */
  getTaskTodos(taskId) {
    return request(`/tasks/${taskId}/todos`);
  },

  /**
   * 获取任务近一年的待办事项创建统计
   * @param {string} taskId - 任务ID
   * @returns {Promise} - 返回待办事项创建统计列表的Promise
   */
  getTodoCreationStats(taskId) {
    return request(`/tasks/${taskId}/todos/stats`);
  }
};

export default taskAPI;
