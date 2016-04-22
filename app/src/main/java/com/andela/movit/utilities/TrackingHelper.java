package com.andela.movit.utilities;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import com.andela.movit.R;
import com.andela.movit.activityrecognition.RecognitionHelper;
import com.andela.movit.async.DbAsync;
import com.andela.movit.data.DbCallback;
import com.andela.movit.data.DbOperation;
import com.andela.movit.data.DbResult;
import com.andela.movit.data.MovementRepo;
import com.andela.movit.listeners.ActivityCallback;
import com.andela.movit.listeners.LocationCallback;
import com.andela.movit.location.LocationHelper;
import com.andela.movit.models.Movement;

public class TrackingHelper {

    private Context context;

    private LocationHelper locationHelper;

    private RecognitionHelper recognitionHelper;

    private Movement movement;

    private boolean isActive;

    private String currentActivity;

    private long timeBeforeLogging;

    private boolean isCurrentActivityLogged;

    public boolean isCurrentActivityLogged() {
        return isCurrentActivityLogged;
    }

    public TrackingHelper(Context context) {
        this.context = context;
        initializeVariables();
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public void setCurrentActivity(String activity) {
        isCurrentActivityLogged = false;
        currentActivity = activity;
    }

    public void setTimeBeforeLogging(long timeBeforeLogging) {
        this.timeBeforeLogging = timeBeforeLogging;
    }

    public void setActivityCallback(ActivityCallback activityCallback) {
        recognitionHelper.setActivityCallback(activityCallback);
    }

    public void setLocationCallback(LocationCallback locationCallback) {
        locationHelper.setLocationCallback(locationCallback);
    }

    private void initializeVariables() {
        locationHelper = new LocationHelper(context);
        recognitionHelper = new RecognitionHelper(context);
        currentActivity = "Unknown";
    }

    public void logCurrentActivity(String activity) {
        if (!isCurrentActivityLogged && !isActivityUnknown(activity)) {
            movement.setActivityName(activity);
            writeMovementToDatabase(movement);
            isCurrentActivityLogged = true;
        }
    }

    private boolean isActivityUnknown(String currentActivity) {
        return currentActivity.equals("Unknown");
    }

    private void writeMovementToDatabase(Movement movement) {
        movement.setTimeStamp(System.currentTimeMillis());
        DbAsync dbAsync = new DbAsync(getDbCallback());
        dbAsync.execute(getInsertOperation(movement));
    }

    public void startTracking() {
        locationHelper.connect();
        recognitionHelper.connect();
        isActive = true;
    }

    public void stopTracking() {
        locationHelper.disconnect();
        recognitionHelper.disconnect();
        isActive = false;
        isCurrentActivityLogged = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean hasTimeElapsed(Chronometer chronometer) {
        long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        return elapsedMillis >= timeBeforeLogging;
    }

    public boolean hasActivityChanged(String activityName) {
        return !currentActivity.equals(activityName);
    }

    private DbCallback getDbCallback() {
        return new DbCallback() {
            @Override
            public void onOperationSuccess(Object result) {
                Toast.makeText(context, "Activity logged successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOperationFail(String errorMessage) {
                Log.e("FOO", errorMessage);
            }
        };
    }

    public String getActivityStatement(String activityName) {
        if (isActivityUnknown(activityName)) {
            return context.getString(R.string.label_activity_name);
        }
        return "You have been "
                + activityName
                + " for:";
    }

    private DbOperation getInsertOperation(final Movement movement) {
        return new DbOperation() {
            @Override
            public DbResult execute() {
                MovementRepo repo = new MovementRepo(context);
                return new DbResult(repo.addMovement(movement), null);
            }
        };
    }
}