import dbRepository from './dbRepository';

export default {
  // 保存任务到IndexedDB
  update(taskId, task) {
    let db_req = dbRepository.open();
    db_req.onsuccess = function (event) {
      let db = event.target.result;
      dbRepository.update(db, "tasks", taskId, task);
    };
  },
  
  // 从IndexedDB中删除任务
  remove(taskId) {
    let db_req = dbRepository.open();
    db_req.onsuccess = function (event) {
      let db = event.target.result;
      dbRepository.delete(db, "tasks", taskId);
    };
  },
  
  // 保存任务分类
  updateCategories(categories) {
    let db_req = dbRepository.open();
    db_req.onsuccess = function (event) {
      let db = event.target.result;
      dbRepository.update(db, "task_categories", "categories", categories);
    };
  },
  
  // 导出所有任务数据
  exportAll() {
    return new Promise((resolve) => {
      let db_req = dbRepository.open();
      db_req.onsuccess = function (event) {
        let db = event.target.result;
        const tasks = {};
        
        dbRepository.selectAll(db, "tasks").onsuccess = function (event) {
          const cursor = event.target.result;
          
          if (cursor) {
            tasks[cursor.key] = cursor.value;
            cursor.continue();
          } else {
            dbRepository.get(db, "task_categories", "categories").onsuccess = function (event) {
              const categories = event.target.result || [];
              resolve({ tasks, categories });
            };
          }
        };
      };
    });
  },
};