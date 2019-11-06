package com.ysten.ystenreport.bean;

/**
 * Created by tanxiaozhong
 * on 2019-10-28 18:03.
 * describe:
 */
public class JsonForBaseParams {


    /**
     * code : 200
     * data : {"heart_beat":1440,"ip":"219.142.184.54","region":215,"report_count":5000,"report_flag":1,"ts":1572965277040}
     */

    private int code;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * heart_beat : 1440
         * ip : 219.142.184.54
         * region : 215
         * report_count : 5000
         * report_flag : 1
         * ts : 1572965277040
         */

        private int heart_beat;
        private String ip;
        private int region;
        private int report_count;
        private int report_flag;
        private long ts;

        public int getHeart_beat() {
            return heart_beat;
        }

        public void setHeart_beat(int heart_beat) {
            this.heart_beat = heart_beat;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getRegion() {
            return region;
        }

        public void setRegion(int region) {
            this.region = region;
        }

        public int getReport_count() {
            return report_count;
        }

        public void setReport_count(int report_count) {
            this.report_count = report_count;
        }

        public int getReport_flag() {
            return report_flag;
        }

        public void setReport_flag(int report_flag) {
            this.report_flag = report_flag;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }
    }
}
