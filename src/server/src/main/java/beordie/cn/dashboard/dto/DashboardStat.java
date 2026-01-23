package beordie.cn.dashboard.dto;

/**
 * @author: eason
 * @create: 2025-12-24
 * @Description: 看板统计项
 */
public class DashboardStat {
    private String id;
    private String title;
    private Object value;
    private DashboardFooter footer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DashboardFooter getFooter() {
        return footer;
    }

    public void setFooter(DashboardFooter footer) {
        this.footer = footer;
    }
}