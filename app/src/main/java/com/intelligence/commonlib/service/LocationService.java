package com.intelligence.commonlib.service;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service implements LocationListener {

    private LocationManager locationManager;
    public static final String LOCATION = "location";
    public static final String LOCATION_ACTION = "locationAction";

    public interface LocationChangeListener{
        void onChangeCity(String cityName);
    }

    public static ArrayList<LocationChangeListener> mLocationListener = new ArrayList<>();

    public static void setLocationListener(LocationChangeListener locationListener){
        if(mLocationListener != null){
            mLocationListener.add(locationListener);
        }
    }
    public static void removeLocationListener(){
        if(mLocationListener != null){
            mLocationListener.clear();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try {
            if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            } else if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        this);
            } else {
//                Toast.makeText(this, "无法定位", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            //to do nothing.
        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Intent intent = new Intent();
            intent.setAction(LOCATION_ACTION);
            intent.putExtra(LOCATION, location.toString());
            sendBroadcast(intent);
            double latitude = Double.parseDouble(location.toString().substring(17, 26));
            double longitude = Double.parseDouble(location.toString().substring(27, 37));
            String cityName = getaddress(latitude,longitude);
            for (LocationChangeListener locationChangeListener : mLocationListener) {
                locationChangeListener.onChangeCity(cityName);
            }
            locationManager.removeUpdates(this);
            stopSelf();
        } catch (Exception e) {
            //to do nothing.
        }
    }


    public String getaddress(double latitude, double longitude) {
        String cityName = "";
        List<Address> addList = null;
        Geocoder ge = new Geocoder(this);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);
                cityName += ad.getLocality();
            }
        }
        return cityName;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}