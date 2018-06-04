package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class EditLocationsActivity extends AppCompatActivity {

    private ArrayList<LocationInfo> myLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_locatios);

        setTitle(getResources().getString(R.string.activityEditLocationName));
        // gumb za nazaj
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("myLocationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {
        }.getType();
        myLocations = gson.fromJson(json, type);

        if (myLocations != null && !myLocations.isEmpty()) {
            ListView mListView = findViewById(R.id.my_list_view);
            LocationListAdapter adapter = new LocationListAdapter(this, R.layout.adapter_location_view, myLocations);
            mListView.setAdapter(adapter);
            // OnClick na item na seznamu prikaze podrobnosti za lokacijo
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Pridobimo lokacijo iz ArrayList na katero je uporabnik pritisnil
                    LocationInfo selectedLocation = (LocationInfo) parent.getItemAtPosition(position);
                    // zazenem activity za urejanje
                    Intent intent = new Intent(getApplicationContext(), EditSelectedLocationActivity.class);
                    intent.putExtra("id_lokacije", selectedLocation.getuID());
                    intent.putExtra("ime_lokacije", selectedLocation.getIme());
                    intent.putExtra("naslov_lokacije", selectedLocation.getNaslov());
                    intent.putExtra("opis_lokacije", selectedLocation.getOpis());
                    intent.putExtra("namig_lokacije", selectedLocation.getNamig());
                    intent.putExtra("lat_lokacije", selectedLocation.getLat());
                    intent.putExtra("lng_lokacije", selectedLocation.getLng());
                    startActivity(intent);
                }
            });
        }
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
}
