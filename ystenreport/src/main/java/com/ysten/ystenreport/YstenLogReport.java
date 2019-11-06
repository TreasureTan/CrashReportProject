package com.ysten.ystenreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ysten.ystenreport.bean.JsonForBaseParams;
import com.ysten.ystenreport.bean.ReportBean;
import com.ysten.ystenreport.bean.ReportForHttpBean;
import com.ysten.ystenreport.bean.ReportMaxAndCountBean;
import com.ysten.ystenreport.bean.SaveCrashBean;
import com.ysten.ystenreport.crash.CrashHandler;
import com.ysten.ystenreport.greendao.DbCore;
import com.ysten.ystenreport.save.ISave;
import com.ysten.ystenreport.save.imp.CrashWriter;
import com.ysten.ystenreport.upload.ILogUpload;
import com.ysten.ystenreport.upload.http.HttpReporter;
import com.ysten.ystenreport.utils.LogUtil;
import com.ysten.ystenreport.utils.MacUtil;
import com.ysten.ystenreport.utils.NetUtil;
import com.ysten.ystenreport.utils.NetworkUtil;
import com.ysten.ystenreport.utils.ReportChangeUtil;
import com.ysten.ystenreport.utils.SoLoader;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tanxiaozhong
 * on 2019-10-24 17:56.
 * describe:
 */
public class YstenLogReport {
    public static final String SDK_REPORT_VERSION = "report_1.0.1_bate";
    private static final String TAG = YstenLogReport.class.getSimpleName();
    private static YstenLogReport mLogReport;
    private boolean isInited = false;
    private Context mContext;
    private String mROOT;
    private ISave mLogSaver;
    private ILogUpload mUpload; //上传
    private long mStartTime = System.currentTimeMillis(); //获取当前时间
    private boolean isNetConnected = false;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private boolean mWifiOnly = false;
    /// 自定义初始化上报日志
    private static final int CMD_PREPARE_REPORT = 1;
    /// 准备上报日志
    private static final int CMD_START_REPORT = 2;
    /// 检查崩溃日志
    private static final int CMD_CHECK_UPLOAD = 3;

    ///重试上报
    private static final int RETRY_UPLOAD_CRASH = 5;

    private static final int RETRY_UPLOAD_ANR = 6;
    private static final int Max_REPORT_TIME = 600;
    private int currentTryReportTime = 0;
    private int tryCrashCount = 0;
    private int tryAnrCount = 0;
    private long tsMax = 0;
    private boolean isNeedNativeLog = false;
    private ReportForHttpBean reportForHttpBean;
    private boolean  isDebug = true;
    /**
     * 默认上报地址
     */
    private static String DEFAULT_REPORT_HOST = "";
    /**
     * crash 地址路径
     */
    private static final String REPORT_ACTION = "report";

    /**
     * anr 地址路径
     */
    private static final String ANR_ACTION = "report/upload";
    /**
     * config路径
     */
    private static final String BASE_PARAMS = "config";
    private static String REPORT_HOST = "";
    private static String REPORT_URL = REPORT_HOST + REPORT_ACTION;
    private static String ANR_URL = REPORT_HOST + ANR_ACTION;
    private static String BASE_PARAMS_URL = REPORT_HOST + BASE_PARAMS;
    /**
     * 上报的最大值
     */
    private static long REPORT_MAX = 5000;
    private int startReport = 0;
    private int heartTime = 24 * 60;
    private static String appId;

    private ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

    private YstenLogReport() {

    }

    public static YstenLogReport getInstance() {
        if (mLogReport == null) {
            synchronized (YstenLogReport.class) {
                if (mLogReport == null) {
                    mLogReport = new YstenLogReport();
                }
            }
        }
        return mLogReport;
    }


    /**
     * 初始化
     */
    public void init(Context context,boolean isDebug) {
        Log.i(TAG, "ReportSdk start init...");
        if (isInited) {
            return;
        }
        isInited = true;
        this.isDebug= isDebug;
        mContext = context.getApplicationContext();
        initData();
    }


    public void init(Context context, String appId,boolean isDebug) {
        Log.i(TAG, "ReportSdk start init...");
        if (isInited) {
            return;
        }
        isInited = true;
        this.appId = appId;
        this.isDebug = isDebug;
        mContext = context.getApplicationContext();
        initData();
    }

