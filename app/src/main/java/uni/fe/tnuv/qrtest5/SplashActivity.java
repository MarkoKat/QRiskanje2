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

        SharedPreferences sharedPrefs = getSharedPreferences("zagon", 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt("zagon2", 1);
        editor.apply();
        Log.v(TAG, "MarkoSplash1 - " + sharedPrefs.getInt("zagon2", 0));

        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
