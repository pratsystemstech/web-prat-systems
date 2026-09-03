package com.pratsystems.asistentepersonal;

import android.app.*;
import android.content.*;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i){
        String text=i.getStringExtra("what");
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        String ch="recordatorios";
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(ch,"Recordatorios",NotificationManager.IMPORTANCE_HIGH));
        Intent open=new Intent(c,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,ch):new Notification.Builder(c);
        b.setContentTitle("Recordatorio").setContentText(text).setSmallIcon(android.R.drawable.ic_dialog_info).setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_HIGH);
        nm.notify((int)(System.currentTimeMillis()%Integer.MAX_VALUE),b.build());
    }
}
