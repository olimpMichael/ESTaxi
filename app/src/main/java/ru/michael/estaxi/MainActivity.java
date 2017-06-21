package ru.michael.estaxi;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static String LOG_TAG = "EsTaxi";
    public static Context context;
    private String path = "http://test.www.estaxi.ru/route.txt";
    private GoogleMap mMap;
    DownloadTask dlTask;
    ArrayList<HashMap<String, Double>> coordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = getBaseContext();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    public void onClick (View view) {
        switch (view.getId()){
            case R.id.button:
                //download file from server
                dlTask = new DownloadTask();
                dlTask.execute(path);

                //get result
                getResultFromFile();
                break;
        }
    }

    protected void getResultFromFile(){
        try{
            Double maxLat = null, minLat = null, minLon = null, maxLon = null;
            ArrayList<HashMap<String, Double>> coordsList = dlTask.get();
            if (coordsList!=null){
                PolylineOptions polyLine = new PolylineOptions();
                polyLine.color(Color.MAGENTA).width(3);

                // set markers on Map
                HashMap<String, Double> hashMapA = coordsList.get(0);
                Double lat_valueA = hashMapA.get("lat");
                Double lon_valueA = hashMapA.get("lon");

                HashMap<String, Double> hashMapB = coordsList.get(coordsList.size()-1);
                Double lat_valueB = hashMapB.get("lat");
                Double lon_valueB = hashMapB.get("lon");

                LatLng A = new LatLng(lat_valueA, lon_valueA);
                mMap.addMarker(new MarkerOptions().position(A).title("A"));
                LatLng B = new LatLng(lat_valueB, lon_valueB);
                mMap.addMarker(new MarkerOptions().position(B).title("B"));


                // draw polyLine
                for (HashMap<String, Double> hashMap : coordsList){
                    Double lat_value = hashMap.get("lat");
                    Double lon_value = hashMap.get("lon");
                    polyLine.add(new LatLng(lat_value,lon_value));

                    if (maxLat == null || maxLat < lat_value) {
                        maxLat = lat_value;
                    }

                    if (minLat == null || minLat > lat_value){
                        minLat = lat_value;
                    }

                    if (maxLon == null || maxLon < lon_value){
                        maxLon = lon_value;
                    }

                    if (minLon == null || minLon > lon_value){
                        minLon = lon_value;
                    }
                }
                mMap.addPolyline(polyLine);
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(maxLat, maxLon));
            builder.include(new LatLng(minLat, minLon));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(),30);
            mMap.animateCamera(cameraUpdate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
