package edu.asu.jlubawy.bluetoothbridge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jlubawy on 11/3/2015.
 */
public class BridgeDatabaseHelper extends SQLiteOpenHelper {

    final static int DB_VERSION = 1;
    final static String DB_NAME = "bluetooth-bridge";
    final static String DB_TABLE_NAME = "data";
    final static String DB_CREATE_TABLE_QUERY = "CREATE TABLE "
            + DB_TABLE_NAME + " (id INTEGER PRIMARY KEY ASC, line TEXT, is_sent INTEGER)";

    private SQLiteDatabase mDatabase;

    BridgeDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void initialize() {
        mDatabase = this.getWritableDatabase();
    }

    public long saveLine(String line) {
        ContentValues values = new ContentValues();

        values.put("id", (Integer)null);
        values.put("line", line);
        values.put("is_sent", 0);

        return mDatabase.insert(DB_TABLE_NAME, null, values);
    }

    public String queryNotSent() {
        Cursor cursor;
        StringBuilder sb;

        cursor = mDatabase.query(
                    DB_TABLE_NAME,
                    new String[]{"line"},
                    "(is_sent = 0)",
                    null,
                    null,
                    null,
                    null,
                    null );

        if (cursor.getCount() <= 0) {
            return null;
        }

        sb = new StringBuilder();
        sb.append("data={\"array\":[");
        while (cursor.moveToNext()) {
            sb.append("\"" + cursor.getString(cursor.getColumnIndex("line")) + "\"");

            if (cursor.isLast()) {
                break;
            }

            sb.append(",");
        }
        sb.append("]}");

        cursor.close();

        return sb.toString();
    }

    public int clearNotSent() {
        ContentValues values = new ContentValues();

        values.put("is_sent", 1);

        return mDatabase.update(
                DB_TABLE_NAME,
                values,
                "(is_sent = 0)",
                null
        );
    }

    public int getNumNotSent() {
        int count;
        Cursor cursor = mDatabase.query(
                DB_TABLE_NAME,
                new String[]{"id"},
                "(is_sent = 0)",
                null,
                null,
                null,
                null,
                null
        );

        count = cursor.getCount();
        cursor.close();

        return count;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* TODO: Implement */
    }
}
