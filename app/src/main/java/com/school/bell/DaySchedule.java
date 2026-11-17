package com.school.bell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DaySchedule {
    public int weekday;           // ثابت من Calendar (SUNDAY..SATURDAY)
    public boolean enabled = true;
    public int firstLessonStart = 8 * 60 + 15; // 8:15 صباحاً
    public List<Period> periods = new ArrayList<>();

    public static final int[] WEEK_ORDER = {
            Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY,
            Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    };

    public static String nameOf(int calDay) {
        switch (calDay) {
            case Calendar.SATURDAY:  return "السبت";
            case Calendar.SUNDAY:    return "الأحد";
            case Calendar.MONDAY:    return "الاثنين";
            case Calendar.TUESDAY:   return "الثلاثاء";
            case Calendar.WEDNESDAY: return "الأربعاء";
            case Calendar.THURSDAY:  return "الخميس";
            default:                 return "الجمعة";
        }
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        JSONArray arr = new JSONArray();
        for (Period p : periods) arr.put(p.toJson());
        try {
            o.put("weekday", weekday);
            o.put("enabled", enabled);
            o.put("first", firstLessonStart);
            o.put("periods", arr);
        } catch (JSONException ignored) {}
        return o;
    }

    public static DaySchedule fromJson(JSONObject o) {
        DaySchedule d = new DaySchedule();
        d.weekday = o.optInt("weekday");
        d.enabled = o.optBoolean("enabled", true);
        d.firstLessonStart = o.optInt("first", 8 * 60 + 15);
        JSONArray arr = o.optJSONArray("periods");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                d.periods.add(Period.fromJson(arr.optJSONObject(i)));
            }
        }
        return d;
    }

    // جدول افتراضي: 6 حصص × 45 دقيقة + استراحة 5 دقائق بينها
    public static DaySchedule defaultsFor(int calDay) {
        DaySchedule d = new DaySchedule();
        d.weekday = calDay;
        int t = d.firstLessonStart;
        String[] names = {"", "الأول", "الثاني", "الثالث", "الرابع", "الخامس", "السادس"};
        for (int i = 1; i <= 6; i++) {
            d.periods.add(new Period("L" + i + calDay, "الدرس " + names[i], t, 45, false));
            t += 45;
            if (i < 6) {
                d.periods.add(new Period("B" + i + calDay, "استراحة", t, 5, true));
                t += 5;
            }
        }
        return d;
    }

    public static void sortPeriods(DaySchedule d) {
        java.util.Collections.sort(d.periods,
                (a, b) -> Integer.compare(a.startMinutes, b.startMinutes));
    }
}
