package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddLocationActivity extends AppCompatActivity {


    private DatabaseReference mDatabase;
    private EditText ime;
    private EditText opis;
    private EditText lat;
    private EditText lng;
    private String idL;
    final static int MAX_LENGTH = 20;

    private TextView izpis;

    private ArrayList<LocationInfo> allLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");*/
        ime = (EditText) findViewById(R.id.ime);
        opis = (EditText) findViewById(R.id.opis);
        lat = (EditText) findViewById(R.id.lat);
        lng = (EditText) findViewById(R.id.lng);

        izpis = (TextView) findViewById(R.id.izpis);

        loadLocations();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void poslji(View view){

        //String idLok = new Integer(idL).toString();

        // naredi naključek string dolg MAX_LENGTH = 20
        Random r = new Random();

        String alphabet = "12345678901234567890abcdefghijklmnoprqstuvzxywABCDEFGHIJKLMNOPRQSTUVZXYW";
        String randomString = "";
        for (int i = 0; i < 20; i++) {
            randomString += alphabet.charAt(r.nextInt(alphabet.length()));
        }
        idL = randomString;

        writeNewLokacija(idL ,ime.getText().toString(), opis.getText().toString(), Float.valueOf(lat.getText().toString()), Float.valueOf(lng.getText().toString()));
    }

    private void writeNewLokacija(String lokacijaId, String ime, String opis, float lat, float lng) {
        LocationInfo lok = new LocationInfo();
        lok.setIme(ime);
        lok.setOpis(opis);
        lok.setLat(lat);
        lok.setLng(lng);

        mDatabase.child("lokacija").child(lokacijaId).setValue(lok);

        Context context = getApplicationContext();
        Toast.makeText(context, "Upšeno poslano", Toast.LENGTH_SHORT).show();
        loadLocations();
    }
    /*@Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();

                Collection<Object> values = td.values();

                HashMap<String, Object> result =(HashMap<String,Object>) dataSnapshot.getValue();


                allLocations.clear();
                for (DataSnapshot postSnapshot:  dataSnapshot.getChildren()){
                    for (DataSnapshot ds: postSnapshot.getChildren()){
                        LocationInfo currentLoc = new LocationInfo();
                        currentLoc.setIme(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getIme());
                        currentLoc.setOpis(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getOpis());
                        currentLoc.setLat(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLat());
                        currentLoc.setLng(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLng());
                        currentLoc.setuID(ds.getKey());

                        allLocations.add(currentLoc);
                    }
                }

                saveLocations();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("napaka", "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Context context = getApplicationContext();
                Toast.makeText(context, "napaka", Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mDatabase.addValueEventListener(postListener);

    }*/

   /* private void saveLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allLocations);

        String izpisano1 = "\n   POTEM    \n";
        for(LocationInfo loc : allLocations) {
            izpisano1 += loc.getAll();
        }
        izpis.setText(izpisano1);

        editor.remove("locationList");
        editor.commit();
        editor.clear().commit();



        editor.putString("locationList", json);
        editor.apply();

    }*/

    private void loadLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("locationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);


        if (allLocations == null){
            allLocations = new ArrayList<>();
        }

        String izpisano = "\n   VSE LOKACIJE   \n";
        for(LocationInfo loc : allLocations) {
            izpisano += loc.getAll();
        }
        izpis.setText(izpisano);
    }

}
