package edu.ucsb.cs.cs290i.service.detectors.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.location.LocationServiceLogger.LocationServiceLoggerBinder;

public class LocationServiceConnection implements ServiceConnection {

    private LocationServiceLogger service;
    private boolean isBound;
    private final Context c;


    public LocationServiceConnection(Context c) {
        this.c = c;
        bind();
    }


    private void bind() {
        if (!isBound) {
            Intent serviceIntent = new Intent(c, LocationServiceLogger.class);
            c.startService(serviceIntent);
            c.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    public void onServiceConnected(ComponentName c, IBinder iBinder) {
    	LocationServiceLoggerBinder binder = ( LocationServiceLoggerBinder) iBinder;
        service = binder.getService();
    }


    public LocationServiceLogger getService() {
        return service;
    }


    @Override
    public void onServiceDisconnected(ComponentName c1) {
        if (isBound) {
            c.unbindService(this);
        }
    }

}
