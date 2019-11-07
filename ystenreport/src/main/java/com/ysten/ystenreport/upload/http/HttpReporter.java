package com.ysten.ystenreport.upload.http;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ysten.ystenreport.bean.CrashBean;
import com.ysten.ystenreport.bean.JsonBase;
import com.ysten.ystenreport.bean.ReportForHttpBean;
import com.ysten.ystenreport.upload.ILogUpload;
import com.ysten.ystenreport.utils.LogUtil;
import com.ysten.ystenreport.utils.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.security.auth.callback.Callback;

public class HttpReporter implements ILogUpload {
    private static final String TAG = "Report_httpReporter";
    // 每个post参数之间的分隔
    private static final String BOUNDARY = "----YstenReportPostBoundary123456789521251521"; // 定义数据分隔线
    private static final int TRY_REPORT_COUNT = 5;
    private static final int TIME_OUT = 10000;
    private Context mContext;

    public HttpReporter(Context context) {
        mContext = context;
    }

    @Override
    public void sendReportSync(String urlStr, ReportForHttpBean reportForHttpBean, String content, OnUploadFinishedCrashListener listener) {
        if (!NetUtil.isConnected(mContext)) {
            listener.onError("no net");
            return;
        }
        HttpURLConnection hc = null; // http连接器
        BufferedOutputStream requestBos = null;// byte输出流，用来读取服务器返回的信息

        BufferedReader responseReader = null;
        BufferedWriter writer = null;
        try {
            URL url = new URL(urlStr);
            hc = (HttpURLConnection) url.openConnection();

            hc.setRequestProperty("Connection", "Keep-Alive");
            hc.setRequestProperty("Cache-Control", "no-cache");
            hc.setRequestProperty("Accept-Encoding", "gzip");
            hc.setRequestProperty("Content-Encoding", "gzip");
            hc.setConnectTimeout(TIME_OUT);
            hc.setReadTimeout(TIME_OUT);
            hc.setDoOutput(true);
            hc.setDoInput(true);
            hc.setUseCaches(false);
            hc.setRequestMethod("POST");
            hc.setRequestProperty("Content-Type", "application/json;charset=utf-8");//设置参数类型是json格式
            hc.connect();

            requestBos = new BufferedOutputStream(hc.getOutputStream());


            CrashBean crashBean = new Gson().fromJson(content, CrashBean.class);
            reportForHttpBean.setData(crashBean);
            JSONObject jsonCrash = new JSONObject();
            try {
                jsonCrash.put("occTime", crashBean.getOccTime());
                jsonCrash.put("sdInfo", crashBean.getSdInfo());
                jsonCrash.put("memInfo", crashBean.getMemInfo());
                jsonCrash.put("errorType", crashBean.getErrorType());
                jsonCrash.put("exceptInfo", crashBean.getExceptInfo());
                jsonCrash.put("exceptStack", crashBean.getExceptStack());
                jsonCrash.put("threadInfo", crashBean.getThreadInfo());
                jsonCrash.put("allthreadStack", crashBean.getAllthreadStack());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            if (reportForHttpBean != null) {
                JSONObject json = new JSONObject();

                json.put("os", reportForHttpBean.getOs());
                json.put("appver", reportForHttpBean.getAppver());
                json.put("sdkver", reportForHttpBean.getSdkver());
                json.put("mac", reportForHttpBean.getMac());
                json.put("ts", reportForHttpBean.getTs());
                json.put("type", reportForHttpBean.getType());
                json.put("model", reportForHttpBean.getModel());
                json.put("app_id", reportForHttpBean.getApp_id());
                json.put("network", reportForHttpBean.getNetwork());
                json.put("data", jsonCrash);
                String body = json.toString();
                byte[] zipBody = GzipCompress(body, "UTF-8");

                requestBos.write(zipBody);
                requestBos.flush();
                requestBos.close();
                Log.i(TAG, "body=" + body);
//                writer = new BufferedWriter(new OutputStreamWriter(hc.getOutputStream(), "UTF-8"));
//                writer.write(body);
//                writer.close();
                int resultCode = hc.getResponseCode();

                LogUtil.i(TAG, "sendReport resultCode = " + resultCode);
                if (HttpURLConnection.HTTP_OK == resultCode) {
                    LogUtil.i(TAG,"Content-Length="+hc.getRequestProperty("Content-Length"));

                    StringBuffer sb = new StringBuffer();
                    String readLine = new String();
                    responseReader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "UTF-8"));
                    while ((readLine = responseReader.readLine()) != null) {
                        sb.append(readLine).append("\n");
                    }
                    LogUtil.i(TAG, "sendReport result:" + sb.toString());
                    JsonBase jsonBase = new Gson().fromJson(sb.toString(), JsonBase.class);
                    if (jsonBase != null && jsonBase.getCode() == 200) {
                        listener.onSuccess();
                    } else {
                        listener.tryAgain();
                    }

                } else if (HttpURLConnection.HTTP_GATEWAY_TIMEOUT == resultCode) {
                    listener.tryAgain();
                } else {
                    listener.onError(resultCode + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.tryAgain();
        } finally {
            try {
                if (requestBos != null)
                    requestBos.close();
                if (responseReader != null) {
                    responseReader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


    @Override
    public void sendReportWithFileSync(String urlStr, ReportForHttpBean reportForHttpBean, String file,byte[] zipAnr, OnUploadFinishedListener listener) {
        if (!NetUtil.isConnected(mContext)) {
            listener.onError("no net");
            return;
        }
        HttpURLConnection hc = null; // http连接器
        BufferedOutputStream requestBos = null;// byte输出流，用来读取服务器返回的信息
        BufferedReader responseReader = null;
        try {
            URL url = new URL(urlStr);
            hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            hc.setRequestProperty("Accept-Encoding", "gzip");
            hc.setRequestProperty("Content-Encoding", "gzip");
            hc.setRequestProperty("Connection", "Keep-Alive");
            hc.setRequestProperty("Charsert", "UTF-8");
            hc.setConnectTimeout(TIME_OUT);
            hc.setReadTimeout(TIME_OUT);
            hc.setDoOutput(true);
            hc.setDoInput(true);
            hc.setUseCaches(false);
            hc.setRequestMethod("POST");
            hc.connect();

            requestBos = new BufferedOutputStream(hc.getOutputStream());

            String boundary = BOUNDARY;
            StringBuffer resSB = new StringBuffer();

            //先写头boundary
            requestBos.write(("--" + boundary + "\r\n").getBytes("utf-8"));

            //// 1.先写文字 strParams 1:key 2:value
            if (reportForHttpBean != null) {
                String anrLog = new Gson().toJson(reportForHttpBean);
                resSB.append("Content-Disposition: form-data; name=").append("anrLogParams").append("\r\n")
                        .append("\r\n").append(anrLog).append("\r\n").append("--").append(boundary).append("\r\n");
            }

            requestBos.write(resSB.toString().getBytes());
            // 2.再写文件 post流
            // fileParams 1:fileField, 2.fileName, 3.fileType, 4.filePath
            resSB = new StringBuffer();
            if (file != null) {
                File upFile = new File(file);
                resSB.append("Content-Disposition: form-data; name=file").append("; filename=")
                        .append(upFile.getName()).append("\r\n").append("Content-Type: application/octet-stream")
                        .append("\r\n\r\n");

                requestBos.write(resSB.toString().getBytes("utf-8"));
                requestBos.write(zipAnr);

//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                DataInputStream in = new DataInputStream(new FileInputStream(upFile));
                GZIPOutputStream gzip;
                int bytes = 0;
//                try {
//                    gzip = new GZIPOutputStream(out);
//                    byte[] bufferOut = new byte[1024 * 5];
//                    while ((bytes = in.read(bufferOut)) != -1) {
//                        gzip.write(bufferOut, 0, bytes);
//
//                    }
//                    requestBos.write(out.toByteArray(), 0, bytes);
//                    gzip.close();
//                } catch (IOException e) {
//                    LogUtil.e("gzip compress error.", e.getMessage());
//                }
//
//                in.close();
            }

            // 尾
            String endBoundary = "\r\n--" + boundary + "--\r\n";
            // 3.最后写结尾
            requestBos.write(endBoundary.getBytes("utf-8"));
            requestBos.flush();
            requestBos.close();

            int resultCode = hc.getResponseCode();

            LogUtil.i(TAG, "sendReportWithFile resultCode = " + resultCode);
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                responseReader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                LogUtil.i(TAG, "sendReportWithFile result:" + sb.toString());
                JsonBase jsonBase = new Gson().fromJson(sb.toString(), JsonBase.class);
                if (jsonBase != null && jsonBase.getCode() == 200) {
                    listener.onSuccess();
                } else {
                    listener.tryAgain();
                }
            } else if (HttpURLConnection.HTTP_GATEWAY_TIMEOUT == resultCode) {
                listener.tryAgain();
            } else {
                listener.onError(resultCode + "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            listener.tryAgain();
        } finally {
            try {
                if (requestBos != null)
                    requestBos.close();
                if (responseReader != null) {
                    responseReader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void sendReportWithFileASync(String target, Map<String, String> heads, String content, String file,
                                        OnUploadFinishedListener onUploadFinishedListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendReportASync(String urlStr, Map<String, String> heads, String content,
                                OnUploadFinishedListener onUploadFinishedListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getConfig(String urlStr, ReportForHttpBean reportForHttpBean,ConfigFinishedListener listener) {
        if (!NetUtil.isConnected(mContext)) {
            listener.onError("no net");
            return;
        }
        HttpURLConnection hc = null; // http连接器
        BufferedOutputStream requestBos = null;// byte输出流，用来读取服务器返回的信息
        BufferedReader responseReader = null;
        BufferedWriter writer = null;
        try {
            URL url = new URL(urlStr);
            hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("Connection", "Keep-Alive");
            hc.setRequestProperty("Cache-Control", "no-cache");
            hc.setConnectTimeout(TIME_OUT);
            hc.setReadTimeout(TIME_OUT);
            hc.setDoOutput(true);
            hc.setDoInput(true);
            hc.setUseCaches(false);
            hc.setRequestMethod("POST");
            hc.setRequestProperty("Content-Type", "application/json;charset=utf-8");//设置参数类型是json格式
            hc.connect();
//            requestBos = new BufferedOutputStream(hc.getOutputStream());


            if (reportForHttpBean != null) {
                JSONObject json = new JSONObject();
                json.put("os", reportForHttpBean.getOs());
                json.put("appver", reportForHttpBean.getAppver());
                json.put("sdkver", reportForHttpBean.getSdkver());
                json.put("mac", reportForHttpBean.getMac());
                json.put("model", reportForHttpBean.getModel());
                json.put("app_id", reportForHttpBean.getApp_id());
                json.put("network", reportForHttpBean.getNetwork());
                String body = json.toString();
                Log.i(TAG, "body=" + body);
//                byte[] zipBody = GzipCompress(body, "UTF-8");
//                requestBos.write(zipBody);
//                requestBos.flush();
//                requestBos.close();

                writer = new BufferedWriter(new OutputStreamWriter(hc.getOutputStream(), "UTF-8"));
                writer.write(body);
                writer.close();
                int resultCode = hc.getResponseCode();
                LogUtil.i(TAG, "sendReport resultCode = " + resultCode);
                if (HttpURLConnection.HTTP_OK == resultCode) {
                    StringBuffer sb = new StringBuffer();
                    String readLine = new String();
                    responseReader = new BufferedReader(new InputStreamReader(hc.getInputStream(), "UTF-8"));
                    while ((readLine = responseReader.readLine()) != null) {
                        sb.append(readLine).append("\n");
                    }
                    LogUtil.i(TAG, "sendReport result:" + sb.toString());
                    JsonBase jsonBase = new Gson().fromJson(sb.toString(), JsonBase.class);
                    if (jsonBase != null && jsonBase.getCode() == 200) {
                        listener.onSuccess(sb.toString());
                    } else {
                        listener.tryAgain();
                    }

                } else if (HttpURLConnection.HTTP_GATEWAY_TIMEOUT == resultCode) {
                    listener.tryAgain();
                } else {
                    listener.onError(resultCode + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.tryAgain();
        } finally {
            try {
                if (requestBos != null)
                    requestBos.close();
                if (responseReader != null) {
                    responseReader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


    public static byte[] GzipCompress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
            gzip.close();
        } catch (IOException e) {
            LogUtil.e("gzip compress error.", e.getMessage());
        }
        return out.toByteArray();
    }

    public interface GzipUploadListener{
        void onSuccess(byte[] zipAnr);
        void onFail(String error);
    }

    @Override
    public void  GzipUploadFile(String anrFile, GzipUploadListener callback){
        File upFile = new File(anrFile);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(upFile));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip;
            int bytes = 0;
            try {
                gzip = new GZIPOutputStream(out);
                byte[] bufferOut = new byte[1024 * 5];
                while ((bytes = in.read(bufferOut)) != -1) {
                    gzip.write(bufferOut, 0, bytes);

                }
                gzip.close();
                byte[] gzipAnr = out.toByteArray();
                callback.onSuccess(gzipAnr);
            } catch (IOException e) {
                LogUtil.e("gzip compress error.", e.getMessage());
                callback.onFail(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onFail(e.getMessage());
        }
    }

}
