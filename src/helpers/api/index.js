import { request, setBaseUrl } from './request';
import { taskAPI } from './taskAPI';
import { todoAPI } from './todoAPI';
import { configAPI } from './configAPI';

// 创建统一的API服务对象
export const apiService = {
  // 基础请求功能
  request,
  setBaseUrl,
  
  // 模块API
  tasks: taskAPI,
  todoList: todoAPI,
  config: configAPI
};

// 导出各个API模块，以便单独使用
export { request, setBaseUrl };
export { taskAPI };
export { todoAPI };
export { configAPI };

// 默认导出统一的API服务
export default apiService;
