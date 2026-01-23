package beordie.cn.dashboard.dto;

/**
 * @author: eason
 * @create: 2025-12-24
 * @Description: 看板统计项的底部信息
 */
public class DashboardFooter {
    private String type;
    private String text;
    private String color;
    private String icon;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}