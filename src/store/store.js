import {createStore} from 'vuex'
import todoLists from "./modules/todolist.store";
import config from "./modules/config.store";
import actions from "./modules/actions.store";
import notifications from "./modules/notifications.store";
import repeatingEvents from "./modules/repeatingEvent.store";
import repeatingEventsDataCache from "./modules/repeatingEventDateCache.store";
import activeTodo from "./modules/activeTodo.store";
import mainStore from './modules/main.store';
import tasks from './modules/tasks.store';


export const store = createStore({
    modules: {
        config: config,
        todoLists: todoLists,
        actions: actions,
        notifications: notifications,
        repeatingEvents: repeatingEvents,
        repeatingEventsDataCache: repeatingEventsDataCache,
        mainStore: mainStore,
        activeTodo: activeTodo,
        tasks: tasks
    },
    state: {},
    getters: {},
    mutations: {},
    actions: {}
});