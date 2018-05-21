package uni.fe.tnuv.qrtest5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ArrayList<LocationInfo> allLocations;
    private static final String TAG = "MainActivity";
    TextView barcodeResult;

    private SwipeRefreshLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        barcodeResult = (TextView)findViewById(R.id.barcode_result2);
        barcodeResult.setText("Tu bo prikazan rezultat");

        //prebere podatke v allLocations iz sharedPreferences
        loadLocations();

        // za bazo
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // se klice pri vsaki spremembi v bazi
                ArrayList<LocationInfo> currLocationsList = new ArrayList<>();
                for (DataSnapshot postSnapshot:  dataSnapshot.getChildren()){
                    for (DataSnapshot ds: postSnapshot.getChildren()){
                        LocationInfo currentLoc = new LocationInfo();
                        currentLoc.setIme(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getIme());
                        currentLoc.setOpis(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getOpis());
                        currentLoc.setLat(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLat());
                        currentLoc.setLng(postSnapshot.child(ds.getKey()).getValue(LocationInfo.class).getLng());
                        currentLoc.setuID(ds.getKey());

                        currLocationsList.add(currentLoc);
                    }
                }
                allLocations = currLocationsList;
                saveLocations();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("napaka", "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Context context = getApplicationContext();
                Toast.makeText(context, "napaka", Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mDatabase.addValueEventListener(postListener);


        // izpis iz shared Preferences v seznam

        /*ListView  mListView = (ListView) findViewById(R.id.list_view);
        LocationListAdapter  adapter = new LocationListAdapter(this, R.layout.adapter_location_view, allLocations);
        mListView.setAdapter(adapter);*/
        final Context thisContext = this;

        container = (SwipeRefreshLayout) findViewById(R.id.container);
        container.setOnRefreshListener(mOnRefreshListener);
        updateLocationList();

    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            updateLocationList();
        }
    };

    // ko swipas updata list 
    public void updateLocationList(){
        ListView  mListView = (ListView) findViewById(R.id.list_view);
        LocationListAdapter  adapter = new LocationListAdapter(this, R.layout.adapter_location_view, allLocations);
        mListView.setAdapter(adapter);
        if (container.isRefreshing()) {
            container.setRefreshing(false);
        }
    }

    // gumb plus v meniju v action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.add_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddLocationActivity.class);
                //intent.putExtra("barcode", barcode);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    // shrani lokacijo iz baze v sharedPreferences
    private void saveLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allLocations);

        editor.remove("locationList");
        editor.commit();
        //editor.clear().commit();

        editor.putString("locationList", json);
        editor.apply();

    }

    // prebere podatke iz sharedPreferences in jih vrne v obliki ArrayList<LocationInfo>
    private void loadLocations(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("locationList", null);
        Type type = new TypeToken<ArrayList<LocationInfo>>() {}.getType();
        allLocations = gson.fromJson(json, type);

        if (allLocations == null){
            allLocations = new ArrayList<>();
        }
    }
}
