package uni.fe.tnuv.qrtest5;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    public static String[][] tabela = {
            {"123456", "Fakulteta za elektrotehniko", "Namig, ki ga ni", "46.044783", "14.489494"},
            {"111111", "Prešernov spomenik", "Tudi tu ni namioga", "46.051389", "14.506317"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String message = intent.getStringExtra("ime_lokacije");

        TextView textView = findViewById(R.id.textView_ime_lokacije);
        textView.setText(message);

        TextView textView2 = findViewById(R.id.textView_namig);
        int ok = 0;
        for(int i = 0; i < tabela.length; i++){
            if(message.equals(tabela[i][1])){
                textView2.setText(tabela[i][2]);
                ok = 1;
            }
        }
        if(ok == 0) {
            textView2.setText("Napaka pri iskanju namiga!");
        }


    }
}
