package ch.projecthelin.droneonboardapp.services;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.drone.GuidedApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Type;

import java.util.ResourceBundle;

import ch.projecthelin.droneonboardapp.R;

public class DroneConnectionService implements DroneListener, TowerListener, DroneConnectionListener{

    // remove later!
    public static final String TCP_SERVER_IP = "192.168.56.1";
    // public static final int BAUD_RATE_FOR_USB = 115200;
    public static final int TCP_SERVER_PORT = 5760;

    public ConnectionParameter connectionParameter;

    private static DroneConnectionService instance;

    private DroneConnectionListener connectionListener;

    private static ControlTower controlTower;
    private static Drone drone;
    private final Handler handler = new Handler();

    private DroneConnectionService(Context applicationContext){
        controlTower = new ControlTower(applicationContext);
        drone = new Drone(applicationContext);

        controlTower.connect(this);

    }

    public void connect(){
        Bundle extraParams = new Bundle();
        extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, TCP_SERVER_IP);
        extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, TCP_SERVER_PORT);
        connectionParameter = new ConnectionParameter(ConnectionType.TYPE_TCP, extraParams, null);

        drone.connect(connectionParameter);
    }

    public static DroneConnectionService getInstance(Context applicationContext) {
        if(instance == null){
            instance = new DroneConnectionService(applicationContext);
        }
        return instance;
    }    

    public String test(){
        return "Test";
    }


    public void addConnectionListener(DroneConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        DroneState droneState = new DroneState();
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                Log.d(getClass().getCanonicalName(), "STATE_CONNECTED");
                droneState.setIsConnected(true);
                connectionListener.onConnectionStateChange(droneState);
                break;
            case AttributeEvent.STATE_DISCONNECTED:
                Log.d(getClass().getCanonicalName(), "STATE_DISCONNECTED");
                droneState.setIsConnected(false);
                connectionListener.onConnectionStateChange(droneState);
                break;
            case AttributeEvent.STATE_UPDATED:
                break;

            case AttributeEvent.STATE_ARMING:
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                break;

            case AttributeEvent.TYPE_UPDATED:
                break;

            case AttributeEvent.SPEED_UPDATED:
                break;

            case AttributeEvent.HOME_UPDATED:
                break;

            case AttributeEvent.BATTERY_UPDATED:
                Log.d(getClass().getCanonicalName(), "BATTERY_UPDATED");

                Battery droneBattery = drone.getAttribute(AttributeType.BATTERY);
                BatteryState batteryState = new BatteryState(droneBattery.getBatteryVoltage(),
                        droneBattery.getBatteryCurrent(),
                        droneBattery.getBatteryDischarge(),
                        droneBattery.getBatteryRemain());
                droneState.setBatteryState(batteryState);

                connectionListener.onConnectionStateChange(droneState);
                break;

            case AttributeEvent.GPS_FIX:
                Log.d(getClass().getCanonicalName(), "GPS_FIX");

                Gps droneGPS = drone.getAttribute(AttributeType.GPS);
                GPSState gpsState = new GPSState(droneGPS.getFixStatus(),
                        droneGPS.getSatellitesCount(),droneGPS.getPosition().getLatitude(),
                        droneGPS.getPosition().getLongitude());
                droneState.setGPSState(gpsState);

                connectionListener.onConnectionStateChange(droneState);
                break;

            default:
                break;

        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    @Override
    public void onTowerConnected() {
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {
        this.drone.unregisterDroneListener(this);
        this.controlTower.unregisterDrone(this.drone);
    }

    @Override
    public void onConnectionStateChange(DroneState state) {
        Log.d("DroneConnection", "test here");
    }
}

