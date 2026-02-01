package beordie.cn.model;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

public enum TodoSortKey {
    CREATED_AT(Todo::getCreatedAt),
    UPDATED_AT(Todo::getUpdatedAt),
    PRIORITY(Todo::getPriority),
    LIST_ID(Todo::getListId);

    private final SFunction<Todo, ?> getter;

    TodoSortKey(SFunction<Todo, ?> getter) {
        this.getter = getter;
    }

    public SFunction<Todo, ?> getter() {
        return getter;
    }

    public static TodoSortKey from(String sortBy) {
        if (sortBy == null) return CREATED_AT;
        String s = sortBy.trim().toLowerCase();
        switch (s) {
            case "update":
                return UPDATED_AT;
            case "create":
                return CREATED_AT;
            case "priority":
                return PRIORITY;
            case "list":
                return LIST_ID;
            default:
                return CREATED_AT;
        }
    }
}
