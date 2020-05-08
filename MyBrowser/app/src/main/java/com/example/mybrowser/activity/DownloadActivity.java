package com.example.mybrowser.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mybrowser.R;
import com.example.mybrowser.download.DownloadProgressListener;
import com.example.mybrowser.download.FileDownloadered;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {
    private EditText editpath;
    private Button btndown;
    private Button btnstop;
    private TextView textresult;
    private ProgressBar progressbar;
    private static final int PROCESSING = 1;
    private static final int FAILURE = -1;

    private Handler handler = new UIHander();

    private final class UIHander extends Handler{
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESSING:
                    int size = msg.getData().getInt("size");
                    progressbar.setProgress(size);
                    float num = (float)progressbar.getProgress() / (float)progressbar.getMax();
                    int result = (int)(num * 100);
                    textresult.setText(result+ "%");
                    if(progressbar.getProgress() == progressbar.getMax()){
                        Toast.makeText(getApplicationContext(), "文件下载成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FAILURE:
                    Toast.makeText(getApplicationContext(), "文件下载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);

        editpath = findViewById(R.id.editpath);
        btndown = findViewById(R.id.btndown);
        btnstop =  findViewById(R.id.btnstop);
        textresult = findViewById(R.id.textresult);
        progressbar = findViewById(R.id.progressBar);
        ButtonClickListener listener = new ButtonClickListener();
        btndown.setOnClickListener(listener);
        btnstop.setOnClickListener(listener);

        Intent intent=getIntent();
        String message=intent.getStringExtra(WebActivity.EXTRA_MESSAGE);
        editpath.setText(message);
    }


    private final class ButtonClickListener implements View.OnClickListener{
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btndown:
                    String path = editpath.getText().toString();
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                        File saveDir = Environment.getExternalStorageDirectory();
                        download(path, saveDir);
                    }else{
                        Toast.makeText(getApplicationContext(), "sd卡读取失败", Toast.LENGTH_SHORT).show();
                    }
                    btndown.setEnabled(false);
                    btnstop.setEnabled(true);
                    break;

                case R.id.btnstop:
                    exit();
                    btndown.setEnabled(true);
                    btnstop.setEnabled(false);
                    break;
            }
        }
        private DownloadTask task;
        public void exit(){
            if(task!=null) task.exit();
        }
        private void download(String path, File saveDir) {//运行在主线程
            task = new DownloadTask(path, saveDir);
            new Thread(task).start();
        }
        private final class DownloadTask implements Runnable{
            private String path;
            private File saveDir;
            private FileDownloadered loader;
            public DownloadTask(String path, File saveDir) {
                this.path = path;
                this.saveDir = saveDir;
            }
            public void exit(){
                if(loader!=null) loader.exit();
            }

            public void run() {
                try {
                    loader = new FileDownloadered(getApplicationContext(), path, saveDir, 3);
                    progressbar.setMax(loader.getFileSize());
                    loader.download(new DownloadProgressListener() {
                        public void onDownloadSize(int size) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.getData().putInt("size", size);
                            handler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendMessage(handler.obtainMessage(-1));
                }
            }
        }
    }
}