package com.school.bell;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Map;

public class BellScheduler {

    // جدولة الجرس للأيام السبعة القادمة
    public static void scheduleAll(Context ctx, Map<Integer, DaySchedule> map) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        cancelAll(ctx);

        Calendar now = Calendar.getInstance();
        for (int offset = 0; offset < 7; offset++) {
            Calendar day = (Calendar) now.clone();
            day.add(Calendar.DAY_OF_YEAR, offset);
            DaySchedule s = map.get(day.get(Calendar.DAY_OF_WEEK));
            if (s == null || !s.enabled) continue;

            for (int i = 0; i < s.periods.size(); i++) {
                Period p = s.periods.get(i);
                Calendar when = (Calendar) day.clone();
                when.set(Calendar.HOUR_OF_DAY, p.startMinutes / 60);
                when.set(Calendar.MINUTE, p.startMinutes % 60);
                when.set(Calendar.SECOND, 0);
                if (when.getTimeInMillis() <= now.getTimeInMillis()) continue;

                Intent it = new Intent(ctx, AlarmReceiver.class);
                it.putExtra("title", p.title);
                it.putExtra("text", Period.fmt(p.startMinutes));
                int req = day.get(Calendar.DAY_OF_WEEK) * 1000 + i;
                PendingIntent pi = PendingIntent.getBroadcast(ctx, req, it,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            when.getTimeInMillis(), pi);
                } catch (SecurityException ignored) {}
            }
        }
    }

    public static void cancelAll(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int d = 1; d <= 7; d++) {
            for (int i = 0; i < 200; i++) {
                Intent it = new Intent(ctx, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(ctx, d * 1000 + i, it,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
                if (pi != null) {
                    am.cancel(pi);
                    pi.cancel();
                }
            }
        }
    }

    public static void testBellNow(Context ctx) {
        AlarmReceiver.showNotification(ctx, "جرس المدرسة", "تجربة الجرس");
    }
}
