package com.ysten.ystenreport.bean;

/**
 * Created by tanxiaozhong
 * on 2019-10-25 17:03.
 * describe:
 */
public class CrashBean {
    private String occTime, sdInfo, memInfo, errorType, exceptInfo, exceptStack,
            threadInfo,allthreadStack;
    public CrashBean(){

    }
    public CrashBean(String occTime,String sdInfo, String memInfo, String errorType, String exceptInfo, String exceptStack, String threadInfo, String allthreadStack) {
        this.occTime = occTime;
        this.sdInfo = sdInfo;
        this.memInfo = memInfo;
        this.errorType = errorType;
        this.exceptInfo = exceptInfo;
        this.exceptStack = exceptStack;
        this.threadInfo = threadInfo;
        this.allthreadStack = allthreadStack;
    }

    public String getOccTime() {
        return occTime;
    }

    public void setOccTime(String occTime) {
        this.occTime = occTime;
    }


    public String getSdInfo() {
        return sdInfo;
    }

    public void setSdInfo(String sdInfo) {
        this.sdInfo = sdInfo;
    }

    public String getMemInfo() {
        return memInfo;
    }

    public void setMemInfo(String memInfo) {
        this.memInfo = memInfo;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getExceptInfo() {
        return exceptInfo;
    }

    public void setExceptInfo(String exceptInfo) {
        this.exceptInfo = exceptInfo;
    }

    public String getExceptStack() {
        return exceptStack;
    }

    public void setExceptStack(String exceptStack) {
        this.exceptStack = exceptStack;
    }

    public String getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(String threadInfo) {
        this.threadInfo = threadInfo;
    }

    public String getAllthreadStack() {
        return allthreadStack;
    }

    public void setAllthreadStack(String allthreadStack) {
        this.allthreadStack = allthreadStack;
    }

    @Override
    public String toString() {
        return "CrashBean{" +
                "occTime='" + occTime + '\'' +
                ", sdInfo='" + sdInfo + '\'' +
                ", memInfo='" + memInfo + '\'' +
                ", errorType='" + errorType + '\'' +
                ", exceptInfo='" + exceptInfo + '\'' +
                ", exceptStack='" + exceptStack + '\'' +
                ", threadInfo='" + threadInfo + '\'' +
                ", allthreadStack='" + allthreadStack + '\'' +
                '}';
    }
}
