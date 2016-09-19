package com.aleks.dronecommandcenter;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.configuration.ConfigurationManager;
import de.yadrone.base.navdata.ControlState;
import de.yadrone.base.navdata.ControlStateListener;
import de.yadrone.base.navdata.DroneState;
import de.yadrone.base.navdata.NavDataManager;
import de.yadrone.base.navdata.StateListener;
import de.yadrone.base.utils.ARDroneUtils;
import de.yadrone.base.video.VideoDecoder;
import de.yadrone.base.video.VideoManager;
import de.yadrone.base.video.xuggler.XugglerDecoder;

import de.yadrone.base.navdata.BatteryListener;


public class ARDrone implements de.yadrone.base.IARDrone {

    /** default ip address */
    private static final String IP_ADDRESS = "192.168.1.1";

    private String ipaddr = null;
    private InetAddress inetaddr = null;
    private VideoDecoder videoDecoder = null;

    // managers
    private CommandManager commandManager = null;
    private VideoManager videoManager = null;
    private NavDataManager navdataManager = null;
    private ConfigurationManager configurationManager = null;

    // The speed setting has been moved into this class to allow the commandmanager to stay simple and to be able to do
    // more advanced speed calculations for example based on the actual velocity
    // Should we refactor this into a separate API?
    private int speed = 25;
    private Set<ISpeedListener> speedListener = null;

    private int batteryLevel;
    private ControlState state;

    /** constructor */
    public ARDrone() {
        this(IP_ADDRESS);
    }

    public ARDrone(String ipaddr) {
        this(ipaddr, new XugglerDecoder());
    }

    /**
     * Create a new instance of a drone's virtual counterpart.
     * @param ipaddr  The address of the drone, e.g. 192.168.1.1
     * @param videoDecoder  A decoder instance, e.g. 'new XugglerDecoder' or null, if video shall not be used (e.g. on Android devices)
     */
    public ARDrone(String ipaddr, VideoDecoder videoDecoder) {
        this.ipaddr = ipaddr;
        this.videoDecoder = videoDecoder;
        this.speedListener = new HashSet<ISpeedListener>();
        this.getNavDataManager().addBatteryListener(batteryListener);
        this.getNavDataManager().addStateListener(stateListener);
    }

    public synchronized CommandManager getCommandManager() {
        if (commandManager == null) {
            InetAddress ia = getInetAddress();
            commandManager = new CommandManager(ia);
            commandManager.start();
        }
        return commandManager;
    }

    public synchronized NavDataManager getNavDataManager() {
        if (navdataManager == null) {
            InetAddress ia = getInetAddress();
            CommandManager cm = getCommandManager();
            navdataManager = new NavDataManager(ia, cm);
            navdataManager.start();
        }
        return navdataManager;
    }

    @Override
    public VideoManager getVideoManager() {
        return null;
    }

    public synchronized ConfigurationManager getConfigurationManager() {
        if (configurationManager == null) {
            InetAddress ia = getInetAddress();
            CommandManager cm = getCommandManager();
            configurationManager = new ConfigurationManager(ia, cm);
            configurationManager.start();
        }
        return configurationManager;
    }

    @Override
    public void stop() {
        freeze();
        landing();
        CommandManager cm = getCommandManager();
        cm.stop();
        ConfigurationManager cfgm = getConfigurationManager();
        cfgm.close();
        NavDataManager nm = getNavDataManager();
        nm.stop();
        VideoManager vm = getVideoManager();
        if (vm != null)
            vm.close();
    }

    @Override
    public void start() {
        // call get to init and start manager
        getCommandManager();
        getConfigurationManager();
        getNavDataManager();
        //getVideoManager();
    }


    @Override
    public void setHorizontalCamera() {
        if (commandManager != null)
            commandManager.setVideoChannel(VideoChannel.HORI);
    }

    @Override
    public void setVerticalCamera() {
        if (commandManager != null)
            commandManager.setVideoChannel(VideoChannel.VERT);
    }

    @Override
    public void setHorizontalCameraWithVertical() {
        if (commandManager != null)
            commandManager.setVideoChannel(VideoChannel.LARGE_HORI_SMALL_VERT);
    }

    @Override
    public void setVerticalCameraWithHorizontal() {
        if (commandManager != null)
            commandManager.setVideoChannel(VideoChannel.LARGE_VERT_SMALL_HORI);
    }

    @Override
    public void toggleCamera() {
        if (commandManager != null)
            commandManager.setVideoChannel(VideoChannel.NEXT);
    }

    @Override
    public void landing() {
        if (commandManager != null)
            commandManager.landing();
    }

