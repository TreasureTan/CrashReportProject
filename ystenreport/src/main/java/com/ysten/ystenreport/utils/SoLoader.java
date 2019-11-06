package com.ysten.ystenreport.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class SoLoader {
	private static final String TAG = "Report_SoLoader";
	
    private static Context mAppContext;
    private static File mLibDir;
    public static void setAppContext(Context appContext){
    	mAppContext = appContext.getApplicationContext();
    	String mFilePath = mAppContext.getFilesDir().getAbsolutePath() + "/";
        mLibDir = new File(mFilePath + "crash_lib/");
        if(!mLibDir.exists()){
        	mLibDir.mkdirs();
        }
    }
    
    public static void setApkPath(){
    	
    }

    public static void loadLibrary(String libName){
        if(TextUtils.isEmpty(libName)){
            throw new RuntimeException("can not load library without a name");
        }
        try{
            Runtime.getRuntime().loadLibrary(libName);
        }catch(Error error){
            if(!mLibDir.exists()){
            	mLibDir.mkdirs();
            }
            if(!mLibDir.exists()){
                throw error;
            }
            String soName = System.mapLibraryName(libName);
            if(supportArmeabi()){
                File soFile = null;
                ZipFile apkFile;
				try {
					LogUtil.i(TAG, "source dir:" + mAppContext.getApplicationInfo().sourceDir);
					apkFile = new ZipFile(mAppContext.getApplicationInfo().sourceDir);
				} catch (IOException e) {
					throw error;
				}
                if(findLocalLibrary(soName) == null){
                	LogUtil.i(TAG,"soName:" + soName + " is not exist,try copy from apk");
                    extractSoFromApk(apkFile,soName);
                }
                if((soFile=findLocalLibrary(soName))!=null){
                	LogUtil.i(TAG,"load library:" + soFile.getAbsolutePath());
                    System.load(soFile.getAbsolutePath());
                }else{
                    throw error;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean supportArmeabi(){
        if(Build.VERSION.SDK_INT>=21) {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null) {
                for (String abi : abis) {
                    if (abi.equalsIgnoreCase("armeabi")) {
                        return true;
                    }
                }
            }
        }else{
            if(Build.CPU_ABI.contains("armeabi") || Build.CPU_ABI2.contains("armeabi")){
                return true;
            }
        }

        return false;
    }

    private static File findLocalLibrary(String libraryName){
        File soFile;
        if(mLibDir.exists() && (soFile=new File(mLibDir,libraryName)).exists()){
            return soFile;
        }else{
            return null;
        }
    }

	
	private static void extractSoFromApk(ZipFile apkFile,String soName){
        if(findLocalLibrary(soName) != null){
            return;
        }
        LogUtil.i(TAG,"copy from apk start!!");
        try {
            int retryCount = 2;
            do {
                --retryCount;
                String entryName = String.format("lib/armeabi/%s", soName);
                ZipEntry targetEntry = null;
                if (apkFile != null && (targetEntry = apkFile.getEntry(entryName)) != null) {
                    File targetFile = new File(mLibDir, soName);
                    File targetFileTmp = new File(mLibDir,soName+".tmp");
                    if(!targetFileTmp.exists() || targetFileTmp.length()!=targetEntry.getSize()) {
                        if(targetFileTmp.exists()){
                            targetFileTmp.delete();
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(
                                new FileOutputStream(targetFileTmp.getAbsolutePath()));
                        BufferedInputStream bi = new BufferedInputStream(apkFile.getInputStream(targetEntry));
                        byte[] readContent = new byte[512];
                        int readCount = bi.read(readContent);
                        while (readCount != -1) {
                            bos.write(readContent, 0, readCount);
                            readCount = bi.read(readContent);
                        }
                        try {
                            bos.close();
                            bi.close();
                        } catch (Throwable e) {
                        }
                    }
                    if (targetFileTmp.exists()) {
                        if(targetFileTmp.length() == targetEntry.getSize()){
                            targetFileTmp.renameTo(targetFile);
                            if(!targetFile.exists()){
                                targetFileTmp.renameTo(targetFile);
                            }
                            if(targetFile.exists()){
                            	LogUtil.i(TAG,"copy from apk is ok!!!");
                                break;
                            }
                        }else{
                            targetFileTmp.delete();
                        }
                    }

                }
            } while (retryCount > 0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
