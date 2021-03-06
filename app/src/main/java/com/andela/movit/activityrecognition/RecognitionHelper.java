/**
 * This class encapsulates all components concerned with activity recognition. It provides the
 * callbacks required by the Google API client, a means of starting and stopping activity
 * recognition, and a way to set a callback that will be invoked whenever an activity is detected.
 * */

package com.andela.movit.activityrecognition;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.andela.movit.config.Constants;
import com.andela.movit.location.IncomingStringCallback;
import com.andela.movit.receivers.StringBroadcastReceiver;
import com.andela.movit.utilities.FrameworkUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

public class RecognitionHelper implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private IncomingStringCallback activityCallback;

    private StringBroadcastReceiver activityBroadcastReceiver;

    private Context context;

    private GoogleApiClient apiClient;

    private PendingIntent pendingIntent;

    public RecognitionHelper(Context context) {
        this.context = context;
        buildApiClient();
    }

    /**
     * Sets the callback that will be invoked whenever an activity is detected.
     * @param activityCallback the callback object.
     * */

    public void setActivityCallback(IncomingStringCallback activityCallback) {
        this.activityCallback = activityCallback;
    }

    /**
     * Connects the API client and starts activity recognition.
     * */

    public void connect() {
        registerReceiver();
        apiClient.connect();
    }

    /**
     * Stops activity recognition and disconnects the API client.
     * */

    public void disconnect() {
        if (apiClient.isConnected()) {
            unregisterReceiver();
            stopActivityRecognition();
            apiClient.disconnect();
        }
    }

    private void unregisterReceiver() {
        FrameworkUtils.unregisterReceiver(context, activityBroadcastReceiver);
    }

    private void buildApiClient() {
        apiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    private void registerReceiver() {
        activityBroadcastReceiver = FrameworkUtils.registerStringReceiver(
                context, Constants.ACTIVITY_NAME.getValue());
        activityBroadcastReceiver.setIncomingStringCallback(activityCallback);
        LocalBroadcastManager
                .getInstance(context)
                .registerReceiver(activityBroadcastReceiver, getFilter());
    }

    private IntentFilter getFilter() {
        return new IntentFilter(Constants.ACTIVITY_NAME.getValue());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startActivityRecognition();
    }

    private void startActivityRecognition() {
        pendingIntent = getPendingIntent();
        ActivityRecognition.ActivityRecognitionApi
                .requestActivityUpdates(apiClient, 100, pendingIntent);
    }

    private void stopActivityRecognition() {
        ActivityRecognition.ActivityRecognitionApi
                .removeActivityUpdates(apiClient, pendingIntent);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onResult(@NonNull Status status) {
    }
}
