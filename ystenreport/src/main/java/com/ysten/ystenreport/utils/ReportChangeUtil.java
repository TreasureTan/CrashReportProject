package com.ysten.ystenreport.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import com.ysten.ystenreport.bean.CrashBean;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by tanxiaozhong
 * on 2019-10-25 16:55.
 * describe:
 */
public class ReportChangeUtil {

    private static final String TAG = "Report_builder";
    public static final String REPORT_DEFAULT_VALUE = "-";
    private final static long SND_TIME = 1000;
    private final static long MIN_TIME = 60 * SND_TIME;
    private final static long HOUR_TIME = 60 * MIN_TIME;
    private final static long DAY_TIME = 24 * HOUR_TIME;

    private final static String DAY_STR = "\u5929";
    private final static String HOUR_STR = "\u5C0F\u65F6";
    private final static String MIN_STR = "\u5206";
    private final static String SND_STR = "\u79D2";

    private static long mStarttime;
    private static Context mContext;

    public static void init(long startTime, Context appContext) {
        mStarttime = startTime;
        mContext = appContext;
    }

    public static String buildMemInfo() {
        String memInfo = REPORT_DEFAULT_VALUE;
        long totalMem = PerformanceUtils.getTotalMemory();
        long freeMem = PerformanceUtils.getFreeMemory(mContext);

        String freeMemStr = Formatter.formatFileSize(mContext, freeMem);

        memInfo = String.format("%s(%.2f%%)", freeMemStr, (float) freeMem / totalMem * 100);
        return memInfo;
    }

    public static String buildSdInfo() {
        String sdInfo = REPORT_DEFAULT_VALUE;
        File sdcardFiledir = Environment.getExternalStorageDirectory();
        final StatFs stats = new StatFs(sdcardFiledir.getAbsolutePath());
        long usableSpace = (long)stats.getBlockSize() * (long)stats.getAvailableBlocks();
        long totalSpace = (long)stats.getBlockSize() * (long)stats.getBlockCount();

        if (totalSpace > 0) {
            float freeRate = (float) usableSpace / totalSpace;
            String usableSpaceStr = Formatter.formatFileSize(mContext, usableSpace);

            sdInfo = String.format("%s(%.2f%%)", usableSpaceStr, freeRate * 100);
        }
        return sdInfo;
    }

    public static String buildAllThreadsStackInfo(Thread curThread) {
        String threadInfo = REPORT_DEFAULT_VALUE;
        ArrayList<Thread> sortedList = new ArrayList<Thread>();
        StringBuilder threadInfoBuilder = new StringBuilder();
        Map<Thread, StackTraceElement[]> allThInfos = Thread.getAllStackTraces();
        if(allThInfos == null ||
                allThInfos.size() == 0){
            return threadInfo;
        }
        sortedList.addAll(allThInfos.keySet());
        Collections.sort(sortedList, new ThreadComparetor());

        Iterator<Thread> iterator = sortedList.iterator();
        while (iterator.hasNext()) {
            Thread th = iterator.next();
            if (curThread != null &&
                    th.getId() == curThread.getId()) {
                continue;
            }
            StackTraceElement[] trace = allThInfos.get(th);
            if (trace != null) {
                threadInfoBuilder.append("#" + th.getId() + "  " + th.getName() + "\n");
                for (int i = 0; i < trace.length; i++) {
                    threadInfoBuilder.append("\t" + trace[i].toString() + "\n");
                }
                threadInfoBuilder.append("----------thread_line----------" + "\n");
            }
        }
        threadInfo = threadInfoBuilder.toString();
        return threadInfo;
    }

    public static String buildThreadInfo(Thread thread){
        String threadInfo = "#" + thread.getId() + "  " + thread.getName() + "\n";
        return threadInfo;
    }

    public static String buildStackElementInfo(StackTraceElement[] trace) {
        StringBuilder sb = new StringBuilder();
        if (trace != null) {
            for (int i = 0; i < trace.length; i++) {
                sb.append(trace[i].toString() + "\n");
            }
        }
        return sb.toString();
    }

    public static String buildExceptionStackInfo(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        String result = writer.toString();
        String exStr = ex.toString();
        result = result.replaceFirst(Pattern.quote(exStr), "");
        return result = result.replaceAll("(?m)^\\s*", "");
    }

    /**
     * 转换崩溃异常
     * @param thread
     * @param elements
     * @return
     */
    public static CrashBean buildReportInfosArrys(Thread thread, StackTraceElement[] elements){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        String occTime = sdf.format(now);
        ;
//        String usedTime = formatTime(now.getTime() - mStarttime);
        String sdInfo = buildSdInfo();
        String memInfo = buildMemInfo();
        String exceptStack = buildStackElementInfo(elements);
        String errorType = "thread stack";
        String exceptInfo = REPORT_DEFAULT_VALUE;
        String threadInfo = buildThreadInfo(thread);
        String allthreadStack = buildAllThreadsStackInfo(thread);

        CrashBean buildInfos = new CrashBean (occTime, sdInfo, memInfo, errorType, exceptInfo, exceptStack,
                threadInfo,allthreadStack);


        return buildInfos;
    }

//    public static String buildReportInfos(Thread thread, StackTraceElement[] elements) {
//        return ReportTransUtil.transReportTo(buildReportInfosArrys(thread,elements));
//    }

