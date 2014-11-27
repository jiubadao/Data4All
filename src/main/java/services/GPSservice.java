package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GPSservice extends Service implements LocationListener {

    private static final String TAG = "GPStracker";
    /**
     * Are we currently tracking ?
     */
    private boolean isTracking = false;

    /**
     * Is GPS enabled ?
     */
    private boolean isGpsEnabled = false;

    /**
     * Last known location
     */
    private Location lastLocation;

    /**
     * LocationManager
     */
    private LocationManager lmgr;

    @Override
    public void onCreate() {

    }
    
  

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
                5000, // location will be fetched every 5 sec                                                                     
                0, this); // is 0meters for testingpurpose. should be greater than 0

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "gps service destroyed", Toast.LENGTH_SHORT)
                .show();
    }

    public void onLocationChanged(Location location) {
        // We're receiving location, so GPS is enabled
        isGpsEnabled = true;

     
        // TODO this is the place where the location info should be processed.

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Log.d(TAG, "lat=" + lat + " lon=" + lon);

        lastLocation = location;

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not interested in provider status

    }

    public void onProviderEnabled(String provider) {
        isGpsEnabled = true;
        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG)
                .show();

    }

    public void onProviderDisabled(String provider) {
        isGpsEnabled = false;
        Toast.makeText(getBaseContext(),
                "Gps turned off, GPS tracking not possible ", Toast.LENGTH_LONG)
                .show();
    }



    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


}
