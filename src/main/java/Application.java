import ratpack.exec.Blocking;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;

import static ratpack.groovy.Groovy.groovyTemplate;
import static ratpack.jackson.Jackson.json;

import java.util.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Application {
    public static void main(String... args) throws Exception {
        final String listName = "winkel";

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
                                    AtomicInteger maxId = new AtomicInteger(0);

                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + listName + " (id INT, value TEXT, state TEXT)");
                                    ResultSet rs = stmt.executeQuery("SELECT MAX(id) from " + listName);
                                    if (rs.next()) {
                                        maxId.set(rs.getInt(1));
                                    }
                                    System.out.println("POST add: maxId = " + maxId.get() );
                                    stmt.executeUpdate("INSERT INTO " + listName + " (id, value, state) VALUES ('" + (maxId.incrementAndGet()) + "', '" +
                                            ctx.getPathTokens().get("value") + "', 'new')");
                                    System.out.println("POST add: item added");
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName);
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                System.out.println("POST add: Error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<Item> output = new ArrayList<>();
                                System.out.println("POST add: returning items");
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .get("db/:state", ctx -> {
                            System.out.println("GET db");
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName + " WHERE state ='" + ctx.getPathTokens().get("state") + "'");
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                System.out.println("GET db: Error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<Item> output = new ArrayList<>();
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
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
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName);
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                System.out.println("GET db: Error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<Item> output = new ArrayList<>();
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .post("state/:id/:state", ctx -> {
                            System.out.println("POST state id: " + ctx.getPathTokens().get("id") + " to " + ctx.getPathTokens().get("state"));
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
  //                                  UPDATE Products SET ProductName = 'Test' WHERE ProductID = 1
                                    stmt.executeUpdate("UPDATE " + listName + " SET state = '" + ctx.getPathTokens().get("state") + "' WHERE id=" +
                                            ctx.getPathTokens().get("id"));
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName );
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                System.out.println("GET db: Error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {
                                ArrayList<Item> output = new ArrayList<>();
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .post("delete/:id", ctx -> {
                            System.out.println("POST delete: " + ctx.getPathTokens().get("id"));
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    stmt.executeUpdate("DELETE FROM " + listName + " WHERE id=" +
                                            ctx.getPathTokens().get("id"));
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName);
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {

                                ArrayList<Item> output = new ArrayList<>();
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
                                }

                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("results", output);
                                ctx.render(json(attributes));
                            });
                        })

                        .post("purge", ctx -> {
                            System.out.println("POST purge");
                            boolean local = !"cedar-14".equals(System.getenv("STACK"));
                            Blocking.get(() -> {
                                Connection connection = null;

                                try {
                                    connection = DatabaseUrl.extract(local).getConnection();
                                    Statement stmt = connection.createStatement();
                                    stmt.executeUpdate("DELETE FROM " + listName + " WHERE state='check'");
                                    return stmt.executeQuery("SELECT id, value, state FROM " + listName);
                                } finally {
                                    if (connection != null) try {
                                        connection.close();
                                    } catch (SQLException e) {
                                    }
                                }
                            }).onError(throwable -> {
                                Map<String, Object> attributes = new HashMap<>();
                                attributes.put("message", "There was an error: " + throwable);
                                //ctx.render(groovyTemplate(attributes, "error.html"));
                            }).then(rs -> {

                                ArrayList<Item> output = new ArrayList<>();
                                while (rs.next()) {
                                    Item item=new Item (rs.getLong("id"), rs.getString("value"), rs.getString("state"));
                                    output.add(item);
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