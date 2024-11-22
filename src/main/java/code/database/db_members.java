package code.database;

import code.util;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static code.util.extractYear;


public class db_members {
    private Connection db_conn = null;
    public db_members(Connection db_conn){
        this.db_conn = db_conn;
    }

    public int[] importMembers(String CSV_FILE_PATH){
//        String CSV_FILE_PATH = "src/main/resources/membershipform.csv";
        String line;

        List<String[]> data = new ArrayList<>(); // To store parsed data

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            // Read the header line (if necessary)
            String header = br.readLine(); // Uncomment if you want to read the header
            while ((line = br.readLine()) != null) {
                // Use split method to parse the line
                String[] values = line.split(",");
                data.add(values); // Add parsed line to data list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[] count = {0, 0};
        for (String row[] : data) {
            if ((row[7].strip().equalsIgnoreCase("Paid"))) {
                // Get
                int ucid = Integer.parseInt(row[4]);
                String email  = "";
                String payment = "";
                // Make first and last name proper
                String fname = row[1].substring(0,1).toUpperCase() + row[1].substring(1).toLowerCase();
                String lname = row[2].substring(0,1).toUpperCase() + row[2].substring(1).toLowerCase();
                // Extract email
                email = util.extractEmail(row[3]).toLowerCase();
                if (row.length > 9) {
                    payment = row[8];
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss");

                count[0] += add(ucid, util.proper(fname), util.proper(lname), extractYear(row[5]), row[6], LocalDate.parse(row[0], formatter),
                        LocalDate.now().plusYears(1), payment, email);
            }
        }
        count[1] = data.size()-count[0];
        return count;
    }
    public boolean getList(ObservableList<String[]> result) throws SQLException {
        String query = "SELECT * FROM members";
        ResultSet list = null;

        try (Statement stmt = db_conn.createStatement()){
            list = stmt.executeQuery(query);
            int columnCount = list.getMetaData().getColumnCount();
            while (list.next()){
                String[] row = new String[columnCount]; // Create an array to hold the row data
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = list.getString(i); // Store each column value in the array
                }
                result.add(row);
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return true;
    }

    /**
     * Add a member to the code.database
     * @param ucid ucid of the member to be added
     * @param fName first name
     * @param lName last name
     * @param year year of study (1 - 5 (graduate))
     * @param since date first joined as member
     * @param until date membership is valid until
     * @return return 1 if successfully added, 0 otherwise
     */
    public int add(int ucid, String fName, String lName, String year, String citizen,
                   LocalDate since, LocalDate until, String payment, String email){
        String member_query = "INSERT INTO members (ucid, fName, lName, year, citizen, status) VALUES (?, ?, ?, ?, ?, ?) ";
        String membership_query = "INSERT INTO memberships (ucid, since, until, payment, email) VALUES (?, ?, ?, ?, ?) ";
        try (PreparedStatement stmt1 = db_conn.prepareStatement(member_query)) {

            // Set parameters for the prepared statement
            stmt1.setInt(1, ucid);
            stmt1.setString(2, fName);
            stmt1.setString(3, lName);
            stmt1.setString(4, year);
            stmt1.setString(5, citizen);
            // Validity status set to inactive by default
            stmt1.setString(6, "inactive");

            // Change status to active if membership is valid in future
            if (until.isAfter(LocalDate.now())){
                stmt1.setString(6, "active");
            }
            // Execute statement to add a member
            stmt1.executeUpdate();

            // Add row to the memberships table
            PreparedStatement stmt2 = db_conn.prepareStatement(membership_query);
            stmt2.setInt(1, ucid);
            stmt2.setDate(2, Date.valueOf(since));
            stmt2.setDate(3, Date.valueOf(until));
            stmt2.setString(4, payment);
            stmt2.setString(5, email);

            // Execute statement to update membership
            stmt2.executeUpdate();

            return 1; // Return success

        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { // check specific error code for duplicate keys
                return 19;
            }
            return 0; // Return an error
        }
    }

    /**
     * Remove a member from the code.database
     * @param ucid the ucid of the member to be removed
     * @return return 1 if success, 0 otherwise
     */
    public int remove(int ucid){
        String member_query = "DELETE FROM members WHERE ucid = ?";
        String membership_query = "DELETE FROM memberships WHERE ucid = ?";
        try {
            PreparedStatement pstmt = db_conn.prepareStatement(member_query);
            pstmt.setInt(1, ucid);
            int isDeleted = pstmt.executeUpdate();
            // If a member with the UCID existed
            if (isDeleted > 0){
                // Delete their membership
                pstmt = db_conn.prepareStatement(membership_query);
                pstmt.setInt(1, ucid);
                pstmt.executeUpdate();
                // Return success
                return 1;
            }
            // Else return failure
            else{
                return 0;
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // Perhaps when you view a membership,
    // you should be able to edit the member details there
    public int update(int ucid, String year, String citizen, LocalDate until, String payment, String email) throws SQLException {
        // Query for updating members table
        String member_query = "UPDATE members SET "
                + "year = COALESCE(NULLIF(?, ''), year), "
                + "citizen = COALESCE(NULLIF(?, ''), citizen), "
                + "status = ? "
                + "WHERE ucid = ?";

        // Query for updating memberships table
        String membership_query = "UPDATE memberships SET "
                + "until = COALESCE(NULLIF(?, ''), until), "
                + "payment = COALESCE(NULLIF(?, ''), payment), "
                + "email = COALESCE(NULLIF(?, ''), email) "
                + "WHERE ucid = ?";
        try {
            PreparedStatement pstmtMember = db_conn.prepareStatement(member_query);
            pstmtMember.setString(1, year);
            pstmtMember.setString(2, citizen);
            pstmtMember.setString(3, "active");
            if (until.isBefore(LocalDate.now())){
                pstmtMember.setString(3, "inactive");
            }
            pstmtMember.setInt(4, ucid);
            pstmtMember.executeUpdate();

            PreparedStatement pstmtMembership = db_conn.prepareStatement(membership_query);
            pstmtMembership.setDate(1, Date.valueOf(until));
            pstmtMembership.setString(2, payment);
            pstmtMembership.setString(3, email);
            pstmtMembership.setInt(4, ucid);
            pstmtMembership.executeUpdate();
            return 1;
        }
        catch (SQLException e){
//            throw e;
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /**
     * Perform a search by partial or full member data
     * @param data the ucid/first/last name to search for
     * @return true if query executed, false otherwse
     */

    public boolean searchMembers(String data, ObservableList<String[]> matches){
        String query = "SELECT * FROM members WHERE ucid LIKE ? OR fName LIKE ? OR lName LIKE ? ";
        ResultSet list = null;
        try {

            PreparedStatement pstmt = db_conn.prepareStatement(query);

            pstmt.setString(1, "%" + data + "%");
            pstmt.setString(2, "%" + data + "%");
            pstmt.setString(3, "%" + data + "%");
            list = pstmt.executeQuery();

            int columnCount = list.getMetaData().getColumnCount();
            while (list.next()) {
                String[] row = new String[columnCount]; // Create an array to hold the row data
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = list.getString(i); // Store each column value in the array
                }
                matches.add(row);
            }
            return true;
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public ResultSet[] getMembership(String ucid){
        ResultSet[] result = new ResultSet[2];
        String query1 = "SELECT * FROM memberships WHERE ucid = ?";
        String query2 = "SELECT * FROM members WHERE ucid = ?";
        try{
            PreparedStatement pstmt = db_conn.prepareStatement(query1);
            pstmt.setString(1, ucid);
            result[0] = pstmt.executeQuery();
            pstmt = db_conn.prepareStatement(query2);
            pstmt.setString(1, ucid);
            result[1] = pstmt.executeQuery();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public ResultSet getMembersByNationality() throws SQLException {
        String query = "SELECT citizen, count(*) AS number FROM members \n" +
                        "GROUP BY citizen";
        Statement stmt = db_conn.createStatement();
        return stmt.executeQuery(query);
    }
    public ResultSet getMembersByYear() throws SQLException {
        String query = "SELECT year, count(*) AS number FROM members \n" +
                "GROUP BY year";
        Statement stmt = db_conn.createStatement();
        return stmt.executeQuery(query);
    }
    public int[] getMembersNumbers() throws SQLException {
        String query = "SELECT status, count(*) AS number FROM members \n" +
                        "GROUP BY status";
        Statement stmt = db_conn.createStatement();
        ResultSet res = stmt.executeQuery(query);
        int[] numbers = {0,0};
        while (res.next()){
            if (res.getString("status").equalsIgnoreCase("active")){
                numbers[0] += res.getInt("number");
            }
            else if (res.getString("status").equalsIgnoreCase("inactive")){
                numbers[1] +=  res.getInt("number");
            }
        }
        return numbers;
    }

    /*
    Function to get top 5 most recent members
     */
    public String[][] getRecentFiveMembers() throws SQLException {
        String query = "SELECT ucid, fName, lName FROM members\n" +
                        "WHERE ucid IN (\n" +
                        "SELECT ucid FROM memberships\n" +
                        "ORDER BY since LIMIT 5)";
        Statement stmt = null;
        String[][] result = new String[5][3];
        try {
            stmt = db_conn.createStatement();
            ResultSet query_result = stmt.executeQuery(query);
            int i = 0;
            while(query_result.next()){
                result[i][0] = query_result.getString("ucid");
                result[i][1] = query_result.getString("fName");
                result[i][2] = query_result.getString("lName");
                i++;
            }
        } catch (SQLException e) {
            // do nothing
        }
        return result;
    }

    public Set<String> getActiveUcids() throws SQLException {
        String query = "SELECT ucid FROM members\n" +
                "WHERE status = 'active'";
        Set<String> result = new HashSet<>();
        try{
            Statement stmt = db_conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()){
                result.add(rs.getString("ucid"));
            }
        }
        catch (SQLException e){

        }
        return result;
    }
}
