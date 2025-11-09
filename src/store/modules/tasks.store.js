import dbRepository from '../../repositories/dbRepository';

const state = {
  tasks: {}, // 存储所有任务，键为任务ID，值为任务对象
  taskCategories: [], // 存储任务分类
  activeTaskId: null, // 当前激活的任务ID
  showTaskManagement: false, // 是否显示任务管理页面
  showTaskCreationModal: false, // 是否显示创建任务模态框
  showTaskCategoryManagement: false, // 是否显示任务分类管理
  showTaskKanban: false, // 是否显示任务看板页面
};

const getters = {
  tasks(state) {
    return state.tasks;
  },
  taskCategories(state) {
    return state.taskCategories;
  },
  activeTask(state) {
    return state.tasks[state.activeTaskId] || null;
  },
  // 获取指定分类的任务
  tasksByCategory: (state) => (categoryId) => {
    if (!categoryId) return Object.values(state.tasks);
    return Object.values(state.tasks).filter(task => task.category === categoryId);
  },
  
  // 获取任务管理页面显示状态
  showTaskManagement(state) {
    return state.showTaskManagement;
  },
  
  // 获取创建任务模态框显示状态
  showTaskCreationModal(state) {
    return state.showTaskCreationModal;
  },
  
  // 获取任务分类管理显示状态
  showTaskCategoryManagement(state) {
    return state.showTaskCategoryManagement;
  },
  
  // 获取任务看板页面显示状态
  showTaskKanban(state) {
    return state.showTaskKanban;
  },
};

const mutations = {
  // 加载任务数据
  loadTasks(state, tasks) {
    state.tasks = { ...state.tasks, ...tasks };
  },
  
  // 添加新任务
  addTask(state, task) {
    state.tasks[task.id] = task;
  },
  
  // 更新任务
  updateTask(state, { taskId, updates }) {
    if (state.tasks[taskId]) {
      state.tasks[taskId] = { ...state.tasks[taskId], ...updates };
    }
  },
  
  // 删除任务
  removeTask(state, taskId) {
    delete state.tasks[taskId];
  },
  
  // 设置激活的任务
  setActiveTask(state, taskId) {
    state.activeTaskId = taskId;
  },
  
  // 加载任务分类
  loadTaskCategories(state, categories) {
    state.taskCategories = categories;
  },
  
  // 添加任务分类
  addTaskCategory(state, category) {
    state.taskCategories.push(category);
  },
  
  // 更新任务分类
  updateTaskCategory(state, { index, updates }) {
    if (state.taskCategories[index]) {
      state.taskCategories[index] = { ...state.taskCategories[index], ...updates };
    }
  },
  
  // 删除任务分类
  removeTaskCategory(state, index) {
    state.taskCategories.splice(index, 1);
  },
  
  // 控制任务管理页面显示
  showTaskManagement(state, show) {
    state.showTaskManagement = show;
  },
  
  // 控制创建任务模态框显示
  showTaskCreationModal(state, show) {
    state.showTaskCreationModal = show;
  },
  
  // 控制任务分类管理显示
  showTaskCategoryManagement(state, show) {
    state.showTaskCategoryManagement = show;
  },
  
  // 控制任务看板页面显示
  showTaskKanban(state, show) {
    state.showTaskKanban = show;
  },
  
  // 添加Todo到任务
  addTodoToTask(state, { taskId, todoId }) {
    if (state.tasks[taskId]) {
      if (!state.tasks[taskId].todos) {
        state.tasks[taskId].todos = [];
      }
      if (!state.tasks[taskId].todos.includes(todoId)) {
        state.tasks[taskId].todos.push(todoId);
      }
    }
  },
  
  // 从任务中移除Todo
  removeTodoFromTask(state, { taskId, todoId }) {
    if (state.tasks[taskId] && state.tasks[taskId].todos) {
      const todoIndex = state.tasks[taskId].todos.indexOf(todoId);
      if (todoIndex > -1) {
        state.tasks[taskId].todos.splice(todoIndex, 1);
      }
    }
  },
};

const actions = {
  // 加载任务数据的异步操作
  loadTasks({ commit }) {
    return new Promise((resolve) => {
      let db_req = dbRepository.open();
      db_req.onsuccess = function (event) {
        let db = event.target.result;
        const tasks = {};
        
        // 正确实现游标遍历，收集所有任务数据
        const request = dbRepository.selectAll(db, "tasks");
        request.onsuccess = function(event) {
          const cursor = event.target.result;
          if (cursor) {
            tasks[cursor.key] = cursor.value;
            cursor.continue();
          } else {
            // 当所有数据都收集完成后才提交
            commit("loadTasks", tasks);
            resolve();
          }
        };
      };
    });
  },
  
  // 加载任务分类的异步操作
  loadTaskCategories({ commit }) {
    return new Promise((resolve) => {
      let db_req = dbRepository.open();
      db_req.onsuccess = function (event) {
        let db = event.target.result;
        dbRepository.get(db, "task_categories", "categories").onsuccess = function (event) {
          const categories = event.target.result || [];
          commit("loadTaskCategories", categories);
          resolve();
        };
      };
    });
  },
};

export default {
  namespaced: false,
  state,
  getters,
  actions,
  mutations,
};