/**
 * 前端Todo模型类，用于统一管理Todo数据结构
 */
class Todo {
  constructor(id, text, checked = 0, listId, createdAt = null, updatedAt = null) {
    this.id = id;
    this.text = text;
    this.checked = checked;
    this.listId = listId;
    this.createdAt = createdAt || new Date().toISOString();
    this.updatedAt = updatedAt || new Date().toISOString();
    this.description = null;
    this.subTodos = [];
    this.color = null;
    this.priority = 0;
    this.tags = null;
    this.time = null;
    this.alarm = 0;
    this.repeatingEventId = null;
    this.taskId = null;
    this.milestoneId = null;
  }

  /**
   * 从JSON对象创建Todo实例
   * @param {Object} json - JSON对象
   * @returns {Todo} Todo实例
   */
  static fromJson(json) {
    const todo = new Todo(
      json.id,
      json.text,
      json.checked,
      json.listId,
      json.createdAt,
      json.updatedAt
    );
    
    todo.description = json.description;
    todo.subTodos = json.subTodos || json.subTaskList || [];
    todo.color = json.color;
    todo.priority = json.priority || 0;
    todo.tags = json.tags;
    todo.time = json.time;
    todo.alarm = json.alarm || 0;
    todo.repeatingEventId = json.repeatingEventId || json.repeatingEvent;
    todo.milestoneId = json.milestoneId;
    todo.taskId = json.taskId;
    
    return todo;
  }

  /**
   * 转换为JSON对象，适用于API请求
   * @returns {Object} JSON对象
   */
  toJson() {
    return {
      id: this.id,
      text: this.text,
      checked: this.checked,
      listId: this.listId,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt,
      description: this.description,
      subTodos: this.subTodos,
      color: this.color,
      priority: this.priority,
      tags: this.tags,
      time: this.time,
      alarm: this.alarm,
      repeatingEventId: this.repeatingEventId,
      taskId: this.taskId,
      milestoneId: this.milestoneId
    };
  }

  /**
   * 更新Todo属性
   * @param {Object} updates - 要更新的属性对象
   */
  update(updates) {
    Object.assign(this, updates);
    this.updatedAt = new Date().toISOString();
  }

  /**
   * 切换Todo的完成状态
   */
  toggleChecked() {
    this.checked = this.checked === 0 ? 1 : 0;
    this.updatedAt = new Date().toISOString();
  }

  /**
   * 添加子任务
   * @param {Object} subTodo - 子任务对象
   */
  addSubTodo(subTodo) {
    this.subTodos.push(subTodo);
    this.updatedAt = new Date().toISOString();
  }

  /**
   * 更新子任务
   * @param {string} subTodoId - 子任务ID
   * @param {Object} updates - 要更新的属性对象
   */
  updateSubTodo(subTodoId, updates) {
    const subTodo = this.subTodos.find(st => st.id === subTodoId);
    if (subTodo) {
      Object.assign(subTodo, updates);
      this.updatedAt = new Date().toISOString();
    }
  }

  /**
   * 删除子任务
   * @param {string} subTodoId - 子任务ID
   */
  deleteSubTodo(subTodoId) {
    this.subTodos = this.subTodos.filter(st => st.id !== subTodoId);
    this.updatedAt = new Date().toISOString();
  }
}

export default Todo;