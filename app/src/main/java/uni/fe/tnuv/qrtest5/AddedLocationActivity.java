package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddedLocationActivity extends AppCompatActivity {

    private String lokacijaId;
    private String ime;
    private String naslov;
    private String namig;
    private float lat;
    private float lng;

    private TextView qrCode;
    private Button downloadQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_location);

        setTitle(getResources().getString(R.string.activityAddedName));
        // gumb za nazaj
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pridobivanje podatkov o dodani lokaciji
        Bundle bundle = getIntent().getExtras();
        lokacijaId = bundle.getString("id_lokacije");
        ime = bundle.getString("ime_lokacije");
        naslov = bundle.getString("naslov_lokacije");
        namig = bundle.getString("namig_lokacije");
        lat = bundle.getFloat("lat_lokacije");
        lng = bundle.getFloat("lng_lokacije");


        qrCode = findViewById(R.id.qr_code);
        downloadQr = findViewById(R.id.download_qr_code);

        qrCode.setText("ID LOKACIJE: "+lokacijaId+"\nIME LOKACIJE: "+ime+"\nNAMIG ZA LOKACIJO:"+namig+"\nKOORDINATE:\n"+lat+"\n"+lng);
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

    // odpre se stran za prenos QR kode
    public void prenesiQr(View view){
        String url = "https://chart.googleapis.com/chart?cht=qr&chl="+lokacijaId+"&chs=500x500&choe=UTF-8&chld=L%7C2";
        Intent website = new Intent(Intent.ACTION_VIEW);
        website.setData(Uri.parse(url));
        startActivity(website);
    }
}
