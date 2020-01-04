package lol.kek.lab4;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rss.db"; // название бд
    private static final int SCHEMA = 2; // версия базы данных
    static final String TABLE = "rss"; // название таблицы в бд
    // названия столбцов
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCR = "description";
    public static final String COLUMN_PUBDATE = "pubDate";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_IMAGEURL = "imageUrl";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE + " TEXT, "
                + COLUMN_DESCR + " TEXT, " + COLUMN_PUBDATE + " LONG," + COLUMN_LINK
                + " TEXT, " + COLUMN_IMAGEURL + " TEXT);" );
        long curentDate = new Date().getTime();

        db.execSQL("INSERT INTO "+ TABLE +" (" + COLUMN_TITLE + ", " + COLUMN_DESCR + ", "
                + COLUMN_PUBDATE  + ", " + COLUMN_LINK + ", " + COLUMN_IMAGEURL + ") VALUES ('TITLE1', 'SIMPLE TEXT1', '" +  curentDate + "', 'link1', 'link2');");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}
