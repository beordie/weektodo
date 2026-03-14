import todoAPI from "../../helpers/api/todoAPI";
import Todo from "../../models/todoModel";

const state = {
  todoLists: {},
  cTodoListIds: [],
  selectedDates: [],
};

const getters = {
  todoLists(state) {
    return state.todoLists;
  },
  cTodoListIds(state) {
    return state.cTodoListIds;
  },
  selectedDates(state) {
    return state.selectedDates;
  },
};

const mutations = {
  loadTodoLists(state, obj) {
    state.todoLists[obj.todoListId] = obj.todoList;
  },
  clearTodoList(state, obj) {
    state.todoLists[obj] = [];
  },
  checkTodo(state, obj) {
    const todo = state.todoLists[obj.toDoListId][obj.index];
    todo.checked = todo.checked === 0 ? 1 : 0;
  },
  moveTodoToEnd(state, obj) {
      state.todoLists[obj.toDoListId].push(state.todoLists[obj.toDoListId].splice(obj.index, 1)[0]);
  },
  addTodo(state, toDo) {
    // 使用Todo模型标准化数据结构
    const todo = Todo.fromJson(toDo);
    // 调用 API 创建待办事项
    todoAPI.createTodo(todo.toJson())
      .then(() => {
        // 创建成功后，重新加载当前列表的 todos
        return this.dispatch('loadTodoLists', todo.listId);
      })
      .catch((error) => {
        console.error('创建待办事项失败:', error);
      });
  },
  // param obj: {todoId: string, task: {}}
  updateTodo(state, obj) {
    if (obj.todoId && obj.task) {
      // 使用Todo模型标准化数据结构
      const todo = Todo.fromJson(obj.task);
      // 调用 API 更新待办事项
      todoAPI.updateTodo(obj.todoId, todo.toJson())
      .then(() => {
        // 创建成功后，重新加载当前列表的 todos
        return this.dispatch('loadTodoLists', todo.listId);
      }).catch((error) => {
          console.error('更新待办事项失败:', error);
        });
    }
  },
  // param obj: {toDoListId: string, todoId: string}
  removeTodo(state, obj) {
    // 直接调用 API 删除后端数据
    if (obj.todoId) {
      todoAPI.deleteTodo(obj.todoId)
      .then(() => {
        // 创建成功后，重新加载当前列表的 todos
        return this.dispatch('loadTodoLists', obj.toDoListId);
      }).catch((error) => {
          console.error('删除待办事项失败:', error);
        });
    }
  },
  // param obj: {toDoListId: string, todoId: string}
  toggleTodo(state, obj) {
    // 调用 API 切换待办事项状态
    if (obj.todoId) {
      todoAPI.toggleTodo(obj.todoId)
      .then(() => {
        // 切换成功后，重新加载当前列表的 todos
        return this.dispatch('loadTodoLists', obj.toDoListId);
      }).catch((error) => {
          console.error('切换待办事项状态失败:', error);
        });
    }
  },
  // param obj: {toDoListId: string, todoId: string, subTaskIndex: number}
  toggleSubTask({ dispatch }, obj) {
    // 调用 API 切换子任务状态
    if (obj.todoId) {
      todoAPI.toggleSubTask(obj.todoId, obj.subTaskIndex)
      .then(() => {
        // 切换成功后，重新加载当前列表的 todos
        return dispatch('loadTodoLists', obj.toDoListId);
      }).catch((error) => {
          console.error('切换子任务状态失败:', error);
        });
    }
  },
  insertTodo(state, obj) {
    state.todoLists[obj.toDoListId].splice(obj.index, 0, obj.toDo);
  },
  checkAllItems(state, toDoListId) {
    state.todoLists[toDoListId].forEach((toDo) => {
      toDo.checked = 1;
    });
  },
  moveUndoneItems(state, obj) {
    for (let i = state.todoLists[obj.origenId].length - 1; i >= 0; i--) {
      if (state.todoLists[obj.origenId][i].checked === 0) {
        state.todoLists[obj.origenId][i].repeatingEventId = null;
        state.todoLists[obj.origenId][i].listId = obj.destinyId;
        state.todoLists[obj.destinyId].unshift(state.todoLists[obj.origenId][i]);
        state.todoLists[obj.origenId].splice(i, 1);
      }
    }
  },
  loadCustomTodoListsIds(state, obj) {
    state.cTodoListIds = obj;
  },
  newCustomTodoList(state, obj) {
    state.cTodoListIds.push(obj);
    this.commit("loadTodoLists", { todoListId: obj.listId, todoList: [] });
  },
  removeCustomTodoList(state, obj) {
    delete state.todoLists[obj.id];
    state.cTodoListIds.splice(obj.index, 1);
  },
  updateCustomTodoList(state, obj) {
    state.cTodoListIds[obj.index].listName = obj.name;
  },
  updateSelectedDates(state, selectedDates) {
    state.selectedDates = selectedDates;
  },
};

const actions = {
  loadTodoLists({ commit }, todoListId) {
    return new Promise((resolve, reject) => {
      // 调用API获取待办事项列表
      todoAPI.getTodosByListId(todoListId)
        .then((todoList) => {
          commit("loadTodoLists", { todoListId: todoListId, todoList: todoList });
          resolve();
        })
        .catch((error) => {
          console.error("获取待办事项列表失败:", error);
          // API调用失败，不再从本地数据库获取
          reject(error);
        });
    });
  }
};

export default {
  namespaced: false,
  state,
  getters,
  actions,
  mutations,
};
