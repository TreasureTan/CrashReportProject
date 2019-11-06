package com.ysten.ystenreport.bean;

/**
 * Created by tanxiaozhong
 * on 2019-10-26 16:24.
 * describe:
 */
public class ReportForHttpBean {

    public ReportForHttpBean(){

    }

    public ReportForHttpBean(String os, String appver, String sdkver, String mac, long ts, int type,String model,String app_id,long  network) {
        this.os = os;
        this.appver = appver;
        this.sdkver = sdkver;
        this.mac = mac;
        this.ts = ts;
        this.type = type;
        this.model = model;
        this.app_id = app_id;
        this.network = network;
    }

    ///android系统版本
    private String os;
    ///集成应用版本x`
    private String appver;
    ///SDK版本
    private String sdkver;
    ///终端mac地址
    private String mac;
    ///服务器当前时间
    private long ts;

    private CrashBean data;

    ///上报类型 0 crash， 1 exception ，2 anr
    public static int TYPE_CRASH = 0;
    public static int TYPE_EXCEPTION = 1;
    public static int TYPE_ANR = 2;
    private int type;

    //终端类型
    private String model;
    //应用id
    private String app_id;

    private long network;

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getAppver() {
        return appver;
    }

    public void setAppver(String appver) {
        this.appver = appver;
    }

    public String getSdkver() {
        return sdkver;
    }

    public void setSdkver(String sdkver) {
        this.sdkver = sdkver;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }




    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public long getNetwork() {
        return network;
    }

    public void setNetwork(long network) {
        this.network = network;
    }
    public CrashBean getData() {
        return data;
    }

    public void setData(CrashBean data) {
        this.data = data;
    }



    @Override
    public String toString() {
        return "ReportForHttpBean{" +
                "os='" + os + '\'' +
                ", appver='" + appver + '\'' +
                ", sdkver='" + sdkver + '\'' +
                ", mac='" + mac + '\'' +
                ", ts='" + ts + '\'' +
                ", type=" + type +
                ", model=" + model +
                ", app_id=" + app_id +
                ", network=" + network + '\'' +
                '}';
    }
}

