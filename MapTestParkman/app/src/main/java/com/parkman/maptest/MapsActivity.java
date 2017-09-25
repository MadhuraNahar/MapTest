package com.parkman.maptest;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.parkman.maptest.model.LocationData;
import com.parkman.maptest.model.location_data;
import com.parkman.maptest.model.zones;
import com.parkman.maptest.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    LatLngBounds bounds;
    private Marker customMarker;
    View markerView;
    String jsonResponse;
    Gson gson;
    LocationData locationData;
    String current_location, currentLatitude, currentLongitude;
    LatLng currentLatLng;
    ArrayList<zones> zonesList;
    PolygonOptions options;
    ArrayList<String> polygonList;
    String priceCurrency = "\u20ac";
    TextView markerText;

    List<LatLng> latLngList;
    List<List<LatLng>> listOfAllLatsLngs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        parseMapData();
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initializeUiSettings();
        setUpCustomMarker();
        loadMapView();
        getZonesData();

        if(currentLatitude != null && currentLongitude != null){
            currentLatLng = new LatLng(Double.valueOf(currentLatitude), Double.valueOf(currentLongitude));
        }

        customMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("Park here")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, markerView))));

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(bounds != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
                }

            }
        });

        // Set listeners for click events.

        mMap.setOnPolygonClickListener(this);
        mMap.setOnCameraChangeListener(this);
    }

    private void getZonesData() {
        if(locationData == null)
            return;

        String[] polygonSingle = new String[0];

        zonesList = locationData.getLocation_data().getZones();

        polygonList = new ArrayList<>();
        listOfAllLatsLngs = new ArrayList<List<LatLng>>();
        for(int i = 0;i < zonesList.size(); i++){
            polygonList.add(zonesList.get(i).getPolygon());
        }

        for (int i = 0; i < polygonList.size(); i++){
            options = new PolygonOptions();
            polygonSingle = polygonList.get(i).split(",");

            latLngList = new ArrayList<LatLng>();

            for (int j = 0; j < polygonSingle.length; j++) {
                String[] latLngPoly = polygonSingle[j].trim().split("\\s+");
                latLngList.add(new LatLng(Double.valueOf(latLngPoly[0].trim()), Double.valueOf(latLngPoly[1].trim())));

                options.add(new LatLng(Double.valueOf(latLngPoly[0].trim()), Double.valueOf(latLngPoly[1].trim())));
            }

            listOfAllLatsLngs.add(latLngList);
            Polygon polygon = mMap.addPolygon(options);
            if((zonesList.get(i).getPayment_is_allowed()).equals("1")){
                polygon.setFillColor(getResources().getColor(R.color.green));
                polygon.setStrokeColor(Color.BLACK);
                polygon.setStrokeWidth(2.0f);
            } else if((zonesList.get(i).getPayment_is_allowed()).equals("0")){
                polygon.setFillColor(getResources().getColor(R.color.gray));
                polygon.setStrokeColor(Color.BLACK);
                polygon.setStrokeWidth(2.0f);
            }

        }
        System.out.println("All Latlng list size is: "+listOfAllLatsLngs.size());
        for (int i = 0; i<listOfAllLatsLngs.size();i++){
            for (int j = 0; j<latLngList.size();j++){
               Log.d("List at "+i, "Latitude: "+ latLngList.get(j).latitude+ " Longitude: "+latLngList.get(j).longitude);
            }

        }
    }

    private void parseMapData() {

        try {
            jsonResponse = Utils.loadJSONFromAsset("parkman_json.txt",MapsActivity.this);
            gson = new Gson();
            locationData = gson.fromJson(jsonResponse, LocationData.class);
            getCurrentLocation();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCurrentLocation() {
        if(locationData == null)
            return;

        current_location = locationData.getCurrent_location();
        String[] currLocation = current_location.split(",");
        Log.d("Latitude: "+currLocation[0], "Longitude: "+currLocation[1]);
        currentLatitude = currLocation[0].trim();
        currentLongitude = currLocation[1].trim();
    }

    private void loadMapView() {
        if(locationData == null)
            return;

        location_data.bounds json_bounds = locationData.getLocation_data().getBounds();
        String north = json_bounds.getNorth();
        String east = json_bounds.getEast();
        String south = json_bounds.getSouth();
        String west = json_bounds.getWest();

        LatLng southWest = new LatLng(Double.valueOf(south), Double.valueOf(west));
        LatLng northEast = new LatLng(Double.valueOf(north), Double.valueOf(east));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(southWest);
        builder.include(northEast);
        bounds = builder.build();
    }


    private void setUpCustomMarker() {
        markerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        markerText = (TextView)markerView.findViewById(R.id.marker_txt);
        markerText.setText("0.00"+ priceCurrency);
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void initializeUiSettings() {

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }


    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(Action.TYPE_VIEW,"Maps Page", Uri.parse("http://host/path"),
                Uri.parse("android-app://com.parkman.maptest/http/host/path"));
        AppIndex.AppIndexApi.start(client, viewAction);

    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(Action.TYPE_VIEW,"Maps Page", Uri.parse("http://host/path"),
                Uri.parse("android-app://com.parkman.maptest/http/host/path"));
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onCameraChange(CameraPosition position) {

       if(customMarker != null){
           customMarker.remove();
       }
        customMarker = mMap.addMarker(new MarkerOptions()
                .position(position.target)
                .title("Title")
                .snippet("Description")
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, markerView))));
        customMarker.setPosition(position.target);


        for (int i = 0; i<listOfAllLatsLngs.size();i++){
            /*for (int j = 0; j<latLngList.size();j++){
                Log.d("List at "+i, "Latitude: "+ latLngList.get(j).latitude+ " Longitude: "+latLngList.get(j).longitude);
            }*/
            boolean containsPolygon = PolyUtil.containsLocation(position.target,listOfAllLatsLngs.get(i),false);

            System.out.println(containsPolygon);
            if(containsPolygon == true){
//                Polygon polygon = mMap.addPolygon(new PolygonOptions().addAll(listOfAllLatsLngs.get(i)));
//                polygon.setFillColor(Color.RED);
                Log.d("Marker in Polygon: "+i, "Service price: " +zonesList.get(i).getService_price());
                markerText.setText(zonesList.get(i).getService_price() + zonesList.get(i).getCurrency());
                customMarker.setTitle(zonesList.get(i).getName()+" : " +zonesList.get(i).getProvider_name());
                customMarker.setSnippet(zonesList.get(i).getName()+" : " +zonesList.get(i).getProvider_name());
            }

        }
    }

}
