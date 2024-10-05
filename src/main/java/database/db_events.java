package database;

import java.sql.*;

public class db_events {

    private Connection db_conn = null;

    public db_events(Connection db_conn){
        this.db_conn = db_conn;
    }
    public ResultSet getEvents(){
        String query = "SELECT * FROM events";
        ResultSet list = null;
        try (Statement stmt = db_conn.createStatement()){
            list = stmt.executeQuery(query);
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return list;
    }

    public int addEvent(int e_id, int year, String name, int attendance, int budget){
        return 0;
    }
    public int deleteEvent(int e_id){
        return 0;
    }
}
