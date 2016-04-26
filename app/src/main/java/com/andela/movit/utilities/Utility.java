package com.andela.movit.utilities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.andela.movit.Movit;
import com.andela.movit.models.Movement;
import com.andela.movit.receivers.LocationBroadcastReceiver;
import com.andela.movit.receivers.StringBroadcastReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.andela.movit.config.Constants.*;

public class Utility {

    public static Movit getApp() {
        return Movit.getApp();
    }

    public static void launchActivity(Activity activity, Class activityClass) {
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
    }

    public static void makeToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void stopService(Context context, Class serviceClass) {
        context.stopService(new Intent(context, serviceClass));
    }

    public static Intent putMovementInIntent(Movement movement, Intent intent) {
        intent.putExtra(PLACE_NAME.getValue(), movement.getPlaceName());
        intent.putExtra(ACTIVITY_NAME.getValue(), movement.getActivityName());
        intent.putExtra(LATITUDE.getValue(), Double.toString(movement.getLatitude()));
        intent.putExtra(LONGITUDE.getValue(), Double.toString(movement.getLongitude()));
        intent.putExtra(TIMESTAMP.getValue(), Long.toString(movement.getTimeStamp()));
        return intent;
    }

    public static Movement getMovementFromBundle(Bundle bundle) {
        Movement mv = new Movement();
        mv.setActivityName(getStringFromBundle(bundle, ACTIVITY_NAME.getValue()));
        mv.setPlaceName(getStringFromBundle(bundle, PLACE_NAME.getValue()));
        mv.setLatitude(Double.parseDouble(getStringFromBundle(bundle, LATITUDE.getValue())));
        mv.setLongitude(Double.parseDouble(getStringFromBundle(bundle, LONGITUDE.getValue())));
        mv.setTimeStamp(Long.parseLong(getStringFromBundle(bundle, TIMESTAMP.getValue())));
        return mv;
    }

    private static String getStringFromBundle(Bundle bundle, String name) {
        return bundle.getString(name);
    }

    public static String getCoordsString(Movement movement) {
        return Double.toString(movement.getLatitude())
                + ", "
                + Double.toString(movement.getLongitude());
    }

    public static StringBroadcastReceiver registerStringReceiver(Context context, String actionName) {
        StringBroadcastReceiver receiver = new StringBroadcastReceiver(actionName);
        LocalBroadcastManager
                .getInstance(context)
                .registerReceiver(receiver, getFilter(actionName));
        return receiver;
    }

    private static IntentFilter getFilter(String actionName) {
        return new IntentFilter(actionName);
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public static int[] getDateValues(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new int[] {year, month, day};
    }

    public static Date generateDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    public static String getDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy");
        return format.format(date);
    }
}
