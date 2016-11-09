package com.multi.download;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 设置带通知栏的后台下载
 *
 * @author zhaolei 2015-1-31
 */
public class DownloadService extends Service {

    public static final String KEY_DOWNLOAD_SERVICE_URL = "com.multi.download.service.url";
    public static final String KEY_DOWNLOAD_SERVICE_PARENT_DIR = "com.multi.download.service.parent.dir";
    public static final String KEY_DOWNLOAD_SERVICE_FILE_NAME = "com.multi.download.service.file.name";
    public static final String ACTION_DOWNLOAD_SERVICE_START = "com.multi.download.service.start";
    public static final String ACTION_DOWNLOAD_SERVICE_STOP = "com.multi.download.service.stop";
    private File apkFile;
    private String mUrl;
    private String mParentDir;
    private String mFileName;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_DOWNLOAD_SERVICE_START.equals(action)) {
                //开启下载
                mUrl = intent.getStringExtra(KEY_DOWNLOAD_SERVICE_URL);
                mParentDir = intent.getStringExtra(KEY_DOWNLOAD_SERVICE_PARENT_DIR);
                mFileName = intent.getStringExtra(KEY_DOWNLOAD_SERVICE_FILE_NAME);
                download();
            }else if(ACTION_DOWNLOAD_SERVICE_STOP.equals(action)){
                //停止下载

            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void download() {
        Thread downloadThread = new Thread(mdownApkRunnable);
        downloadThread.start();
    }

    private Runnable mdownApkRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                URL url = new URL(mUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    //apk大小
                    long length = conn.getContentLength();
                    Log.i("download", "文件总长度：" + length);

                    File apkDir = new File(mParentDir);
                    if (!apkDir.exists()) {
                        apkDir.mkdirs();
                    }
                    apkFile = new File(apkDir, mFileName);
                    RandomAccessFile raf = new RandomAccessFile(apkFile.getAbsolutePath(), "rwd");
                    raf.setLength(length);
                    raf.close();
                    DownloadManager.getInstance().setTotalLength(length);
                    DownloadManager.getInstance().start();

                } else {
                    Toast.makeText(getBaseContext(),"下载失败",Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {

            }
        }
    };


}
