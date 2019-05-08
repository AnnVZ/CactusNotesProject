package spark.bd;

public class Note {
    private long id;
    private String text;
    private long userId;

    public Note() {
    }

    public Note(long id, String text, long userId) {
        this.id = id;
        this.text = text;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public long getUserId() {
        return userId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return text;
    }
}