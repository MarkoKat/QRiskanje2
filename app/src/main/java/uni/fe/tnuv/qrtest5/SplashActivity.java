package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String ZAGON = getResources().getString(R.string.strZagon);
        String ZAGON2 = getResources().getString(R.string.strZagon2);

        // Oznaci da je bila aplikacija ponovno zagnana - zemljevid se postavi na trenutno lokacijo
        SharedPreferences sharedPrefs = getSharedPreferences(ZAGON, 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt(ZAGON2, 1);
        editor.apply();

        // Takoj po zagonu preklopi na MainActivity - seznam
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
