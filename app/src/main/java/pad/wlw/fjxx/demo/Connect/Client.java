package pad.wlw.fjxx.demo.Connect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
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

    @SuppressLint("CommitPrefEdits")
    public Client(Context context, String IP) throws IOException {
        super(IP, SERVER_PORT);
        SERVER_IP = IP;
        this.client = this;
        Log.i("Seccess", "成功连接到服务器");
        this.setKeepAlive(true);
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
        save();
    }

    @SuppressLint("CommitPrefEdits")
    public Client(Context context) throws IOException {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        Log.i("Seccess", "成功连接到服务器");
        this.setKeepAlive(true);
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
        save();
    }

    private void save() {
//        saveFile(1);
        receiveFile(client);
    }

    /**
     * @param pathType
     * @1 Photo
     * @2 Movie
     */
    public void saveFile(int pathType) {
        try {
            dis = new DataInputStream(this.getInputStream());
            String fileName = dis.readUTF();
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + fileName);
//            @SuppressLint("SdCardPath") File file = new File("/sdcard/" + (pathType == 1 ? "Photo/" : "Movie/")
//                    + fileName);
            Log.i("文件目录", Environment.getExternalStorageDirectory().toString());
            fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = dis.read(bytes, 0, bytes.length)) != -1) {
                fos.write(bytes, 0, len);
                fos.flush();
            }
            Set<String> fileSet = new HashSet<>();
            fileSet.add(fileName);
            saveState(pathType, fileSet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                this.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            fileNum = din.readInt();
            fileinfos = new mFileInfo[fileNum];
            Set<String> names = new HashSet<>();
            for (int i = 0; i < fileNum; i++) {
                fileinfos[i] = new mFileInfo();
                String name = din.readUTF();
                names.add(name);
                fileinfos[i].mFileName = name;
                fileinfos[i].mFileSize = din.readLong();
            }
            //1指的是数据类型，1是Photo
            saveState(1, names);
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
//                        move(buf, writeLen, leftLen);
                        //代替上面那个move的作用
                        temp = new byte[8192];
//                        System.arraycopy(temp, 0, buf, writeLen, leftLen);
                        System.arraycopy(buf, writeLen, temp, 0, leftLen);
                        buf = temp;
                        break;
                    } else {
                        fout.write(buf, 0, bufferedLen); // 全部写入
                        writeLens += bufferedLen;
                        totalWriteLens += bufferedLen;
                        if (totalWriteLens >= totalSize) {
                            //mListener.report(GroupChatActivity.FAIL, null);
                            return;
                        }
                        leftLen = 0;
                    }
                    //mListener.report(GroupChatActivity.PROGRESS,
                    //(int) (totalWriteLens * 100 / totalSize));
                } // end while
                fout.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("文件接收失败", e.toString());
                Log.d("接收文件失败", "receive file Exception");
            }
        } // end for
        //mListener.report(GroupChatActivity.FAIL, null);
    }

    private void saveState(int pathType, Set<String> files) {
        editor.putInt("Type", pathType);
        editor.putStringSet("Files", files);
        editor.commit();
    }
}
