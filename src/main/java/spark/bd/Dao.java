package spark.bd;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

public class Dao {
    private final Db db;

    public Dao(Db db) {
        this.db = db;
    }

    public void createTables() {
//        db.executeUpdate("DROP TABLE IF EXISTS my_users;");
//        db.executeUpdate("DROP TABLE IF EXISTS notes;");
        db.executeUpdate("CREATE TABLE IF NOT EXISTS my_users(id IDENTITY(1,1) NOT NULL PRIMARY KEY, name VARCHAR(30) NOT NULL UNIQUE, email VARCHAR(30) UNIQUE, password VARCHAR(30) NOT NULL);");
        db.executeUpdate("CREATE TABLE IF NOT EXISTS notes(id IDENTITY NOT NULL PRIMARY KEY, topic VARCHAR(25), text VARCHAR(1000) NOT NULL, importance BIGINT NOT NULL, datetime VARCHAR(20), form VARCHAR(10) NOT NULL, type VARCHAR(20) NOT NULL, user_id BIGINT NOT NULL);");
        db.executeUpdate("ALTER TABLE IF EXISTS notes ADD CONSTRAINT IF NOT EXISTS fk_user_id FOREIGN KEY (user_id) REFERENCES my_users(id) ON DELETE CASCADE;");
    }

    public void insertUser(String name, String email, String password) {
        if (email.length() == 0)
            db.executeUpdate("INSERT INTO my_users (name, password) VALUES (" + db.escapeSQL(name) + ", " + db.escapeSQL(password) + ");");
        else
            db.executeUpdate("INSERT INTO my_users (name, email, password) VALUES (" + db.escapeSQL(name) + ", " + db.escapeSQL(email) + ", " + db.escapeSQL(password) + ");");
    }

    public void insertNote(String topic, String text, String datetime, String form, String type, long userId) {
        String date = datetime;
        if (datetime.isEmpty())
            date = "0";
        if (topic.length() == 0)
            db.executeUpdate("INSERT INTO notes (text, importance, datetime, form, type, user_id) VALUES (" + db.escapeSQL(text) + ", 0, " + db.escapeSQL(date) + ", " + db.escapeSQL(form) + ", " + db.escapeSQL(type) + ", " + userId + ");");
        else
            db.executeUpdate("INSERT INTO notes (topic, text, importance, datetime, form, type, user_id) VALUES (" + db.escapeSQL(topic) + ", " + db.escapeSQL(text) + ", 0, " +  db.escapeSQL(date) + ", " + db.escapeSQL(form) + ", " + db.escapeSQL(type) + ", " + userId + ");");
    }

    public void deleteNote(String noteId) {
        db.executeUpdate("DELETE FROM notes WHERE id = " + db.escapeSQL(noteId) + ";");
    }

    public void updateNoteImportance(String noteId, String value) {
        db.executeUpdate("UPDATE notes SET importance = " + db.escapeSQL(value)+ " WHERE id = " + db.escapeSQL(noteId) + ";");
    }

    public void updateNoteText(String noteId, String value) {
        db.executeUpdate("UPDATE notes SET text = " + db.escapeSQL(value)+ " WHERE id = " + db.escapeSQL(noteId) + ";");
    }

    public void updateNoteTopic(String noteId, String value) {
        db.executeUpdate("UPDATE notes SET topic = " + db.escapeSQL(value)+ " WHERE id = " + db.escapeSQL(noteId) + ";");
    }

    public void updateNoteDate(String noteId, String value) {
        db.executeUpdate("UPDATE notes SET datetime = " + db.escapeSQL(value)+ " WHERE id = " + db.escapeSQL(noteId) + ";");
    }

    public User getUser(String name) {
        String sql = "SELECT * FROM my_users WHERE name = " + db.escapeSQL(name) + ";";
        return db.executeQuery(sql, resultSet -> {
           if (resultSet.first()) {
               long id = resultSet.getLong("id");
               String currentUserName = resultSet.getString("name");
               String email = resultSet.getString("email");
               String password = resultSet.getString("password");
               return new User(id, currentUserName, email, password);
           }
           return null;
        });
    }

    public Note getNote(Long id) {
        String sql = "SELECT * FROM notes WHERE id = " + db.escapeSQL(id + "") + ";";
        return db.executeQuery(sql, resultSet -> {
            if (resultSet.first()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("topic");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String datetime = resultSet.getString("datetime");
                String form = resultSet.getString("form");
                String type = resultSet.getString("type");
                long user_id = resultSet.getLong("user_id");
                return new Note(noteId, topic, text, importance, datetime, form, type, user_id);
            }
            return null;
        });
    }

    public List<Note> getNotesBySearch(Long id, String search) {
        List<Note> result = new ArrayList<>();
        String sql = "SELECT n.* FROM notes AS n INNER JOIN my_users AS u ON n.user_id = u.id WHERE u.id = " + db.escapeSQL(id + "") + " AND (topic LIKE " + db.escapeSQL('%' + search + '%') + " OR text LIKE " + db.escapeSQL('%' + search + '%') + ");";
        return db.executeQuery(sql, resultSet -> {
            while (resultSet.next()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("topic");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String datetime = resultSet.getString("datetime");
                String form = resultSet.getString("form");
                String type = resultSet.getString("type");
                long user_id = resultSet.getLong("user_id");
                result.add(new Note(noteId, topic, text, importance, datetime, form, type, user_id));
            }
            return result;
        });
    }

    public List<Note> getUserNotes(Long id) {
        List<Note> result = new ArrayList<>();
        String sql = "SELECT n.* FROM notes AS n INNER JOIN my_users AS u ON n.user_id = u.id WHERE u.id= " + db.escapeSQL(id + "") + ";";
        return db.executeQuery(sql, resultSet -> {
            while (resultSet.next()) {
                long noteId = resultSet.getLong("id");
                String topic = resultSet.getString("topic");
                String text = resultSet.getString("text");
                long importance = resultSet.getLong("importance");
                String datetime = resultSet.getString("datetime");
                String form = resultSet.getString("form");
                String type = resultSet.getString("type");
                long user_id = resultSet.getLong("user_id");
                result.add(new Note(noteId, topic, text, importance, datetime, form, type, user_id));
            }
            return result;
        });
    }
}
