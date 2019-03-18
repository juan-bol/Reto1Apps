package com.appmoviles.reto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.appmoviles.reto1.DialogFragMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DialogFragMarker.DialogFragMarkerActions {

    private static final int REQUEST_CODE = 11;
    private GoogleMap mMap;
    private LocationManager manager;

    private Marker me;
    private List<Marker> markers;
    private LatLng latLngMarcador;

    private FloatingActionButton fab_limpiar;
    private TextView tv_box;

    private boolean primerAcercamiento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        markers = new ArrayList<Marker>();
        primerAcercamiento=true;

        tv_box = (TextView) findViewById(R.id.tv_box);
        fab_limpiar = findViewById(R.id.fab_limpiar);
        fab_limpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Marker marker : markers) {
                    marker.remove();
                }
                markers.clear();
                Toast.makeText(MapsActivity.this, "Se limpiaron todos los marcadores de lugares", Toast.LENGTH_LONG).show();
                calcularDistancias();
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                openDialogMarker();
                latLngMarcador = latLng;
            }
        });

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, "Not Enough Permission", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            //Agregar el listener de ubicacion
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        moving(location);
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
                });
            } else if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        moving(location);
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
                });
            }
        }

    }

    public void openDialogMarker() {
        DialogFragMarker dialogo = new DialogFragMarker();
        dialogo.show(getSupportFragmentManager(), "Dialogo Marcador");
    }

    @Override
    public void agregarMarcador(String nombreMarcador) {
        if (nombreMarcador == null || nombreMarcador.equals("")) {
            Toast.makeText(MapsActivity.this, "El nombre del marcador no debe ser nulo", Toast.LENGTH_SHORT).show();
        } else {
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLngMarcador).title(nombreMarcador));
            Location location = null;
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = new Location(LocationManager.NETWORK_PROVIDER);
            } else if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = new Location(LocationManager.GPS_PROVIDER);
            }
            location.setAltitude(latLngMarcador.latitude);
            location.setLatitude(latLngMarcador.longitude);
            markers.add(marker);
            calcularDistancias();
        }

    }

    public String getAddress(Location location) {
        String direccion = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addressList.get(0);
            direccion = address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return direccion;
    }

    public void moving(Location location) {
        String msj = "LAT: " + location.getLatitude() + " , LONG: " + location.getLongitude();
        Log.e(">>>", "LAT: " + location.getLatitude() + " , LONG: " + location.getLongitude());
        Toast.makeText(MapsActivity.this, msj, Toast.LENGTH_LONG).show();

        if (me != null) me.remove();
        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        me = mMap.addMarker(new MarkerOptions().position(myPosition)
                .title("Me").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        me.setSnippet("Tu ubicación:" + getAddress(location));

        if(primerAcercamiento){
            moveToCurrentLocation(myPosition);
            primerAcercamiento=false;
        }
        calcularDistancias();
    }


    private void moveToCurrentLocation(LatLng currentLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);


    }

    public void calcularDistancias(){
        float menor=-1;
        Marker masCercano = null;
        for (Marker marker: markers){
            if (marker!=me){
                LatLng markerLatLng = marker.getPosition();
                Location markerLocation = new Location(LocationManager.NETWORK_PROVIDER);
                markerLocation.setLatitude(markerLatLng.latitude);
                markerLocation.setLongitude(markerLatLng.longitude);
                LatLng myLatLng = me.getPosition();
                Location myLocation = new Location(LocationManager.NETWORK_PROVIDER);
                myLocation.setLatitude(myLatLng.latitude);
                myLocation.setLongitude(myLatLng.longitude);

                float distance = myLocation.distanceTo(markerLocation)/1000;
                marker.setSnippet("Usted se encuentra a "+distance+" Kms");
                if(menor==-1 || distance<menor) {
                    menor = distance;
                    masCercano = marker;
                }
            }
        }
        String mensaje="";
        if(masCercano==null){
            mensaje="Agrega marcadores tocando cualquier parte del mapa :3";
        }
        else {
            if(menor<3){ //TODO
                mensaje = "El lugar más cercano es "+masCercano.getTitle()+" a "+menor+" Kms";
            }
            else {
                mensaje = "El lugar más cercano es "+masCercano.getTitle();
            }
        }
        tv_box.setText(mensaje);
    }
}

