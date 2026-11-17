package com.school.bell;

import android.content.Context;

import java.util.Map;

// يعيد جدولة الجرس بعد إعادة تشغيل الجهاز
public class MapHolder {
    public static void reschedule(Context ctx) {
        Map<Integer, DaySchedule> map = ScheduleStorage.load(ctx);
        BellScheduler.scheduleAll(ctx, map);
    }
}
