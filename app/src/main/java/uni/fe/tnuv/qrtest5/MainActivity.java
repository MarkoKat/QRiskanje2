package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //komentar za testirat github
    //Markotov komentar
    //branch test n
    //branch test d
    private DatabaseReference mDatabase;
    private ArrayList<LocationInfo> allLocations;
    private static final String TAG = "MainActivity";
    private String filename; //za lokalno shranjevanje podatkov o lokacijah
    private String filenameUser; //za shranjevanje podatkov o že najdenih QR kodah
    private TextView info;

    private SwipeRefreshLayout container;

    private Button listAllLocations;
    private ArrayList<LocationInfo> toBeFoundLocations;
    private Button listFoundLocations;
    private ArrayList<LocationInfo> foundLocations;

    private Boolean displayingAllLocations = true;
    private ArrayList<LocationInfo> displayingLocations;

    private ImageButton listButton;
    private ImageButton scanButton;
    private ImageButton mapButton;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 12;

    private FusedLocationProviderClient mFusedLocationClient;
    private Double currLat;
    private Double currLng;
    private Boolean foundLastLoc = false;
    private boolean prikazanSeznam = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DatotecniSistem datotecni = new DatotecniSistem();
        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        info = findViewById(R.id.info);
        listAllLocations = findViewById(R.id.all_locations);
        listFoundLocations = findViewById(R.id.found_locations);

        listButton = findViewById(R.id.button_list);
        scanButton = findViewById(R.id.scan_barcode);
        mapButton = findViewById(R.id.button_maps);

        // refresh od swipanju
        container = (SwipeRefreshLayout) findViewById(R.id.container);
        container.setOnRefreshListener(mOnRefreshListener);

        //samo zacasni tabeli
        String[][] tabela = {
                {"123456", "Fakulteta za elektrotehniko", "Namig, ki ga ni, ali pa ga samo ne vidiš", "46.044783", "14.489494"},
                {"111111", "Prešernov spomenik", "Tudi tu ni namioga", "46.051389", "14.506317"},
                {"000001", "Stari grad smlednik :D", "Za tisto sivo skalo", "46.165446", "14.442362"}};

        String[][] tabelaUser = {
                {"123456", "0"},
                {"111111", "0"},
                {"000001", "0"}
        };

        String tabelaUser2;
        /*
        //Pretvarjanje tabele v en String
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabela.length; i++) {
            StringBuilder tmp = new StringBuilder();
            for (int j = 0; j < tabela[i].length; j++) {
                tmp.append(tabela[i][j]).append("#");
            }
            sb.append(tmp).append("%");
        }


        datotecni.vpisiVDatoteko(filename, sb.toString());
        */







        //prebere podatke v allLocations iz sharedPreferences
        loadLocations();
        //Log.i(TAG, allLocations.toString());
        LocationInfo tmpLoc = new LocationInfo();
        Log.i(TAG, "Marko345" + allLocations.size());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allLocations.size(); i++) {

            String tmp = allLocations.get(i).getuID() + "#"
                    + allLocations.get(i).getIme() + "#"
                    + allLocations.get(i).getOpis() + "#"
                    + allLocations.get(i).getLat() + "#"
                    + allLocations.get(i).getLng() + "#" + "%";
            Log.i(TAG, "Marko345" + tmp);
            sb.append(tmp);

        }
        vpisiVDatoteko(filename, sb.toString());

        //Pretvarjanje enega stringa v tabelo
        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        //List<String> tabela4 = new ArrayList<String>();
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }
        Log.i(TAG, "Marko555" + tabela2);




        //Ugotavljanje prvega zagona aplikacije
        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            // first time task
            // Posodobitev tabele z najdenimi lokacijami
            StringBuilder sbUser = new StringBuilder();
            for (int i = 0; i < allLocations.size(); i++) {

                String tmpUser = allLocations.get(i).getuID() + "#" + "0" + "#" + "%";

                sbUser.append(tmpUser);

            }
            Log.i(TAG, "Marko222"+ sbUser.toString());
            vpisiVDatoteko(filenameUser, sbUser.toString());
        }
        else {
            // Posodobitev tabele z najdenimi lokacijami
            StringBuilder sbUser = new StringBuilder();

            String tabelaUser22 = beriIzDatoteke(filenameUser);
            String[] tabelaUser3 = tabelaUser22.split("%");
            String[][] tabelaUser4 = new String[tabelaUser3.length][2];
            for (int m = 0; m < tabelaUser3.length; m++) {
                String[] tabelaUserTMP = tabelaUser3[m].split("#");
                tabelaUser4[m] = tabelaUserTMP;
            }

            for (int i = 0; i < allLocations.size(); i++) {
                String tmpUser = "";
                //String tmpUser = allLocations.get(i).getuID() + "#" + "0" + "#" + "%";
                int ok = 0;
                for (int n = 0; n < tabelaUser4.length; n++) {
                    if(allLocations.get(i).getuID().equals(tabelaUser4[n][0])) {
                        tmpUser = allLocations.get(i).getuID() + "#" + tabelaUser4[n][1] + "#" + "%";
                        ok = 1;
                        break;
                    }
                }
                if (ok == 0) {
                    tmpUser = allLocations.get(i).getuID() + "#" + "0" + "#" + "%";
                }

                sbUser.append(tmpUser);
            }


            Log.i(TAG, "Marko222"+ sbUser.toString());
            vpisiVDatoteko(filenameUser, sbUser.toString());
        }


        // za bazo
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // se klice pri vsaki spremembi v bazi
                ArrayList<LocationInfo> currLocationsList = new ArrayList<>();
                for (DataSnapshot postSnapshot:  dataSnapshot.getChildren()){
                    for (DataSnapshot ds: postSnapshot.getChildren()){
                        LocationInfo currentLoc = new LocationInfo();
                        currentLoc.setIme(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getIme());
                        currentLoc.setOpis(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getOpis());
                        currentLoc.setLat(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLat());
                        currentLoc.setLng(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLng());
                        currentLoc.setuID(ds.getKey());

                        currLocationsList.add(currentLoc);
                    }
                }
                allLocations = currLocationsList;
                saveLocations();

                /** TUKAJ DODAJ DELITEV V ISKANE IN ZE NAJDENE LOKACIJE*/

                toBeFoundLocations = allLocations;
                foundLocations = allLocations;




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("napaka", "loadPost:onCancelled", databaseError.toException());
                Context context = getApplicationContext();
                Toast.makeText(context, "napaka", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addValueEventListener(postListener);

        //Pridobi lokacijo iz GPS
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
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

                                foundLastLoc = true;
                                if (!allLocations.isEmpty() && allLocations != null){
                                    updateLocationList();
                                }
                            }
                        }
                    });
        }


        //izpis iz shared Preferences v seznam
        if (!allLocations.isEmpty() && allLocations != null) {
            updateLocationList();
        }
    }

    // refresh od swipanju
    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if(!allLocations.isEmpty() && allLocations != null){
                updateLocationList();
            }
        }
    };

    // ko swipas updata list
    public void updateLocationList(){
        if (!prikazanSeznam){
            prikazanSeznam = true;
        }
        if(displayingAllLocations = true){
            displayingLocations = toBeFoundLocations;
        }
        else{
            displayingLocations = foundLocations;
        }

        // ce je dobljena lokacija, lahko vsaki lokaciji dodamo se distance
        if (foundLastLoc){
            for (int i = 0; i < allLocations.size(); i++) {
                Location loc1 = new Location("");
                loc1.setLatitude(currLat);
                loc1.setLongitude(currLng);

                Location loc2 = new Location("");
                loc2.setLatitude(allLocations.get(i).getLat());
                loc2.setLongitude(allLocations.get(i).getLng());

                float distanceInMeters = loc1.distanceTo(loc2);
                allLocations.get(i).setDist(distanceInMeters);
            }
        }

        ListView  mListView = (ListView) findViewById(R.id.list_view);
        LocationListAdapter  adapter = new LocationListAdapter(this, R.layout.adapter_location_view, displayingLocations);
        mListView.setAdapter(adapter);
        // OnClick na item na seznamu prikaze podrobnosti za lokacijo
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pridobimo lokacijo iz ArrayList na katero je uporabnik pritisnil
                LocationInfo selectedLocation = (LocationInfo) parent.getItemAtPosition(position);
                prikaziPodrobnosti(selectedLocation.getIme());
            }
        });

        // da izgine ikonca za osvezevanje
        if (container.isRefreshing()) {
            container.setRefreshing(false);
        }
    }

    // gumb plus v meniju v action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.add_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ko kliknes gumb plus v meniju v action bar se odpre activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddLocationActivity.class);
                //intent.putExtra("barcode", barcode);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Zagon Qr scannerja
    public void scanBarcode(View v) {
        //Intent intent = new Intent(this, ScanBarcodeActivity.class);
        //startActivityForResult(intent, 0);
        scanButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listButton.setBackgroundColor(Color.GRAY);

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

    // ko se konca skeniranje - prekines ali najdes lokacijo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null) {
                listButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                scanButton.setBackgroundColor(Color.GRAY);
                Toast.makeText(this, "Prekinili ste skeniranje", Toast.LENGTH_LONG).show();
            }
            else{
                listButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                scanButton.setBackgroundColor(Color.GRAY);
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("barcode", result.getContents());
                startActivity(intent);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    // prikaze zemlejvid
    public void showMaps(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        //intent.putExtra("barcode", barcode);
        startActivity(intent);
        mapButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listButton.setBackgroundColor(Color.GRAY);
    }

    //Za shranjevanje v datotečni sistem------------------------------------------------------------
    private void vpisiVDatoteko(String filenameLoc, String vsebina){
        try {
            //ustvarimo izhodni tok
            FileOutputStream os = openFileOutput(filenameLoc, Context.MODE_PRIVATE);
            //zapisemo posredovano vsebino v datoteko
            os.write(vsebina.getBytes());
            //sprostimo izhodni tok
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    // shrani lokacijo iz baze v sharedPreferences
    private void saveLocations(){
        if (!prikazanSeznam && (!allLocations.isEmpty() && allLocations != null)){
            prikazanSeznam = true;
            updateLocationList();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allLocations);

        editor.remove("locationList");
        editor.commit();

        editor.putString("locationList", json);
        editor.apply();

    }

    // prebere podatke iz sharedPreferences in jih vrne v obliki ArrayList<LocationInfo>
    private void loadLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("locationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);

        if (allLocations == null){
            allLocations = new ArrayList<>();
            foundLocations = new ArrayList<>();
            toBeFoundLocations = new ArrayList<>();
        }
        else{
            /** TUKAJ DODAJ DELITEV V ISKANE IN ZE NAJDENE LOKACIJE*/
            toBeFoundLocations = allLocations;
            // se prikaze samo prva lokacija v seznamu trenutno
            foundLocations = allLocations;
        }
        if (!prikazanSeznam && (!allLocations.isEmpty() && allLocations != null)){
            prikazanSeznam = true;
            updateLocationList();
        }
    }

    // ob kliku na lokacijo v seznamu se odpre aktivnost s podrobnostjo
    public void prikaziPodrobnosti(String imeLokacije) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ime_lokacije", imeLokacije);
        startActivity(intent);
    }

    // Prikaze seznam vseh se ne najdenih lokacij
    public void displayAllLocations(View view){
        displayingAllLocations = true;
        if(!allLocations.isEmpty() && allLocations != null){
            updateLocationList();
        }
        listAllLocations.setClickable(false);
        listFoundLocations.setClickable(true);
        listAllLocations.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listFoundLocations.setBackgroundColor(Color.GRAY);
    }

    // Prikaze seznam ze najdenih lokacij
    public void displayFoundLocations(View view){
        displayingAllLocations = false;
        if(!allLocations.isEmpty() && allLocations != null){
            updateLocationList();
        }
        listAllLocations.setClickable(true);
        listFoundLocations.setClickable(false);
        listFoundLocations.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listAllLocations.setBackgroundColor(Color.GRAY);
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


        listButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        scanButton.setBackgroundColor(Color.GRAY);
        mapButton.setBackgroundColor(Color.GRAY);
        if (!allLocations.isEmpty() && allLocations != null){
            updateLocationList();
        }

        // dodaj request za lokacijo vsakih 20 sekund (max 2 minuti)
        if (mFusedLocationClient != null) {
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(20000); // 10 second interval
            mLocationRequest.setFastestInterval(120000); // two minute interval
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                final String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};

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



    // ZA LOKACIJO

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                currLat = location.getLatitude();
                currLng = location.getLongitude();
                if (!foundLastLoc){
                    foundLastLoc = true;
                    if(!allLocations.isEmpty() && allLocations != null){
                        updateLocationList();
                    }
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
                    if(!allLocations.isEmpty() && allLocations != null){
                        updateLocationList();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if(!allLocations.isEmpty() && allLocations != null){
                        updateLocationList();
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
