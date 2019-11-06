package com.ysten.ystenreport.upload;

import com.ysten.ystenreport.bean.ReportForHttpBean;

import java.util.Map;

/**
 * 上传日志的接口
 */
public interface ILogUpload {

    public interface OnUploadFinishedListener {
        void onSuccess();


        void onError(String error);

        void tryAgain();
    }

    public interface ConfigFinishedListener {
        void onSuccess(String data);
        void tryAgain();

        void onError(String error);

    }

    public interface OnUploadFinishedCrashListener {
        void onSuccess();


        void onError(String error);

        void tryAgain();
    }


    /**
     * 上传ANR文件
     * @param target
     * @param reportForHttpBean
     * @param file
     * @param listener
     */
    public void sendReportWithFileSync(String target, ReportForHttpBean reportForHttpBean,
                                          String file,OnUploadFinishedListener listener);

    /**
     * 上传crash
     * @param urlStr
     * @param reportForHttpBean
     * @param content
     * @return
     */
    public void sendReportSync(String urlStr, ReportForHttpBean reportForHttpBean, String content,OnUploadFinishedCrashListener listener);

    /**
     * 上传anr文件
     * @param target
     * @param heads
     * @param content
     * @param file
     * @param onUploadFinishedListener
     */
    public void sendReportWithFileASync(String target, Map<String, String> heads, String content,
                                        String file, OnUploadFinishedListener onUploadFinishedListener);

    public void sendReportASync(String urlStr, Map<String, String> heads, String content,
                                OnUploadFinishedListener onUploadFinishedListener);

    public void getConfig(String urlStr, ReportForHttpBean reportForHttpBean,ConfigFinishedListener listener);
}