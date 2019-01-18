package pad.wlw.fjxx.demo.Util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * 安卓6.0以上的动态权限申请
 */
public class SD_Permission {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static int permission = -1;

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            if (!hasPermission(activity)) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //检测是否有写的权限
    public static boolean hasPermission(Activity activity) {
        if (permission == -1) {
            permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }
}
