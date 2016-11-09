package com.zl.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.multi.download.ApkInfoManager;
import com.multi.download.DownLoadEntity;
import com.multi.download.DownloadManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url = "http://122.72.112.193/imtt.dd.qq.com/16891/8DA33A2D28BB72929C9D888EE2A06A9E.apk?mkey=5822d3aa43d6a29a&f=8b5d&c=0&fsname=ren.shilian_1.1.0_4.apk&hsr=4d5s&p=.apk";
        startDownloadAPK(url);
    }

    private void startDownloadAPK(String url) {
        String parentDir = ApkInfoManager.getApkParent(this);
        if(TextUtils.isEmpty(parentDir)){
            Toast.makeText(this,"SD卡未挂载",Toast.LENGTH_SHORT).show();
            return;
        }
        String apkName = ApkInfoManager.getApkName();
        DownLoadEntity entity = new DownLoadEntity(url,parentDir,4,apkName);
        DownloadManager.getInstance().init(this,entity);

    }

}
