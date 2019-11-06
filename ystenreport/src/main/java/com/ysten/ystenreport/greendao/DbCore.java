package com.ysten.ystenreport.greendao;

import android.content.Context;
import android.util.Log;


import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;

/**
 * 作者：Picasso on 2016/10/14 10:49
 * 详情：核心辅助类DbCore,用于获取DaoMaster和DaoSession
 */

public class DbCore {

    private static final String DEFAULT_DB_NAME = "default_report.db";
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private static Context mContext;
    private static String DB_NAME;

    public static void init(Context context) {
        init(context, DEFAULT_DB_NAME);
    }

    public static void init(Context context, String dbName) {
        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }
        mContext = context.getApplicationContext();
        DB_NAME = dbName;
    }

    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            SQLiteOpenHelper helper = new SQLiteOpenHelper(mContext, DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        String absolutePath = mContext.getDatabasePath(DB_NAME).getAbsolutePath();
        Log.e("DbCore","absolutePath="+absolutePath);

        return daoMaster;
    }

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public static void enableQueryBuilderLog(){

        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

}
