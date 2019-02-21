package pad.wlw.fjxx.demo.Connect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.mbms.FileInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Client extends Socket {
    private static String SERVER_IP = "192.168.0.104";
    private static final int SERVER_PORT = 8899;
    private DataInputStream dis;
    private FileOutputStream fos;
    private Socket client;
    private SharedPreferences SP;
    private SharedPreferences.Editor editor;

    private Handler handler = null;
    private int fileType = -1;

    @SuppressLint("CommitPrefEdits")
    public Client(Context context, String IP, Handler handler) throws IOException {
        super(IP, SERVER_PORT);
        SERVER_IP = IP;
        this.client = this;
        this.handler = handler;
        Log.i("Seccess", "成功连接到服务器");
        this.setKeepAlive(true);
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
        save();
    }

    @SuppressLint("CommitPrefEdits")
    public Client(Context context, Handler handler) throws IOException {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        this.handler = handler;
        Log.i("Seccess", "成功连接到服务器");
        this.setKeepAlive(true);
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
        save();
    }

    public void save() {
//        saveFile(1);
        receiveFile(client);
    }

    class mFileInfo {
        String mFileName;
        long mFileSize;
    }

    private void receiveFile(Socket socket) {
        File dirs = new File(Environment.getExternalStorageDirectory().toString() + "/");
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        DataInputStream din = null;
        int fileNum = 0;
        long totalSize = 0;
        mFileInfo[] fileinfos = null;
        try {
            din = new DataInputStream(new BufferedInputStream(
                    socket.getInputStream()));
            fileType = din.readInt();
            //如果接收的是网页内容，则停止接收文件
            if (fileType == 3) {
                String url = din.readUTF();
                editor.putString("URL", url).commit();
                saveState(fileType);
                SendMsg(fileType);
                return;
            }
            fileNum = din.readInt();
            fileinfos = new mFileInfo[fileNum];
            for (int i = 0; i < fileNum; i++) {
                fileinfos[i] = new mFileInfo();
                fileinfos[i].mFileName = din.readUTF();
                fileinfos[i].mFileSize = din.readLong();
            }
            //1指的是数据类型，1是Photo
            saveState(1);
            totalSize = din.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("IO错误", "readInt Exception");
            System.exit(0);
        }
        System.out.println(fileNum);
        System.out.println(totalSize);
        for (mFileInfo fileinfo : fileinfos) {
            System.out.println(fileinfo.mFileName);
            System.out.println(fileinfo.mFileSize);
        }
        // // /////////////////////////////////////////////////////////////////
        int leftLen = 0; // 写满文件后缓存区中剩余的字节长度。
        int bufferedLen = 0; // 当前缓冲区中的字节数
        int writeLen = 0; // 每次向文件中写入的字节数
        long writeLens = 0; // 当前已经向单个文件中写入的字节总数
        long totalWriteLens = 0; // 写入的所有字节数
        byte buf[] = new byte[8192];
        byte temp[];  //缓存区部分内容    缓冲对应System.arraycopy
        for (int i = 0; i < fileNum; i++) {
            writeLens = 0;// 当前已经向单个文件中写入的字节总数
            try {
//                FileOutputStream fout = new FileOutputStream(dirs
//                        + fileinfos[i].mFileName);
                @SuppressLint("SdCardPath") FileOutputStream fout = new FileOutputStream("/sdcard/"
                        + fileinfos[i].mFileName);
                while (true) {
                    if (leftLen > 0) {
                        bufferedLen = leftLen;
                    } else {
                        bufferedLen = din.read(buf);
                    }
                    if (bufferedLen == -1) {
                        return;
                    }
                    System.out.println("readlen" + bufferedLen);
                    // 如果已写入文件的字节数加上缓存区中的字节数已大于文件的大小，只写入缓存区的部分内容。
                    if (writeLens + bufferedLen >= fileinfos[i].mFileSize) {
                        leftLen = (int) (writeLens + bufferedLen - fileinfos[i].mFileSize);
                        writeLen = bufferedLen - leftLen;
                        fout.write(buf, 0, writeLen); // 写入部分
                        totalWriteLens += writeLen;
                        System.arraycopy(buf, writeLen, buf, 0, leftLen);
//                        temp = new byte[8192];
//                        System.arraycopy(buf, writeLen, temp, 0, leftLen);
//                        buf = temp;
                        break;
                    } else {
                        fout.write(buf, 0, bufferedLen); // 全部写入
                        writeLens += bufferedLen;
                        totalWriteLens += bufferedLen;
                        if (totalWriteLens >= totalSize) {
                            return;
                        }
                        leftLen = 0;
                    }
                    //(int) (totalWriteLens * 100 / totalSize));
                } // end while
                fout.close();
                SendMsg(fileType);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("文件接收失败", e.toString());
                Log.d("接收文件失败", "receive file Exception");
            }
        }
    }

    //保存已经接收的数据类型。
    //如果程序意外关闭。则在开启的时候检查是否已经保存过文件。如果有，直接打开
    private void saveState(int pathType) {
        editor.putInt("Type", pathType);
        editor.commit();
    }

    private void SendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        handler.handleMessage(msg);
    }
}
