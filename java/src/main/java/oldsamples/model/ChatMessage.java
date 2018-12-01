package oldsamples.model;

public class ChatMessage {
    private String from;
    private String avatar;
    private String body;
    private Long time;
    private String controlEvent;
    private boolean error;
    private Object task;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Object getTask() {
        return task;
    }

    public void setTask(Object task) {
        this.task = task;
    }

    public Long getTime() {
        return time;

    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getControlEvent() {
        return controlEvent;
    }

    public void setControlEvent(String controlEvent) {
        this.controlEvent = controlEvent;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
