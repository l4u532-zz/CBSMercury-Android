package dk.cbs.kl.CBSMercury;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DBHelper {

    // declare logging TAG & conn login credentials placeholder
    Connection conn;
    private String TAG = DBHelper.class.getSimpleName();
    ResultSet queryResult = null;
    Integer numberParticipants;

    // get current time
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date current_time = new Date();
    String timestamp = dateFormat.format(current_time);

    // ArrayLists for fetching signups
    ArrayList<String> list_subscriptionIDs = new ArrayList<>();
    ArrayList<String> list_currentpartic = new ArrayList<>();

    // DB connector
    public DBHelper() {
        try {
            Class.forName(AppConfig.DRIVER);
            // provide login credentials to DriverManager
            conn = DriverManager.getConnection(AppConfig.connectionString, AppConfig.db_user, AppConfig.db_pass);
            // createTable() would come here
        } catch (ClassNotFoundException c) {
            Log.e(TAG, c.getMessage());
            c.printStackTrace();
        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
            s.printStackTrace();
        }
    }

    /**
     * INSERT BOOKING
     **/
    public boolean addDetails(String DB_userID, String trainingID) {
        boolean result = false;
        try {
            /** insert values into table **/
            PreparedStatement st = conn.prepareStatement("INSERT INTO " + AppConfig.TABLE_subscriptions + "(userID, trainingID)" + "VALUES(?,?)");
            // pass values to VALUES(?,?)
            st.setString(1, DB_userID);
            st.setString(2, trainingID);
            // return TRUE if query successful **/
            result = st.execute();
            /** always close conn if ≠ using DB **/
            st.close();
        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
            s.printStackTrace();
        }
        return result; // true if st.execute() was successful
    }

    public boolean removeDetails(String DB_userID, String trainingID) {
        boolean result = true;
        try {
            /** insert values into table **/
            PreparedStatement st = conn.prepareStatement("DELETE FROM `xfit_subscriptions` WHERE `userID` = " + DB_userID + " AND `trainingID` = " + trainingID);
            // return TRUE if query successful **/
            if (st.execute() == false){
                result = false;
            }
            /** always close conn if ≠ using DB **/
            st.close();
        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
            s.printStackTrace();
        }
        return result; // true if st.execute() was successful
    }

    /**
     * GET WHOLE TABLE
     **/
    public ResultSet getAllTrainings() {

        // SQL query
        String query = "SELECT * FROM " + AppConfig.TABLE_trainings + " WHERE `starts` > '" + timestamp + "' ORDER BY `starts` ASC";

        ResultSet queryResult = null;
        try {
            Statement st = conn.createStatement(); // create empty query
            queryResult = st.executeQuery(query); // fire query

        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }

        return queryResult; // returns result of SQL query
    }

    /**
     * get all SUBSCRIPTIONS for one specific user
     **/
    public ArrayList<String> getSubscriptionIDs() throws SQLException {

        String query = "SELECT * FROM `xfit_subscriptions` WHERE `userID` = 999"; // ??? change userID to argument

        try {
            Statement st = conn.createStatement(); // create empty query
            queryResult = st.executeQuery(query); // fire above query

        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }

        // put fetched trainingIDs into an Array
        while (queryResult != null && queryResult.next()) {
            list_subscriptionIDs.add(queryResult.getString("trainingID"));
            queryResult.next();
        }
        // returns list of IDs
        return list_subscriptionIDs;
    }

    public boolean checkIfBooked(String currentTrainingID) throws SQLException {
        String query = "SELECT * FROM `xfit_subscriptions` WHERE `trainingID` = '" + currentTrainingID + "'";

        try {
            Statement st = conn.createStatement(); // create empty query
            queryResult = st.executeQuery(query); // fire above query

        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }

        boolean booked = false;

        /** !!! NEVER RETURNS TRUE **/
        while (queryResult != null && queryResult.next()) {
            if(queryResult.getString("userID").equals(AppConfig.DB_userID)) {
                booked = true;
            }
            queryResult.next();
        }
        return booked;
    }


    /**
     * get TRAININGDETAILS for a specific ArrayList of trainingIDs
     **/
    public ResultSet getSpecificTrainings(ArrayList<String> list_subscriptionIDs) {
        // convert trainingIDs into SQL syntax
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i <list_subscriptionIDs.size(); i++) {
            sb.append("`ID` = '");
            sb.append(list_subscriptionIDs.get(i) + "'");
            if (i < list_subscriptionIDs.size() - 1) { // only append "OR" condition if the current id is NOT the last item of the list
                sb.append(" OR ");
            }
        }

        String convertedTrainingIDs = sb.toString();

        // get ResultSet from passed trainingIDs
        String query_2 = "SELECT * FROM `xfit_trainings` WHERE " + convertedTrainingIDs;
        Log.e("DBH - qryTrainingIDs", query_2);

        try {
            Statement st = conn.createStatement();
            queryResult = st.executeQuery(query_2);

        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }
        return queryResult;
    }

    /**
     * get CURRENTPARTICIPANTS for a specific ArrayList of trainingIDs
     **/
    public ArrayList getAllSignups(ArrayList<String> list_trainingID) {
        Integer number;

        for (String id : list_trainingID) {
            String query_2 = "SELECT COUNT(*) FROM `xfit_subscriptions` WHERE `trainingID` = '" + id + "'";
            try {
                Statement st = conn.createStatement();
                queryResult = st.executeQuery(query_2);

                if (queryResult != null && queryResult.next()) {
                    number = queryResult.getInt("COUNT(*)");
                    list_currentpartic.add(number.toString());// add COUNT
                } else {
                    Log.i("DBH - #partic", "query failed");
                }

            } catch (SQLException s) {
                Log.e(TAG, s.getMessage());
            }
        }
        //getAllTrainingDescriptions();
        return list_currentpartic;
    }

    /**
     * get CURRENTPARTICIPANTS
     **/
    public Integer getAllSignups(String currentTrainingID) {

        numberParticipants = 888; // initialise uncommon number for debugging

        String query = "SELECT COUNT(*) FROM " + AppConfig.TABLE_subscriptions + " WHERE `trainingID` = '" + currentTrainingID + "'";

        //ResultSet queryResult = null;

        try {
            Statement st = conn.createStatement();
            queryResult = st.executeQuery(query);
            if (queryResult != null && queryResult.next()) {
                numberParticipants = queryResult.getInt("COUNT(*)");
                Log.e(TAG, numberParticipants.toString());
            }
        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }
        Log.e(TAG, numberParticipants.toString());
        return numberParticipants;
    }

    /**
     * get TRAINING DESCRIPTION for a specific ArrayList of trainingIDs
     **/
    public HashMap<String, String> getAllTrainingDescriptions() {
        HashMap<String, String> map_trainingDescriptions = new HashMap<String, String>();
        String query = "SELECT `ID`, `name` FROM `xfit_teams`";

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                map_trainingDescriptions.put(rs.getString("ID"), rs.getString("name"));
            }
        } catch (SQLException s) {
            Log.e(TAG, s.getMessage());
        }
        // display elements
        Set set = map_trainingDescriptions.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
        }
        //Log.e(TAG, String.valueOf(Arrays.asList(map_trainingDescriptions)));
        return map_trainingDescriptions;
    }
}