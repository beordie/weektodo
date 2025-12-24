import { request } from './request';

/**
 * 配置相关API
 */
export const configAPI = {
  /**
   * 获取配置
   * @returns {Promise} - 返回配置的Promise
   */
  get() {
    return request('/config');
  },
  
  /**
   * 获取分类配置
   * @returns {Promise} - 返回分类配置的Promise
   */
  getCategories() {
    return request('/config/categories');
  },
  
  /**
   * 获取优先级配置
   * @returns {Promise} - 返回优先级配置的Promise
   */
  getPriorities() {
    return request('/config/priorities');
  },
  
  /**
   * 更新配置
   * @param {object} config - 配置对象
   * @returns {Promise} - 返回更新结果的Promise
   */
  update(config) {
    return request('/config', {
      method: 'PUT',
      body: JSON.stringify(config)
    });
  }
};

export default configAPI;
