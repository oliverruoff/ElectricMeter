package eu.de.bw.hn.electricmeter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class activity_entries extends AppCompatActivity {

    ListView listView;
    TreeMap<Calendar, Float> timeRecordsMap;
    Context entriesContext;

    private ArrayList<String> stringListFromCalendarFloatMap(TreeMap<Calendar, Float> calFloatMap) {
        ArrayList<String> stringList = new ArrayList<>();

        for (Map.Entry<Calendar, Float> entry : calFloatMap.entrySet()) {
            Calendar key = entry.getKey();
            float value = entry.getValue();
            stringList.add(Utils.getStringFromCalendar(key) + ": " + value);
        }

        return stringList;

    }

    private void displayItems(Context mainContext) {

        ArrayList<String> stringList = stringListFromCalendarFloatMap(timeRecordsMap);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                stringList );

        listView.setAdapter(arrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries);

        timeRecordsMap = Utils.loadMap(this);
        listView = (ListView) findViewById(R.id.listView);
        entriesContext = this;
        displayItems(this);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(entriesContext);
                alert.setTitle("Sure remove?");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ArrayList<String> stringList = stringListFromCalendarFloatMap(timeRecordsMap);
                        String desiredItem = stringList.get(pos);
                        Calendar removeKey = Utils.getCalendarFromString(entriesContext, desiredItem);
                        timeRecordsMap.remove(removeKey);
                        // Toast.makeText(entriesContext, "Removed key: " + removeKey, Toast.LENGTH_SHORT).show();
                        Log.v("long clicked","pos: " + pos);
                        Utils.saveMap(entriesContext, timeRecordsMap);
                        displayItems(entriesContext);
                        return;
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                return;
                            }
                        });
                alert.show();
                return true;
            }
        });
    }
}
