package com.multi.download;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by zhaolei on 2016/11/9.
 * 多线程下载
 */
public class DownLoadThread extends Thread {

    private String mUrl;
    private String mDestPath;
    private long mStartPosition;
    private long mEndPosition;
    private boolean mIsCancel;
    private IDownLoadListener mDownLoadListener;


    /**
     * @param url           下载url
     * @param destPath      下载文件保存位置
     * @param startPosition 下载起始位置
     * @param endPosition   下载结束位置
     */
    public DownLoadThread(String url, String destPath, long startPosition, long endPosition, IDownLoadListener downLoadListener) {
        this.mUrl = url;
        this.mDestPath = destPath;
        this.mStartPosition = startPosition;
        this.mEndPosition = endPosition;
        this.mDownLoadListener = downLoadListener;
    }

    public boolean isCancel() {
        return mIsCancel;
    }

    public void setIsCancel(boolean isCancel) {
        this.mIsCancel = isCancel;
    }

    @Override
    public void run() {
        synchronized (this) {
            InputStream in = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                //指定从服务器下载的位置
                conn.setRequestProperty("Range", "bytes=" + mStartPosition + "-" + mEndPosition);
                int code = conn.getResponseCode();
                if ((code + "").startsWith("2")) {
                    in = conn.getInputStream();
                    raf = new RandomAccessFile(mDestPath, "rwd");
                    //偏移读写位置
                    raf.seek(mStartPosition);
                    int len;
                    byte[] buffer = new byte[4*1024];
                    while ((len = in.read(buffer)) != -1 && !mIsCancel) {
                        raf.write(buffer, 0, len);
                        Log.i("download",getName()+"  下载了 "+len);
                        if (mDownLoadListener != null) {
                            mDownLoadListener.onIncrement(len);
                        }
                    }

                    if (!mIsCancel) {
                        if (mDownLoadListener != null) {
                            mDownLoadListener.onDownloadPartSuccess();
                        }
                    } else {
                        mDownLoadListener.onCancel();
                    }


                } else {
                    if (mDownLoadListener != null) {
                        mDownLoadListener.onDownloadPartFail();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownloadPartFail();
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
