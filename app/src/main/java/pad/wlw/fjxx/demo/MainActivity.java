package pad.wlw.fjxx.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import pad.wlw.fjxx.demo.Connect.Client;
import pad.wlw.fjxx.demo.Connect.Server;
import pad.wlw.fjxx.demo.Util.SD_Permission;

public class MainActivity extends AppCompatActivity {
    private Button btn_accept;
    private EditText et_ip;
    private TextView tv_IP;
    private Client client;

//    private Timer timer;
//    private TimerTask timerTask;

    /**
     * @param pathType
     * @1 Photo
     * @2 Movie
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("收到消息", String.valueOf(msg.what));
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //检测APP有没有SD卡的读写权限
        SD_Permission.verifyStoragePermissions(this);
        //展示本机IP
        ShowIP();
        //开启Socket服务器
        new Thread() {
            @Override
            public void run() {
                Server server = new Server(MainActivity.this, handler);
                server.Start();
            }
        }.start();
        Toast.makeText(MainActivity.this, "启动服务器", Toast.LENGTH_SHORT).show();
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    client = new Client(MainActivity.this, handler);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        timer.schedule(timerTask, 0, 5000);
//        Con();
    }

    private void ShowIP() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        tv_IP.setText(ip);
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        timer.cancel();
//    }

//    private void Con(final String IP) {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Client client = new Client(MainActivity.this, IP, handler);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

//    private void Con() {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    if (client == null) {
//                        client = new Client(MainActivity.this, handler);
////                        client.save();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    private void initView() {
        btn_accept = findViewById(R.id.btn_accept);
        et_ip = findViewById(R.id.et_ip);
        tv_IP = findViewById(R.id.tv_IP);
//        btn_accept.setOnClickListener(this);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_accept:
//                String IP = et_ip.getText().toString();
//                if (!IP.equals("")) {
//                    Con(IP);
//                } else {
//                    Con();
//                }
//                break;
//        }
//    }
}
