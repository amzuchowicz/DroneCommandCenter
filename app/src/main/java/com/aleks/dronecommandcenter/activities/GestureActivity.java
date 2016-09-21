package com.aleks.dronecommandcenter.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aleks.dronecommandcenter.ARDrone;
import com.aleks.dronecommandcenter.DroneCommandCenterApp;
import com.aleks.dronecommandcenter.R;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;
import org.wiigee.filter.HighPassFilter;
import org.wiigee.filter.LowPassFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GestureActivity extends AppCompatActivity {

    private static final int TRAIN_BUTTON = 0x01;
    private static final int SAVE_BUTTON = 0x02;
    private static final int RECOGNISE_BUTTON = 0x03;

    private AndroidWiigee wiigee;
    private ARDrone drone;
    //private BebopDrone bebop;
    private int gestureID = 0;
    private HashMap<Integer, Integer> gestureIdCommand = new HashMap<>();
    private final long DELAY = 3000;

    private Button btnTrain;
    private Button btnRecognise;
    private Spinner spnCommand;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private boolean isTraining = false;
    private boolean isRecognising = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        wiigee = new AndroidWiigee(this);
        wiigee.setTrainButton(TRAIN_BUTTON);
        wiigee.setRecognitionButton(RECOGNISE_BUTTON);
        wiigee.setCloseGestureButton(SAVE_BUTTON);

        final ToggleButton tglPractice = (ToggleButton) findViewById(R.id.tglPractice);
        final TextView tvRecognised = (TextView) findViewById(R.id.tvRecognised);

        prefs = getSharedPreferences("com.aleks.dronecommandcenter.gestures", Context.MODE_PRIVATE);
        editor = prefs.edit();

        for(Map.Entry entry : prefs.getAll().entrySet()) {
            if(entry != null) {
                gestureIdCommand.put(Integer.parseInt((String) entry.getKey()), (Integer) entry.getValue());
                //wiigee.getDevice().getProcessingUnit().loadGesture("Gesture" + entry.getKey());
                gestureID++;
            }
        }
        wiigee.getDevice().getProcessingUnit().loadClassifier("dcc_gestures");

        DroneCommandCenterApp app = (DroneCommandCenterApp) getApplicationContext();
        drone = app.getARDrone();
        //bebop = app.getBebop();

        wiigee.addGestureListener(new GestureListener() {
            @Override
            public void gestureReceived(GestureEvent event) {
                System.out.println("Recognized: " + event.getId() + " Probability: " + event.getProbability());
                String result = "Nothing Recognised!";

                if(!gestureIdCommand.isEmpty()) {
                    int commandPos = gestureIdCommand.get(event.getId());
                    if(event.isValid()) {
                        result = "Recognised: " + spnCommand.getItemAtPosition(commandPos).toString();
                        if (tglPractice.isChecked()) {
                            switch (commandPos) {
                                case 0:
                                    drone.up();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                case 1:
                                    drone.down();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                case 2:
                                    drone.forward();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                case 3:
                                    drone.backward();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                case 4:
                                    drone.goLeft();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                case 5:
                                    drone.goRight();
                                    drone.doFor(DELAY, getApplicationContext());
                                    break;
                                default:
                                    //do nothing
                            }
                        }
                    }
                }
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                tvRecognised.setText(result);
            }
        });

        btnTrain = (Button) findViewById(R.id.btnTrain);
        btnTrain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case (MotionEvent.ACTION_DOWN): {
                    wiigee.getDevice().fireButtonPressedEvent(TRAIN_BUTTON);
                    break;
                }
                case (MotionEvent.ACTION_UP): {
                    wiigee.getDevice().fireButtonReleasedEvent(TRAIN_BUTTON);
                    //wiigee.getDevice().saveGesture(0, "test.txt");

                    wiigee.getDevice().fireButtonPressedEvent(SAVE_BUTTON);
                    wiigee.getDevice().fireButtonReleasedEvent(SAVE_BUTTON);

                    int commandPos = spnCommand.getSelectedItemPosition();
                    gestureIdCommand.put(gestureID, commandPos);
                    System.out.println(gestureIdCommand);
                    //wiigee.getDevice().getProcessingUnit().saveGesture(gestureID, "Gesture" + gestureID);
                    //wiigee.getDevice().getProcessingUnit().saveGesture(1, "Gesture" + gestureID);
                    wiigee.getDevice().getProcessingUnit().saveClassifier("dcc_gestures");
                    editor.putInt(gestureID + "", commandPos);
                    editor.apply();

                    gestureID++;
                }
            }
            return false;
            }
        });

        btnRecognise = (Button) findViewById(R.id.btnRecognise);
        btnRecognise.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case (MotionEvent.ACTION_DOWN): {
                    wiigee.getDevice().fireButtonPressedEvent(RECOGNISE_BUTTON);
                    break;
                }
                case (MotionEvent.ACTION_UP): {
                    wiigee.getDevice().fireButtonReleasedEvent(RECOGNISE_BUTTON);
                    break;
                }
            }
            return false;
            }
        });

        //wiigee.addFilter(new HighPassFilter(0.3));
        //wiigee.addFilter(new LowPassFilter(0.03));
        //wiigee.getDevice().getProcessingUnit().reset();
        //wiigee.getDevice().onSensorChanged();

        spnCommand = (Spinner) findViewById(R.id.spnCommand);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gesture_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCommand.setAdapter(adapter);
    }

    public void clearAllGestures(View v) {
        for(Map.Entry entry : gestureIdCommand.entrySet()) {
            editor.remove(entry.getKey().toString());
        }
        editor.apply();
        gestureIdCommand.clear();
        wiigee.getDevice().getProcessingUnit().reset();
        wiigee.getDevice().getProcessingUnit().saveClassifier("dcc_gestures");
        gestureID = 0;
        //wiigee = new AndroidWiigee(this);
    }
/*
    public void sendTrain(View v) {
        if(isTraining) {
            wiigee.getDevice().fireButtonReleasedEvent(TRAIN_BUTTON);
            isTraining = false;
            //wiigee.getDevice().saveGesture(0, "test.txt");
            wiigee.getDevice().fireButtonPressedEvent(SAVE_BUTTON);
            wiigee.getDevice().fireButtonReleasedEvent(SAVE_BUTTON);
        }
        else {
            wiigee.getDevice().fireButtonPressedEvent(TRAIN_BUTTON);
            isTraining = true;
        }
    }

    public void sendRecognise(View v) {
        if(isRecognising) {
            wiigee.getDevice().fireButtonReleasedEvent(RECOGNISE_BUTTON);
            isRecognising = false;
        }
        else {
            wiigee.getDevice().fireButtonPressedEvent(RECOGNISE_BUTTON);
            isRecognising = true;
        }
    }
*/

    public void sendEmergency(View v) {
        drone.reset();
        //bebop.emergency();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wiigee.getDevice().setAccelerationEnabled(true);
        } catch (IOException e) {

        }

    }
}
