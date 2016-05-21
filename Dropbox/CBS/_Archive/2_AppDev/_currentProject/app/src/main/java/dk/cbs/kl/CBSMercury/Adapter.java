package dk.cbs.kl.CBSMercury;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Adapter extends ArrayAdapter<String> {

    ArrayList<String> list_starts = new ArrayList<String>();
    ArrayList<String> list_trainingID = new ArrayList<String>();
    ArrayList<String> list_teamID = new ArrayList<String>();
    ArrayList<String> list_duration = new ArrayList<String>();
    ArrayList<String> list_maxpartic = new ArrayList<String>();
    ArrayList<String> list_currentpartic = new ArrayList<String>();
    ArrayList<String> list_status = new ArrayList<String>();


    public Adapter(Context context, ArrayList list_starts, ArrayList list_trainingID, ArrayList list_teamID, ArrayList list_duration, ArrayList list_maxpartic, ArrayList<String> list_currentpartic, ArrayList<String> list_status) {
        /** for each individual item, use CUSTOM_ROW with 'String foods' **/
        super(context, R.layout.custom_row, list_starts);
        this.list_starts = list_starts;
        this.list_trainingID = list_trainingID;
        this.list_teamID = list_teamID;
        this.list_duration = list_duration;
        this.list_maxpartic = list_maxpartic;
        this.list_currentpartic = list_currentpartic;
        this.list_status = list_status;
    }

    /** this is how the String should be laid out **/
    @Override
    // GetView maps data to individual row
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater buckysInflater = LayoutInflater.from(getContext());
        /** this defines one custom row **/
        View customView = buckysInflater.inflate(R.layout.custom_row, parent, false);

        /** make references to String, LargeText & Picture **/
        TextView txt_starts = (TextView) customView.findViewById(R.id.txt_starts);
        TextView txt_trainingID = (TextView) customView.findViewById(R.id.txt_trainingID);
        TextView txt_teamID = (TextView) customView.findViewById(R.id.txt_teamID);
        TextView txt_duration = (TextView) customView.findViewById(R.id.txt_duration);
        TextView txt_maxpartic = (TextView) customView.findViewById(R.id.txt_maxpartic);
        TextView txt_currentpartic = (TextView) customView.findViewById(R.id.txt_currentpartic);
        TextView txt_status = (TextView) customView.findViewById(R.id.txt_status);
        Button btn_subscribe = (Button) customView.findViewById(R.id.btn_subscribe);

        /** change text dynamically based on String passed **/
        txt_starts.setText(list_starts.get(position));
        txt_trainingID.setText(list_trainingID.get(position));
        txt_teamID.setText(list_teamID.get(position));
        txt_duration.setText(list_duration.get(position));
        txt_maxpartic.setText(list_maxpartic.get(position));
        txt_currentpartic.setText(list_currentpartic.get(position));
        txt_status.setText(list_status.get(position));

        /** return row **/
        return customView;
    }

    private void ShowMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }
}
