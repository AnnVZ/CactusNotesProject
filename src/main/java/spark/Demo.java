package spark;

import spark.bd.Dao;
import spark.bd.Db;
import spark.bd.Note;
import spark.bd.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringTokenizer;

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

    private static String setSearchedNotes(String body, String content, String search) {
        String result = setNotes(body, content);
        return result.replace("<input type=\"text\" name=\"search_text\" id=\"search_text\" placeholder=\"Note\" maxlength=\"25\" spellcheck=\"false\"\n" +
                "                autocomplete=\"off\">", "<input type=\"text\" name=\"search_text\" id=\"search_text\" placeholder=\"Note\" maxlength=\"25\" spellcheck=\"false\"\n" +
                "                autocomplete=\"off\" value=\"" + search + "\">");
    }

    private static String changeNote(String body, String content) {
        return body.replace("<!--changing goes here -->", content);
    }

    private static String createDivNoteBody(String topic, String text, long importance, String datetime, String form, String type, long index) {
        String content = "";
        content += "<div class=\"note\">\n" +
                "                <div class=\"note_head\">\n" +
                "                    <p class=\"topic\">";
        if (topic != null)
            content += topic;
        content += "</p>\n" +
                "                    <a href=\"/change/" + index + "\" id=\"c" + index + "\"><img src=\"img/change.jpg\" alt=\"change\" title=\"change\"></a>\n" +
                "                    <a href=\"/mark/" + index + "\" id=\"m" + index + "\"><img src=\"img/";
        if (importance == 1)
            content += "important.jpg";
        else
            content += "notimportant.jpg";
        content += "\" alt=\"importance\" title=\"mark\"></a>\n" +
                "                    <a href=\"/delete/" + index + "\" id=\"d" + index + "\"><img src=\"img/delete.png\" alt=\"delete\" title=\"delete\"></a>\n" +
                "                </div>\n" +
                "                <div class=\"note_text\">\n" +
                "                    <p>";
        if (form.equals("list")) {
            content += "• ";
            content += text.replace("<br>", "<br>• ");
        } else
            content += text;
        content += "</p>";
        if (!type.equals("none")) {
            content += "<img src=\"img/" + type + ".png\" alt=\"type\" class=\"type_image\">";
        }
        if (!datetime.isEmpty())
            content += "<p class=\"date\">" + datetime + "</p>";
        content += "                </div>\n" +
                "            </div>";
        return content;
    }

    private static String getDateTime() {
        String day = String.valueOf(LocalDateTime.now()).substring(0, String.valueOf(LocalDateTime.now()).indexOf("T"));
        String time = String.valueOf(LocalDateTime.now()).substring(String.valueOf(LocalDateTime.now()).indexOf("T") + 1, String.valueOf(LocalDateTime.now()).indexOf("T") + 6);
        return day + " " + time;
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
                if (notes.get(i).getImportance() == 1) {
                    String date = notes.get(i).getDatetime();
                    if (date.equals("0"))
                        date = "";
                    content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), date, notes.get(i).getForm(), notes.get(i).getType(), notes.get(i).getId());
                }
            }
            for (int i = notes.size() - 1; i >= 0; --i) {
                if (notes.get(i).getImportance() != 1) {
                    String date = notes.get(i).getDatetime();
                    if (date.equals("0"))
                        date = "";
                    content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), date, notes.get(i).getForm(), notes.get(i).getType(), notes.get(i).getId());
                }
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
            note = note.replace("\r\n", "<br>");
            String datetime = request.queryParams("datetime");
            String form = request.queryParams("form");
            String type = request.queryParams("radio");
            if (datetime == null)
                datetime = "";
            else
                datetime = getDateTime();
            dao.insertNote(topic, note, datetime, form, type, getUserId(request));
            response.redirect("/notes");
            return null;
        });

        get("/change/:index", (request, response) -> {
            String noteIndex = request.params("index");
            Note currentNote = dao.getNote(Long.parseLong(noteIndex));
            String topic = currentNote.getTopic();
            String text = currentNote.getText();
            String datetime = currentNote.getDatetime();
            if (datetime.equals("0"))
                datetime = "";
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
            text = text.replace("<br>", "\r\n");
            content += text;
            content += "</textarea><p><input type=\"checkbox\" name=\"datetime\" value=\"show\"";
            if (datetime.length() > 0)
                content += " checked";
            content += ">show date and time</p>\n" +
                    "                <input type=\"submit\" class=\"button button_add\" value=\"Save\">\n" +
                    "            </form>";
            response.type("text/html");
            return changeNote(getFileAsString("public/change.html"), content);
        });

        post("/change/:index", (request, response) -> {
            String noteIndex = request.params("index");
            String topic = request.queryParams("topic");
            String text = request.queryParams("note");
            text = text.replace("\r\n", "<br>");
            String datetime = request.queryParams("datetime");
            if (datetime == null)
                datetime = "";
            else
                datetime = getDateTime();
            dao.updateNoteText(noteIndex, text);
            dao.updateNoteTopic(noteIndex, topic);
            dao.updateNoteDate(noteIndex, datetime);
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

        get("/search", (request, response) -> {
            String text = request.queryParams("search_text");
            if (text.isEmpty())
                response.redirect("/notes");
            else {
                List<Note> notes = dao.getNotesBySearch(getUserId(request), text);
                if (notes.isEmpty()) {
                    String content = "No notes found";
                    return setSearchedNotes(getFileAsString("public/notes.html"), content, text);
                } else {
                    String content = "";
                    content += "<div class=\"notes_block\">\n";
                    for (int i = notes.size() - 1; i >= 0; --i) {
                        if (notes.get(i).getImportance() == 1) {
                            String date = notes.get(i).getDatetime();
                            if (date.equals("0"))
                                date = "";
                            content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), date, notes.get(i).getForm(), notes.get(i).getType(), notes.get(i).getId());
                        }
                    }
                    for (int i = notes.size() - 1; i >= 0; --i) {
                        if (notes.get(i).getImportance() != 1) {
                            String date = notes.get(i).getDatetime();
                            if (date.equals("0"))
                                date = "";
                            content += createDivNoteBody(notes.get(i).getTopic(), notes.get(i).getText(), notes.get(i).getImportance(), date, notes.get(i).getForm(), notes.get(i).getType(), notes.get(i).getId());
                        }
                    }
                    content += "</div>";
                    response.type("text/html");
                    return setSearchedNotes(getFileAsString("public/notes.html"), content, text);
                }
            }
            return null;
        });
    }

    public static void main(String[] args) {
        dao = new Dao(new Db());
        dao.createTables();
        run();
    }
}