    public void setReportHost(String reportHost) {
        if (!TextUtils.isEmpty(reportHost)) {
            String lastHostChat = reportHost.substring(reportHost.length() - 1, reportHost.length());
            if (!lastHostChat.equals("/")) {
                reportHost = reportHost + "/";
            }
        }
        REPORT_HOST = reportHost;
        if (REPORT_HOST.equals(DEFAULT_REPORT_HOST)) {
            Log.i(TAG, "setReportHost 设置了相同域名");
            return;
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessage(CMD_PREPARE_REPORT);
        }
        DEFAULT_REPORT_HOST = REPORT_HOST;

    }


    private void initData() {
        //初始化数据库
        DbCore.init(mContext);
        DbCore.enableQueryBuilderLog();
        LogUtil.setIsDebug(isDebug);
        if (TextUtils.isEmpty(mROOT)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mROOT = getCacheDir(mContext) + "/crashLog/";
            } else {
                mROOT = getInternalDir() + "/crashLog/";
            }
        }

        File rootFile = new File(mROOT);
        if (!rootFile.exists()) {
            boolean mkdirs = rootFile.mkdirs();
            Log.e(TAG, "mkdirs==" + mkdirs);
        }

        SoLoader.setAppContext(mContext.getApplicationContext());

        if (mLogSaver == null) {
            mLogSaver = new CrashWriter(mContext, mROOT);
        }

        if (mUpload == null) {
            mUpload = new HttpReporter(mContext);
        }
        ReportChangeUtil.init(mStartTime, mContext);

        ///初始化 设置地址路径
        CrashHandler.getInstance().init1(mContext, mLogSaver, mROOT);

        isNetConnected = NetUtil.isConnected(mContext);
        mHandlerThread = new HandlerThread("crash_report");
        mHandlerThread.start();
        mHandler = new UploadHandler(mHandlerThread.getLooper());
        mHandler.sendEmptyMessage(CMD_PREPARE_REPORT);

