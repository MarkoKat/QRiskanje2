package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DatotecniSistem extends AppCompatActivity {

    //Za shranjevanje v datotečni sistem------------------------------------------------------------
    public void vpisiVDatoteko(String filenameLoc, String vsebina){
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

    public String beriIzDatoteke(String filenameLoc) {
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
