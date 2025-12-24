// API服务层，用于与后端进行通信
// 该文件现在是一个包装器，使用新的模块化API结构
// 这样可以确保现有的代码引用不会中断

import apiService from './api/index';

// 导出新的API服务
export default apiService;

// 保持向后兼容性，导出与原来相同的接口
export { apiService };
