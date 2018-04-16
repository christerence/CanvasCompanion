package deaddevs.com.studentcompanion.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase sqLiteDatabase;

    public DatabaseManager(Context context) { dbOpenHelper = new DBOpenHelper(context); }

    public void open() {
        sqLiteDatabase = dbOpenHelper.getWritableDatabase();
    }

    public void close() {
        sqLiteDatabase.close();
    }

    public void insertCanvasInfo(String title) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBOpenHelper.COLUMN_NAME, title);
        contentValues.put(DBOpenHelper.COLUMN_GRADE, 0);
        sqLiteDatabase.insert(DBOpenHelper.TABLE_NAME, null, contentValues);
    }

    public List<String> getAllRecord() {
        Cursor cursor = sqLiteDatabase.query(DBOpenHelper.TABLE_NAME,
                new String[]{DBOpenHelper.COLUMN_ID, DBOpenHelper.COLUMN_NAME,
                        DBOpenHelper.COLUMN_GRADE},
                null, null, null, null, null);
        cursor.moveToFirst();
        List<String> result = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            result.add(cursor.getInt(0) + "///" +
                    cursor.getString (1) + "///" +
                    cursor.getString(2));
            cursor.moveToNext();
        }
        return result;
    }




}
