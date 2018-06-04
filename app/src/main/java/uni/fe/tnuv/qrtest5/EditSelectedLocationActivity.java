package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class EditSelectedLocationActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_selected_location);


        // Pridobivanje podatkov o dodani lokaciji
        Bundle bundle = getIntent().getExtras();
        lokacijaId = bundle.getString("id_lokacije");
        ime = bundle.getString("ime_lokacije");
        naslov = bundle.getString("naslov_lokacije");
        opis = bundle.getString("opis_lokacije");
        namig = bundle.getString("namig_lokacije");
        lat = bundle.getFloat("lat_lokacije");
        lng = bundle.getFloat("lng_lokacije");

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
        // gumb za nazaj
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
