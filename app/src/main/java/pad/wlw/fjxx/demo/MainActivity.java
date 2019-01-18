package pad.wlw.fjxx.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

import pad.wlw.fjxx.demo.Connect.Client;
import pad.wlw.fjxx.demo.Util.SD_Permission;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SD_Permission.verifyStoragePermissions(this);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Client client = new Client(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
