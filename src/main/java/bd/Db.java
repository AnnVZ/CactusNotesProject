package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Db {
    public void executeUpdate(String sql) {
        withStatement(statement -> {
            statement.executeUpdate(sql);
            return 0;
        });
    }

    public <T> T executeQuery(String sql, JdbcFunction<ResultSet, T> mapper) {
        return withStatement(statement -> {
            ResultSet resultSet = statement.executeQuery(sql);
            T result = mapper.apply(resultSet);
            resultSet.close();
            return result;
        });
    }

    private <T> T withStatement(JdbcFunction<Statement, T> function) {
        Connection connection = null;
        Statement statement = null;
        try {
//            Class.forName("org.h2.Driver");
//            connection = DriverManager.getConnection("jdbc:h2:./db/db");
            Class.forName("com.mysql.jdbc.Driver");
//            connection = DriverManager.getConnection("jdbc:mysql://b054d4783a0d3e:86886655@us-cdbr-east.cleardb.com/heroku_86caf156694a1bb");
            connection = DriverManager.getConnection("jdbc:mysql://eu-cdbr-west-03.cleardb.net/heroku_86caf156694a1bb", "b054d4783a0d3e", "86886655");
            statement = connection.createStatement();
            return function.apply(statement);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String escapeSQL(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    @FunctionalInterface
    public interface JdbcFunction<I, O> {
        O apply(I var1) throws Exception;
    }
}
