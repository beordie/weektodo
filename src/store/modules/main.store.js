const state = {
    clicks: 0,
    clicksTimer: null,
    undoElement: null,
    listToClearId: null,
    currentTaskId: null
}

const getters = {
    clicks(state) {
        return state.clicks;
    },
    clicksTimer(state) {
        return state.clicksTimer;
    },
    undoElement(state) {
        return state.undoElement;
    },
    listToClearId(state) {
        return state.listToClearId;
    },
    currentTaskId(state) {
        return state.currentTaskId;
    }
}

const mutations = {
    setClicks(state, obj) {
        state.clicks = obj;
    },
    setClicksTimer(state, obj) {
        state.clicksTimer = obj;
    },
    setUndoElement(state, obj) {
        state.undoElement = obj;
    },
    setListToClear(state, obj) {
        state.listToClearId = obj;
    },
    setCurrentTaskId(state, obj) {
        state.currentTaskId = obj;
    }
}

export default {
    namespaced: false,
    state,
    getters,
    mutations
}
