package pad.wlw.fjxx.demo.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pad.wlw.fjxx.demo.R;

public class  ImageFragment extends Fragment {

    //图片地址
    private String PATH_URL=Environment.getExternalStorageDirectory().toString()+"/FJXX/Pictures";
    //切换时间（分钟）
    private int date=1;
    private ImageView image1, image2;
    private ImageView[] imageViews = new ImageView[2];
    private int currentpos = 1;
    private Animator animator1, animator2;
    private List<String> imageNames;

    Timer timer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_image,container,false);
        imageNames=getAllName(PATH_URL);

        initView(view);

        return view;
    }

    @SuppressLint("ResourceType")
    private void initView(View view) {
        image1=view.findViewById(R.id.image1);
        image2=view.findViewById(R.id.image2);
        imageViews[0]=image1;
        imageViews[1]=image2;

        image1.setImageURI(Uri.fromFile(new File(imageNames.get(0))));
        image2.setImageURI(Uri.fromFile(new File(imageNames.get(1))));
        animator1 = AnimatorInflater.loadAnimator(getActivity(), R.anim.out_anim);
        animator2 = AnimatorInflater.loadAnimator(getActivity(), R.anim.in_anim);
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentpos++;
                if (currentpos % imageViews.length == 0) {
                    image1.setImageURI(Uri.fromFile(new File(imageNames.get(currentpos % imageNames.size()))));
                } else if (currentpos % imageViews.length == 1) {
                    image2.setImageURI(Uri.fromFile(new File(imageNames.get(currentpos % imageNames.size()))));
                }
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animator1.setTarget(imageViews[(currentpos + 1) % imageViews.length]);
                        animator2.setTarget(imageViews[currentpos % imageViews.length]);
                        animator1.start();
                        animator2.start();
                    }
                });
            }
        }, 5000, date*60*1000);


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

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
