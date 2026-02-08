import dbRepository from "../../repositories/dbRepository";
import todoAPI from "../../helpers/api/todoAPI";

const state = {
  repeatingEventList: {},
  repeatingEventByDate: {},
};

const getters = {
  repeatingEventList(state) {
    return state.repeatingEventList;
  },
  repeatingEventByDate(state) {
    return state.repeatingEventByDate;
  },
};

const mutations = {
  loadRepeatingEvent(state, repeatingEvent) {
    state.repeatingEventList[repeatingEvent.id] = repeatingEvent;
  },
  updateRepeatingEvent(state, obj) {
    state.repeatingEventList[obj.key] = obj.val;
  },
  removeRepeatingEvent(state, id) {
    delete state.repeatingEventList[id];
  },
  loadRepeatingEventList(state, repeatingEventList) {
    state.repeatingEventList = repeatingEventList;
  },
  loadRepeatingEventGeneratedByDate(state, obj) {
    state.repeatingEventByDate[obj.key] = obj.val ? obj.val : {};
  },
};

const actions = {
  loadRepeatingEvent({ commit }, repeatingEventId) {
    let db_req = dbRepository.open();

    db_req.onsuccess = function (event) {
      let db = event.target.result;
      var get_req = dbRepository.get(db, "repeating_events", repeatingEventId);

      get_req.onsuccess = function (event) {
        let repeatingEvent = event.target.result;
        commit("loadRepeatingEvent", repeatingEvent);
      };
    };
  },
  loadAllRepeatingEvent({ commit }) {
    return new Promise((resolve) => {
      let db_req = dbRepository.open();
      db_req.onsuccess = function (event) {
        let db = event.target.result;
        let get_req = dbRepository.selectAll(db, "repeating_events");
        var repeatingEvents = {};
        get_req.onsuccess = function () {
          let cursor = get_req.result;
          if (cursor) {
            repeatingEvents[cursor.key] = cursor.value;
            cursor.continue();
          } else {
            commit("loadRepeatingEventList", repeatingEvents);
            resolve();
          }
        };
      };
    });
  },
  loadRepeatingEventGeneratedByDate({ commit }, date) {
    return new Promise((resolve) => {
      let db_req = dbRepository.open();
      db_req.onsuccess = function (event) {
        let db = event.target.result;
        var get_req = dbRepository.get(db, "repeating_events_by_date", date);
        get_req.onsuccess = function (event) {
          let re_list = event.target.result;
          commit("loadRepeatingEventGeneratedByDate", { key: date, val: re_list });
          resolve();
        };
      };
    });
  },
  createRepeatingEvent({ dispatch }, { todoId, re_event, listId }) {
    console.log('dispatch.createRepeatingEvent.input', { todoId, listId, re_event });
    const payload = {
      id: re_event.id,
      startDate: re_event.start_date,
      repeatingRule: re_event.repeating_rule,
      type: parseInt(re_event.type, 10),
      occurrencesType: re_event.ocurrencesType,
      endDate: re_event.end_date,
    };
    console.log('dispatch.createRepeatingEvent.payload', payload);
    return todoAPI.createRepeatingEvent(todoId, payload)
      .then((res) => {
        console.log('dispatch.createRepeatingEvent.result', res);
        if (listId) return dispatch("loadTodoLists", listId);
      })
      .catch((err) => {
        console.error('dispatch.createRepeatingEvent.error', err);
        throw err;
      });
  },
  deleteRepeatingEvent({ dispatch }, { todoId, id, listId }) {
    console.log('dispatch.deleteRepeatingEvent.input', { todoId, id, listId });
    return todoAPI.deleteRepeatingEvent(todoId, id)
      .then((res) => {
        console.log('dispatch.deleteRepeatingEvent.result', res);
        if (listId) return dispatch("loadTodoLists", listId);
      })
      .catch((err) => {
        console.error('dispatch.deleteRepeatingEvent.error', err);
        throw err;
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
