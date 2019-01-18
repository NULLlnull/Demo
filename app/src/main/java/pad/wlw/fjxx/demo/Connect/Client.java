package pad.wlw.fjxx.demo.Connect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Client extends Socket {
    private static final String SERVER_IP = "192.168.0.100";
    private static final int SERVER_PORT = 8899;
    private DataInputStream dis;
    private FileOutputStream fos;
    private Socket client;
    private SharedPreferences SP;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public Client(Context context) throws IOException {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        Log.i("Seccess", "成功连接到服务器");
        this.setKeepAlive(true);
        SP = context.getSharedPreferences("Save", Context.MODE_PRIVATE);
        editor = SP.edit();
        saveFile(1);
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
//            try {
//                if (fos != null)
//                    fos.close();
//                if (dis != null)
//                    dis.close();
//                this.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private void saveState(int pathType, Set<String> files) {
        editor.putInt("Type", pathType);
        editor.putStringSet("Files", files);
        editor.commit();
    }
}
