package es.ua.eps.gpsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GPSActivity extends AppCompatActivity implements View.OnClickListener {
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private Button button_getLocation;
    private Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        button_getLocation = findViewById(R.id.button_getLocation);
        button_getLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        flag = displayGpsStatus();

        if (flag) {
            Log.d("DebugApp", "GPS ON");

            locationListener = new MyLocationManager();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20, locationListener);
        } else {
            Log.d("DebugApp", "GPS OFF");
            alertbox("GPS STATUS", "Your GPS is OFF");
        }
    }

    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();

        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);

        return gpsStatus;
    }


    class MyLocationManager implements LocationListener {
        private Context context = getBaseContext();

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(context, "Location changed \n Lat: " + location.getLatitude() + " \n Lng: "
                        + location.getLongitude(), Toast.LENGTH_SHORT).show();

            String longitude = "Longitude: " + location.getLongitude();
            Log.d("DebugApp", longitude);

            String latitude = "Latitude: " + location.getLatitude();
            Log.d("DebugApp", latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
