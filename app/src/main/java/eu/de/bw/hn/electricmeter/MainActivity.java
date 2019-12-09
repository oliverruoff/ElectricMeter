package eu.de.bw.hn.electricmeter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.AlertDialog.Builder;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    Calendar date;
    HashMap<Calendar, Float> timeRecordsMap = new HashMap<>();

    String DATETIMEPATTERN = "yyyy-MM-dd'T'HH:mm";
    String SHAREDPREFERENCESTIMERECORDSMAP = "savedMapName";
    String SPMAPKEY = "hashKey";

    float chosenValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadMap();

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

    private String getStringFromCalendar(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat(DATETIMEPATTERN);
        return format.format(date.getTime());
    }

    private Calendar getCalendarFromString(String date) {
        Calendar cal = Calendar.getInstance();
        Locale currentLocale = getResources().getConfiguration().locale;
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEPATTERN, currentLocale);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
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
                        alert.setTitle("Title");
                        alert.setMessage("Message :");
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
                                String dateTime = getStringFromCalendar(date);
                                Toast.makeText(mainContext, "Chosen time: " + dateTime + " Chosen value: " + chosenValue, Toast.LENGTH_LONG).show();
                                timeRecordsMap.put(date, chosenValue);
                                saveMap();
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
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> parseCalendarFloatMapToStringString(HashMap<Calendar, Float> calFloatMap) {
        HashMap<String, String> stringMap = new HashMap<>();
        for (Map.Entry<Calendar, Float> entry : calFloatMap.entrySet()) {
            Calendar key = entry.getKey();
            Float value = entry.getValue();
            stringMap.put(getStringFromCalendar(key), value.toString());
        }
        return stringMap;
    }

    private HashMap<Calendar, Float> parseStringStringMapToCalendarFloat(HashMap<String, String> strStrMap) {
        HashMap<Calendar, Float> calFloatMap = new HashMap<>();
        for (Map.Entry<String, String> entry : strStrMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            calFloatMap.put(getCalendarFromString(key), Float.parseFloat(value));
        }
        return calFloatMap;
    }

    private void saveMap() {
        Gson gson = new Gson();
        HashMap<String, String> stringMap = parseCalendarFloatMapToStringString(timeRecordsMap);
        String hashMapString = gson.toJson(stringMap);
        Log.d(TAG, hashMapString);
        //save in shared prefs
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFERENCESTIMERECORDSMAP, MODE_PRIVATE);
        prefs.edit().putString(SPMAPKEY, hashMapString).apply();
    }

    private void loadMap() {
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFERENCESTIMERECORDSMAP, MODE_PRIVATE);
        //get from shared prefs
        try {
            String storedHashMapString = prefs.getString(SPMAPKEY, "Failed to load map!");
            java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap <String, String> stringMap = gson.fromJson(storedHashMapString, type);
            timeRecordsMap = parseStringStringMapToCalendarFloat(stringMap);
            Log.d(TAG, "map: " + timeRecordsMap);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

}
