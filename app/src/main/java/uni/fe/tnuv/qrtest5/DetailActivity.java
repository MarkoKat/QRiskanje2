package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.Stack;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class DetailActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    public static String[][] tabelaUser;

    public static String[][] tabela;
    private String filename;
    private String filenameUser;

    private String LONGITUDE;
    private String LATITUDE;
    private String ZOOM;
    private String BEARING;
    private String TILT;
    private String POLOZAJ;

    public int trenutnaKoda;

    private static final String TAG = "DetailActivity";

    public static Stack<Class<?>> parents = new Stack<Class<?>>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        LONGITUDE = getResources().getString(R.string.strLongitude);
        LATITUDE = getResources().getString(R.string.strLatitude);
        ZOOM = getResources().getString(R.string.strZoom);
        BEARING = getResources().getString(R.string.strBearing);
        TILT = getResources().getString(R.string.strTilt);
        POLOZAJ = getResources().getString(R.string.strPolozaj);

        //Branje lokacij iz datotecnega sistema
        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }
        tabela = tabela4;

        String tabelaUser2 = beriIzDatoteke(filenameUser);
        String[] tabelaUser3 = tabelaUser2.split("%");
        String[][] tabelaUser4 = new String[tabelaUser3.length][2];
        for (int i = 0; i < tabelaUser3.length; i++) {
            String[] tabelaUserTMP = tabelaUser3[i].split("#");
            tabelaUser4[i] = tabelaUserTMP;
        }
        tabelaUser = tabelaUser4;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        String message = intent.getStringExtra("ime_lokacije");
        boolean naZemljevidu = intent.getBooleanExtra("naZemljevidu", false);

        TextView textView_ime = findViewById(R.id.textView_ime_lokacije);
        TextView textView_opis = findViewById(R.id.textView_opis);
        TextView textView_namig = findViewById(R.id.textView_namig);
        TextView textView_kraj = findViewById(R.id.textView_kraj);

        // Obojestranska poravnava - samo na Oreo
        if (Build.VERSION.SDK_INT >= 26) {
            textView_opis.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
            textView_namig.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

        TextView textViewId = findViewById(R.id.textView_id);
        int ok = 0;
        for(int i = 0; i < tabela.length; i++){
            if(message.equals(tabela[i][1])){
                if(tabela[i][2] != null) { textView_opis.setText(tabela[i][2]); }
                else { textView_opis.setText(getResources().getString(R.string.napaka)); }

                if(tabela[i][5] != null) { textView_namig.setText(tabela[i][5]); }
                else { textView_namig.setText(getResources().getString(R.string.napaka)); }

                if(tabela[i][6] != null) { textView_kraj.setText(tabela[i][6]); }
                else { textView_kraj.setText(getResources().getString(R.string.napaka)); }

                textViewId.setText("ID: " + tabela[i][0]);
                ok = 1;
                trenutnaKoda = i;
                if(tabelaUser[i][1].equals("1")) {
                    int unicode = 0x2714; // Kljukica pred imenom lokacije
                    textView_ime.setText(getEmojiByUnicode(unicode) + " " +  message);
                    textView_ime.setTextColor(Color.parseColor("#00a813"));
                }
                else {
                    textView_ime.setText(message);
                }
            }
        }
        if(ok == 0) {
            textView_opis.setText(getResources().getString(R.string.napakaNamig));
            textView_namig.setText(getResources().getString(R.string.napaka));
            textView_kraj.setText(getResources().getString(R.string.napaka));
        }

        // Pridobivanje lokacije za izracun razdalje
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            TextView textViewOddaljenost = findViewById(R.id.textView_oddaljenost);
            textViewOddaljenost.setText(getResources().getString(R.string.obvestiloNiLokacijskihStoritevDetail));
        }
        else{
            if (gps_enabled) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {

                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    Double latitude = location.getLatitude();
                                    Double longitude = location.getLongitude();

                                    Location loc1 = new Location("");
                                    loc1.setLatitude(latitude);
                                    loc1.setLongitude(longitude);

                                    Location loc2 = new Location("");
                                    loc2.setLatitude(Double.parseDouble(tabela[trenutnaKoda][3]));
                                    loc2.setLongitude(Double.parseDouble(tabela[trenutnaKoda][4]));

                                    float distanceInMeters = loc1.distanceTo(loc2);

                                    String urejenaRazdalja;

                                    if (distanceInMeters > 1000) {
                                        float distance2 = distanceInMeters / 1000;
                                        urejenaRazdalja = (double) Math.round(distance2 * 10d) / 10d + "km";
                                    } else {
                                        urejenaRazdalja = Math.round(distanceInMeters) + "m";
                                    }

                                    TextView textViewOddaljenost = findViewById(R.id.textView_oddaljenost);
                                    textViewOddaljenost.setText("Razdalja: " + urejenaRazdalja);
                                }
                            }
                        });
            }
            else {
                TextView textViewOddaljenost = findViewById(R.id.textView_oddaljenost);
                textViewOddaljenost.setText(getResources().getString(R.string.obvestiloNiLokacijskihStoritevDetail));
            }
        }

        // Prikazi gumb za prikaz na zemljevidu ce pride iz seznama
        Button zem = findViewById(R.id.gumbNaZemljevid);
        if(!naZemljevidu) {
            zem.setVisibility(View.INVISIBLE);
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

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                Intent intent = getIntent();
                String parent = intent.getStringExtra("parentAct");
                parent =  parent.substring(6,parent.length());
                Class prej = MainActivity.class;

                try {
                    prej = Class.forName(parent);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                Intent parentActivityIntent = new Intent(this, prej);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void prikaziZemljevid(View v) {
        try{
            SharedPreferences sharedPrefs = getSharedPreferences(POLOZAJ, 0);

            SharedPreferences.Editor editor = sharedPrefs.edit();

            editor.putFloat(LATITUDE, Float.parseFloat(tabela[trenutnaKoda][3]));
            editor.putFloat(LONGITUDE, Float.parseFloat(tabela[trenutnaKoda][4]));
            editor.putFloat(ZOOM, 17);
            editor.putFloat(TILT, 0);
            editor.putFloat(BEARING, 0);
            editor.apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("izPodrobnosti", true);
        startActivity(intent);
    }
}
