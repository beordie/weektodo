package beordie.cn.web;

import org.springframework.web.reactive.function.server.ServerRequest;

public class PageQuery {
    private final Integer page;
    private final Integer size;
    private final String sortBy;
    private final String sortOrder;

    private PageQuery(Integer page, Integer size, String sortBy, String sortOrder) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public static PageQuery from(ServerRequest request) {
        Integer p = request.queryParam("page").map(PageQuery::toIntOrNull).orElse(0);
        Integer s = request.queryParam("size").map(PageQuery::toIntOrNull).orElse(10);
        String sb = request.queryParam("sortBy").orElse(null);
        String so = request.queryParam("sortOrder").orElse(null);
        return new PageQuery(p, s, sb, so);
    }

    private static Integer toIntOrNull(String v) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return null;
        }
    }

    public int limit() {
        int s = size != null ? size : 10;
        return Math.max(s, 1);
    }

    public int offset() {
        int p = page != null ? page : 0;
        int safePage = Math.max(p, 0);
        return safePage * limit();
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public boolean desc() {
        return !"asc".equalsIgnoreCase(sortOrder);
    }
}
