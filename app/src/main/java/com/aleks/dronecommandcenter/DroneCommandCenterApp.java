package com.aleks.dronecommandcenter;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import de.yadrone.base.*;

/**
 * Created by Aleks on 1/08/2016.
 */
public class DroneCommandCenterApp extends Application {
    // The drone is kept in the application context so that all activities use the same drone instance
    private ARDrone drone;
    EngineConnector engineConnector;

    public void onCreate() {
        //super.onCreate();
        drone = new ARDrone();
        drone.getCommandManager().emergency();
        drone.setSpeed(15); //default is 25

        EngineConnector.setContext(this);
        engineConnector = EngineConnector.shareInstance();
        //engineConnector.delegate = this;
    }

    public ARDrone getARDrone()
    {
        return drone;
    }

    public EngineConnector getEngineConnector() {
        return engineConnector;
    }

}
