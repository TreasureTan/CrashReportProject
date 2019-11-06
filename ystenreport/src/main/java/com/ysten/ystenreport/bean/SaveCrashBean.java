package com.ysten.ystenreport.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by tanxiaozhong
 * on 2019-10-24 23:16.
 * describe:
 */
@Entity
public class SaveCrashBean {
    //id
    @Id
    private long id;
    ///当前进程的pid
    private String pid;
    //包名
    private String packageName;
    //崩溃触发时间
    private String crashTime;
    //日志详情
    private String crashLog;
    //类型
    private String type;




    //是否已经上报
    private boolean isReport;


    @Generated(hash = 1712945249)
    public SaveCrashBean(long id, String pid, String packageName, String crashTime,
            String crashLog, String type, boolean isReport) {
        this.id = id;
        this.pid = pid;
        this.packageName = packageName;
        this.crashTime = crashTime;
        this.crashLog = crashLog;
        this.type = type;
        this.isReport = isReport;
    }

    @Generated(hash = 1161334431)
    public SaveCrashBean() {
    }


    public boolean getIsReport() {
        return this.isReport;
    }

    public void setIsReport(boolean isReport) {
        this.isReport = isReport;
    }

    public String getCrashLog() {
        return this.crashLog;
    }

    public void setCrashLog(String crashLog) {
        this.crashLog = crashLog;
    }

    public String getCrashTime() {
        return this.crashTime;
    }

    public void setCrashTime(String crashTime) {
        this.crashTime = crashTime;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SaveCrashBean{" +
                "id=" + id +
                ", pid='" + pid + '\'' +
                ", packageName='" + packageName + '\'' +
                ", crashTime='" + crashTime + '\'' +
                ", crashLog='" + crashLog + '\'' +
                ", type='" + type + '\'' +
                ", isReport=" + isReport +
                '}';
    }
}