package com.ysten.ystenreport.save;


import com.ysten.ystenreport.bean.ReportBean;
import com.ysten.ystenreport.encryption.IEncryption;

import java.util.List;

/**
 * 保存日志与崩溃信息的接口
 */
public interface ISave {
	
	public static final String TYPE_CRASH = "crash";
	public static final String TYPE_ANR = "anr";
	public static final String TYPE_NATIVE = "native";
	public static final String TYPE_STACK = "stack";
	public static final String TYPE_ERROR = "error";
	public static final String TYPE_LOG = "log";

	/**
	 * 存储崩溃
	 * @param thread
	 * @param ex
	 * @param type
	 */
    public void writeCrash(Thread thread, Throwable ex, String type);
	public void checkAndDeleteMoreSql();

	public boolean checkPassCrashDate(String type);
    public void writeCrash(Thread thread, String content, String summary, String type);

    public void setEncodeType(IEncryption encodeType);

    
    public void writeReport(ReportBean reportBean);


    public void setReportParams(long MaxLines,int startReport);


}
