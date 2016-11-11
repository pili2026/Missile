package mcs;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;


import com.example.thunder.missile.R;

import java.io.File;

/**
 * Created by jeremy on 2016/6/29.
 */
public class VideoView extends AppCompatActivity {
    private android.widget.VideoView v;
    private String[] playList;
    private int playingIndex = 0;
    public String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE); // set no title
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // set
        // fullscreen
        setContentView(R.layout.animation);
        Intent data = getIntent(); // 接收從上一activity傳來的參數
        Bundle bundle = data.getExtras();
        String temp = bundle.getString("playList");
        temp=temp.substring(temp.indexOf("&")+1, temp.length());
        System.out.println("看清單拉  "+temp);
        playList = temp.split("&");

        v = (android.widget.VideoView) findViewById(R.id.videoView1);

        v.setMediaController(new MediaController(VideoView.this));

        v.setVideoURI(Uri.parse(sdcardPath + playList[playingIndex]));
        v.setSoundEffectsEnabled(v.isSoundEffectsEnabled());
        v.requestFocus();
        v.start();
        // ------------------------------------
		/*
		 * v.setVideoURI(Uri.parse("/sdcard/"+playList[playingIndex]));
		 * v.requestFocus(); v.start();
		 */
        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
            }
        });
        v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                playingIndex++;
                if (playingIndex < playList.length)  {

                    v.setVideoPath(sdcardPath + playList[playingIndex]);
                    v.requestFocus();
                    v.start();
                }else{

                    //finish();
                }
				/*
				 * else arg0.release(); //finish();
				 */
            }
        });

    }

    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }
}
