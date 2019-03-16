package pad.wlw.fjxx.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import pad.wlw.fjxx.demo.Connect.Client;
import pad.wlw.fjxx.demo.Connect.Server;
import pad.wlw.fjxx.demo.Util.SD_Permission;
import pad.wlw.fjxx.demo.fragment.ImageFragment;
import pad.wlw.fjxx.demo.fragment.MainFragment;
import pad.wlw.fjxx.demo.fragment.VideoFragment;
import pad.wlw.fjxx.demo.fragment.WebFragment;
import pad.wlw.fjxx.demo1.R;

public class MainActivity extends FragmentActivity {
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
            FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
            Fragment fragment=null;
            switch (msg.what) {
                case 1:
                    Log.i("Handler","1图片");
                    fragment=new ImageFragment();
                    break;
                case 2:
                    Log.i("Handler","2视频");
                   fragment=new VideoFragment();
                    break;
                case 3:
                    Log.i("Handler","3web");
                    fragment=new WebFragment();
                    break;
                case 4:
                    Log.i("Handler","4main");
                    fragment=new MainFragment();
                    break;
            }
            transaction.replace(R.id.mian_relativeLayout, fragment);
            transaction.commit(); // 提交创建Fragment请求
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        //检测APP有没有SD卡的读写权限
        SD_Permission.verifyStoragePermissions(this);
        //获取上次退出前的状态
        getstate();
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

    private void getstate() {
        SharedPreferences SP=getSharedPreferences("Save",Context.MODE_PRIVATE);
        int atate=SP.getInt("Type",0);
        if (atate!=0){
            Message message=Message.obtain();
            message.what=atate;
            handler.sendMessage(message);
        }
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
