package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddLocationActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private EditText ime;
    private EditText opis;
    private EditText lat;
    private EditText lng;
    private int idL = 0;

    private TextView izpis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");*/
        ime = (EditText) findViewById(R.id.ime);
        opis = (EditText) findViewById(R.id.opis);
        lat = (EditText) findViewById(R.id.lat);
        lng = (EditText) findViewById(R.id.lng);

        izpis = (TextView) findViewById(R.id.izpis);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void poslji(View view){

        String idLok = new Integer(idL).toString();
        writeNewLokacija(idLok ,ime.getText().toString(), opis.getText().toString(), Float.valueOf(lat.getText().toString()), Float.valueOf(lng.getText().toString()));
        idL++;
    }

    private void writeNewLokacija(String lokacijaId, String ime, String opis, float lat, float lng) {
        LocationInfo lok = new LocationInfo(ime, opis, lat, lng);

        mDatabase.child("lokacija").child(lokacijaId).setValue(lok);
    }
    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();

                Collection<Object> values = td.values();

                HashMap<String, Object> result =(HashMap<String,Object>) dataSnapshot.getValue();
                /*for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    PostLocation loc = snapshot.getValue(PostLocation.class);
                    result.put(snapshot.getKey(), loc);
                }*/

                // [START_EXCLUDE]
                Log.d("NUJNO", dataSnapshot.toString());
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                izpis.setText(result.toString());

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Context context = getApplicationContext();
                Toast.makeText(context, "napaka", Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mDatabase.addValueEventListener(postListener);
        // [END post_value_event_listener]

    }
}
