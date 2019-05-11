package spark;

import spark.bd.Dao;
import spark.bd.Db;
import spark.bd.Note;
import spark.bd.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static spark.Spark.*;

public class Demo {

    private static Dao dao;

    private static Long getUserId(Request request) {
        String userId = request.cookie("userId");
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId);
    }

    private static void setUserId(Response response, long id) {
        response.cookie("userId", id + "");
    }

    private static String getFileAsString(String filename) {
        try {
//            Resources.toString(Resources.getResource(filename), Charsets.UTF_8);
//            return new String(Files.readAllBytes(Paths.get(Demo.class.getClassLoader().getResource(filename).getFile())));
            return new String(Files.readAllBytes(Paths.get("src/main/resources/" + filename)));
        } catch (IOException e) {
            return null;
        }
    }

    private static String addScript(String body, String content) {
        return body.replace("</body>", "<script>" + content + "</script></body>");
    }

    private static String setNotes(String body, String content) {
        return body.replace("<!--notes go here-->", content);
    }

    private static String changeNote(String body, String content) {
        return body.replace("<!--changing goes here -->", content);
    }

    private static String createDivNoteBody(String topic, String text, long importance, long index) {
        String content = "";
        content += "<div class=\"note\">\n" +
                "                <div class=\"note_head\">\n" +
                "                    <p class=\"topic\">";
        if (topic != null)
            content += topic;
        content += "</p>\n" +
                "                    <a href=\"/change/" + index + "\"><img src=\"img/change.jpg\" alt=\"change\" title=\"change\"></a>\n" +
                "                    <a href=\"/mark/" + index + "\"><img src=\"img/";
        if (importance == 1)
            content += "important.jpg";
        else
            content += "notimportant.jpg";
        content += "\" alt=\"importance\" title=\"mark\"></a>\n" +
                "                    <a href=\"/delete/" + index + "\"><img src=\"img/delete.png\" alt=\"delete\" title=\"delete\"></a>\n" +
                "                </div>\n" +
                "                <div class=\"note_text\">\n" +
                "                    <p>";
        content += text;
        content += "</p>\n" +
                "                </div>\n" +
                "            </div>";
        return content;
    }

    private static void run() {
        port(8080);
        staticFiles.location("/public");

        Filter onlyForUsers = (request, response) -> {
            Long id = getUserId(request);
            if (id == null) {
                response.redirect("/login");
            }
        };

        Filter onlyForAnons = (request, response) -> {
            Long id = getUserId(request);
            if (id != null) {
                response.redirect("/notes");
            }
        };

        before("/notes", onlyForUsers);
        before("/add", onlyForUsers);
        before("/login", onlyForAnons);
        before("/register", onlyForAnons);

        get("/", (request, response) -> {
            response.redirect("index.html");
            return null;
        });

        get("/logout", (request, response) -> {
            response.removeCookie("userId");
            response.redirect("signin.html");
            return null;
        });

        get("/login", (request, response) -> {
            response.redirect("signin.html");
            return null;
        });

        post("/login", (request, response) -> {
            String name = request.queryParams("name");
            String pass = request.queryParams("password");
            User user = dao.getUser(name);
            if (user == null || !pass.equals(user.getPassword())) {
                response.type("text/html");
                return addScript(getFileAsString("public/signin.html"), "alert('Can not log in')");
            } else {
                setUserId(response, user.getId());
                response.redirect("/notes");
                return null;
            }
        });

        get("/registration", (request, response) -> {
            response.redirect("registration.html");
            return null;
        });

        post("/registration", (request, response) -> {
            String name = request.queryParams("reg_login");
            String email = request.queryParams("reg_email");
            String pass = request.queryParams("reg_password");
            if (dao.getUser(name) != null || name.length() == 0 || pass.length() == 0) {
                response.type("text/html");
                return addScript(getFileAsString("public/registration.html"), "alert('Cannot sign up')");
            } else {
                dao.insertUser(name, email, pass);
                User user = dao.getUser(name);
                setUserId(response, user.getId());
                response.redirect("/notes");
                return null;
            }
        });

        get("/notes", (request, response) -> {
            String content = "";
            List<Note> notes = dao.getUserNotes(getUserId(request));
            content += "<div class=\"notes_block\">\n";
            for (int i = notes.size() - 1; i >= 0; --i) {
                if (notes.get(i).getImportance() == 1)
                    content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), notes.get(i).getId());
            }
            for (int i = notes.size() - 1; i >= 0; --i) {
                if (notes.get(i).getImportance() != 1)
                    content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), notes.get(i).getId());
            }
            content += "</div>";
            response.type("text/html");
            return setNotes(getFileAsString("public/notes.html"), content);
        });

        get("/add", (request, response) -> {
            response.redirect("add.html");
            return null;
        });

        post("/add", (request, response) -> {
            String topic = request.queryParams("topic");
            String note = request.queryParams("note");
            dao.insertNote(topic, note, getUserId(request));
            response.redirect("/notes");
            return null;
        });

        get("/change/:index", (request, response) -> {
            String noteIndex = request.params("index");
            Note currentNote = dao.getNote(Long.parseLong(noteIndex));
            String topic = currentNote.getTopic();
            String text = currentNote.getText();
            String content = "<form action=\"/change/";
            content += noteIndex;
            content += "\" method=\"POST\">\n" +
                    "                <p>Change topic</p>\n" +
                    "                <textarea name=\"topic\" id=\"topic\" class=\"topic_change\" cols=\"30\" rows=\"1\" placeholder=\"Topic\" maxlength=\"25\" spellcheck=\"false\" autocomplete=\"off\">";
            if (topic != null)
                content += topic;
            content += "</textarea>\n" +
                    "                <p>Change note text</p>\n" +
                    "                <textarea name=\"note\" id=\"note\" cols=\"30\" rows=\"14\" placeholder=\"Note\" maxlength=\"1000\"\n" +
                    "                    spellcheck=\"false\" required>";
            content += text;
            content += "</textarea>\n" +
                    "                <input type=\"submit\" class=\"button button_add\" value=\"Save\">\n" +
                    "            </form>";
            response.type("text/html");
            return changeNote(getFileAsString("public/change.html"), content);
        });

        post("/change/:index", (request, response) -> {
            String noteIndex = request.params("index");
            String topic = request.queryParams("topic");
            String text = request.queryParams("note");
            dao.updateNoteText(noteIndex, text);
            dao.updateNoteTopic(noteIndex, topic);
            response.redirect("/notes");
            return null;
        });

        get("/mark/:index", (request, response) -> {
            String noteIndex = request.params("index");
            Note currentNote = dao.getNote(Long.parseLong(noteIndex));
            if (currentNote.getImportance() == 1)
                dao.updateNoteImportance(noteIndex, "0");
            else
                dao.updateNoteImportance(noteIndex, "1");
            response.redirect("/notes");
            return null;
        });

        get("/delete/:index", (request, response) -> {
            String noteIndex = request.params("index");
            dao.deleteNote(noteIndex);
            response.redirect("/notes");
            return null;
        });
    }

    public static void main(String[] args) {
        dao = new Dao(new Db());
//        dao.insertUser("name", "", "123123");
        dao.createTables();
        run();
    }
}
