package beordie.cn.web;

import org.springframework.web.reactive.function.server.ServerRequest;

public class TodoPageQuery {
    private final PageQuery base;
    private final String taskId;

    private TodoPageQuery(PageQuery base, String taskId) {
        this.base = base;
        this.taskId = taskId;
    }

    public static TodoPageQuery from(ServerRequest request) {
        PageQuery base = PageQuery.from(request);
        String taskId = request.queryParam("taskId").orElse(null);
        return new TodoPageQuery(base, taskId);
    }

    public String getTaskId() {
        return taskId;
    }

    public int offset() {
        return base.offset();
    }

    public int limit() {
        return base.limit();
    }

    public String getSortBy() {
        return base.getSortBy();
    }

    public boolean desc() {
        return base.desc();
    }
}
