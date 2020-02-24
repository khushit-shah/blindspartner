package com.khushitshah.blindspartner.libs.Location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class Location {
    private Context a;
    private LocationManager locationManager;
    private LocationInterface locInterface;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mlocationCallback;
    private LocationSettingsRequest.Builder builder;

    /**
     * Currently depreciated.
     * Can be used afterwards when we implement functionality of navigation
     */
    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(final android.location.Location location) {
            double lat = location.getLatitude();
            double lon = location.getLatitude();
            System.out.println(lat + ":" + lon);
            String data = getAddress(lat, lon);
            System.out.println(data);
            locInterface.location(data);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * Provides functionality of getting location.
     *
     * @param a MainActivity for its context,
     */
    public Location(Context a) {
        this.a = a;
        locationManager = (LocationManager) a.getSystemService(LOCATION_SERVICE);
    }


    /**
     * Checks all location provider gets last known location.
     * url: https://stackoverflow.com/questions/20438627/getlastknownlocation-returns-null
     */
    private android.location.Location getLastKnownLocation() {
        locationManager = (LocationManager) a.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        android.location.Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") android.location.Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /**
     * After location has been fetched and converted to string.
     * Needs Location permissions.
     *
     * @param locInterface Calls <code>locInterface.location()</code>
     */

    public void getLocation(LocationInterface locInterface) {
        this.locInterface = locInterface;
        android.location.Location loc = getLastKnownLocation();
        if (loc == null) {
            locInterface.location("can't fetch location try again later!");
            return;
        }
        String location = getAddress(loc.getLatitude(), loc.getLongitude());
        locInterface.location(location);
    }

    public android.location.Location getCurLocation() {
        android.location.Location loc = getLastKnownLocation();
        if (loc == null) {
            locInterface.location("can't fetch location try again later!");
            return loc;
        }
        return loc;
    }

    public void getConstantLocation(LocationInterface callback, Activity activity) {
        //        android.location.Location loc = getLastKnownLocation();
        //        if (loc == null) {
        //            locInterface.loc/ation("can't fetch location try again later!");
        //            return;
        //        }
        //        String location = getAddress(loc.getLatitude(), loc.getLongitude());
        //        locInterface.location(location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fetchLastLocation(activity);
        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                android.location.Location loc = null;
                float macAccuracy = -1f;
                for (android.location.Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    if (location.getAccuracy() > macAccuracy) {
                        loc = location;
                        macAccuracy = location.getAccuracy();
                    }
                    Log.e("CONTINIOUSLOC: ", location.toString());
                }

                callback.location(loc.getLatitude() + "," + loc.getLongitude());
            }
        };

        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                mlocationCallback,
                Looper.getMainLooper());
//        checkLocationSetting(builder);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

    /**
     * Gets String address from latitude and longitude.
     */
    private String getAddress(double lat, double lng) {
        System.out.println(lat + ":" + lng);
        Geocoder geocoder = new Geocoder(a.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            System.out.println(obj.toString());
            String add = obj.getAddressLine(0);
            add = add.replace(obj.getPostalCode(), obj.getPostalCode().replace("", " "));
            System.out.println("add:" + add);
            // Check if location is detected.
            if (add.replace(",", "").length() < 2) return "Can't detect location.";
            return add;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(a.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return "Can't detect location!";
        }
    }

    private void fetchLastLocation(Activity activity) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        if (location != null) {
                            // Logic to handle location object
                            Log.e("LAST LOCATION: ", location.toString()); // You will get your last location here
                        }
                    }
                });

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void stopConstantLocation() {
        fusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

//    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {
//
//        SettingsClient client = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                // All location settings are satisfied. The client can initialize
//                // location requests here.
//                // ...
//                startLocationUpdates();
//                return;
//            }
//        });
//
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull final Exception e) {
//                if (e instanceof ResolvableApiException) {
//                    // Location settings are not satisfied, but this can be fixed
//                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
//                    builder1.setTitle("Continious Location Request");
//                    builder1.setMessage("This request is essential to get location update continiously");
//                    builder1.create();
//                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ResolvableApiException resolvable = (ResolvableApiException) e;
//                            try {
//                                resolvable.startResolutionForResult(MainHomeActivity.this,
//                                        REQUEST_CHECK_SETTINGS);
//                            } catch (IntentSender.SendIntentException e1) {
//                                e1.printStackTrace();
//                            }
//                        }
//                    });
//                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(mContext, "Location update permission not granted", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    builder1.show();
//                }
//            }
//        });
//
//    }
}
