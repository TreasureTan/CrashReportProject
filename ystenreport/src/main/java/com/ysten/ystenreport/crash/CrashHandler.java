package com.ysten.ystenreport.crash;

import android.content.Context;
import android.util.Log;

import com.ysten.ystenreport.bean.ReportBean;
import com.ysten.ystenreport.bean.SaveCrashBean;
import com.ysten.ystenreport.greendao.DbCore;
import com.ysten.ystenreport.greendao.SaveCrashBeanDao;
import com.ysten.ystenreport.save.ISave;
import com.ysten.ystenreport.utils.LogUtil;
import com.ysten.ystenreport.utils.ReportChangeUtil;
import com.ysten.ystenreport.utils.SoLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 自定义的崩溃捕获Handler
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "Report_CrashHandler";
    private static CrashHandler INSTANCE = new CrashHandler();
    private static boolean mNativeLoaded = false;
    /**
     * 。系统默认异常处理
     */
    private static final Thread.UncaughtExceptionHandler sDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();


    /**
     * 设置日志的保存方式
     */
    private static ISave mSave;

    private File mNativeLogFile;

    private String mRootPath;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public static native void init(String path, long time);

    /**
     * 初始化,，设置此CrashHandler来响应崩溃事件
     *
     * @param logSaver 保存的方式
     */
    public void init1(Context context, ISave logSaver, String rootPath) {
        LogUtil.i(TAG, "in init1!");
        mSave = logSaver;
        Thread.setDefaultUncaughtExceptionHandler(this);
        mRootPath = rootPath;
    }

    public void init2() {
        LogUtil.i(TAG, "in init2!");
        String nativeLogUtilFile = mRootPath + "/native_log";
        mNativeLogFile = new File(nativeLogUtilFile);
        try {
            SoLoader.loadLibrary("libbreakpad_client");
            init(nativeLogUtilFile, System.currentTimeMillis() / 1000);
            mNativeLoaded = true;
        } catch (Error e) {
            mNativeLoaded = false;
            e.printStackTrace();
        }
    }

    public void clearNativeReport() {
        if (mNativeLogFile.exists()) {
            mNativeLogFile.delete();
        }
    }


    public void deleteSql(SaveCrashBean crashBean) {
        DbCore.getDaoSession().getSaveCrashBeanDao().delete(crashBean);
    }


    //第一行参数:   starttime  occurTime  usedTime  signal  threadId  threadName

    public ReportBean getNativeLogReport() {
        LogUtil.i(TAG, "try collect native crash log------");
        if (mNativeLogFile.exists()) {
            FileInputStream fis = null;
            BufferedReader br = null;
            InputStreamReader isr = null;
            try {
                fis = new FileInputStream(mNativeLogFile);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
                String expAbout = br.readLine();
                String[] expAboutArrys = expAbout.split(" ");
                if (expAboutArrys != null) {
                    String occurTime = ReportChangeUtil.REPORT_DEFAULT_VALUE;
                    String usedTime = ReportChangeUtil.REPORT_DEFAULT_VALUE;
                    try {
                        if (expAboutArrys.length > 1) {
                            long occur = Long.parseLong(expAboutArrys[1]) * 1000;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(occur);
                            occurTime = sdf.format(date);
                        } else {
                            occurTime = "unknown";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (expAboutArrys.length > 2) {
                            long used = Long.parseLong(expAboutArrys[2]) * 1000;
                            usedTime = ReportChangeUtil.formatTime(used);
                        } else {
                            usedTime = "unknown";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String signal = "unknown";

                    if (expAboutArrys.length > 3) {
                        signal = expAboutArrys[3];
                    }

                    StringBuffer content = new StringBuffer();

                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        content.append(line + "\n");
                    }

                    String threadId = "N";
                    String threadName = "native";

                    if (expAboutArrys.length > 4) {
                        threadId = expAboutArrys[4];
                    }

                    if (expAboutArrys.length > 5) {
                        threadName = expAboutArrys[5];
                    }

                    LogUtil.i(TAG, "got native log------");
                    ReportBean bean = new ReportBean();

                    String reportInfo = ReportChangeUtil.buildReportInfos(threadId, threadName, occurTime, usedTime, content.toString(), "native exception", signal);
                    bean.setDetail(reportInfo);
                    bean.setType(ISave.TYPE_NATIVE);
                    return bean;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (isr != null) {
                        isr.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        LogUtil.i(TAG, "no native crash log------");
        return null;
    }


    public List<SaveCrashBean> getAllErrorList() {
        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.Type.eq(ISave.TYPE_ERROR)).list();
        return list;
    }

    public List<SaveCrashBean> getAllCrashList() {
        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.Type.eq(ISave.TYPE_CRASH)).list();
        return list;
    }


    /**
     * 获取anr的数据信息
     *
     * @param context
     * @return
     */
    public List<SaveCrashBean> getAllAnrList(Context context) {
        LogUtil.i(TAG, "try collect anr log------");
        String anrPath = "/data/anr/traces.txt";
        File anrFile = new File(anrPath);
        long modifyTime = anrFile.lastModified();
        String lastUpDateTime = modifyTime + "";
        boolean isProcessed = false;
        List<SaveCrashBean> crashBeanList = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.Type.eq(ISave.TYPE_ANR)).list();
        if (crashBeanList != null && crashBeanList.size() != 0) {
            LogUtil.i(TAG, "anr report count:" + crashBeanList.size());
            for (int i = 0; i < crashBeanList.size(); i++) {
                SaveCrashBean bean = crashBeanList.get(i);
                if (!lastUpDateTime.equals(bean.getCrashTime())) {
                    isProcessed = true;
                } else {
                    // FIXME: 2019-10-26 证明当前的anr 是上次的
                    isProcessed = false;
                }
            }
        } else {
            isProcessed = true;
        }

        if (isProcessed) {
            Log.i(TAG, "there is a new anr");
            //3.判断当前anr 是否是自身的anr.
            FileInputStream fis = null;
            BufferedReader br = null;
            InputStreamReader isr = null;
            if (anrFile.exists()){
                try {
                    fis = new FileInputStream(anrFile);
                    isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);

                    String line = null;
                    int lineCount = 5;
                    int i = 0;
                    while (++i < lineCount) {
                        line = br.readLine();
                        Pattern pattern = Pattern.compile("^Cmd.*line:");
                        Matcher mather = pattern.matcher(line);
                        if (mather.find()) {
                            String[] msg = line.split(":");
                            if (msg.length > 0 && msg[1] != null &&
                                    context.getPackageName().equals(msg[1].trim())) {

                                // FIXME: 2019-10-26  如果当前的anr文件是自己包名下的anr异常 则保存
                                if (mSave != null) {
//                                    boolean passCrashDate = mSave.checkPassCrashDate(ISave.TYPE_ANR);
//                                    if (passCrashDate){
//                                        break;
//                                    }
                                    mSave.checkAndDeleteMoreSql();
                                }
                                List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().list();
                                if (list!=null && list.size()>100){
                                   break;
                                }
                                SaveCrashBean reportBean = new SaveCrashBean();
                                reportBean.setType(ISave.TYPE_ANR);
                                reportBean.setCrashLog(anrPath);
                                reportBean.setCrashTime(lastUpDateTime);
                                reportBean.setIsReport(false);
                                DbCore.getDaoSession().getSaveCrashBeanDao().insertOrReplace(reportBean);
//                                try{
//                                    List<SaveCrashBean> listCrashs = new ArrayList<>();
//                                    for (int j=1;j<200;j++){
//                                        SaveCrashBean newBean= new SaveCrashBean();
//                                        newBean.setId(System.currentTimeMillis()- j);
//                                        newBean.setCrashTime(System.currentTimeMillis()+"");
//                                        newBean.setIsReport(false);
//                                        newBean.setCrashLog(reportBean.getCrashLog());
//                                        newBean.setType(reportBean.getType());
//                                        listCrashs.add(newBean);
//                                    }
//                                    DbCore.getDaoSession().getSaveCrashBeanDao().insertOrReplaceInTx(listCrashs);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                        if (isr != null) {
                            isr.close();
                        }
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }else {
                Log.i(TAG, "there is no anr file");
            }


        } else {

            Log.i(TAG, "there is no new anr");
        }
        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.Type.eq(ISave.TYPE_ANR)).list();
        return list;
    }


    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable e) {
        Log.d(TAG, "MUnCaughtExceptionHandler,uncaughtException, thread: " + thread
                + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
                + e);
        if (mSave != null) {
            mSave.writeCrash(thread, e, ISave.TYPE_CRASH);
        }

        sDefaultHandler.uncaughtException(thread, e);
    }
}


