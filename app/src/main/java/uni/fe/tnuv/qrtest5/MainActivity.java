package uni.fe.tnuv.qrtest5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
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
    TextView barcodeResult;

    private SwipeRefreshLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DatotecniSistem datotecni = new DatotecniSistem();
        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        barcodeResult = (TextView)findViewById(R.id.barcode_result2);
        barcodeResult.setText("Tu bo prikazan rezultat");

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

        // refresh od swipanju
        container = (SwipeRefreshLayout) findViewById(R.id.container);
        container.setOnRefreshListener(mOnRefreshListener);

        // izpis iz shared Preferences v seznam
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
        ListView  mListView = (ListView) findViewById(R.id.list_view);
        LocationListAdapter  adapter = new LocationListAdapter(this, R.layout.adapter_location_view, allLocations);
        mListView.setAdapter(adapter);
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

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0) {
            if(resultCode == CommonStatusCodes.SUCCESS) {
                if(data!=null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    Intent intent = new Intent(this, ResultActivity.class);
                    intent.putExtra("barcode", barcode.displayValue);
                    startActivity(intent);

                    /*Barcode barcode = data.getParcelableExtra("barcode");
                    Log.i(TAG, "Koda2: " + barcode.displayValue);
                    barcodeResult.setText(barcode.displayValue);*/
                /*}
                else {
                    barcodeResult.setText("QR koda ni bila najdena");
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null) {
                Toast.makeText(this, "Prekinili ste skeniranje", Toast.LENGTH_LONG).show();
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

    public void showMaps(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        //intent.putExtra("barcode", barcode);
        startActivity(intent);
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
        }
    }
}