        //监听网络变化
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        ConnectionChangeReceiver receiver = new ConnectionChangeReceiver();
        if (mContext != null) {
            mContext.registerReceiver(receiver, intentFilter);
        }

    }


    public void getAnrMessage() {
        mHandler.sendEmptyMessage(CMD_CHECK_UPLOAD);
    }

    class UploadHandler extends Handler {


        public UploadHandler(Looper looper) {
            super(looper);
        }

        private void checkCrashLog() {
            LogUtil.i(TAG, "try collect crash report-----");
            List<SaveCrashBean> beans = CrashHandler.getInstance().getAllCrashList();
            if (beans != null) {
                LogUtil.i(TAG, "report crash count:" + beans.size());
                Iterator<SaveCrashBean> iterator = beans.iterator();
                while (iterator.hasNext()) {
                    final SaveCrashBean report = iterator.next();
                    if (!report.getIsReport()) {
                        final String reportContent = report.getCrashLog();
                        LogUtil.i(TAG, "crash reportContent:" + reportContent);
                        if (reportForHttpBean != null) {
                            reportForHttpBean.setTs(Long.parseLong(report.getCrashTime()) - tsMax);
                            reportForHttpBean.setApp_id(appId);
                            reportForHttpBean.setType(ReportForHttpBean.TYPE_CRASH);
                            if (mContext != null) {
                                reportForHttpBean.setNetwork(NetworkUtil.getNetworkType(mContext));
                            }
                        }

                        mThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                mUpload.sendReportSync(REPORT_URL, reportForHttpBean, reportContent, new ILogUpload.OnUploadFinishedCrashListener() {
                                    @Override
                                    public void onSuccess() {
                                        LogUtil.i(TAG, "crash report success");
                                        tryCrashCount = 0;
                                        report.setIsReport(true);
                                        DbCore.getDaoSession().getSaveCrashBeanDao().update(report);

                                        ///新增策略 记录上报最大值
                                        updateMaxReportCount();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        LogUtil.i(TAG, "crash report fail");
                                    }

                                    @Override
                                    public void tryAgain() {
                                        LogUtil.i(TAG, "crash report tryAgain");
                                        ++tryCrashCount;
                                        currentTryReportTime = (int) Math.pow(2, tryCrashCount);
                                        if (currentTryReportTime >= Max_REPORT_TIME) {
                                            currentTryReportTime = Max_REPORT_TIME;
                                        }
                                        mHandler.sendEmptyMessageDelayed(RETRY_UPLOAD_CRASH, currentTryReportTime * 1000);
                                    }
                                });

                            }
                        });
                    }

                }

            }
        }

        private void checkErrorLog() {
            LogUtil.i(TAG, "try collect error report-----");
            List<SaveCrashBean> beans = CrashHandler.getInstance().getAllErrorList();
            if (beans != null) {
                LogUtil.i(TAG, "report error count:" + beans.size());
                Iterator<SaveCrashBean> iterator = beans.iterator();
                while (iterator.hasNext()) {
                    final SaveCrashBean report = iterator.next();
                    if (!report.getIsReport()) {
                        final String reportContent = report.toString();
                        LogUtil.i(TAG, "error reportContent:" + reportContent);
                        mThreadPool.execute(new Runnable() {

                            @Override
                            public void run() {
                                if (reportForHttpBean != null) {
                                    reportForHttpBean.setTs(Long.parseLong(report.getCrashTime()) - tsMax);
                                    reportForHttpBean.setApp_id(appId);
                                    if (mContext != null) {
                                        reportForHttpBean.setNetwork(NetworkUtil.getNetworkType(mContext));
                                    }

                                }

                                mUpload.sendReportSync(REPORT_URL, reportForHttpBean, reportContent, new ILogUpload.OnUploadFinishedCrashListener() {
                                    @Override
                                    public void onSuccess() {
                                        LogUtil.i(TAG, "error report success");
                                        ///新增策略 记录上报最大值
                                        updateMaxReportCount();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        LogUtil.i(TAG, "error report fail");
                                    }

                                    @Override
                                    public void tryAgain() {

                                    }
                                });
                            }
                        });
                    }
                }
            }
        }

        private void checkNativeLog() {
            ReportBean reportBean = CrashHandler.getInstance().getNativeLogReport();
            if (reportBean != null) {
                final String reportContent = reportBean.getDetail();
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        LogUtil.i(TAG, "report native content:" + reportContent);
                        mUpload.sendReportSync(REPORT_URL, reportForHttpBean, reportContent, new ILogUpload.OnUploadFinishedCrashListener() {
                            @Override
                            public void onSuccess() {
                                LogUtil.i(TAG, "native report success");
                                CrashHandler.getInstance().clearNativeReport();
                                ///新增策略 记录上报最大值
                                updateMaxReportCount();
                            }

                            @Override
                            public void onError(String error) {
                                LogUtil.i(TAG, "native crash report fail");
                            }

                            @Override
                            public void tryAgain() {

                            }
                        });
                    }
                });
            }
        }

        /**
         * 检查anr日志
         */
        private void checkAnrLog() {
            LogUtil.i(TAG, "try collect anr report-----");
            List<SaveCrashBean> beans = CrashHandler.getInstance().getAllAnrList(mContext);
            LogUtil.i(TAG, "report anr count:" + beans.size());
            if (beans != null && beans.size() > 0) {
                Iterator<SaveCrashBean> iterator = beans.iterator();
                while (iterator.hasNext()) {
                    final SaveCrashBean report = iterator.next();
                    if (!report.getIsReport()) {
                        LogUtil.i(TAG, " have new report anr");
                        File anrFile = new File(report.getCrashLog());
                        if (anrFile.exists()) {
                            mThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtil.i(TAG, "upload anr file-----");
                                    if (reportForHttpBean != null) {
                                        reportForHttpBean.setTs(Long.parseLong(report.getCrashTime()) - tsMax);
                                        reportForHttpBean.setApp_id(appId);
                                        reportForHttpBean.setType(ReportForHttpBean.TYPE_ANR);
                                        if (mContext != null) {
                                            reportForHttpBean.setNetwork(NetworkUtil.getNetworkType(mContext));
                                        }
                                    }

                                    mUpload.sendReportWithFileSync(ANR_URL, reportForHttpBean, report.getCrashLog(), new ILogUpload.OnUploadFinishedListener() {
                                        @Override
                                        public void onSuccess() {
                                            LogUtil.i(TAG, "upload anr file success-----");
                                            tryAnrCount = 0;
                                            report.setIsReport(true);
                                            DbCore.getDaoSession().getSaveCrashBeanDao().update(report);
                                            ///新增策略 记录上报最大值
                                            updateMaxReportCount();

                                        }

                                        @Override
                                        public void onError(String error) {
                                            LogUtil.i(TAG, "upload anr file fail-----");
                                        }

                                        @Override
                                        public void tryAgain() {
                                            LogUtil.i(TAG, "upload anr file tryAgain-----");
                                            ++tryAnrCount;
                                            currentTryReportTime = (int) Math.pow(2, tryAnrCount);
                                            if (currentTryReportTime >= Max_REPORT_TIME) {
                                                currentTryReportTime = Max_REPORT_TIME;
                                            }
                                            if (mHandler != null) {
                                                mHandler.sendEmptyMessageDelayed(RETRY_UPLOAD_ANR, currentTryReportTime);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        }


        /**
         * 检查需要上报的日志 及 状态
         */
        private void checkNeedUploadReport() {
//            DbCore.getDaoSession().getSaveCrashBeanDao().deleteAll();
            if (isNetAvailable()) {
                checkCrashLog();
                checkErrorLog();
                if (isNeedNativeLog) {
                    checkNativeLog();
                }
                checkAnrLog();
            }
        }


        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == CMD_PREPARE_REPORT) {
                // FIXME: 2019-10-25 检查崩溃日志
                mHandler.removeMessages(CMD_PREPARE_REPORT);
                // FIXME: 2019-10-25 初始化上报
//                CrashHandler.getInstance().init2();
                // FIXME: 2019-10-29 初始化配置信息
                getHost();

            } else if (what == CMD_CHECK_UPLOAD) {
                // FIXME: 2019-10-25 检查崩溃日志
                mHandler.removeMessages(CMD_CHECK_UPLOAD);
                checkNeedUploadReport();

            } else if (what == CMD_START_REPORT) {
                LogUtil.i(TAG,"check upload");
                // FIXME: 2019-10-25   ///开始上报
                mHandler.removeMessages(CMD_START_REPORT);
                if (isNetAvailable()) {
                    // FIXME: 2019-10-25  //启动检测崩溃日志.
                    if (!mHandler.hasMessages(CMD_CHECK_UPLOAD)) {
                        mHandler.sendEmptyMessage(CMD_CHECK_UPLOAD);
                    }
                }

            } else if (what == RETRY_UPLOAD_CRASH) {
                checkCrashLog();
            } else if (what == RETRY_UPLOAD_ANR) {
                checkAnrLog();
            }
        }


        /**
         * 初始化域名
         */
        private void getHost() {
            LogUtil.i(TAG, "try get host...");
            if (!TextUtils.isEmpty(REPORT_HOST)) {
                REPORT_URL = REPORT_HOST + REPORT_ACTION;
                ANR_URL = REPORT_HOST + ANR_ACTION;
                BASE_PARAMS_URL = REPORT_HOST + BASE_PARAMS;
                LogUtil.i(TAG, "REPORT_URL:" + REPORT_URL);
                LogUtil.i(TAG, "ANR_URL:" + ANR_URL);
                LogUtil.i(TAG, "BASE_PARAMS_URL:" + BASE_PARAMS_URL);
                try {
                    getBaseParams();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 获取配置信息
         */
        private void getBaseParams() {
            LogUtil.i(TAG, "try get base params...");
            final String url = BASE_PARAMS_URL;
//            syncHttpGet(url, callBack);
            final PackageManager pm = mContext.getPackageManager();
            if (reportForHttpBean == null) {
                reportForHttpBean = new ReportForHttpBean();
            }
            try {
                final PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                reportForHttpBean.setAppver(pi.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            reportForHttpBean.setOs("Android " + Build.VERSION.RELEASE + ",level " + Build.VERSION.SDK);
            reportForHttpBean.setSdkver(SDK_REPORT_VERSION);
            reportForHttpBean.setMac(MacUtil.getMac());

            reportForHttpBean.setModel(Build.MANUFACTURER + "_" + Build.MODEL);
            reportForHttpBean.setApp_id(appId);
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    LogUtil.i(TAG, "try get config...");
                    mUpload.getConfig(url, reportForHttpBean, new ILogUpload.ConfigFinishedListener() {
                        @Override
                        public void onSuccess(String result) {
                            JsonForBaseParams jsonBase = new Gson().fromJson(result, JsonForBaseParams.class);

                            if (jsonBase != null && jsonBase.getCode() == 200) {
                                if (jsonBase.getData() != null) {
                                    tsMax = System.currentTimeMillis() - jsonBase.getData().getTs();

                                    if (jsonBase.getData().getReport_count() != 0) {
                                        REPORT_MAX = jsonBase.getData().getReport_count();
                                    }

                                    heartTime = jsonBase.getData().getHeart_beat();
                                    startReport = jsonBase.getData().getReport_flag();
                                }
                            }

                            //设置上报条件
                            setSaveParams(REPORT_MAX, startReport);
                            //启动上报
                            startReport();
                            //触发心跳
                            startHeart();
                        }

                        @Override
                        public void tryAgain() {

                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                }
            });
        }

    }

    /**
     * 设置上报条件
     *
     * @param reportMax
     * @param startReport
     */
    private void setSaveParams(long reportMax, int startReport) {
        if (mLogSaver != null) {
            mLogSaver.setReportParams(reportMax, startReport);
        }
        List<ReportMaxAndCountBean> list = DbCore.getDaoSession().getReportMaxAndCountBeanDao().queryBuilder().list();
        if (list != null && list.size() > 0) {
            ReportMaxAndCountBean reportCountBean = list.get(0);
            reportCountBean.setReportMax(reportMax);
            DbCore.getDaoSession().getReportMaxAndCountBeanDao().insertOrReplace(reportCountBean);
        } else {
            ReportMaxAndCountBean reportCountBean = new ReportMaxAndCountBean();
            reportCountBean.setId(System.currentTimeMillis());
            reportCountBean.setReportMax(reportMax);
            DbCore.getDaoSession().getReportMaxAndCountBeanDao().insertOrReplace(reportCountBean);
        }
    }


    /**
     * 发送上报
     */
    private void startReport() {
        boolean isReportMax = checkIsReportMax();
        ///如果达到最大值 不在启动上报
        if (isReportMax) {
            return;
        }
        if (startReport == 0) {
            return;
        }
        if (mHandler != null) {
            mHandler.removeMessages(CMD_START_REPORT);
            Random random = new Random();
            int randomReport = random.nextInt(300 * 1000);
            Log.i(TAG,randomReport+"毫秒后开始上报数据");
            ///发送日志上报
            mHandler.sendEmptyMessageDelayed(CMD_START_REPORT, randomReport);
        }
    }

    private void startHeart() {
        //特定时间触发一次 心跳
        if (mHandler != null) {
            Random random = new Random();
            int randomReport = random.nextInt(300);
            mHandler.sendEmptyMessageDelayed(CMD_PREPARE_REPORT, (heartTime * 60 + randomReport) * 1000);
        }

    }


    private String getCacheDir(Context context) {
        // SD卡是否存在
        boolean isSDCardExist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        boolean isRootDirExist = Environment.getExternalStorageDirectory().exists();
        if (isSDCardExist && isRootDirExist) {
            mROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/files/";
        } else {
            mROOT = context.getFilesDir().getAbsolutePath() + "/files/";
        }
        return mROOT;
    }

    private String getInternalDir() {
        return mContext.getFilesDir().getAbsolutePath();
    }


    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean netAvailable = isNetAvailable();

            if (netAvailable && !isNetConnected) {
                LogUtil.i(TAG, "net work changed available");
                isNetConnected = true;
                mHandler.sendEmptyMessage(CMD_START_REPORT);
            } else if (!netAvailable) {
                LogUtil.i(TAG, "net work changed unavailable");
                isNetConnected = false;
            }
        }
    }

    private boolean isNetAvailable() {
        if (NetUtil.isConnected(mContext) &&
                (mWifiOnly ? NetUtil.isWifi(mContext) : true)) {
            return true;
        }
        return false;
    }


    /**
     * 新增策略 记录上报最大值
     */
    private void updateMaxReportCount() {
        List<ReportMaxAndCountBean> list = DbCore.getDaoSession().getReportMaxAndCountBeanDao().queryBuilder().list();
        if (list != null && list.size() > 0) {
            ReportMaxAndCountBean reportCountBean = list.get(0);
            long count = reportCountBean.getReportSuccessCount() + 1;
            reportCountBean.setReportSuccessCount(count);
            DbCore.getDaoSession().getReportMaxAndCountBeanDao().insertOrReplace(reportCountBean);
        }
    }

    /**
     * 检测是否达到最大值
     *
     * @return
     */
    private boolean checkIsReportMax() {
        List<ReportMaxAndCountBean> list = DbCore.getDaoSession().getReportMaxAndCountBeanDao().queryBuilder().list();
        if (list != null && list.size() >= 0) {
            ReportMaxAndCountBean reportCountBean = list.get(0);
            if (reportCountBean.getReportSuccessCount() >= REPORT_MAX) {
                return true;
            }

        }
        return false;
    }


}
