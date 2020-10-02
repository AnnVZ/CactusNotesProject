package bd;

public class Note {
    private long id;
    private String topic;
    private String text;
    private long importance;
    private String datetime;
    private String form;
    private String type;
    private long userId;

    public Note() {
    }

    public Note(long id, String topic, String text, long importance, String datetime, String form, String type, long userId) {
        this.id = id;
        this.topic = topic;
        this.text = text;
        this.importance = importance;
        this.datetime = datetime;
        this.form = form;
        this.type = type;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }

    public long getImportance() {
        return importance;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getForm() {
        return form;
    }

    public String getType() {
        return type;
    }

    public long getUserId() {
        return userId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImportance(long importance) {
        this.importance = importance;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return text;
    }
}