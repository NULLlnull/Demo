package pad.wlw.fjxx.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import pad.wlw.fjxx.demo.Connect.Client;
import pad.wlw.fjxx.demo.Util.SD_Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_accept;
    private EditText et_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //检测APP有没有SD卡的读写权限
        SD_Permission.verifyStoragePermissions(this);
//        Con();
    }

    private void Con(final String IP) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Client client = new Client(MainActivity.this, IP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void Con() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Client client = new Client(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initView() {
        btn_accept = findViewById(R.id.btn_accept);
        et_ip = findViewById(R.id.et_ip);
        btn_accept.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                String IP = et_ip.getText().toString();
                if (!IP.equals("")) {
                    Con(IP);
                } else {
                    Con();
                }
                break;
        }
    }
}
