package code.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class db_control {

    public static String url = "jdbc:sqlite:src/main/resources/dummy.db"; // Path to SQLite file
    private static Connection connection = null;
    private db_members membersLink = null;
    private db_events eventsLink = null;

    public db_control() {
        try {
            // Establish connection to the code.database
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        membersLink = new db_members(connection);
        eventsLink = new db_events(connection);
        db_init();
    }

    private void db_init() {
        String sql_members = "CREATE TABLE IF NOT EXISTS members (\n"
                + " ucid integer PRIMARY KEY,\n"
                + " fName text NOT NULL,\n"
                + " lName text NOT NULL,\n"
                + " year text NOT NULL,\n"
                + " citizen text NOT NULL,\n"
                + " status text NOT NULL\n"
                + " );";
                
        String sql_events = "CREATE TABLE IF NOT EXISTS events (\n"
                + " e_id integer PRIMARY KEY,\n"
                + " year integer NOT NULL,\n"
                + " name text NOT NULL,\n"
                + " e_date date NOT NULL,\n"
                + " attendance integer NOT NULL,\n"
                + " budget integer NOT NULL\n"
                + ");";
        String sql_memberships = "CREATE TABLE IF NOT EXISTS memberships (\n"
                + " ucid integer PRIMARY KEY,\n"
                + " since date NOT NULL,\n"
                + " until date NOT NULL,\n"
                + " payment text NOT NULL,\n"
                + " email text NOT NULL,\n"
                + " FOREIGN KEY (ucid) REFERENCES members(ucid)\n"
                + ");";
        
        String sql_participations = "CREATE TABLE IF NOT EXISTS participations (\n"
                + " ucid integer,\n"
                + " e_id integer,\n"
                + " capacity text NOT NULL,\n"
                + " PRIMARY KEY (ucid, e_id),\n"
                + " FOREIGN KEY (ucid) REFERENCES members(ucid),\n"
                + " FOREIGN KEY (e_id) REFERENCES events(e_id)\n"
                + ");";
        
        try (Statement stmt = connection.createStatement()) {
            // Execute the SQL statements
            stmt.execute(sql_members);
            stmt.execute(sql_events);
            stmt.execute(sql_memberships);
            stmt.execute(sql_participations);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public db_members getMembersLink(){
        return membersLink;
    }
    public db_events getEventsLink(){
        return eventsLink;
    }

    public void db_end() throws SQLException {
        connection.close();
    }
//    public static void main(String[] args) {
//        // connect();
////        initDb();
//    }
}