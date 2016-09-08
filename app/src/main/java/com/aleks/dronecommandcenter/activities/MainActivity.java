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

public class MainActivity extends AppCompatActivity {

    EngineConnector engineConnector;
    ARDrone drone;

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
        try {
            drone.start();
        }
        catch(Exception e) {
            e.printStackTrace();
            if (drone != null) {
                drone.stop();
            }
        }

        engineConnector = app.getEngineConnector();

        new Thread(new Runnable() {
            public void run()
            {
                final TextView tvDroneBattery = (TextView) findViewById(R.id.tvDroneBattery);
                final TextView tvBCIBattery = (TextView) findViewById(R.id.tvBCIBattery);
                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(drone.getNavDataManager().isConnected()) {
                                tvDroneBattery.setText("Drone Battery: " + drone.getBatteryLevel() + "%");
                            }
                            else {
                                tvDroneBattery.setText("Drone Disconnected!");
                            }
                            if(engineConnector.isConnected) {
                                int[] batt = IEmoStateDLL.IS_GetBatteryChargeLevel();
                                int BCIBattery = (int) ((float) batt[0] / batt[1] * 100);
                                tvBCIBattery.setText("BCI Battery: " + BCIBattery + "%");
                                BCISensors.update((View) findViewById(R.id.BCISensors));
                            }
                            else {
                                tvBCIBattery.setText("BCI Disconnected!");
                            }
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void getBCIStats(View v) {
        //IEdk.IEE_ConnectInsightDevice(1);
        int[] batt = IEmoStateDLL.IS_GetBatteryChargeLevel();
        int count = IEdk.IEE_GetInsightDeviceCount();
        int emoState = IEdk.IEE_EmoEngineEventGetEmoState();
        String insightName = IEdk.IEE_GetInsightDeviceName(0);
        TextView tv = (TextView) findViewById(R.id.tvBCIBattery);
        tv.setText(batt[0] + " " + batt[1]);
        System.out.println(drone.getBatteryLevel() + " ");
        System.out.println(drone.getCommandManager().isConnected());
        drone.reset();

        Intent intent = new Intent(this, MentalCommandActivity.class);
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
}
