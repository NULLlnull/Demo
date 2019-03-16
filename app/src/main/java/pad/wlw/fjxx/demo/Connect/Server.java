package pad.wlw.fjxx.demo.Connect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {
    ServerSocket server;
    Handler handler;
    SharedPreferences SP;
    SharedPreferences.Editor editor;
    String SD_Path;
    String path;

    @SuppressLint("CommitPrefEdits")
    public Server(Context context, Handler handler) {
        this.handler = handler;
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
    }

    public void Start() {
        try {
            server = new ServerSocket(8899);
            while (true) {
                Socket socket = server.accept();
                Log.d("Client connection", new Date().toString());
                socket.setKeepAlive(true);
                Connect(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Connect(Socket socket) {
        DataInputStream din = null;
        int fileType = -1;
        int delType = -1;
        try {
            din = new DataInputStream(new BufferedInputStream(
                    socket.getInputStream()));
            fileType = din.readInt();
            if (fileType == 3) {
                Web(din);
            } else if (fileType == 4) {
                delType = din.readInt();
                SendDelMSG(delType);
                final int finalDelType = delType;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        switch (finalDelType) {
                            case 1:
                            case 2:
                                DelDir(finalDelType == 1 ? "Pictures" : "Movies");
                                break;
                            case 3:
                                editor.remove("URL");
                                editor.remove("Type").commit();
                                break;
                            case 4:
                                DelDir("Pictures");
                                DelDir("Movies");
                                editor.remove("URL");
                                editor.remove("Type").commit();
                                break;
                            default:
                                break;
                        }
                    }
                }.start();
            } else {
                SD_Path = Environment.getExternalStorageDirectory().toString();
                path = SD_Path + "/FJXX/" + (fileType == 1 ? "Pictures" : "Movies") + "/";
                CheckDir();
//                path = "/sdcard/FJXX/" + (fileType == 1 ? "Pictures" : "Movies") + "/";
                File(din);
            }
            SaveAndSendMsg(fileType);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IO错误", e.toString());
//            System.exit(0);
        } finally {
            try {
                if (din != null) {
                    din.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //检测文件夹是否被创建
    private void CheckDir() {
        File rDir = new File(SD_Path + "/FJXX");
        if (!rDir.exists()) {
            rDir.mkdir();

            File rpDir = new File(SD_Path + "/FJXX/Pictures");
            if (!rpDir.exists()) {
                rpDir.mkdir();
            }
            File rmDir = new File(SD_Path + "/FJXX/Movies");
            if (!rmDir.exists()) {
                rmDir.mkdir();
            }
        }
    }

    private void DelDir(String dirName) {
        SD_Path = Environment.getExternalStorageDirectory().toString();
        path = SD_Path + "/FJXX/" + (dirName) + "/";
        File[] files = new File(path).listFiles();
        Log.d("文件列表", files.toString());
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    //保存成功之后保存状态以及告诉主界面保存完成
    private void SaveAndSendMsg(int fileType) {
        editor.putInt("Type", fileType).commit();
        Message msg = new Message();
        msg.what = fileType;
        handler.handleMessage(msg);
        Log.d("Save successfully", "Type:" + fileType);
    }

    private void SendDelMSG(int delType) {
        Message msg = new Message();
        msg.what = 4;
        msg.obj = String.valueOf(delType);
        handler.handleMessage(msg);
    }

    class mFileInfo {
        String mFileName;
        long mFileSize;
    }

    //保存文件
    private void File(DataInputStream din) throws IOException {
//        File dirs = new File(Environment.getExternalStorageDirectory().toString() + "/");
//        if (!dirs.exists()) {
//            dirs.mkdirs();
//        }
        int fileNum = 0;
        long totalSize = 0;
        mFileInfo[] fileinfos = null;
        fileNum = din.readInt();
        fileinfos = new mFileInfo[fileNum];
        for (int i = 0; i < fileNum; i++) {
            fileinfos[i] = new mFileInfo();
            fileinfos[i].mFileName = din.readUTF();
            fileinfos[i].mFileSize = din.readLong();
        }
        totalSize = din.readLong();
        //开始接收文件
        int leftLen = 0; // 写满文件后缓存区中剩余的字节长度。
        int bufferedLen = 0; // 当前缓冲区中的字节数
        int writeLen = 0; // 每次向文件中写入的字节数
        long writeLens = 0; // 当前已经向单个文件中写入的字节总数
        long totalWriteLens = 0; // 写入的所有字节数
        byte buf[] = new byte[8192];
        byte temp[];  //缓存区部分内容    缓冲对应System.arraycopy
        for (int i = 0; i < fileNum; i++) {
            writeLens = 0;// 当前已经向单个文件中写入的字节总数
            @SuppressLint("SdCardPath") FileOutputStream fout = new FileOutputStream(path
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
                Log.d("正在接收中：", String.valueOf(totalWriteLens / totalSize));
                // 如果已写入文件的字节数加上缓存区中的字节数已大于文件的大小，只写入缓存区的部分内容。
                if (writeLens + bufferedLen >= fileinfos[i].mFileSize) {
                    leftLen = (int) (writeLens + bufferedLen - fileinfos[i].mFileSize);
                    writeLen = bufferedLen - leftLen;
                    fout.write(buf, 0, writeLen); // 写入部分
                    totalWriteLens += writeLen;
                    //把已经写入的字节向前移动
//                    temp = new byte[8192];
//                    System.arraycopy(buf, writeLen, temp, 0, leftLen);
//                    buf = temp;
                    System.arraycopy(buf, writeLen, buf, 0, leftLen);
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
            }
            Log.d("文件接收完毕", "共接收了" + fileNum + "个文件");
            fout.close();
        }
    }

    //保存网页
    private void Web(DataInputStream din) throws IOException {
        String url = din.readUTF();
        editor.putString("URL", url).commit();
        Log.d("当前播放的网页是：", SP.getString("URL", "无"));
    }
}
