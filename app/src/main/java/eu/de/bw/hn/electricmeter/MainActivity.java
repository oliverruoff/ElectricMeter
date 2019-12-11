package eu.de.bw.hn.electricmeter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import static android.app.AlertDialog.Builder;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    Calendar date;
    TreeMap<Calendar, Float> timeRecordsMap = new TreeMap<>();

    Context MainActivityContext;
    TextView averageCurrentTextView;
    TextView averageOverallTextView;
    TextView averageYearTextView;
    TextView totalLastTextView;
    TextView lastYearTextView;


    float chosenValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivityContext = this;

        initViews(this);

        loadMap();

        fillTextViews();
        showLineGraph();
        showBarChart();

        // Toast.makeText(this, "Size: " + timeRecordsMap.size(), Toast.LENGTH_LONG).show();

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

    private void initViews(Context context) {
        averageCurrentTextView = (TextView) findViewById(R.id.averageCurrent);
        averageOverallTextView = (TextView) findViewById(R.id.averageOverall);
        averageYearTextView = (TextView) findViewById(R.id.averageYear);
        totalLastTextView = (TextView) findViewById(R.id.totalLast);
        lastYearTextView = (TextView) findViewById(R.id.lastYear);
    }

    private void fillTextViews() {
        if (timeRecordsMap.size() < 2) {
            return;
        }
        // Average Overall
        Calendar startDate = (Calendar) timeRecordsMap.keySet().toArray()[0];
        Calendar endDate = (Calendar) timeRecordsMap.keySet().toArray()[timeRecordsMap.size() - 1];
        float startNo = timeRecordsMap.get(timeRecordsMap.keySet().toArray()[0]);
        float endNo = timeRecordsMap.get(timeRecordsMap.keySet().toArray()[timeRecordsMap.size() - 1]);
        String averageOverallStr = String.format("%.01f", getAverage(startDate, endDate, startNo, endNo));
        averageOverallTextView.setText(averageOverallStr);

        // Average Current
        Calendar currentStartDate = (Calendar) timeRecordsMap.keySet().toArray()[timeRecordsMap.size() - 2];
        float currentStartNo = timeRecordsMap.get(timeRecordsMap.keySet().toArray()[timeRecordsMap.size() - 2]);
        String averageCurrentStr = String.format("%.01f", getAverage(currentStartDate, endDate, currentStartNo, endNo));
        averageCurrentTextView.setText(averageCurrentStr);

        // Average Year
        float averageYear = getAverage(startDate, endDate, startNo, endNo) * 365;
        String averageYearStr = String.format("%.01f", getAverage(startDate, endDate, startNo, endNo) * 365);
        averageYearTextView.setText(averageYearStr);

        // Total Last
        String totalLastStr = String.format("%.01f", (endNo - currentStartNo));
        totalLastTextView.setText(totalLastStr);

        // Average Last Year
        String averageLastYear = String.format("%.01f", getAverage(currentStartDate, endDate, currentStartNo, endNo) * 365);
        lastYearTextView.setText(averageLastYear);
    }

    private float getAverage(Calendar startDate, Calendar endDate, float startNo, float endNo) {
        long timeDeltaMillis = endDate.getTime().getTime() - startDate.getTime().getTime();
        float measurementDelta = endNo - startNo;
        float milliAverage = measurementDelta / timeDeltaMillis;
        return milliAverage * 1000 * 60 * 60 * 24;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadMap();
        showLineGraph();
        showBarChart();
        fillTextViews();
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
                        alert.setTitle("Enter measurement");
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
                                String dateTime = MyUtils.getStringFromCalendar(date);
                                Toast.makeText(mainContext, "Chosen time: " + dateTime + " Chosen value: " + chosenValue, Toast.LENGTH_LONG).show();
                                timeRecordsMap.put(date, chosenValue);
                                saveMap();
                                showLineGraph();
                                showBarChart();
                                fillTextViews();
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
        MyUtils.saveMap(MainActivityContext, timeRecordsMap);
    }

    private void loadMap() {
        timeRecordsMap = MyUtils.loadMap(MainActivityContext);
    }

    private void showBarChart() {
        if (timeRecordsMap.size() < 1) {
            return;
        }
        BarChart bChart = findViewById(R.id.barChart);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < timeRecordsMap.size() - 1; i++) {
            Calendar startDate = (Calendar) timeRecordsMap.keySet().toArray()[i];
            Calendar endDate = (Calendar) timeRecordsMap.keySet().toArray()[i + 1];
            float startNo = timeRecordsMap.get(timeRecordsMap.keySet().toArray()[i]);
            float endNo = timeRecordsMap.get(timeRecordsMap.keySet().toArray()[i + 1]);
            float average = getAverage(startDate, endDate, startNo, endNo);
            float millisEnd = ((Calendar) timeRecordsMap.keySet().toArray()[i + 1]).getTimeInMillis();
            entries.add(new BarEntry(millisEnd, average));
        }

        bChart.getDescription().setTextColor(getResources().getColor(R.color.colorAccent));
        bChart.getAxisLeft().setTextColor(getResources().getColor((R.color.colorAccent)));
        bChart.getDescription().setText(""); // Appears in the bottom right corner of the graph
        bChart.getDescription().setTextColor(getResources().getColor(R.color.colorAccent));
        bChart.getLegend().setEnabled(false);
        bChart.getAxisRight().disableGridDashedLine();
        bChart.getAxisLeft().setDrawGridLines(false);
        bChart.getAxisRight().setDrawGridLines(false);
        bChart.getXAxis().setDrawGridLines(false);
        bChart.getAxisRight().setDrawLabels(false);
        bChart.getXAxis().setLabelRotationAngle(30);
        bChart.getXAxis().setTextColor(getResources().getColor(R.color.colorAccent));
        bChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        // bChart.getXAxis().setDrawLabels(false);

        bChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis((long) value);
                String calStr = MyUtils.getStringFromCalendar(cal);
                return calStr;
            }
        });

        BarDataSet set = new BarDataSet(entries, "");
        set.setValueTextColor(getResources().getColor(R.color.colorAccent));
        set.setColor(getResources().getColor(R.color.colorPrimary));
        set.setBarBorderColor(getResources().getColor(R.color.colorAccent));
        set.setBarBorderWidth(1f);
        BarData data = new BarData(set);
        data.setBarWidth(86400000f); // Millis of one day
        bChart.setData(data);
        bChart.setPinchZoom(true);
        set.setHighlightEnabled(false);
        bChart.setFitBars(true); // make the x-axis fit exactly all bars
        bChart.invalidate(); // refresh
    }

    private void showLineGraph() {
        if (timeRecordsMap.size() < 1) {
            return;
        }
        LineChart mChart = findViewById(R.id.chart);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setLabelRotationAngle(30);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.colorAccent));

        mChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorAccent));

        mChart.getAxisRight().setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getLegend().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        mChart.getDescription().setText(""); // Appears in the bottom right corner of the graph
        mChart.getDescription().setTextColor(getResources().getColor(R.color.colorAccent));


        ArrayList<Entry> values = new ArrayList<>();
        for (Calendar cal : timeRecordsMap.keySet()) {
            long x = cal.getTimeInMillis();
            values.add(new Entry(x, timeRecordsMap.get(cal)));
        }

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis((long) value);
                String calStr = MyUtils.getStringFromCalendar(cal);
                return calStr;
            }
        });

        LineDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();

            mChart.getData().setHighlightEnabled(false);
        } else {
            set1 = new LineDataSet(values, "");
            //set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.setColor(getResources().getColor(R.color.colorAccent));
            set1.setCircleColor(getResources().getColor(R.color.colorAccent));
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setValueTextColor(getResources().getColor(R.color.colorAccent));
            set1.setFormLineWidth(1f);
            set1.setFormSize(15.f);
            set1.setDrawFilled(true);
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.color.colorPrimary);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.WHITE);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            mChart.setData(data);
        }

    }

}
