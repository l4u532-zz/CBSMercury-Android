package dk.cbs.kl.CBSMercury;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplayTrainings extends AppCompatActivity {

    /** declare UI components **/
    private Button btn_showTrainings, btn_showSignups, btn_showMap;
    private TextView txt_showMap;
    ResultSet qry_allTrainings, qry_allSignups;
    String currentTrainingID, currentTrainingDesc, currentTeamID;
    String str_booking_free = "Free to join", str_booking_full = "No spots free", str_booking_signedup = "Signed up!";
    Integer currentMaxParticipants;
    ArrayList<String> list_starts = new ArrayList<>(), list_trainingID = new ArrayList<>(), list_teamID = new ArrayList<>(), list_duration = new ArrayList<>(), list_maxpartic = new ArrayList<>(), list_currentpartic = new ArrayList<>(), list_status = new ArrayList<>();
    HashMap<String, String> map_allTrainingDescriptions;
    ListView buckysListView;
    Adapter buckysAdapter;

    public void populateUI_signups() throws SQLException {
        clearLists();

        while (qry_allSignups != null) {

            String weekday = new SimpleDateFormat("EE, dd.MM., HH:mm").format(qry_allSignups.getTimestamp("starts"));

            list_starts.add(weekday);
            list_trainingID.add(qry_allSignups.getString("ID"));
            Log.i("DT - currentID", qry_allSignups.getString("ID"));
            list_teamID.add(qry_allSignups.getString("teamID"));
            list_duration.add(qry_allSignups.getString("owDuration"));
            list_maxpartic.add(qry_allSignups.getString("owMaxParticipants"));
            list_currentpartic.add("0");
            list_status.add("Missing data.");

            qry_allSignups.next();
            Log.e("CurrentID", list_trainingID.toString());
        }
    }

    public void populateUI_generalData() throws SQLException {
        while (qry_allTrainings != null) {

            String weekday = new SimpleDateFormat("EE, dd.MM., HH:mm").format(qry_allTrainings.getTimestamp("starts"));

            list_starts.add(weekday);
            list_trainingID.add(qry_allTrainings.getString("ID"));
            list_teamID.add(qry_allTrainings.getString("teamID"));
            list_duration.add(qry_allTrainings.getString("owDuration"));
            list_maxpartic.add(qry_allTrainings.getString("owMaxParticipants"));
            list_currentpartic.add("0");
            list_status.add("Missing data.");

            qry_allTrainings.next();
        }
    }

    public void clearLists() {
        list_starts.clear();
        list_trainingID.clear();
        list_teamID.clear();
        list_duration.clear();
        list_maxpartic.clear();
        list_currentpartic.clear();
        list_status.clear();
    }

    public void notifyUIChange() {
        buckysAdapter = new Adapter(this, list_starts, list_trainingID, list_teamID, list_duration, list_maxpartic, list_currentpartic, list_status); // pass stuff to Adapter :-D
        buckysListView.setAdapter(buckysAdapter);
        buckysAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_display2);

        /** Populate UI & RAM with DBResults on start **/
        new GetAllTrainings().execute();
        new GetTrainingDescriptions().execute(); // fetch HashMap of training descriptions and translate trainingID to user-friendly name
        new GetCurrentParticipants().execute(list_trainingID);

        btn_showTrainings = (Button) findViewById(R.id.btn_showTrainings);
        btn_showSignups = (Button) findViewById(R.id.btn_showParticipants);
        txt_showMap = (TextView) findViewById(R.id.txt_ShowMap);

        buckysListView = (ListView) findViewById(R.id.buckysListView);
        buckysListView.setItemsCanFocus(false);

        // onClickListener
        buckysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  currentTrainingID = list_trainingID.get(position).toString();
                  currentMaxParticipants = Integer.parseInt(list_maxpartic.get(position));
                  if (list_status.get(position).equals(str_booking_free)) {
                      new AddDetails().execute(currentTrainingID);
                      new GetCurrentParticipants().execute();
                  } else {
                      new RemoveDetails().execute(currentTrainingID);
                      new GetCurrentParticipants().execute();
                  }
                  }
              });

        btn_showTrainings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetAllTrainings().execute();
                new GetTrainingDescriptions().execute(list_teamID); // fetch HashMap of training descriptions and translate trainingID to user-friendly name
                new GetCurrentParticipants().execute(list_trainingID);
            }
        });

        btn_showSignups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetSignups().execute();
                new GetTrainingDescriptions().execute(list_teamID); // fetch HashMap of training descriptions and translate trainingID to user-friendly name
                new GetCurrentParticipants().execute(list_trainingID);
            }
        });

        txt_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayTrainings.this, Map1.class);
                startActivity(intent);

            }
        });
    }

    class GetAllTrainings extends AsyncTask<Void, Void, ResultSet> {
            private ProgressDialog pDialog;
            private void showDialog() {
                if(!pDialog.isShowing()){
                    pDialog.show();
                }
            }
            private void hideDialog() {
                if((pDialog).isShowing()){
                    pDialog.hide();
                }
            }
            //DBHelper db = new DBHelper(); // +++ previous global conn

            @Override
            protected void onPreExecute(){
                pDialog = new ProgressDialog(DisplayTrainings.this);
                pDialog.setCancelable(false);
                pDialog.setMessage("Getting Details..." );
                showDialog();
            }

            /** Fetch Data **/
            @Override
            protected ResultSet doInBackground(Void... params) {
                DBHelper db = new DBHelper(); // +++ change this to make it a global conn
                qry_allTrainings = db.getAllTrainings();
                return qry_allTrainings;
            }

            /** Update UI **/
            @Override
            protected void onPostExecute(ResultSet x) {
                hideDialog();
                try {
                    if (qry_allTrainings != null && qry_allTrainings.next()) {
                        populateUI_generalData();
                    } else {
                        ShowMessage("Could not retrieve available trainings.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                notifyUIChange();
            }
        }

    class GetTrainingDescriptions extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        private ProgressDialog pDialog;
        private void showDialog() {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }
        private void hideDialog() {
            if((pDialog).isShowing()){
                pDialog.hide();
            }
        }
        //DBHelper db = new DBHelper(); // +++ previous global conn

        @Override
        protected void onPreExecute(){
            pDialog = new ProgressDialog(DisplayTrainings.this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Getting training descriptions..." );
            showDialog();
        }

        /** Fetch Data **/
        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            DBHelper db = new DBHelper(); // +++ change this to make it a global conn
            map_allTrainingDescriptions = db.getAllTrainingDescriptions();
            //list_teamID.clear(); not needed because loop starts at 0
            for(int i = 0; i < list_teamID.size(); i++) {
                currentTeamID = list_teamID.get(i); // get current teamID from list_teamID
//                Log.e("currentID", currentTeamID);
                currentTrainingDesc = map_allTrainingDescriptions.get(currentTeamID); // translate teamID to description
//                Log.e("description", currentTrainingDesc);
                list_teamID.set(i, currentTrainingDesc);
            }
            Log.e("descArray", list_teamID.toString());
            return list_teamID;
        }

        /** Update UI **/
        @Override
        protected void onPostExecute(ArrayList<String> x) {
            hideDialog();
            notifyUIChange();
        }
    }

    class GetCurrentParticipants extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        private ProgressDialog pDialog;
        private void showDialog() {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }
        private void hideDialog() {
            if((pDialog).isShowing()){
                pDialog.hide();
            }
        }

        @Override
        protected void onPreExecute(){
            pDialog = new ProgressDialog(DisplayTrainings.this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Checking number of participants..." );
            showDialog();
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            //list_currentpartic.clear(); // crashes app
            DBHelper db = new DBHelper();
            // change status of booking
            for(int i = 0; i < list_currentpartic.size(); i++) {
                try {
                    if(db.checkIfBooked(list_trainingID.get(i))) {
                        list_status.add(i, str_booking_signedup);
                    }
                    else if(Integer.parseInt(list_currentpartic.get(i)) >= Integer.parseInt(list_maxpartic.get(i))) {
                        list_status.add(i, str_booking_full);
                    }
                    else {
                        list_status.add(i, str_booking_free);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            list_currentpartic = db.getAllSignups(list_trainingID); // pass all trainingIDs
            return list_currentpartic;
        }

        // hideDialog after exec
        @Override
        protected void onPostExecute(ArrayList<String> x) {
            hideDialog();
            notifyUIChange();
        }
    }

    class AddDetails extends AsyncTask<String, Void, Boolean> {
        private boolean querySuccess;

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute(){
            querySuccess = false;
            pDialog = new ProgressDialog(DisplayTrainings.this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Subscribing to training #" + currentTrainingID);
            showDialog();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            DBHelper db = new DBHelper();
            if (db.getAllSignups(currentTrainingID) < currentMaxParticipants) {
                querySuccess = db.addDetails(AppConfig.DB_userID, currentTrainingID);
            } else {
                //ShowMessage("Sorry - this training is already fully booked.");
            }
            return querySuccess;
        }

        @Override
        protected void onPostExecute(Boolean x) {
            hideDialog();
            if(querySuccess == false) {
                ShowMessage("You are now subscribed to the training.");
            } else {
                // do nothing
            }
        }

        /** show/hide dialog **/
        private void showDialog() {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }

        private void hideDialog() {
            if((pDialog).isShowing()){
                pDialog.hide();
            }
        }

    }

    class RemoveDetails extends AsyncTask<String, Void, Boolean> {
        private boolean querySuccess;

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute(){
            querySuccess = false;
            pDialog = new ProgressDialog(DisplayTrainings.this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Unsubscribing from training #" + currentTrainingID);
            showDialog();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            DBHelper db = new DBHelper();
            querySuccess = db.removeDetails(AppConfig.DB_userID, currentTrainingID);
            return querySuccess;
        }

        @Override
        protected void onPostExecute(Boolean x) {
            hideDialog();
            if(querySuccess == false) {
                ShowMessage("You are now UNSUBSCRIBED from the training.");
            } else {
                ShowMessage("Could not unsubscribe from training.");
            }
        }

        /** show/hide dialog **/
        private void showDialog() {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }

        private void hideDialog() {
            if((pDialog).isShowing()){
                pDialog.hide();
            }
        }

    }

    class GetSignups extends AsyncTask<Void, Void, ResultSet> {
        private ProgressDialog pDialog;
        private void showDialog() {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }
        private void hideDialog() {
            if((pDialog).isShowing()){
                pDialog.hide();
            }
        }
        //DBHelper db = new DBHelper(); // +++ previous global conn

        @Override
        protected void onPreExecute(){
            pDialog = new ProgressDialog(DisplayTrainings.this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Getting Details..." );
            showDialog();
        }

        /** Fetch Data **/
        @Override
        protected ResultSet doInBackground(Void... params) {
            DBHelper db = new DBHelper(); // +++ change this to make it a global conn
            try {
                qry_allSignups = db.getSpecificTrainings(db.getSubscriptionIDs());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return qry_allSignups;
        }

        /** Update UI **/
        @Override
        protected void onPostExecute(ResultSet x) {
            hideDialog();
            try {
                if (qry_allSignups != null && qry_allSignups.next()) {
                    populateUI_signups();
                    notifyUIChange();
                } else {
                    ShowMessage("You have no sign-ups.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }


    // Generic error msg handler
    private void ShowMessage(String msg) {
        Toast.makeText(DisplayTrainings.this, msg, Toast.LENGTH_SHORT).show();
    }
}
