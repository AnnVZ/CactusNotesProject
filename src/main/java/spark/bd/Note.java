package spark.bd;

public class Note {
    private long id;
    private String topic;
    private String text;
    private long importance;
    private long userId;

    public Note() {
    }

    public Note(long id, String topic, String text, long importance, long userId) {
        this.id = id;
        this.topic = topic;
        this.text = text;
        this.importance = importance;
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

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return text;
    }
}