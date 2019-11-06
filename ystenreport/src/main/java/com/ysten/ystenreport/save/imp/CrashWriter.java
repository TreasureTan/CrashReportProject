package com.ysten.ystenreport.save.imp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.ysten.ystenreport.bean.CrashBean;
import com.ysten.ystenreport.bean.ReportBean;
import com.ysten.ystenreport.bean.ReportMaxAndCountBean;
import com.ysten.ystenreport.bean.SaveCrashBean;
import com.ysten.ystenreport.greendao.DbCore;
import com.ysten.ystenreport.greendao.SaveCrashBeanDao;
import com.ysten.ystenreport.save.BaseSaver;
import com.ysten.ystenreport.save.ISave;
import com.ysten.ystenreport.utils.LogUtil;
import com.ysten.ystenreport.utils.ReportChangeUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 在崩溃之后，马上异步保存崩溃信息，完成后退出线程，并且将崩溃信息都写在数据库.
 */
public class CrashWriter extends BaseSaver {

    private final static String TAG = "Report_CrashWriter";
    private static final int LIMIT_COUNT = 100;
    private static final int MAX_SPACE_TIME = 5 * 60 * 1000;
    private String mCrashPath;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private long maxReport = 5000;
    private int isStart = 0;
    public CrashWriter(Context context, String logRootDirectory) {
        super(context, logRootDirectory);
        mCrashPath = logRootDirectory;
        File logFolder = new File(mCrashPath);
        if (!logFolder.exists()) {
            boolean mkdirs = logFolder.mkdirs();
            Log.e(TAG, "创建文件 mkdirs = " + mkdirs);
        }

    }

    @Override
    public synchronized void writeCrash(Thread thread, Throwable ex, String type) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
//		writeCrashReportLocal(ex);
        //是否启动上报
        boolean isReport = checkIsReport();
        if (!isReport){
            return;
        }
        //是否超过间隔时间
        boolean passCrashDate = checkPassCrashDate(ISave.TYPE_CRASH);
        if (!passCrashDate){
            return;
        }
        //是否启动删除数据策略
        checkAndDeleteMoreSql();

        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().list();
        ///超过限制 不记录数据
        if (list != null && list.size() > LIMIT_COUNT) {
            return;
        }

        CrashBean crashLog = ReportChangeUtil.buildReportInfosArrys(thread, ex);
        LogUtil.i(TAG, "reportInfo crashLog:" + crashLog.toString());
        JSONObject jsonCrash = new JSONObject();
        try {
            jsonCrash.put("occTime", crashLog.getOccTime());
            jsonCrash.put("sdInfo", crashLog.getSdInfo());
            jsonCrash.put("memInfo", crashLog.getMemInfo());
            jsonCrash.put("errorType", crashLog.getErrorType());
            jsonCrash.put("exceptInfo", crashLog.getExceptInfo());
            jsonCrash.put("exceptStack", crashLog.getExceptStack());
            jsonCrash.put("threadInfo", crashLog.getThreadInfo());
            jsonCrash.put("allthreadStack", crashLog.getAllthreadStack());
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        String crash = StringEscapeUtils.unescapeJava(jsonCrash.toString());
        SaveCrashBean crashBean = new SaveCrashBean();
        crashBean.setId(System.currentTimeMillis());
        crashBean.setCrashLog(jsonCrash.toString());
        crashBean.setType(type);
        crashBean.setCrashTime(System.currentTimeMillis() + "");
        crashBean.setIsReport(false);
        DbCore.getDaoSession().getSaveCrashBeanDao().insertOrReplace(crashBean);
//        try {
//            List<SaveCrashBean> listCrashs = new ArrayList<>();
//            for (int j = 1; j < 5; j++) {
//                SaveCrashBean newBean = new SaveCrashBean();
//                newBean.setId(System.currentTimeMillis() - j);
//                newBean.setCrashTime(System.currentTimeMillis()-j + "");
//                newBean.setIsReport(false);
//                newBean.setCrashLog(crashBean.getCrashLog());
//                newBean.setType(crashBean.getType());
//                listCrashs.add(newBean);
//            }
//            DbCore.getDaoSession().getSaveCrashBeanDao().insertOrReplaceInTx(listCrashs);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 批量删除过多数据
     */
    @Override
    public void checkAndDeleteMoreSql() {
        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().list();
        if (list != null && list.size() >= LIMIT_COUNT) {
            List<SaveCrashBean> reportList = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.IsReport.eq(true)).list();
            DbCore.getDaoSession().getSaveCrashBeanDao().deleteInTx(reportList);
        }

    }


    @Override
    public boolean checkPassCrashDate(String type) {
        List<SaveCrashBean> list = DbCore.getDaoSession().getSaveCrashBeanDao().queryBuilder().where(SaveCrashBeanDao.Properties.Type.eq(type)).orderDesc(SaveCrashBeanDao.Properties.Id).list();
        if (list != null && list.size() > 0) {
            SaveCrashBean crashBean = list.get(0);
            if (System.currentTimeMillis() - crashBean.getId() > MAX_SPACE_TIME) {
                return true;
            }
        }
        return false;
    }


    /**
     * 将错误日志写到本地
     *
     * @param e
     */
    private void writeCrashReportLocal(Throwable e) {
        StringBuffer sb = new StringBuffer();

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        Log.e("uncaughtException", "MUnCaughtExceptionHandler==" + result);
//        saveCatchInfo2File(e);
        saveCatchHandlerInfo2File(e);
    }

    private String saveCatchHandlerInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "report-crash-" + time + "-" + timestamp + ".log";
            String file_dir = mCrashPath;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(file_dir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(file_dir + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());


                fos.close();
                Log.v("CrashHandle", "--- " + dir.getPath());
            }
            return fileName;
        } catch (Exception e) {
            //Log.e(TAG, "an error occured while writing file...", e);
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void writeCrash(Thread thread, String content, String summary, String type) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        String reportInfo = ReportChangeUtil.buildReportInfos(thread, content, summary, type);
        //LogUtil.i(TAG, "reportInfo:" + reportInfo);
        ReportBean reportBean = new ReportBean();
        reportBean.setDetail(reportInfo);
        reportBean.setType(type);
        reportBean.setReported(false);

    }


    public void writeReport(ReportBean reportBean) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

    }

    @Override
    public void setReportParams(long MaxLines, int startReport) {
        this.maxReport = MaxLines;
        this.isStart = startReport;
    }

    private boolean checkIsReport() {
        List<ReportMaxAndCountBean> list = DbCore.getDaoSession().getReportMaxAndCountBeanDao().queryBuilder().list();
        if (list != null && list.size() < maxReport && isStart ==1) {
            return true;
        }
        return  false;
    }


}
