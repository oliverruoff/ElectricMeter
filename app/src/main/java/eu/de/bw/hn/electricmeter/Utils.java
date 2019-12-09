package eu.de.bw.hn.electricmeter;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

class Utils {

    private static String DATETIMEPATTERN = "yyyy-MM-dd'T'HH:mm";
    private static String SHAREDPREFERENCESTIMERECORDSMAP = "savedMapName";
    private static String SPMAPKEY = "hashKey";

    static void saveMap(Context context, TreeMap<Calendar, Float> timeRecordsMap) {
        Gson gson = new Gson();
        TreeMap<String, String> stringMap = parseCalendarFloatMapToStringString(timeRecordsMap);
        String hashMapString = gson.toJson(stringMap);
        //save in shared prefs
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFERENCESTIMERECORDSMAP, MODE_PRIVATE);
        prefs.edit().putString(SPMAPKEY, hashMapString).apply();
    }

    static TreeMap<Calendar, Float> loadMap(Context context) {
        Gson gson = new Gson();
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFERENCESTIMERECORDSMAP, MODE_PRIVATE);
        //get from shared prefs
        TreeMap<Calendar, Float> timeRecordsMap = new TreeMap<>();
        try {
            String storedHashMapString = prefs.getString(SPMAPKEY, "Failed to load map!");
            java.lang.reflect.Type type = new TypeToken<TreeMap<String, String>>() {
            }.getType();
            TreeMap<String, String> stringMap = gson.fromJson(storedHashMapString, type);
            timeRecordsMap = parseStringStringMapToCalendarFloat(context, stringMap);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return timeRecordsMap;
    }

    static String getStringFromCalendar(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat(DATETIMEPATTERN);
        return format.format(date.getTime());
    }

    static Calendar getCalendarFromString(Context context, String date) {
        Calendar cal = Calendar.getInstance();
        Locale currentLocale = context.getResources().getConfiguration().locale;
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEPATTERN, currentLocale);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    private static TreeMap<String, String> parseCalendarFloatMapToStringString(TreeMap<Calendar, Float> calFloatMap) {
        TreeMap<String, String> stringMap = new TreeMap<>();
        for (Map.Entry<Calendar, Float> entry : calFloatMap.entrySet()) {
            Calendar key = entry.getKey();
            Float value = entry.getValue();
            stringMap.put(getStringFromCalendar(key), value.toString());
        }
        return stringMap;
    }

    private static TreeMap<Calendar, Float> parseStringStringMapToCalendarFloat(Context context, TreeMap<String, String> strStrMap) {
        TreeMap<Calendar, Float> calFloatMap = new TreeMap<>();
        for (Map.Entry<String, String> entry : strStrMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            calFloatMap.put(getCalendarFromString(context, key), Float.parseFloat(value));
        }
        return calFloatMap;
    }

}
