package com.aleks.dronecommandcenter.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.aleks.dronecommandcenter.ARDrone;
import com.aleks.dronecommandcenter.BCISensors;
import com.aleks.dronecommandcenter.DroneCommandCenterApp;
import com.aleks.dronecommandcenter.EngineConnector;
import com.aleks.dronecommandcenter.R;
import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEmoStateDLL;

import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;

public class MainActivity extends AppCompatActivity implements AttitudeListener, BatteryListener {

    EngineConnector engineConnector;
    ARDrone drone;
    TextView tvDroneBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        //EngineConnector.setContext(this);
        //engineConnector = EngineConnector.shareInstance();
        //engineConnector.delegate = this;

        //IEdk.IEE_EngineConnect(this,"");
        //tv = (TextView) findViewById(R.id.tvBCIStats);

        DroneCommandCenterApp app = (DroneCommandCenterApp)getApplicationContext();
        drone = app.getARDrone();
        tvDroneBattery = (TextView) findViewById(R.id.tvDroneBattery);

        try {
            drone.start();
        }
        catch(Exception e) {
            e.printStackTrace();
            if (drone != null) {
                drone.stop();
            }
        }
        drone.getNavDataManager().addAttitudeListener(this);
        drone.getNavDataManager().addBatteryListener(this);

        engineConnector = app.getEngineConnector();

        new Thread(new Runnable() {
            public void run()
            {
                final TextView tvBCIBattery = (TextView) findViewById(R.id.tvBCIBattery);
                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(engineConnector.isConnected) {
                                int[] batt = IEmoStateDLL.IS_GetBatteryChargeLevel();
                                int BCIBattery = (int) ((float) batt[0] / batt[1] * 100);
                                tvBCIBattery.setText("BCI Battery: " + BCIBattery + "%");
                                BCISensors.update(findViewById(R.id.BCISensors));
                            }
                            else {
                                tvBCIBattery.setText("BCI Disconnected!");
                            }
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void launchMental(View v) {
        Intent intent = new Intent(this, MentalCommandActivity.class);
        startActivity(intent);
    }

    public void launchFacial(View v) {
        Intent intent = new Intent(this, FacialCommandActivity.class);
        startActivity(intent);
    }

    public void launchGesture(View v) {
        Intent intent = new Intent(this, GestureActivity.class);
        startActivity(intent);
    }

    public void launchVoice(View v) {
        Intent intent = new Intent(this, VoiceActivity.class);
        startActivity(intent);
    }

    @Override
    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView tvDroneSensors = (TextView) findViewById(R.id.tvDroneSensors);
                tvDroneSensors.setText("Drone sensors:\n\nPitch: " + pitch + "\nRoll: " + roll + "\nYaw: " + yaw);
            }
        });
    }

    @Override
    public void attitudeUpdated(float v, float v1) {}

    @Override
    public void windCompensation(float v, float v1) {}

    @Override
    public void batteryLevelChanged(final int battery) {
        runOnUiThread(new Runnable() {
            public void run() {
                tvDroneBattery.setText("Drone battery: " + battery + "%");
            }
        });
    }

    @Override
    public void voltageChanged(int i) {

    }
}
