import spark.Filter;
import spark.Request;
import spark.Response;
import bd.Dao;
import bd.Db;
import bd.Note;
import bd.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static String getUserSearch(Request request) {
        String userSearch = request.cookie("userSearch");
        return userSearch;
    }

    private static String getUserTypes(Request request) {
        String userTypes = request.cookie("userTypes");
        return userTypes;
    }

    private static void setUserId(Response response, long id) {
        response.cookie("userId", id + "");
    }

    private static void setUserSearch(Response response, String search) {
        response.cookie("userSearch", search);
    }

    private static void setUserTypes(Response response, String types) {
        response.cookie("userTypes", types);
    }

    private static String getFileAsString(String filename) {
        try {
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

    private static String setSearch(String body, String search) {
        return body.replace("<input type=\"text\" name=\"search_text\" id=\"search_text\" placeholder=\"Search\" maxlength=\"25\"\n" +
                "                    spellcheck=\"false\" autocomplete=\"off\">", "<input type=\"text\" name=\"search_text\" id=\"search_text\" placeholder=\"Search\" maxlength=\"25\"\n" +
                "                    spellcheck=\"false\" autocomplete=\"off\" value=\"" + search + "\">");
    }

    private static String setTypes(String body, String types) {
        if (types.equals("0"))
            return body;
        String result = body;
        if (types.contains("work")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_work\" value=\"work\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_work\" value=\"work\" onchange='this.form.submit()' checked>");
        }
        if (types.contains("study")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_study\" value=\"study\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_study\" value=\"study\" onchange='this.form.submit()' checked>");
        }
        if (types.contains("sofa")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_sofa\" value=\"sofa\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_sofa\" value=\"sofa\" onchange='this.form.submit()' checked>");
        }
        if (types.contains("event")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_event\" value=\"event\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_event\" value=\"event\" onchange='this.form.submit()' checked>");
        }
        if (types.contains("shoplist")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_shoplist\" value=\"shoplist\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_shoplist\" value=\"shoplist\" onchange='this.form.submit()' checked>");
        }
        if (types.contains("recipe")) {
            result = result.replace("<input type=\"checkbox\" name=\"filter_recipe\" value=\"recipe\" onchange='this.form.submit()'>", "<input type=\"checkbox\" name=\"filter_recipe\" value=\"recipe\" onchange='this.form.submit()' checked>");
        }
        return result;
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
                "                    <a href=\"/change/" + index + "\" id=\"c" + index + "\"><img src=\"img/change.jpg\" alt=\"change\" title=\"Edit\"></a>\n" +
                "                    <a href=\"/mark/" + index + "\" id=\"m" + index + "\"><img src=\"img/";
        if (importance == 1)
            content += "important.jpg";
        else
            content += "notimportant.jpg";
        content += "\" alt=\"importance\" title=\"Mark\"></a>\n" +
                "                    <a href=\"/delete/" + index + "\" id=\"d" + index + "\"><img src=\"img/delete.png\" alt=\"delete\" title=\"Delete\"></a>\n" +
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
//        port(8080);
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
        before("/notes_new", onlyForUsers);
        before("/add", onlyForUsers);
        before("/change/:index", onlyForUsers);
        before("/delete/:index", onlyForUsers);
        before("/mark/:index", onlyForUsers);
        before("/filter", onlyForUsers);
        before("/search", onlyForUsers);
        before("/logout", onlyForUsers);
        before("/login", onlyForAnons);
        before("/register", onlyForAnons);

        get("/", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("index.html");
            return null;
        });

        get("/index", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("index.html");
            return null;
        });

        get("/logout", (request, response) -> {
            response.removeCookie("userId");
            response.removeCookie("userSearch");
            response.removeCookie("userTypes");
            response.redirect("signin.html");
            return null;
        });

        get("/login", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("signin.html");
            halt();
            return null;
        });

        post("/login", (request, response) -> {
            String name = request.queryParams("name");
            String pass = request.queryParams("password");
            User user = dao.getUser(name);
            if (user == null) {
                response.type("text/html");
                return addScript(getFileAsString("public/signin.html"), "alert('Such user does not exist')");
            }

            if (!pass.equals(user.getPassword())) {
                response.type("text/html");
                return addScript(getFileAsString("public/signin.html"), "alert('Wrong password entered')");
            } else {
                setUserId(response, user.getId());
                response.redirect("/notes");
                return null;
            }
        });

        get("/registration", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("registration.html");
            return null;
        });

        post("/registration", (request, response) -> {
            String name = request.queryParams("reg_login");
            String email = request.queryParams("reg_email");
            String pass = request.queryParams("reg_password");
            if (dao.getUser(name) != null) {
                response.type("text/html");
                return addScript(getFileAsString("public/registration.html"), "alert('User with the same name already exists')");
            }
            if (name.length() == 0 || pass.length() == 0) {
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

        get("/notes_new", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            response.redirect("/notes");
            return null;
        });

        get("/notes", (request, response) -> {
            String returnValue = getFileAsString("public/notes.html");
            if (getUserSearch(request) != null) {
                returnValue = setSearch(returnValue, getUserSearch(request));
            }
            if (getUserTypes(request) != null) {
                returnValue = setTypes(returnValue, getUserTypes(request));
            }
            String content = "";
            List<Note> notes = dao.getUserNotes(getUserId(request));
            if (notes.size() == 0) {
                content = "No notes found";
            } else {
                if (getUserSearch(request) != null) {
                    notes.clear();
                    notes = dao.getNotesBySearch(getUserId(request), getUserSearch(request));
                }
                if (getUserTypes(request) != null) {
                    List<Note> notesToDelete = new ArrayList<>();
                    for (Note note : notes) {
                        if (!getUserTypes(request).contains(note.getType())) {
                            notesToDelete.add(note);
                        }
                    }
                    notes.removeAll(notesToDelete);
                }
                if (notes.size() == 0) {
                    content = "No notes found";
                } else {
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
                }
            }
            response.type("text/html");
            return setNotes(returnValue, content);
        });

        get("/add", (request, response) -> {
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
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
            response.removeCookie("userTypes");
            response.removeCookie("userSearch");
            String noteIndex = request.params("index");
            Note currentNote = dao.getNote(Long.parseLong(noteIndex));
            String topic = currentNote.getTopic();
            String text = currentNote.getText();
            String datetime = currentNote.getDatetime();
            if (datetime.equals("0"))
                datetime = "";
            String content = "<form action=\"/change/";
            content += noteIndex;
            content += "\" method=\"POST\" class=\"chekbox-two\">\n" +
                    "                <p>Edit topic</p>\n" +
                    "                <textarea name=\"topic\" id=\"topic\" class=\"topic_change\" cols=\"30\" rows=\"1\" placeholder=\"Topic\" maxlength=\"25\" spellcheck=\"false\" autocomplete=\"off\">";
            if (topic != null)
                content += topic;
            content += "</textarea>\n" +
                    "                <p>Edit note text</p>\n" +
                    "                <textarea name=\"note\" id=\"note\" cols=\"30\" rows=\"14\" placeholder=\"Note\" maxlength=\"1000\"\n" +
                    "                    spellcheck=\"false\" required>";
            text = text.replace("<br>", "\r\n");
            content += text;
            content += "</textarea><div class=\"form chekbox-two margin\">\n" +
                    "                    <label class=\"checkbox f\"><input type=\"checkbox\" name=\"datetime\" value=\"show\"";
            if (datetime.length() > 0)
                content += " checked";
            content += "><span class=\"checkbox__icon\"></span>" +
                    "   show date and time</label></div>\n" +
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
            String search = request.queryParams("search_text");
            if (search.isEmpty())
                response.removeCookie("userSearch");
            else
                setUserSearch(response, search);
            response.redirect("/notes");
            return null;
        });

        get("/filter", (request, response) -> {
            response.removeCookie("userTypes");
            String filterWork = request.queryParams("filter_work");
            String filterStudy = request.queryParams("filter_study");
            String filterHome = request.queryParams("filter_sofa");
            String filterEvent = request.queryParams("filter_event");
            String filterShoplist = request.queryParams("filter_shoplist");
            String filterRecipe = request.queryParams("filter_recipe");
            String res = "";
            if (filterWork != null)
                res += "work";
            if (filterStudy != null)
                res += "study";
            if (filterHome != null)
                res += "sofa";
            if (filterEvent != null)
                res += "event";
            if (filterShoplist != null)
                res += "shoplist";
            if (filterRecipe != null)
                res += "recipe";
            if (!res.equals(""))
                setUserTypes(response, res);
            response.redirect("/notes");
            return null;
        });
    }

    public static void main(String[] args) {
        dao = new Dao(new Db());
        dao.createTables();
//        User user = dao.getUser(1);
//        if (user != null) {
//            System.out.println("exists");
//        }
//        dao.deleteUser("1");
//        User user1 = dao.getUser(1);
//        if (user1 != null) {
//            System.out.println("exists");
//        }
        run();
    }
}
