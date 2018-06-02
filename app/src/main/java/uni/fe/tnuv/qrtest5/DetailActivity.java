package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class DetailActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    public static String[][] tabelaUser;

    public static String[][] tabela;
    private String filename;
    private String filenameUser;

    public int trenutnaKoda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        //Branje lokacij iz datotecnega sistema
        String tabela2 = beriIzDatoteke(filename);
        //Log.i(TAG,tabela2);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        //List<String> tabela4 = new ArrayList<String>();
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

        TextView textView = findViewById(R.id.textView_ime_lokacije);



        TextView textView2 = findViewById(R.id.textView_namig);
        if (Build.VERSION.SDK_INT >= 26) {
            // Call some material design APIs here
            textView2.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        } else {
            // Implement this feature without material design
        }

        TextView textViewId = findViewById(R.id.textView_id);
        int ok = 0;
        for(int i = 0; i < tabela.length; i++){
            if(message.equals(tabela[i][1])){
                textView2.setText(tabela[i][2]);
                textViewId.setText("ID: " + tabela[i][0]);
                ok = 1;
                trenutnaKoda = i;
                if(tabelaUser[i][1].equals("1")) {
                    int unicode = 0x2714;
                    textView.setText(getEmojiByUnicode(unicode) + " " +  message);
                    textView.setTextColor(Color.parseColor("#00a813"));
                }
                else {
                    textView.setText(message);
                }
            }
        }
        if(ok == 0) {
            textView2.setText("Napaka pri iskanju namiga!");
        }


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
            textViewOddaljenost.setText("Za prikaz razdalje morate omogočiti lokacijske storitve");
        }
        else{
            if (gps_enabled == true) {
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

                                    String urejenaRazdalja = "bs";

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
                textViewOddaljenost.setText("Za prikaz razdalje morate omogočiti lokacijske storitve");
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

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}
