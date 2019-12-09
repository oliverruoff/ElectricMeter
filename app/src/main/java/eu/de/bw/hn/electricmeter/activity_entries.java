package eu.de.bw.hn.electricmeter;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class activity_entries extends AppCompatActivity {

    ListView listView;
    TreeMap<Calendar, Float> timeRecordsMap;

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
        displayItems(this);
    }
}
