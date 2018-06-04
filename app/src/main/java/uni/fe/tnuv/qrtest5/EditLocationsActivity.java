package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class EditLocationsActivity extends AppCompatActivity {

    private ArrayList<LocationInfo> allLocations;
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


        gson = new Gson();
        json = sharedPreferences.getString("locationList", null);
        type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);

        for (int i = 0; i<allLocations.size(); i++){
            for (int j = 0; j<myLocations.size(); j++){
                if(allLocations.get(i).getuID().equals(myLocations.get(j).getuID())){
                    myLocations.get(j).setIme(allLocations.get(i).getIme());
                    myLocations.get(j).setNaslov(allLocations.get(i).getNaslov());
                    myLocations.get(j).setOpis(allLocations.get(i).getOpis());
                    myLocations.get(j).setNamig(allLocations.get(i).getNamig());
                    myLocations.get(j).setLat(allLocations.get(i).getLat());
                    myLocations.get(j).setLng(allLocations.get(i).getLng());
                }
            }
        }

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
                    startActivity(intent);
                }
            });
        }else{
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
