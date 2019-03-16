package pad.wlw.fjxx.demo.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pad.wlw.fjxx.demo.R;
import pad.wlw.fjxx.demo.view.MyVideoView;

public class VideoFragment extends Fragment {
    private MyVideoView mMyVideoView;
    //视频地址
    private String PATH_URL=Environment.getExternalStorageDirectory().toString()+"/FJXX/Movies";
    //视频名称
    List<String> pathNames=null;
    //视频x变量
    private int x=0;


    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        pathNames=getAllName(PATH_URL);
        initView(view);

        return view;
    }


    private void initView(View view) {
        mMyVideoView = view.findViewById(R.id.videoView);
        mMyVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                x++;
                if (x>pathNames.size()-1){
                    x=0;
                }
                mMyVideoView.setVideoPath(pathNames.get(x));
                mMyVideoView.start();
            }
        });
        if (pathNames.size()>0){
            mMyVideoView.setVideoPath(pathNames.get(x));
            mMyVideoView.start();
        }
    }

    //获取一个文件下的所有文件名
    private List<String> getAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (file.length() == 0) {
            Log.i("getAllName", "空目录");
            return null;
        }
        List<String> strings=new ArrayList<>();
        for (int i=0;i<files.length;i++){
            strings.add(files[i].getAbsolutePath());
        }
        return strings;
    }
}
