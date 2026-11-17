package com.school.bell;

import org.json.JSONException;
import org.json.JSONObject;

public class Period {
    public String id, title;
    public int startMinutes; // من منتصف الليل
    public int duration;     // بالدقائق
    public boolean isBreak;

    public Period(String id, String title, int startMinutes, int duration, boolean isBreak) {
        this.id = id;
        this.title = title;
        this.startMinutes = startMinutes;
        this.duration = duration;
        this.isBreak = isBreak;
    }

    public String timeRange() {
        return fmt(startMinutes) + " - " + fmt(startMinutes + duration);
    }

    public static String fmt(int m) {
        int h24 = (m / 60) % 24;
        int min = m % 60;
        String suffix = h24 >= 12 ? "م" : "ص";
        int h = h24 % 12 == 0 ? 12 : h24 % 12;
        return h + ":" + (min < 10 ? "0" + min : String.valueOf(min)) + " " + suffix;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            o.put("start", startMinutes);
            o.put("duration", duration);
            o.put("isBreak", isBreak);
        } catch (JSONException ignored) {}
        return o;
    }

    public static Period fromJson(JSONObject o) {
        return new Period(
                o.optString("id"),
                o.optString("title"),
                o.optInt("start"),
                o.optInt("duration"),
                o.optBoolean("isBreak"));
    }
}
