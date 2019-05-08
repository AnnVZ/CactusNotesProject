package spark.bd;

import java.util.ArrayList;
import java.util.List;

public class Dao {
    private final Db db;

    public Dao(Db db) {
        this.db = db;
    }

    public void createTables() {
        db.executeUpdate("DROP TABLE IF EXISTS users;");
        db.executeUpdate("DROP TABLE IF EXISTS notes;");
        db.executeUpdate("CREATE TABLE IF NOT EXISTS users(id IDENTITY NOT NULL PRIMARY KEY, name VARCHAR(30) NOT NULL UNIQUE, email VARCHAR(30) NOT NULL UNIQUE, password VARCHAR(30) NOT NULL);");
        db.executeUpdate("CREATE TABLE IF NOT EXISTS notes(id IDENTITY NOT NULL PRIMARY KEY, text VARCHAR(250) NOT NULL, user_id BIGINT NOT NULL);");
        db.executeUpdate("ALTER TABLE IF EXISTS notes ADD CONSTRAINT IF NOT EXISTS fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;");
    }

    public void insertUser(String name, String email, String password) {
        db.executeUpdate("INSERT INTO users (name, email, password) VALUES (" + db.escapeSQL(name) + ", " + db.escapeSQL(email) + ", " + db.escapeSQL(password) + ");");
    }

    public void insertNote(String text, long userId) {
        db.executeUpdate("INSERT INTO notes (text, user_id) VALUES (" + db.escapeSQL(text) + ", " + userId + ");");
    }

    public void deleteNote(String text, long userId) {
        db.executeUpdate("DELETE FROM notes WHERE text = " + db.escapeSQL(text) + " AND user_id = " + userId + ";");
    }

    public User getUser(String name) {
        String sql = "SELECT * FROM users WHERE name = " + db.escapeSQL(name) + ";";
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

    public List<Note> getUserNotes(String name) {
        List<Note> result = new ArrayList<>();
        String sql = "SELECT n.* FROM notes AS n INNER JOIN users AS u ON n.user_id = u.id WHERE u.name = " + db.escapeSQL(name) + ";";
        return db.executeQuery(sql, resultSet -> {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String text = resultSet.getString("text");
                long user_id = resultSet.getLong("user_id");
                result.add(new Note(id, text, user_id));
            }
            return result;
        });
    }
}
