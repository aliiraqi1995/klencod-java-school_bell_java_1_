package com.school.bell;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        showNotification(context,
                title != null ? title : "جرس المدرسة",
                text != null ? text : "");
    }

    public static void showNotification(Context ctx, String title, String text) {
        Uri sound = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.bell);
        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open,
                PendingIntent.FLAG_IMMUTABLE);

        Notification n = new NotificationCompat.Builder(ctx, App.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setSound(sound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        try {
            NotificationManagerCompat.from(ctx).notify((int) System.currentTimeMillis(), n);
        } catch (SecurityException ignored) {}
    }
}
