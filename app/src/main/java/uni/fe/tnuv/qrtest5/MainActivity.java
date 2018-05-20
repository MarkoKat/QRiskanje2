package uni.fe.tnuv.qrtest5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //komentar za testirat github
    //Markotov komentar
    //branch test n
    private static final String TAG = "MainActivity";
    private String filename;
    private String filenameUser;
    TextView barcodeResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        barcodeResult = (TextView)findViewById(R.id.barcode_result2);
        barcodeResult.setText("Tu bo prikazan rezultat");

        String[][] tabela = {
                {"123456", "Fakulteta za elektrotehniko", "Namig, ki ga ni, ali pa ga samo ne vidiš", "46.044783", "14.489494"},
                {"111111", "Prešernov spomenik", "Tudi tu ni namioga", "46.051389", "14.506317"},
                {"000001", "Stari grad smlednik :D", "Za tisto sivo skalo", "46.165446", "14.442362"}};

        String[][] tabelaUser = {
                {"123456", "0"},
                {"111111", "0"},
                {"000001", "1"}
        };

        String tabelaUser2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabela.length; i++) {
            StringBuilder tmp = new StringBuilder();
            for (int j = 0; j < tabela[i].length; j++) {
                tmp.append(tabela[i][j]).append("#");
            }
            sb.append(tmp).append("%");
        }
        //Log.i(TAG,sb.toString());
        vpisiVDatoteko(filename, sb.toString());

        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        //List<String> tabela4 = new ArrayList<String>();
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }

        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            // first time task
            StringBuilder sbUser = new StringBuilder();
            for (int i = 0; i < tabelaUser.length; i++) {
                StringBuilder tmpUser = new StringBuilder();
                for (int j = 0; j < tabelaUser[i].length; j++) {
                    tmpUser.append(tabelaUser[i][j]).append("#");
                }
                sbUser.append(tmpUser).append("%");
            }
            Log.i(TAG, sbUser.toString());
            vpisiVDatoteko(filenameUser, sbUser.toString());
            tabelaUser2 = beriIzDatoteke(filenameUser);

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }
        else {
            tabelaUser2 = beriIzDatoteke(filenameUser);
        }



        barcodeResult.setText(tabelaUser2);
    }

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
}
