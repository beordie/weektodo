const state = {
  activeTodo: {
    toDo: {
      text: "Text",
      checked: 0,
      listId: 1,
      desc: "",
      subTaskList: [],
      color: "none",
      priority: 0,
      tags: [],
      time: null,
      alarm: 0,
      repeatingEvent: null,
    },
  },
};

const getters = {
  activeTodo(state) {
    return state.activeTodo;
  },
};

const mutations = {
  setActiveTodo(state, obj) {
    console.log("setActiveTodo:", obj.toDo);
    state.activeTodo = obj;
  },
};

const actions = {};

export default {
  namespaced: false,
  state,
  getters,
  actions,
  mutations,
};
