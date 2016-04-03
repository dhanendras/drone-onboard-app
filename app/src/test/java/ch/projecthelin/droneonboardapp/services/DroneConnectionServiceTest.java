package ch.projecthelin.droneonboardapp.services;

import android.os.Bundle;
import ch.projecthelin.droneonboardapp.DroneOnboardApp;
import ch.projecthelin.droneonboardapp.di.*;
import ch.projecthelin.droneonboardapp.dto.dronestate.DroneState;
import ch.projecthelin.droneonboardapp.mappers.DroneStateMapper;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DroneConnectionServiceTest {

    private DroneConnectionService service;
    private DroneConnectionListener droneConnectionListener;

    ControlTower tower;

    Drone drone;

    @Before
    public void setupServiceAndListener() {
        tower = mock(ControlTower.class);
        drone = mock(Drone.class);
        TestAppModule module = new TestAppModule();
        module.setControlTower(tower);
        module.setDrone(drone);

        TestAppComponent component = DaggerTestAppComponent.builder()
                .testAppModule(module)
                .build();

        droneConnectionListener = mock(DroneConnectionListener.class);
        service = new DroneConnectionService(tower, drone);
    }

    @Test
    public void ChangedGPSState() throws Exception {
        Gps gps = new Gps();
        gps.setFixType(3);
        gps.setSatCount(10);

        when(drone.getAttribute(AttributeType.GPS)).thenReturn(gps);

        service.addConnectionListener(droneConnectionListener);
        service.onDroneEvent(AttributeEvent.WARNING_NO_GPS, new Bundle());

        verify(droneConnectionListener).onGPSStateChange(DroneStateMapper.getGPSState(gps));
    }

    @Test
    public void ChangedConnectionState() throws Exception {
        Altitude altitude = new Altitude();
        altitude.setAltitude(100);
        altitude.setTargetAltitude(150);

        Speed speed = new Speed();
        speed.setGroundSpeed(5);
        speed.setVerticalSpeed(1);

        Type type = new Type();
        type.setFirmware(Type.Firmware.ARDU_COPTER);

        when(drone.getAttribute(AttributeType.ALTITUDE)).thenReturn(altitude);
        when(drone.getAttribute(AttributeType.SPEED)).thenReturn(speed);
        when(drone.getAttribute(AttributeType.TYPE)).thenReturn(type);

        service.addConnectionListener(droneConnectionListener);

        service.onDroneEvent(AttributeEvent.STATE_CONNECTED, new Bundle());

        DroneState droneState = DroneStateMapper.getDroneState(drone);
        droneState.setIsConnected(true);

        verify(droneConnectionListener).onDroneStateChange(droneState);
    }
}