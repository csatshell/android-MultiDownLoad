# MultiDownloadDemo
android版本更新多线程下载

使用方法:
1,manifest配置
a,配置权限
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />

b,配置service
<service android:name="com.multi.download.DownloadService" />

2,开启下载
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
