import ratpack.exec.Blocking;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;

import static ratpack.groovy.Groovy.groovyTemplate;
import static ratpack.jackson.Jackson.json;

import java.util.*;
import java.sql.*;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Application {
    public static void main(String... args) throws Exception {
        RatpackServer.start(s -> s
                .serverConfig(c -> c
                        .baseDir(BaseDir.find())
                        .env())

                .registry(Guice.registry(b -> b
                        .module(TextTemplateModule.class, conf -> conf.setStaticallyCompile(true))))

                .handlers(chain -> chain
                        .get(ctx -> ctx.render(groovyTemplate("index.html")))

                        .post("add/:value", ctx -> {
                            System.out.println("POST add: " + ctx.getPathTokens().get("value"));
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS test2 (value TEXT)");
                                    stmt.executeUpdate("INSERT INTO test2 (value) VALUES ('" +
                                            ctx.getPathTokens().get("value") + "')");
                                    return stmt.executeQuery("SELECT value FROM test2");
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<String> output = new ArrayList<>();
                                while (rs.next()) {
                                    output.add(rs.getString("value"));
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .get("db", ctx -> {
                            System.out.println("GET db");
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    return stmt.executeQuery("SELECT value FROM test2");
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<String> output = new ArrayList<>();
                                while (rs.next()) {
                                    output.add(rs.getString("value"));
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
//                                ctx.render(groovyTemplate(attributes, "db.html"));
                                ctx.render(json(attributes));
                            });
                        })

                        .post("delete/:value", ctx -> {
                            System.out.println("POST delete: " + ctx.getPathTokens().get("value"));
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    stmt.executeUpdate("DELETE FROM test2 WHERE value='" +
                                            ctx.getPathTokens().get("value") + "'");
                                    return stmt.executeQuery("SELECT value FROM test2");
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<String> output = new ArrayList<>();
                                while (rs.next()) {
                                    output.add(rs.getString("value"));
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .files(f -> f.dir("public"))
                )
        );
    }
}