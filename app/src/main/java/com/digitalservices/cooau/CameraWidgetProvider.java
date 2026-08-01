package com.digitalservices.cooau;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class CameraWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_LAUNCH = "com.digitalservices.cooau.ACTION_WIDGET_LAUNCH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_camera);

            Intent intent = new Intent(context, CameraWidgetProvider.class);
            intent.setAction(ACTION_WIDGET_LAUNCH);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_WIDGET_LAUNCH.equals(intent.getAction())) {
            IntentHelper.launchCamera(context, true);
        }
    }
}
