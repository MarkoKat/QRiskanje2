package uni.fe.tnuv.qrtest5;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;

public class DetailActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    public static String[][] tabela;
    private String filename;

    public int trenutnaKoda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        filename = getResources().getString(R.string.datotekaZVsebino);
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        String message = intent.getStringExtra("ime_lokacije");

        TextView textView = findViewById(R.id.textView_ime_lokacije);
        textView.setText(message);

        TextView textView2 = findViewById(R.id.textView_namig);
        int ok = 0;
        for(int i = 0; i < tabela.length; i++){
            if(message.equals(tabela[i][1])){
                textView2.setText(tabela[i][2]);
                ok = 1;
                trenutnaKoda = i;
            }
        }
        if(ok == 0) {
            textView2.setText("Napaka pri iskanju namiga!");
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
        else{

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

                                if(distanceInMeters > 1000) {
                                    float distance2 = distanceInMeters / 1000;
                                    urejenaRazdalja = (double)Math.round(distance2 * 10d) / 10d + "km";
                                }
                                else {
                                    urejenaRazdalja = Math.round(distanceInMeters) + "m";
                                }

                                TextView textViewOddaljenost = findViewById(R.id.textView_oddaljenost);
                                textViewOddaljenost.setText("Razdalja: " + urejenaRazdalja);
                            }
                        }
                    });
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
}
