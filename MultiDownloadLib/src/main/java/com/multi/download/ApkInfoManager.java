package com.multi.download;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by zhaolei on 2016/11/9.
 */
public class ApkInfoManager {
    public static String getApkParent(Context context){
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            StringBuffer sb = new StringBuffer();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append(File.separator);
            sb.append(context.getPackageName());
            sb.append(File.separator);
            sb.append("apk");
            return sb.toString();
        }
        return null;
    }

    public static String getApkName(){
        StringBuffer sb = new StringBuffer();
        sb.append(System.currentTimeMillis());
        sb.append("_");
        sb.append("shilian.apk");
        return sb.toString();
    }
}
