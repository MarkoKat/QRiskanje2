package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ResultActivity extends AppCompatActivity {

    //public static String[][] tabela = {{"123456", "Fakulteta za elektrotehniko"}, {"111111", "Prešernov spomenik"}};
    private String filename;
    private String filenameUser;
    public static String[][] tabela;
    public static String[][] tabelaUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        filename = getResources().getString(R.string.datotekaZVsebino);
        filenameUser = getResources().getString(R.string.datotekaZVsebinoUser);

        //Branje lokacij iz datotecnega sistema
        String tabela2 = beriIzDatoteke(filename);
        String[] tabela3 = tabela2.split("%");
        String[][] tabela4 = new String[tabela3.length][5];
        for (int i = 0; i < tabela3.length; i++) {
            String[] tabelaTMP = tabela3[i].split("#");
            tabela4[i] = tabelaTMP;
        }
        tabela = tabela4;

        String tabelaUser2 = beriIzDatoteke(filenameUser);
        String[] tabelaUser3 = tabelaUser2.split("%");
        String[][] tabelaUser4 = new String[tabelaUser3.length][2];
        for (int i = 0; i < tabelaUser3.length; i++) {
            String[] tabelaUserTMP = tabelaUser3[i].split("#");
            tabelaUser4[i] = tabelaUserTMP;
        }
        tabelaUser = tabelaUser4;



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
                tabelaUser[i][1] = "1";
                StringBuilder sbUser = new StringBuilder();
                for (int i2 = 0; i2 < tabelaUser.length; i2++) {
                    StringBuilder tmpUser = new StringBuilder();
                    for (int j2 = 0; j2 < tabelaUser[i2].length; j2++) {
                        tmpUser.append(tabelaUser[i2][j2]).append("#");
                    }
                    sbUser.append(tmpUser).append("%");
                }
                vpisiVDatoteko(filenameUser, sbUser.toString());
            }
        }
        if(ok == 0) {
            textView.setText("QR koda ni veljavna!");
        }
    }

    //Za shranjevanje v datotečni sistem------------------------------------------------------------
    private void vpisiVDatoteko(String filenameLoc, String vsebina){
        try {
            //ustvarimo izhodni tok
            FileOutputStream os = openFileOutput(filenameLoc, Context.MODE_PRIVATE);
            //zapisemo posredovano vsebino v datoteko
            os.write(vsebina.getBytes());
            //sprostimo izhodni tok
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String beriIzDatoteke(String filenameLoc){
        FileInputStream inputStream;

        File file = new File(getFilesDir(), filenameLoc);
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            inputStream = openFileInput(filenameLoc);
            inputStream.read(bytes);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String vsebina = new String(bytes);

        return vsebina;
    }
}