    public static String[] buildReportInfosArrys(Thread thread, String content,String summary, String contentType){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        String occTime = sdf.format(now);
        String usedTime = formatTime(now.getTime() - mStarttime);
        String sdInfo = buildSdInfo();
        String memInfo = buildMemInfo();
        String errorType = contentType;
        String exceptInfo = summary;
        String exceptStack = content;
        String threadInfo = buildThreadInfo(thread);
        String allthreadStack = buildAllThreadsStackInfo(thread);

        String[] buildInfos = new String[] { occTime, usedTime, sdInfo, memInfo, errorType, exceptInfo, exceptStack,
                threadInfo,allthreadStack};
        for (int i = 0; i < buildInfos.length; i++) {
            LogUtil.i(TAG, buildInfos[i]);
        }

        return buildInfos;
    }

    public static String buildReportInfos(Thread thread, String content,String summary, String type) {
        // occTime、usedTime、sdInfo、memInfo、errorType、exceptInfo、exceptStack、threadInfo

        return ReportTransUtil.transReportTo(buildReportInfosArrys(thread,content,summary,type));
    }

    public static String[] buildReportInfosArrys(String threadId,String threadName,String occTime,String usedTime,String content, String summary,String type){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdInfo = REPORT_DEFAULT_VALUE;
        String memInfo = REPORT_DEFAULT_VALUE;
        String errorType = type;
        String exceptInfo = summary;
        String exceptStack = content;
        String threadInfo = "#" + threadId + "   " + threadName;
        String allthreadStack = REPORT_DEFAULT_VALUE;

        String[] buildInfos = new String[] { occTime, usedTime, sdInfo, memInfo, errorType, exceptInfo, exceptStack,
                threadInfo,allthreadStack};
        for (int i = 0; i < buildInfos.length; i++) {
            LogUtil.i(TAG, buildInfos[i]);
        }

        return buildInfos;
    }


    public static String buildReportInfos(String threadId,String threadName, String occTime,String usedTime,String content,String summary, String type) {
        // occTime、usedTime、sdInfo、memInfo、errorType、exceptInfo、exceptStack、threadInfo

        return ReportTransUtil.transReportTo(buildReportInfosArrys(threadId,threadName,occTime,usedTime,content,summary,type));
    }


    /**
     * 转换崩溃异常
     * @param thread
     * @param ex
     * @return
     */
    public static CrashBean buildReportInfosArrys(Thread thread, Throwable ex){
        // occTime、usedTime、sdInfo、memInfo、errorType、exceptInfo、exceptStack、threadInfo
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        String occTime = sdf.format(now);
//        String usedTime = formatTime(now.getTime() - mStarttime);
        String sdInfo = buildSdInfo();
        String memInfo = buildMemInfo();
        String errorType = ex.getClass().getName();
        String exceptInfo = ex.getMessage();
        String exceptStack = buildExceptionStackInfo(ex);
        String threadInfo = buildThreadInfo(thread);
        String allthreadStack = buildAllThreadsStackInfo(thread);
        Log.e(TAG,"occTime"+occTime);
//        Log.e(TAG,"usedTime"+usedTime);
        Log.e(TAG,"sdInfo"+sdInfo);
        Log.e(TAG,"memInfo"+memInfo);
        Log.e(TAG,"errorType"+errorType);
        Log.e(TAG,"exceptInfo"+exceptInfo);
        Log.e(TAG,"exceptStack"+exceptStack);
        Log.e(TAG,"threadInfo"+threadInfo);
        Log.e(TAG,"allthreadStack"+allthreadStack);
        CrashBean buildInfos = new CrashBean (occTime, sdInfo, memInfo, errorType, exceptInfo, exceptStack,
                threadInfo,allthreadStack);
        return buildInfos;
    }

//    public static String buildReportInfos(Thread thread, Throwable ex) {
//        return ReportTransUtil.transReportTo(buildReportInfosArrys(thread,ex));
//    }

    public static String formatTime(long time) {
        long days = time / DAY_TIME;
        long hours = (time % DAY_TIME) / HOUR_TIME;
        long mins = (time % HOUR_TIME) / MIN_TIME;
        long snds = (time % MIN_TIME) / SND_TIME;
        if (days > 0) {
            return days + DAY_STR + hours + HOUR_STR + mins + MIN_STR + snds + SND_STR;
        }
        if (hours > 0) {
            return hours + HOUR_STR + mins + MIN_STR + snds + SND_STR;
        }
        if (mins > 0) {
            return mins + MIN_STR + snds + SND_STR;
        }
        if (snds > 0) {
            return snds + SND_STR;
        }
        return 0 + "";
    }

    static class ThreadComparetor implements Comparator<Thread> {

        @Override
        public int compare(Thread o1, Thread o2) {
            int ret = 0;
            long compare = o1.getId() - o2.getId();
            if (compare < 0) {
                ret = -1;
            } else if (compare > 0) {
                ret = 1;
            }
            return ret;
        }
    }

}
