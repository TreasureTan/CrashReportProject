package com.ysten.ystenreport.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.ysten.ystenreport.bean.ReportMaxAndCountBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "REPORT_MAX_AND_COUNT_BEAN".
*/
public class ReportMaxAndCountBeanDao extends AbstractDao<ReportMaxAndCountBean, Long> {

    public static final String TABLENAME = "REPORT_MAX_AND_COUNT_BEAN";

    /**
     * Properties of entity ReportMaxAndCountBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property ReportMax = new Property(1, long.class, "reportMax", false, "REPORT_MAX");
        public final static Property ReportSuccessCount = new Property(2, long.class, "reportSuccessCount", false, "REPORT_SUCCESS_COUNT");
    }


    public ReportMaxAndCountBeanDao(DaoConfig config) {
        super(config);
    }
    
    public ReportMaxAndCountBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"REPORT_MAX_AND_COUNT_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"REPORT_MAX\" INTEGER NOT NULL ," + // 1: reportMax
                "\"REPORT_SUCCESS_COUNT\" INTEGER NOT NULL );"); // 2: reportSuccessCount
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"REPORT_MAX_AND_COUNT_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ReportMaxAndCountBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getReportMax());
        stmt.bindLong(3, entity.getReportSuccessCount());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ReportMaxAndCountBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getReportMax());
        stmt.bindLong(3, entity.getReportSuccessCount());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public ReportMaxAndCountBean readEntity(Cursor cursor, int offset) {
        ReportMaxAndCountBean entity = new ReportMaxAndCountBean( //
            cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // reportMax
            cursor.getLong(offset + 2) // reportSuccessCount
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ReportMaxAndCountBean entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setReportMax(cursor.getLong(offset + 1));
        entity.setReportSuccessCount(cursor.getLong(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ReportMaxAndCountBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ReportMaxAndCountBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ReportMaxAndCountBean entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
