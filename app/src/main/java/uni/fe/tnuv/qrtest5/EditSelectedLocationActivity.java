package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class EditSelectedLocationActivity extends AppCompatActivity {

    private ArrayList<LocationInfo> allLocations;
    private ArrayList<LocationInfo> myLocations;

    private String lokacijaId;
    private String ime;
    private String naslov;
    private String opis;
    private String namig;
    private float lat;
    private float lng;

    private TextView idTV;
    private EditText imeET;
    private EditText naslovET;
    private EditText opisET;
    private EditText namigET;
    private EditText latET;
    private EditText lngET;

    private int indexLoc;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_selected_location);

        // gumb za nazaj
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pridobivanje podatkov o dodani lokaciji
        Bundle bundle = getIntent().getExtras();
        lokacijaId = bundle.getString("id_lokacije");

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("locationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);

        Log.d("EDIT", lokacijaId);
        for (int i = 0; i<allLocations.size(); i++){
            if(allLocations.get(i).getuID().equals(lokacijaId)){
                indexLoc = i;
                ime = allLocations.get(i).getIme();
                naslov = allLocations.get(i).getNaslov();
                opis = allLocations.get(i).getOpis();
                namig = allLocations.get(i).getNamig();
                lat = allLocations.get(i).getLat();
                lat = allLocations.get(i).getLng();
            }
        }

        //pridobi EditText it layout-a
        idTV = findViewById(R.id.id_edit);
        imeET = findViewById(R.id.ime_edit);
        naslovET = findViewById(R.id.naslov_edit);
        opisET = findViewById(R.id.opis_edit);
        namigET = findViewById(R.id.namig_edit);
        latET = findViewById(R.id.lat_edit);
        lngET = findViewById(R.id.lng_edit);

        idTV.setText("ID LOKACIJE: "+lokacijaId);
        imeET.setText(ime);
        naslovET.setText(naslov);
        opisET.setText(opis);
        namigET.setText(namig);
        latET.setText(String.valueOf(lat));
        lngET.setText(String.valueOf(lng));

        setTitle("Urejanje lokacije: "+ime);


        mDatabase = FirebaseDatabase.getInstance().getReference();
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

    public void prenesiQr(View view){
        String url = "https://chart.googleapis.com/chart?cht=qr&chl="+lokacijaId+"&chs=500x500&choe=UTF-8&chld=L%7C2";
        Intent website = new Intent(Intent.ACTION_VIEW);
        website.setData(Uri.parse(url));
        startActivity(website);
    }
    public void posodobi(View view){
        if (imeET.getText().toString().matches("") || naslovET.getText().toString().matches("") || opisET.getText().toString().matches("") || namigET.getText().toString().matches("")  || latET.getText().toString().matches("") || lngET.getText().toString().matches("") ){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastEnterAllData), Toast.LENGTH_SHORT).show();
        }
        else {
            if (AppNetworkStatus.getInstance(getApplicationContext()).isOnline()) {
                try {
                    ime = imeET.getText().toString();
                    naslov = naslovET.getText().toString();
                    opis = opisET.getText().toString();
                    namig = namigET.getText().toString();
                    lat = Float.parseFloat(latET.getText().toString());
                    lng = Float.parseFloat(lngET.getText().toString());


                    mDatabase.child("lokacija").child(lokacijaId).child("ime").setValue(ime);
                    mDatabase.child("lokacija").child(lokacijaId).child("naslov").setValue(naslov);
                    mDatabase.child("lokacija").child(lokacijaId).child("opis").setValue(opis);
                    mDatabase.child("lokacija").child(lokacijaId).child("namig").setValue(namig);
                    mDatabase.child("lokacija").child(lokacijaId).child("lat").setValue(lat);
                    mDatabase.child("lokacija").child(lokacijaId).child("lng").setValue(lng);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastSuccessUpdate), Toast.LENGTH_LONG).show();

                    allLocations.get(indexLoc).setIme(ime);
                    allLocations.get(indexLoc).setNaslov(naslov);
                    allLocations.get(indexLoc).setOpis(opis);
                    allLocations.get(indexLoc).setNamig(namig);
                    allLocations.get(indexLoc).setLat(lat);
                    allLocations.get(indexLoc).setLng(lng);


                    SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(allLocations);

                    editor.remove("locationList");
                    editor.commit();

                    editor.putString("locationList", json);
                    editor.apply();

                    Intent intentEdit = new Intent(this, EditLocationsActivity.class);
                    startActivity(intentEdit);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastFailUpdate), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastFailUpdateNoConnection), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void deleteLoc(View view){
        if (AppNetworkStatus.getInstance(getApplicationContext()).isOnline()) {
            try {
                mDatabase.child("lokacija").child(lokacijaId).setValue(null);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastSuccessDelete), Toast.LENGTH_LONG).show();

                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPreferences.getString("myLocationList", null);
                Type type = new TypeToken<ArrayList<LocationInfo>>() {
                }.getType();
                myLocations = gson.fromJson(json, type);

                int indexDel = -1;
                for(int i= 0; i<myLocations.size(); i++){
                    if(myLocations.get(i).getuID().equals(lokacijaId)){
                        indexDel = i;
                        break;
                    }
                }
                if(indexDel != -1){
                    myLocations.remove(indexDel);
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                gson = new Gson();
                json = gson.toJson(myLocations);

                editor.remove("myLocationList");
                editor.commit();

                editor.putString("myLocationList", json);
                editor.apply();
                if(myLocations.isEmpty()){
                    Intent intentEdit = new Intent(this, MainActivity.class);
                    startActivity(intentEdit);
                }
                else{
                    Intent intentEdit = new Intent(this, EditLocationsActivity.class);
                    startActivity(intentEdit);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastFailUpdate), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastFailUpdateNoConnection), Toast.LENGTH_LONG).show();
        }
    }
}