    public void land() {
        landing();
    }

    @Override
    public void takeOff() {
        if (commandManager != null)
        {
            // we send emergency command before we take off which will toggle emergency mode off if the drone is actualy in emergency mode
            // contributed by Naushad, see post on https://projects.ardrone.org/boards/1/topics/show/5259 from 30.04.2014
            commandManager.emergency();

            commandManager.takeOff();
        }
    }

    @Override
    public void reset() {
        if (commandManager != null)
            commandManager.emergency();
    }

    @Override
    public void forward() {
        if (commandManager != null)
            commandManager.forward(speed);
    }

    @Override
    public void backward() {
        if (commandManager != null)
            commandManager.backward(speed);
    }

    @Override
    public void spinRight() {
        if (commandManager != null)
            commandManager.spinRight(speed);
    }

    @Override
    public void spinLeft() {
        if (commandManager != null)
            commandManager.spinLeft(speed);
    }

    @Override
    public void up() {
        if (commandManager != null)
            commandManager.up(speed);
    }

    @Override
    public void down() {
        if (commandManager != null)
            commandManager.down(speed);
    }

    @Override
    public void goRight() {
        if (commandManager != null)
            commandManager.goRight(speed);
    }

    @Override
    public void goLeft() {
        if (commandManager != null)
            commandManager.goLeft(speed);
    }

    @Override
    public void freeze() {
        if (commandManager != null)
            commandManager.freeze();
    }

    public void hover() {
        if (commandManager != null)
            commandManager.hover();
    }

    @Override
    public void setMaxAltitude(int altitude) {
        if (commandManager != null)
            commandManager.setMaxAltitude(altitude);
    }

    @Override
    public void setMinAltitude(int altitude) {
        if (commandManager != null)
            commandManager.setMinAltitude(altitude);
    }

    @Override
    public void move3D(int speedX, int speedY, int speedZ, int speedSpin) {
        if (commandManager != null)
            commandManager.move(speedX, speedY, speedZ, speedSpin);
    }

    @Override
    public void setSpeed(int speed) {
        if (this.speed != speed)
        {
            this.speed = speed;

            // inform listener
            Iterator<ISpeedListener> iter = speedListener.iterator();
            while(iter.hasNext())
            {
                iter.next().speedUpdated(speed);
            }
        }
        else
        {
            this.speed = speed;
        }
    }

    private BatteryListener batteryListener = new BatteryListener() {
        public void voltageChanged(int vbat_raw) {

        }

        public void batteryLevelChanged(int batteryLevel) {
            ARDrone.this.batteryLevel = batteryLevel;
        }
    };

    public int getBatteryLevel() {
        return batteryLevel;
    }

    private StateListener stateListener = new StateListener() {
        @Override
        public void stateChanged(DroneState droneState) {
        }
        @Override
        public void controlStateChanged(ControlState controlState) {
            ARDrone.this.state = controlState;
        }
    };

    public void doFor(long millis, Context context) {
        final Context context2 = context;
        new CountDownTimer(millis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                ARDrone.this.hover();
                //Toast.makeText(context2, "Hovering!", Toast.LENGTH_SHORT).show();

            }
        }.start();
    }

    public ControlState getState() {
        return state;
    }

    @Override
    public void addSpeedListener(de.yadrone.base.ARDrone.ISpeedListener iSpeedListener) {

    }

    @Override
    public void removeSpeedListener(de.yadrone.base.ARDrone.ISpeedListener iSpeedListener) {

    }

    /**
     * 0.01-1.0 -> 1-100%
     *
     * @return 1-100%
     */
    @Override
    public int getSpeed() {
        return speed;
    }

    public void addSpeedListener(ISpeedListener speedListener)
    {
        this.speedListener.add(speedListener);
    }

    public void removeSpeedListener(ISpeedListener speedListener) {
        this.speedListener.remove(speedListener);
    }

    public interface ISpeedListener {
        public void speedUpdated(int speed);
    }

    /**
     * Call upon an exception occurred in one of the managers
     */
    public static void error(String message, Object obj) {
        System.err.println("[" + obj.getClass() + "] " + message);
    }

    private InetAddress getInetAddress() {
        if (inetaddr == null) {
            StringTokenizer st = new StringTokenizer(ipaddr, ".");
            byte[] ipBytes = new byte[4];
            if (st.countTokens() == 4) {
                for (int i = 0; i < 4; i++) {
                    ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
                }
            } else {
                error("Incorrect IP address format: " + ipaddr, this);
                return null;
            }
            try {
                inetaddr = InetAddress.getByAddress(ipBytes);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return inetaddr;
    }
}