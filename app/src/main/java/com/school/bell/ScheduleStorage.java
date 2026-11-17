package com.school.bell;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class ScheduleStorage {
    private static final String KEY = "schedules_v1";

    public static Map<Integer, DaySchedule> load(Context ctx) {
        Map<Integer, DaySchedule> map = new HashMap<>();
        for (int d : DaySchedule.WEEK_ORDER) {
            map.put(d, DaySchedule.defaultsFor(d));
        }
        SharedPreferences prefs = ctx.getSharedPreferences("bell", Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY, null);
        if (raw != null) {
            try {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    DaySchedule s = DaySchedule.fromJson(arr.getJSONObject(i));
                    map.put(s.weekday, s);
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    public static void save(Context ctx, Map<Integer, DaySchedule> map) {
        JSONArray arr = new JSONArray();
        for (DaySchedule s : map.values()) arr.put(s.toJson());
        ctx.getSharedPreferences("bell", Context.MODE_PRIVATE)
                .edit().putString(KEY, arr.toString()).apply();
    }
}
