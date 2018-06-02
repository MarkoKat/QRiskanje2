package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static String[][] tabela;
    public static String[][] tabelaUser;

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 12;
    private String filename;
    private String filenameUser;

    private boolean mapReady = false;

    private String LONGITUDE = "longitude";
    private String LATITUDE = "latitude";
    private String ZOOM = "zoom";
    private String BEARING = "bearing";
    private String TILT = "tilt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LinearLayout pickActivity = (LinearLayout) findViewById(R.id.pick_activity);
        pickActivity.bringToFront();

        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LONGITUDE = getResources().getString(R.string.strLongitude);
        LATITUDE = getResources().getString(R.string.strLatitude);
        ZOOM = getResources().getString(R.string.strZoom);
        BEARING = getResources().getString(R.string.strBearing);
        TILT = getResources().getString(R.string.strTilt);

        //Branje lokacij iz datotecnega sistema
        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }
        tabela = tabela4;

        // Branje tabele najdenih lokacij iz datotecnega sistema
        String tabelaUser2 = beriIzDatoteke(filenameUser);
        String[] tabelaUser3 = tabelaUser2.split("%");
        String[][] tabelaUser4 = new String[tabelaUser3.length][2];
        for (int i = 0; i < tabelaUser3.length; i++) {
            String[] tabelaUserTMP = tabelaUser3[i].split("#");
            tabelaUser4[i] = tabelaUserTMP;
        }
        tabelaUser = tabelaUser4;

        // Pridobivanje podatka o trenutni lokaciji + preverjanje dovoljenj
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
                return;
            }

            final Activity thisActivity = this;

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(thisActivity, permissions,
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            };
        } else {
            // Permission has already been granted
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                // Ko je aplikacija ponovno zagnana
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                                //mMap.animateCamera(cameraUpdate);
                            }
                        }
                    });
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mapReady = true; // Za preprecevanje napake ob prvem zagonu

        // Pridobivanje podatkov o zadnji poziciji zemljevida
        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);

        double latitude = sharedPrefs.getFloat(LATITUDE, 0);
        double longitude = sharedPrefs.getFloat(LONGITUDE, 0);
        LatLng target = new LatLng(latitude, longitude);

        float zoom = sharedPrefs.getFloat(ZOOM, 0);
        float bearing = sharedPrefs.getFloat(BEARING, 0);
        float tilt = sharedPrefs.getFloat(TILT, 0);

        CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

        mMap.moveCamera(update);

        // Prikaz uporabnikove lokacije na zemljevidu (modra pika)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
        else{
            mMap.setMyLocationEnabled(true);
        }

        postaviMarkerje();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                prikaziPodrobnosti(marker.getTitle());
            }
        });
    }

    // Zagon skenerja QR kod
    public void scanBarcode(View v) {

        final Activity activity = this;
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(false);

        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // Upravljanje rezultata skeniranja QR kode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null) {
                Toast.makeText(this, getResources().getString(R.string.strPrekinjenoSkeniranje), Toast.LENGTH_LONG).show();
            }
            else{
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("barcode", result.getContents());
                startActivity(intent);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    // Prehod na aktivnost s seznamom lokacij
    public void showList(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Prikaz podrobnosti lokacije ob kliku na ime nad markerjem
    public void prikaziPodrobnosti(String imeLokacije) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ime_lokacije", imeLokacije);
        startActivity(intent);
    }

    // Upravljenje z dovoljenjem za uporabo lokacije
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
                return;
            }
        }
    }

    private String beriIzDatoteke(String filenameLoc){
        FileInputStream inputStream;

        File file = new File(getFilesDir(), filenameLoc);
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            inputStream = openFileInput(filenameLoc);
            inputStream.read(bytes);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String vsebina = new String(bytes);

        return vsebina;
    }

    private void postaviMarkerje() {
        for(int i = 0; i < tabela.length; i++) {

            if(tabelaUser[i][1].equals("1")) {
                Marker newmarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(tabela[i][3]), Double.parseDouble(tabela[i][4])))
                        .title(tabela[i][1])
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                newmarker.setTag(0);
            }
            else {
                Marker newmarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(tabela[i][3]), Double.parseDouble(tabela[i][4])))
                        .title(tabela[i][1])
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                newmarker.setTag(0);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Shranjevanje trenutne pozicije zemljevida
        if(mapReady) {
            try{
                SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPrefs.edit();
                CameraPosition position = mMap.getCameraPosition();

                editor.putFloat(LATITUDE, (float) position.target.latitude);
                editor.putFloat(LONGITUDE, (float) position.target.longitude);
                editor.putFloat(ZOOM, position.zoom);
                editor.putFloat(TILT, position.tilt);
                editor.putFloat(BEARING, position.bearing);
                editor.apply();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
