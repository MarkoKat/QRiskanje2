package uni.fe.tnuv.qrtest5;

import android.app.Activity;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    //komentar za testirat github
    private static final String TAG = "MainActivity";
    TextView barcodeResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        barcodeResult = (TextView)findViewById(R.id.barcode_result2);
        barcodeResult.setText("Tu bo prikazan rezultat");
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
}
