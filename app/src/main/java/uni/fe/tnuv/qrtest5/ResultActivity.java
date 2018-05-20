package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    public static String[][] tabela = {{"123456", "Fakulteta za elektrotehniko"}, {"111111", "Prešernov spomenik"}};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String message = intent.getStringExtra("barcode");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.barcode_result);
        textView.setText(message);

        int ok = 0;
        for(int i = 0; i < tabela.length; i++){
            if(message.equals(tabela[i][0])){
                textView.setText(tabela[i][1]);
                ok = 1;
            }
        }
        if(ok == 0) {
            textView.setText("QR koda ni veljavna!");
        }
    }
}
