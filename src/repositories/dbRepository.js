export default {
    open() {
        // 增加数据库版本号，强制重新创建对象存储
        var req = indexedDB.open('weekToDo', 5);
        req.onupgradeneeded = function (event) {
            console.log('数据库升级，创建对象存储...');
            var db = event.target.result;
            
            // 确保所有必需的对象存储都被创建
            if (!db.objectStoreNames.contains("todo_lists")) {
                db.createObjectStore('todo_lists', {autoIncrement: false});
                console.log('创建todo_lists对象存储');
            }

            if (!db.objectStoreNames.contains("repeating_events")) {
                db.createObjectStore('repeating_events', {autoIncrement: false});
                console.log('创建repeating_events对象存储');
            }

            if (!db.objectStoreNames.contains("repeating_events_by_date")) {
                db.createObjectStore('repeating_events_by_date', {autoIncrement: false});
                console.log('创建repeating_events_by_date对象存储');
            }

            if (!db.objectStoreNames.contains("tasks")) {
                db.createObjectStore('tasks', {autoIncrement: false});
                console.log('创建tasks对象存储');
            }

            if (!db.objectStoreNames.contains("task_categories")) {
                db.createObjectStore('task_categories', {autoIncrement: false});
                console.log('创建task_categories对象存储');
            }
        }
        req.onerror = function (event) {
            console.error('打开数据库失败:', event.target.errorCode);
        }
        return req;
    },
    get(db, table, id) {
        try {
            let tx = db.transaction([table], 'readonly');
            let store = tx.objectStore(table);
            let req = store.get(id);
            return req;
        } catch (error) {
            console.error(`get操作失败，表${table}不存在:`, error);
            // 创建一个假的请求对象，防止应用崩溃
            const fakeRequest = {
                onsuccess: null,
                onerror: null
            };
            setTimeout(() => {
                if (fakeRequest.onsuccess) {
                    fakeRequest.onsuccess({ target: { result: null } });
                }
            }, 0);
            return fakeRequest;
        }
    },
    add(db, table, id, obj) {
        let tx = db.transaction([table], 'readwrite');
        let store = tx.objectStore(table);
        let req = store.add(obj, id);
        return req;
    },
    update(db, table, id, obj) {
        let tx = db.transaction([table], 'readwrite');
        let store = tx.objectStore(table);
        let new_obj = JSON.parse(JSON.stringify(obj));
        let req = store.put(new_obj,id);
        return req;
    },
    delete(db, table, id) {
        let tx = db.transaction([table], 'readwrite');
        let store = tx.objectStore(table);
        let req = store.delete(id);
        return req;
    },
    selectAll(db, table){
        try {
            // 使用readonly事务，因为只是读取数据
            let tx = db.transaction([table], 'readonly');
            let store = tx.objectStore(table);
            let req = store.openCursor();
            return req;
        } catch (error) {
            console.error(`selectAll操作失败，表${table}不存在:`, error);
            // 创建一个假的请求对象，防止应用崩溃
            const fakeRequest = {
                onsuccess: null,
                onerror: null
            };
            setTimeout(() => {
                if (fakeRequest.onsuccess) {
                    fakeRequest.onsuccess({ target: { result: null } });
                }
            }, 0);
            return fakeRequest;
        }
    },
    clear(db, table){
        let tx = db.transaction([table], 'readwrite');
        let store = tx.objectStore(table);
        let req = store.clear();
        return req;
    }
};
