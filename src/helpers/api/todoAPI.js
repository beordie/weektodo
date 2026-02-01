import { request } from './request.js';

/**
 * 待办事项相关API
 */
export const todoAPI = {
  /**
   * 获取所有待办事项
   * @returns {Promise} - 返回所有待办事项的Promise
   */
  getAll() {
    return request('/todos');
  },
  /**
   * 分页获取待办事项，支持排序
   * @param {{page:number,size:number,sortBy?:string,sortOrder?:'asc'|'desc',taskId?:string}} params
   */
  getAllPaged(params) {
    const q = new URLSearchParams();
    if (typeof params.page === 'number') q.set('page', String(params.page));
    if (typeof params.size === 'number') q.set('size', String(params.size));
    if (params.taskId) q.set('taskId', params.taskId);
    return request(`/todos?${q.toString()}`);
  },
  
  /**
   * 根据ID获取待办事项
   * @param {string} id - 待办事项ID
   * @returns {Promise} - 返回特定待办事项的Promise
   */
  getTodoById(id) {
    return request(`/todos/${id}`);
  },
  
  /**
   * 根据列表ID获取待办事项
   * @param {string} listId - 列表ID
   * @returns {Promise} - 返回列表下所有待办事项的Promise
   */
  getTodosByListId(listId) {
    return request(`/todos/list/${listId}`);
  },
  
  /**
   * 根据任务内容搜索待办事项
   * @param {string} task - 任务内容
   * @returns {Promise} - 返回搜索结果的Promise
   */
  getTodosByTask(task) {
    return request(`/todos/search/by-task?task=${encodeURIComponent(task)}`);
  },
  
  /**
   * 根据里程碑搜索待办事项
   * @param {string} milestone - 里程碑名称
   * @returns {Promise} - 返回搜索结果的Promise
   */
  getTodosByMilestone(milestone) {
    return request(`/todos/search/by-milestone?milestone=${encodeURIComponent(milestone)}`);
  },
  
  /**
   * 创建待办事项
   * @param {object} todo - 待办事项对象
   * @returns {Promise} - 返回创建结果的Promise
   */
  createTodo(todo) {
    return request('/todos', {
      method: 'POST',
      body: JSON.stringify(todo)
    });
  },
  
  /**
   * 更新待办事项
   * @param {string} id - 待办事项ID
   * @param {object} updates - 更新的内容
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateTodo(id, updates) {
    return request(`/todos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(updates)
    });
  },
  
  /**
   * 删除待办事项
   * @param {string} id - 待办事项ID
   * @returns {Promise} - 返回删除结果的Promise
   */
  deleteTodo(id) {
    return request(`/todos/${id}`, {
      method: 'DELETE'
    });
  },
  
  /**
   * 切换待办事项的完成状态
   * @param {string} id - 待办事项ID
   * @returns {Promise} - 返回切换结果的Promise
   */
  toggleTodo(id) {
    return request(`/todos/${id}/toggle`, {
      method: 'PATCH'
    });
  },
  
  /**
   * 切换子任务的完成状态
   * @param {string} id - 待办事项ID
   * @param {number} index - 子任务索引
   * @returns {Promise} - 返回切换结果的Promise
   */
  toggleSubTask(id, index) {
    return request(`/todos/${id}/subtask/${index}/toggle`, {
      method: 'PATCH'
    });
  }
};

export default todoAPI;
