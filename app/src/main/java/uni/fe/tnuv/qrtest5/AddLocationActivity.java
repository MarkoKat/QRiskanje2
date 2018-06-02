package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddLocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseReference mDatabase;

    private EditText imeET;
    private EditText opisET;
    private EditText latET;
    private EditText lngET;
    private CheckBox manualGps;
    private TextView qrCode;
    private Button downloadQr;

    private String uID;
    private Double currLat;
    private Double currLng;

    private Boolean foundLastLoc = false;

    final static int MAX_LENGTH = 20;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 12;

    private ArrayList<LocationInfo> allLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // gumb za nazaj
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // povezava z bazo
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //pridobi EditText it layout-a
        imeET = findViewById(R.id.ime);
        opisET = findViewById(R.id.opis);
        latET = findViewById(R.id.lat);
        lngET = findViewById(R.id.lng);
        manualGps = findViewById(R.id.use_gps);
        qrCode = findViewById(R.id.qr_code);
        downloadQr = findViewById(R.id.download_qr_code);

        // preberi vse lokacije shranjene v sharedPreferences
        loadLocations();

        //Pridobi lokacijo iz GPS
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                manualGps.setEnabled(false);
                manualGps.setText(manualGps.getText().toString()+" - Za vklop omogočite lokacijo");
                manualGps.setChecked(false);
                setUseGpsLocation(manualGps.isChecked());
                foundLastLoc = false;
        }
        else{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                currLat = location.getLatitude();
                                currLng = location.getLongitude();

                                manualGps.setChecked(true);
                                setUseGpsLocation(manualGps.isChecked());
                                foundLastLoc = true;
                            }
                            else{
                                manualGps.setEnabled(false);
                                manualGps.setText(manualGps.getText().toString()+" - Za vklop omogočite lokacijo");
                                manualGps.setChecked(false);
                                setUseGpsLocation(manualGps.isChecked());
                                foundLastLoc = false;
                            }
                        }
                    });
        }


        manualGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setUseGpsLocation(isChecked);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        //odstrani request za lokacijo
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // dodaj request za lokacijo vsakih 20 sekund (max 2 minuti)
        if (mFusedLocationClient != null) {
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(20000); // 10 second interval
            mLocationRequest.setFastestInterval(120000); // two minute interval
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        }
    }

    // Za gumb nazaj
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // funckija poslje lokacijo v bazo
    public void poslji(View view){

        // preveri ce so izpolnjeni vsi podatki
        if (imeET.getText().toString().matches("") || opisET.getText().toString().matches("") || latET.getText().toString().matches("") || lngET.getText().toString().matches("") ){
            Toast.makeText(getApplicationContext(), "Prosimo izpolnite vse podatke!", Toast.LENGTH_SHORT).show();
        }
        else{
            if (Math.abs(Double.valueOf(latET.getText().toString())) > 90 || Math.abs(Double.valueOf(lngET.getText().toString())) > 180) {
                Toast.makeText(getApplicationContext(), "Prosimo vnesite veljavne koordinate!", Toast.LENGTH_SHORT).show();
            }
            else{
                // preveri ce uID ze obstaja v bazi, ki jo ima
                Boolean idAlreadyExists = true;
                while (idAlreadyExists){
                    // funckija generira in vrne random string sestavljen iz črk in velikih ter malih črk
                    uID = generateuID();
                    idAlreadyExists = false;
                    for (int i = 0; i<allLocations.size(); i++){
                        String currId = allLocations.get(i).getuID();
                        if(uID.matches(currId)){
                            idAlreadyExists = true;
                        }
                    }
                }
                // zapis v bazo
                writeNewLokacija(uID ,imeET.getText().toString(), opisET.getText().toString(), Float.valueOf(latET.getText().toString()), Float.valueOf(lngET.getText().toString()));
            }
        }
    }

    private void writeNewLokacija(String lokacijaId, String ime, String opis, float lat, float lng) {
        LocationInfo lok = new LocationInfo();
        lok.setIme(ime);
        lok.setOpis(opis);
        lok.setLat(lat);
        lok.setLng(lng);

        // Preveri ce je na voljo povezava z internetom, ce je shrani lokacijo na internet
        if (AppNetworkStatus.getInstance(getApplicationContext()).isOnline()) {
            /** Internet is available */
            mDatabase.child("lokacija").child(lokacijaId).setValue(lok);

            Toast.makeText(getApplicationContext(), "Nova lokacija je bila upšeno poslana!", Toast.LENGTH_LONG).show();

            //ponastavimo vsa vnosna polja
            imeET.setText(null);
            opisET.setText(null);
            if (!manualGps.isChecked()){
                latET.setText(null);
                lngET.setText(null);
            }

            qrCode.setText("ID LOKACIJE: "+lokacijaId+"\nIME LOKACIJE: "+ime+"\nKOORDINATE: "+lat+"  "+lng);
            qrCode.setVisibility(View.VISIBLE);
            downloadQr.setVisibility(View.VISIBLE);
            downloadQr.setClickable(true);



            loadLocations();
        } else {
            /** Internet is NOT available */
            Toast.makeText(getApplicationContext(), "Internetna povezava ni na voljo - lokacija ni bila poslana", Toast.LENGTH_LONG).show();
        }

    }

    // prebere lokacije iz shrared preferences
    private void loadLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("locationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);

        if (allLocations == null){
            allLocations = new ArrayList<>();
        }
    }

    private String generateuID(){
        // naredi naključek string dolg MAX_LENGTH = 20
        Random r = new Random();
        String alphabet = "12345678901234567890123456789012345678901234567890abcdefghijklmnoprqstuvzxywABCDEFGHIJKLMNOPRQSTUVZXYW";
        String randomString = "";
        for (int i = 0; i < MAX_LENGTH; i++) {
            randomString += alphabet.charAt(r.nextInt(alphabet.length()));
        }
        return randomString;
    }

    // odpre se stran za prenos QR kode
    public void prenesiQr(View view){
        String url = "https://chart.googleapis.com/chart?cht=qr&chl="+uID+"&chs=500x500&choe=UTF-8&chld=L%7C2";
        Intent website = new Intent(Intent.ACTION_VIEW);
        website.setData(Uri.parse(url));
        startActivity(website);
    }

    private void setUseGpsLocation(Boolean checked){
        if (checked){
            latET.setEnabled(false);
            lngET.setEnabled(false);
            if (currLat != null || currLng != null){
                latET.setText(currLat.toString());
                lngET.setText(currLng.toString());
            }
        }
        else{
            latET.setEnabled(true);
            lngET.setEnabled(true);
            latET.setText(null);
            lngET.setText(null);
        }
    }




    // ZA LOKACIJO

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (!foundLastLoc){
                manualGps.setEnabled(true);
                manualGps.setText("Uporabi trenutno lokacijo");
                manualGps.setChecked(true);
                foundLastLoc = true;
            }
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                currLat = location.getLatitude();
                currLng = location.getLongitude();
                if (manualGps.isChecked()){
                    latET.setText(currLat.toString());
                    lngET.setText(currLng.toString());
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    manualGps.setEnabled(true);
                    manualGps.setText("Uporabi trenutno lokacijo");
                    manualGps.setChecked(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    manualGps.setEnabled(false);
                    manualGps.setText(manualGps.getText().toString()+" - Za vklop omogočite lokacijo");
                    manualGps.setChecked(false);
                    setUseGpsLocation(manualGps.isChecked());
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


}
