package com.ysten.ystenreport.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by tanxiaozhong
 * on 2019-11-06 11:42.
 * describe:
 */
@Entity
public class ReportMaxAndCountBean {
    @Id
    private long id;
    private long reportMax;
    private long reportSuccessCount;
    public long getReportSuccessCount() {
        return this.reportSuccessCount;
    }
    public void setReportSuccessCount(long reportSuccessCount) {
        this.reportSuccessCount = reportSuccessCount;
    }
    public long getReportMax() {
        return this.reportMax;
    }
    public void setReportMax(long reportMax) {
        this.reportMax = reportMax;
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    @Generated(hash = 2079775129)
    public ReportMaxAndCountBean(long id, long reportMax, long reportSuccessCount) {
        this.id = id;
        this.reportMax = reportMax;
        this.reportSuccessCount = reportSuccessCount;
    }
    @Generated(hash = 883761204)
    public ReportMaxAndCountBean() {
    }

}
