package spark;

import spark.bd.Dao;
import spark.bd.Db;
import spark.bd.User;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

public class Demo {

    private static Dao dao;

    public static void run() {
        port(8080);
        /*staticFiles.location("/public");

        get("/", (request, response) -> {
            response.redirect("index.html");
            return null;
        });

        get("/signin.html", (request, response) -> {
            response.redirect("signin.html");
            return null;
        });

        get("/registration.html", (request, response) -> {
            response.redirect("registration.html");
            return null;
        });*/

        post("/notes", (request, response) -> {
            if (request.queryParams("login").length() != 0) {
                String login = request.queryParams("login");
                String password = request.queryParams("password");
                User user = dao.getUser(login);
                if (user != null) { //logging in
                    if (user.getPassword().equals(password)) {
                        response.cookie("sessionId", user.getId() + "");
                        String body = new String(Files.readAllBytes(Paths.get("public/notes.html")));
                        response.body(body);
                        response.type("text/html");
                    } else { //неправильный пароль
                        String body = new String(Files.readAllBytes(Paths.get("public/signin.html")));
                        response.body(body.replace("</body>", "<script>alert('wryyyyy!')</script></body>"));
                        response.type("text/html");
                    }
                } else { //неправильное имя
                    String body = new String(Files.readAllBytes(Paths.get("public/signin.html")));
                    response.body(body.replace("</body>", "<script>alert('wryyyyy!')</script></body>"));
                    response.type("text/html");
                }
            } else { //registration
                String login = request.queryParams("reg_login");
                String email = request.queryParams("reg_email");
                String password = request.queryParams("reg_password");
                if (dao.getUser(login) == null) {
                    dao.insertUser(login, email, password);
                    response.cookie("sessionId", dao.getUser(login).getId() + "");
                    String body = new String(Files.readAllBytes(Paths.get("public/notes.html")));
                    response.body(body);
                    response.type("text/html");
                } else { //пользователь с таким именем уже существует
                    String body = new String(Files.readAllBytes(Paths.get("public/registration.html")));
                    response.body(body.replace("</body>", "<script>alert('wryyyyy!')</script></body>"));
                    response.type("text/html");
                }
            }
            return null;
        });

        /*get("/add.html", (request, response) -> {
            response.redirect("add.html");
            return null;
        });*/
    }

    public static void main(String[] args) {
        dao = new Dao(new Db());
        dao.createTables();
        dao.insertUser("u1", "qwerty@gmail.com", "qwerty");
//        dao.insertUser("user1", "qwerty");
//        User user = dao.getUser("user1");
//        dao.insertNote("my note1", user.getId());
//        dao.insertNote("my note2", user.getId());
//        dao.insertNote("my note3", user.getId());
//        dao.deleteNote("my note2", user.getId());
//        dao.getUserNotes("user1").forEach(System.out::println);

        run();
    }
}
