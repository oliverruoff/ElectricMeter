package eu.de.bw.hn.electricmeter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static android.app.AlertDialog.Builder;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    Calendar date;
    TreeMap<Calendar, Float> timeRecordsMap = new TreeMap<>();

    Context MainActivityContext;


    float chosenValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivityContext = this;

        loadMap();

        showLineGraph();

        Toast.makeText(this, "Size: " + timeRecordsMap.size(), Toast.LENGTH_LONG).show();

        FloatingActionButton fab = findViewById(R.id.fab);
        final Context mainContext = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddClicked(mainContext);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadMap();
        showLineGraph();
    }

    private void onAddClicked(final Context mainContext) {

        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(mainContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(mainContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        Builder alert = new Builder(mainContext);
                        alert.setTitle("Enter current value");
                        // Set an EditText view to get user input
                        final EditText input = new EditText(mainContext);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        alert.setView(input);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (input.getText().toString().matches("")) {
                                    Toast.makeText(mainContext, "Please enter a value!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                chosenValue = Float.parseFloat(input.getText().toString());
                                Log.d("", "Chosen value : " + chosenValue);
                                String dateTime = Utils.getStringFromCalendar(date);
                                Toast.makeText(mainContext, "Chosen time: " + dateTime + " Chosen value: " + chosenValue, Toast.LENGTH_LONG).show();
                                timeRecordsMap.put(date, chosenValue);
                                saveMap();
                                showLineGraph();
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
                        Log.v(TAG, "The chosen date time " + date.getTime());
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, activity_entries.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void saveMap() {
        Utils.saveMap(MainActivityContext, timeRecordsMap);
    }

    private void loadMap() {
        timeRecordsMap = Utils.loadMap(MainActivityContext);
    }

    private void showLineGraph() {
        LineChartView lineChartView = findViewById(R.id.chart);
        String[] xAxisData = new String[timeRecordsMap.keySet().size()];

        ArrayList<String> strList = new ArrayList<>();
        for (Calendar cal: timeRecordsMap.keySet()) {
            strList.add(Utils.getStringFromCalendar(cal));
        }
        strList.toArray(xAxisData);


        Float[] yAxisData = new Float[timeRecordsMap.values().size()];
        timeRecordsMap.values().toArray(yAxisData);

        List yAxisValues = new ArrayList();
        List xAxisValues = new ArrayList();

        Line line = new Line(yAxisValues).setColor(Color.WHITE);

        for(int i = 0; i < xAxisData.length; i++){
            xAxisValues.add(i, new AxisValue(i).setLabel(xAxisData[i]));
        }

        for (int i = 0; i < yAxisData.length; i++){
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axis = new Axis();
        axis.setValues(xAxisValues);
        axis.setTextSize(12);
        axis.setTextColor(Color.WHITE);
        axis.setHasTiltedLabels(true);
        data.setAxisXBottom(axis);

        Axis yAxis = new Axis();
        yAxis.setName("kWh");
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(12);
        data.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = Collections.max(timeRecordsMap.values());
        lineChartView.setZoomEnabled(true);
        lineChartView.setScrollEnabled(true);
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);

    }

}
