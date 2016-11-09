package com.multi.download;

/**
 * Created by zhaolei on 2016/11/9.
 */
public interface IDownLoadListener {

    /**
     * 一个task下载完成
     */
    void onDownloadPartSuccess();

    /**
     * 某个task下载失败
     */
    void onDownloadPartFail();

    /**
     * 任务取消
     */
    void onCancel();

    /**
     * 每次向buffer中读取的大小
     */
    void onIncrement(long increment);
}
