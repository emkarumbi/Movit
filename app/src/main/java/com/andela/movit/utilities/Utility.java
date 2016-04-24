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

    public static void launchActivityWithMovement(Activity activity, Class activityClass,
                                                  Movement movement) {
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(putMovementInIntent(movement, intent));
    }

    public static void launchService(
            Context context, Class serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        context.startService(intent);
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
}
