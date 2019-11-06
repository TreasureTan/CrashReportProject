package com.ysten.ystenreport.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.ysten.ystenreport.bean.ReportMaxAndCountBean;
import com.ysten.ystenreport.utils.LogUtil;

import org.greenrobot.greendao.database.Database;


/**
 * @author li_jian_gang
 * @time 2017/6/29
 */

public class SQLiteOpenHelper extends DaoMaster.OpenHelper {
    public SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        LogUtil.i("greenDAO", "baselib SQLiteOpenHelper Upgrading schema from version " + oldVersion + " to " + newVersion );

        DaoMaster.createAllTables(db,true);
        MigrationHelper.migrate(db, SaveCrashBeanDao.class);
        MigrationHelper.migrate(db, ReportMaxAndCountBeanDao.class);

    }

}

