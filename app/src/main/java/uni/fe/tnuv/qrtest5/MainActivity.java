package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Collections;
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

    private EditText searchInput;
    private ImageButton stopSearchButton;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 12;

    private FusedLocationProviderClient mFusedLocationClient;
    private Double currLat;
    private Double currLng;
    private Boolean foundLastLoc = false;
    private boolean prikazanSeznam = false;
    private boolean prikazanPrazenSeznam = false;

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

        searchInput = findViewById(R.id.search_input);
        stopSearchButton = findViewById(R.id.stop_search);

        // refresh od swipanju
        container = (SwipeRefreshLayout) findViewById(R.id.container);
        container.setOnRefreshListener(mOnRefreshListener);

        String tabelaUser2;

        //prebere podatke v allLocations iz sharedPreferences
        loadLocations();
        LocationInfo tmpLoc = new LocationInfo();


        /*
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allLocations.size(); i++) {

            String tmp = allLocations.get(i).getuID() + "#"
                    + allLocations.get(i).getIme() + "#"
                    + allLocations.get(i).getOpis() + "#"
                    + allLocations.get(i).getLat() + "#"
                    + allLocations.get(i).getLng() + "#" + "%";
            //Log.i(TAG, "Marko345" + tmp);
            sb.append(tmp);

        }
        vpisiVDatoteko(filename, sb.toString());
        */
        posodobiLokalnoTabeloLokacij(); // Podatke o lokacijah shrani v lokalno tabelo

        //Pretvarjanje enega stringa v tabelo
        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        //List<String> tabela4 = new ArrayList<String>();
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }

        //Ugotavljanje prvega zagona aplikacije
        final String PREFS_NAME = "PrviZagonFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            // first time task
            // Prva nastavitev tabele z najdenimi lokacijami
            StringBuilder sbUser = new StringBuilder();
            for (int i = 0; i < allLocations.size(); i++) {
                String tmpUser = allLocations.get(i).getuID() + "#" + "0" + "#" + "%";
                sbUser.append(tmpUser);

            }
            vpisiVDatoteko(filenameUser, sbUser.toString());

            settings.edit().putBoolean("my_first_time", false).apply();
        }
        else {
            // Posodobitev tabele z najdenimi lokacijami
            posodobiLokalnoTabeloNajdenih();
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
                toBeFoundLocations = allLocations;
                foundLocations = allLocations;


                saveLocations();

                /** TUKAJ DODAJ DELITEV V ISKANE IN ZE NAJDENE LOKACIJE*/





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
                                updateLocationList();
                            }
                        }
                    });
        }

        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updateLocationList();
            }
        });


        //izpis seznama ce se ta se vedno ni zgodil
        updateLocationList();
    }

    // refresh od swipanju
    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            updateLocationList();
        }
    };

    // ko swipas updata list
    public void updateLocationList(){
        if (toBeFoundLocations != null && foundLocations != null){
            if (!prikazanSeznam){
                prikazanSeznam = true;
            }
            if(displayingAllLocations == true){
                displayingLocations = toBeFoundLocations;
                if (displayingLocations.isEmpty() && AppNetworkStatus.getInstance(getApplicationContext()).isOnline()){
                    info.setText("Nalagam lokacije...");
                }
                else if(displayingLocations.isEmpty()){
                    info.setText("Za prenos lokacij je potrebna internetna povezava!");
                }
            }
            else{
                displayingLocations = foundLocations;
                if (displayingLocations.isEmpty()){
                    info.setText("Odkril nisi še nobene lokacije. \nOdpravi se na pot in skeniraj QR kode!");
                }
            }

            if (displayingLocations.isEmpty()){
                prikazanPrazenSeznam = true;
            }
            else{
                info.setText("");
            }


            // ce je kaj v searchinputu se naredi filter kar je notr

            if (searchInput.getText().toString() != "" && !displayingLocations.isEmpty()){
                String filter = searchInput.getText().toString();
                ArrayList<LocationInfo> filteredLocations = new ArrayList<>();
                for (int i = 0; i < displayingLocations.size(); i++) {
                    if(displayingLocations.get(i).getIme().toLowerCase().contains(filter.toLowerCase())){
                        filteredLocations.add(displayingLocations.get(i));
                    }
                }
                displayingLocations = filteredLocations;
                if (filteredLocations.isEmpty()) {
                    info.setText("Ne najdem lokacij!");
                }
            }



            // ce je dobljena lokacija, lahko vsaki lokaciji dodamo se distance
            if (foundLastLoc){
                for (int i = 0; i < displayingLocations.size(); i++) {
                    Location loc1 = new Location("");
                    loc1.setLatitude(currLat);
                    loc1.setLongitude(currLng);

                    Location loc2 = new Location("");
                    loc2.setLatitude(displayingLocations.get(i).getLat());
                    loc2.setLongitude(displayingLocations.get(i).getLng());

                    float distanceInMeters = loc1.distanceTo(loc2);
                    displayingLocations.get(i).setDist(distanceInMeters);
                }

                // razvrsti po razdalji od najmanjse do najvecje
                Collections.sort(displayingLocations, new LocationDistanceComparator());
            }
            else{
                // ce ni lokacije razvrsti po imenu
                Collections.sort(displayingLocations, new LocationNameComparator());
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
            // Posodobitev podatkov o lokacijah v datotecnem sistemu
            posodobiLokalnoTabeloLokacij();
            posodobiLokalnoTabeloNajdenih();
        }
        else{
            info.setText("Nalagam lokacije...");
        }


        // da izgine ikonca za osvezevanje
        if (container.isRefreshing()) {
            container.setRefreshing(false);
        }


    }

    // gumba plus in search v meniju v action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
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
            case R.id.action_search:
                if (searchInput.getVisibility() != View.VISIBLE) {
                    searchInput.setVisibility(View.VISIBLE);
                    stopSearchButton.setVisibility(View.VISIBLE);
                }
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

    // Upravljanje rezultata skeniranja QR kode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null) {
                listButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                scanButton.setBackgroundColor(Color.GRAY);
                Toast.makeText(this, getResources().getString(R.string.strPrekinjenoSkeniranje), Toast.LENGTH_LONG).show();
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

    // Prehod na aktivnost z zemljevidom
    public void showMaps(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
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
        if (prikazanPrazenSeznam && (!toBeFoundLocations.isEmpty() || !foundLocations.isEmpty())){
            prikazanPrazenSeznam = false;
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
        if (!prikazanSeznam){
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
        updateLocationList();
        listAllLocations.setClickable(false);
        listFoundLocations.setClickable(true);
        listAllLocations.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listFoundLocations.setBackgroundColor(Color.GRAY);
    }

    // Prikaze seznam ze najdenih lokacij
    public void displayFoundLocations(View view){
        displayingAllLocations = false;
        updateLocationList();
        listAllLocations.setClickable(true);
        listFoundLocations.setClickable(false);
        listFoundLocations.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        listAllLocations.setBackgroundColor(Color.GRAY);
    }

    // nastavi search bar na "gone"
    public void stopSearch(View view){
        searchInput.setVisibility(View.GONE);
        stopSearchButton.setVisibility(View.GONE);

        searchInput.setText("");
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

        updateLocationList();

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
                    updateLocationList();
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
                        updateLocationList();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    updateLocationList();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void posodobiLokalnoTabeloLokacij() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allLocations.size(); i++) {

            String tmp = allLocations.get(i).getuID() + "#"
                    + allLocations.get(i).getIme() + "#"
                    + allLocations.get(i).getOpis() + "#"
                    + allLocations.get(i).getLat() + "#"
                    + allLocations.get(i).getLng() + "#" + "%";
            sb.append(tmp);
        }
        vpisiVDatoteko(filename, sb.toString());
    }

    private void posodobiLokalnoTabeloNajdenih() {
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

        vpisiVDatoteko(filenameUser, sbUser.toString());
    }
}
