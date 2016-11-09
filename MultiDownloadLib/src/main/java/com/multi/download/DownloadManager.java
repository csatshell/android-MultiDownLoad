package com.multi.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhaolei on 2016/11/9.
 */
public class DownloadManager implements IDownLoadListener {
    private static final int DEFAULT_DOWNLOAD_THREAD_NUMBER = 5;
    private static final DownloadManager sInstance = new DownloadManager();
    private List<DownLoadThread> mTasks = new ArrayList<>();
    private DownLoadEntity mEntity;
    private Context mContext;
    private long mDownloadedLength;
    private RemoteViews mRemoteViews;
    private NotificationCompat.Builder mBuilder;

    private NotificationManager mNotificationManager;

    private int mNotifyId = 9999;
    private int mTaskNumber;
    private int mCompletedThreadNumber;
    private Notification nitify;

    private DownloadManager() {

    }

    public static DownloadManager getInstance() {
        return sInstance;
    }

    public DownloadManager init(Context context, DownLoadEntity entity) {
        this.mContext = context;
        this.mEntity = entity;
        mTaskNumber = (mEntity.getTaskNumber() == 0 ? DEFAULT_DOWNLOAD_THREAD_NUMBER : mEntity.getTaskNumber());
        prepareStart();
        return sInstance;
    }

    public void stop() {
        for (DownLoadThread task : mTasks) {
            task.setIsCancel(true);
        }

        ComponentName name = new ComponentName(mContext.getPackageName(), "com.multi.download.DownloadService");
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_DOWNLOAD_SERVICE_STOP);
        intent.setComponent(name);
        mContext.stopService(intent);
    }

    public void prepareStart() {
        ComponentName name = new ComponentName(mContext.getPackageName(), "com.multi.download.DownloadService");
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_DOWNLOAD_SERVICE_START);
        intent.putExtra(DownloadService.KEY_DOWNLOAD_SERVICE_PARENT_DIR, mEntity.getParentDir());
        intent.putExtra(DownloadService.KEY_DOWNLOAD_SERVICE_FILE_NAME, mEntity.getFileName());
        intent.putExtra(DownloadService.KEY_DOWNLOAD_SERVICE_URL, mEntity.getUrl());
        intent.setComponent(name);
        mContext.startService(intent);
    }

    private long startTime;

    public void start() {
        if (mEntity.getTotalLength() <= 0) {
            throw new IllegalArgumentException("下载文件的长度要设置&&长度>=0,需要调用DownLoadManager--->setTotalLength");
        }
        Log.i("download", "开启线程个数:" + mTaskNumber);
        long blockSize = mEntity.getTotalLength() / mTaskNumber;
        for (int i = 1; i <= mTaskNumber; i++) {
            long startIndex = (i - 1) * blockSize;
            long endIndex = i * blockSize - 1;
            if (i == mTaskNumber) {
                endIndex = mEntity.getTotalLength();
            }
            DownLoadThread task = new DownLoadThread(mEntity.getUrl(), mEntity.getParentDir() + File.separator + mEntity.getFileName(), startIndex, endIndex, this);
            mTasks.add(task);
        }

        initNotify();
        for (DownLoadThread task : mTasks) {
            task.start();
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onDownloadPartSuccess() {
        synchronized (this) {
            mCompletedThreadNumber++;
            if (mCompletedThreadNumber >= mTaskNumber) {
                showCustomProgressNotify(100);
                //表明任务下载全部完成
                installApk();
                Log.i("download", (System.currentTimeMillis() - startTime) + "  耗时共计");
                stop();
            }
        }
    }

    private void installApk() {
        File apkFile = new File(mEntity.getParentDir() + File.separator + mEntity.getFileName());
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    @Override
    public void onDownloadPartFail() {
        //如果有其中一个任务失败,那么整个任务就是失败了
        Toast.makeText(mContext, "任务下载失败", Toast.LENGTH_SHORT).show();
        stop();
    }

    @Override
    public void onCancel() {
        Toast.makeText(mContext, "取消下载成功", Toast.LENGTH_SHORT).show();
    }

    private int oldProgress;

    @Override
    public void onIncrement(long increment) {
        mDownloadedLength += increment;
        int progress = (int) (mDownloadedLength * 100 / mEntity.getTotalLength());
        if (progress != oldProgress) {
            showCustomProgressNotify(progress);
            oldProgress = progress;
        }
    }

    public void setTotalLength(long totalLength) {
        mEntity.setTotalLength(totalLength);
    }

    /**
     * 初始化通知栏
     */
    private void initNotify() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setWhen(System.currentTimeMillis())
                .setContentIntent(getDefalutIntent(0)).setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.mipmap.ic_logo);
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.layout_download_notification);
        mRemoteViews.setImageViewResource(R.id.custom_progress_icon, R.mipmap.ic_logo);
        mRemoteViews.setTextViewText(R.id.tv_custom_progress_title, (mContext.getResources().getString(R.string.app_name)));


        mRemoteViews.setTextViewText(R.id.tv_custom_progress_status, 0 + "%");
        mRemoteViews.setProgressBar(R.id.custom_progressbar, 100, 0, false);
        mRemoteViews.setViewVisibility(R.id.custom_progressbar, View.VISIBLE);

        mBuilder.setContent(mRemoteViews).setContentIntent(getDefalutIntent(0)).setTicker("诗恋更新");
        nitify = mBuilder.build();
        nitify.contentView = mRemoteViews;
        mNotificationManager.notify(mNotifyId, nitify);
    }

    /**
     * 显示自定义的带进度条通知栏
     */
    private void showCustomProgressNotify(int progress) {
        if (progress >= 100) {
            mRemoteViews.setTextViewText(R.id.tv_custom_progress_status, "下载完毕");
            mRemoteViews.setProgressBar(R.id.custom_progressbar, 0, 0, false);
            mRemoteViews.setViewVisibility(R.id.custom_progressbar, View.GONE);
        } else {
            mRemoteViews.setTextViewText(R.id.tv_custom_progress_status, progress + "%");
            mRemoteViews.setProgressBar(R.id.custom_progressbar, 100, progress, false);
            mRemoteViews.setViewVisibility(R.id.custom_progressbar, View.VISIBLE);
        }
        mNotificationManager.notify(mNotifyId, nitify);
    }

    /**
     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
     * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT 点击去除：
     * Notification.FLAG_AUTO_CANCEL
     */
    private PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, new Intent(), flags);
        return pendingIntent;
    }
}
