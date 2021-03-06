package com.andela.movit.views.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.andela.movit.Movit;
import com.andela.movit.R;
import com.andela.movit.background.TrackingService;
import com.andela.movit.utilities.FrameworkUtils;
import com.andela.movit.views.activities.SplashActivity;
import com.andela.movit.location.IncomingStringCallback;
import com.andela.movit.location.LocationCallback;
import com.andela.movit.models.Movement;
import com.andela.movit.receivers.LocationBroadcastReceiver;
import com.andela.movit.receivers.StringBroadcastReceiver;
import com.andela.movit.utilities.Utility;

import static com.andela.movit.config.Constants.*;

public class TrackerFragment extends Fragment {

    private View rootView;

    private Activity context;

    private TextView locationName;

    private TextView activityNameView;

    private ImageView trackButton;

    private String currentActivity = "Unknown";

    private Chronometer counter;

    private Movement movement;

    private StringBroadcastReceiver statementReceiver;

    private LocationBroadcastReceiver locationReceiver;

    private ImageView activityIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.rootView = view;
        context = getActivity();
        start();
    }

    private void start() {
        Movit app = Movit.getApp();
        if (app.isAppLaunched()) {
            initializeComponents();
            movement = app.getInitialLocation();
            displayLocation(movement);
        } else {
            launchSplash();
        }
        app.setIdle(true);
    }

    private void launchSplash() {
        FrameworkUtils.startActivity(context, SplashActivity.class);
        context.finish();
    }

    private void initializeComponents() {
        initializeViews();
        setTrackClickListener();
        startService();
    }

    private void startService() {
        Intent intent = new Intent(context, TrackingService.class);
        context.startService(intent);
    }

    private void initializeViews() {
        locationName = (TextView) rootView.findViewById(R.id.location_name);
        activityNameView = (TextView) rootView.findViewById(R.id.activity_name);
        trackButton = (ImageView) rootView.findViewById(R.id.trackButton);
        counter = (Chronometer) rootView.findViewById(R.id.counter);
        activityIcon = (ImageView) rootView.findViewById(R.id.label_status);
    }

    private void setTrackClickListener() {
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTracking();
            }
        });
    }

    private void toggleTracking() {
        if (Movit.getApp().isTracking()) {
            stopTracking();
        } else {
            startTracking();
        }
    }

    private void stopTracking() {
        sendCommandToService("STOP");
        toggleButton(false);
        counter.stop();
        counter.setBase(SystemClock.elapsedRealtime());
    }

    private void sendCommandToService(String command) {
        Intent intent  = new Intent(context, TrackingService.class);
        intent.putExtra(COMMAND.getValue(), command);
        context.startService(intent);
    }

    private void startTracking() {
        sendCommandToService("START");
        toggleButton(true);
        restartCounter();
    }

    private void restartCounter() {
        counter.setBase(SystemClock.elapsedRealtime());
        counter.start();
    }

    private void toggleButton(boolean isTracking) {
        if (isTracking) {
            trackButton.setImageResource(R.drawable.stop);
        } else {
            trackButton.setImageResource(R.drawable.play);
        }
    }

    private void registerStatementReceiver() {
        statementReceiver = FrameworkUtils.registerStringReceiver(context, STATEMENT.getValue());
        statementReceiver.setIncomingStringCallback(statementCallback);
    }

    private void registerLocationReceiver() {
        locationReceiver = new LocationBroadcastReceiver(locationCallback);
        IntentFilter filter = new IntentFilter(LOCATION.getValue());
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver, filter);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationDetected(Movement mv) {
            movement = mv;
            displayLocation(mv);
        }
    };

    private IncomingStringCallback statementCallback = new IncomingStringCallback() {
        @Override
        public void onStringArrive(String activityStatement) {
            displayActivity(activityStatement);
            Movit.getApp().setIdle(true);
        }
    };

    private void displayLocation(Movement mv) {
        locationName.setText(mv.getPlaceName());
    }

    private void displayActivity(String activity) {
        if (!currentActivity.equals(activity)) {
            activityNameView.setText(activity);
            currentActivity = activity;
            activityIcon.setImageResource(FrameworkUtils.getIconId(activity));
            restartCounter();
        }
    }

    @Override
    public void onPause() {
        FrameworkUtils.unregisterReceiver(context, locationReceiver);
        FrameworkUtils.unregisterReceiver(context, statementReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        toggleButton(Movit.getApp().isTracking());
        registerLocationReceiver();
        registerStatementReceiver();
        super.onResume();
    }
}
