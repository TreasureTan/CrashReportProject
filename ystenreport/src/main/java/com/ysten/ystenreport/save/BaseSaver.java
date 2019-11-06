package com.ysten.ystenreport.save;

import android.content.Context;

import com.ysten.ystenreport.encryption.IEncryption;
import com.ysten.ystenreport.utils.LogUtil;


/**
 * 提供通用的保存操作log的日志和设备信息的方法
 */
public abstract class BaseSaver implements ISave {

    private final static String TAG = "Report_BaseSaver";

    protected String mLogUtilFolder;
    protected Context mContext;

    /**
     * 加密方式
     */
    public static IEncryption mEncryption;

    public BaseSaver(Context context,String logRootDirectory) {
        this.mContext = context;
        mLogUtilFolder = logRootDirectory;
    }
    
    
  
    @Override
    public void setEncodeType(IEncryption encodeType) {
        mEncryption = encodeType;
    }

    public String encodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.encrypt(content);
            } catch (Exception e) {
                LogUtil.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }

        return content;

    }

    public String decodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.decrypt(content);
            } catch (Exception e) {
                LogUtil.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }
        return content;
    }
}
