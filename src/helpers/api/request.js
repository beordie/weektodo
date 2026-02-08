// 基础请求配置
const DEFAULT_BASE_URL = '/api';

// API配置对象
const apiConfig = {
  baseUrl: DEFAULT_BASE_URL
};

/**
 * 设置API基础URL
 * @param {string} url - 新的API基础URL
 */
export function setBaseUrl(url) {
  apiConfig.baseUrl = url;
}

/**
 * 封装的fetch请求函数
 * @param {string} endpoint - API端点路径
 * @param {object} options - 请求选项
 * @returns {Promise} - 返回请求结果的Promise
 */
export async function request(endpoint, options = {}) {
  const url = `${apiConfig.baseUrl}${endpoint}`;
  
  // 设置默认请求头
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  
  try {
    const response = await fetch(url, {
      ...options,
      headers
    });
    
    // 检查响应状态
    if (!response.ok) {
      const error = await response.json().catch(() => ({
        message: `请求失败：${response.status} ${response.statusText}`
      }));
      throw new Error(error.message || '请求失败');
    }
    
    // 如果响应状态为204 No Content，则返回null
    if (response.status === 204) {
      return null;
    }
    
    // 解析响应数据
    const responseText = await response.text();
    if (!responseText) {
      return null;
    }
    return JSON.parse(responseText);
  } catch (error) {
    console.error('API请求错误:', error);
    throw error;
  }
}

export default request;
