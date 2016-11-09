package com.multi.download;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by zhaolei on 2016/11/9.
 */
public class DownLoadEntity {
    private String url;
    private String parentDir;
    private int taskNumber;
    private long totalLength;
    private String fileName;
    public DownLoadEntity(String url, String parentDir, int taskNumber, String fileName) {
        this.url = url;
        this.parentDir = parentDir;
        this.fileName =fileName;
        this.taskNumber = taskNumber;
        Executor te = Executors.newFixedThreadPool(3);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }
}
