package lol.kek.lab4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DatabaseAdapter {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseAdapter(Context context){
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public DatabaseAdapter open(){
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    private Cursor getAllEntries(){
        String[] columns = new String[] {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DESCR,
                DatabaseHelper.COLUMN_PUBDATE, DatabaseHelper.COLUMN_LINK, DatabaseHelper.COLUMN_IMAGEURL};
        return  database.query(DatabaseHelper.TABLE, columns, null, null, null, null, null);
    }

    public ArrayList<RssItem> getRsses(){
        ArrayList<RssItem> notes = new ArrayList<>();
        Cursor cursor = getAllEntries();
        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCR));
                long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_PUBDATE));
                String link = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LINK));
                String imageUrl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGEURL));

                notes.add(new RssItem(id, title, description, new Date(pubDate), link, imageUrl));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return  notes;
    }

    public long getCount(){
        return DatabaseUtils.queryNumEntries(database, DatabaseHelper.TABLE);
    }

    public long insert(RssItem rssItem){

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TITLE, rssItem.getTitle());
        cv.put(DatabaseHelper.COLUMN_DESCR, rssItem.getDescription());
        cv.put(DatabaseHelper.COLUMN_PUBDATE, rssItem.getPubDate().getTime());
        cv.put(DatabaseHelper.COLUMN_LINK, rssItem.getLink());
        cv.put(DatabaseHelper.COLUMN_IMAGEURL, rssItem.getImageUrl());

        return  database.insert(DatabaseHelper.TABLE, null, cv);
    }

    public void refreshDB(ArrayList<RssItem> rssItems) {
        clear();
        insertMultiple(rssItems);
    }

    public void insertMultiple(ArrayList<RssItem> rssItems) {
        database.beginTransaction();

        for (RssItem rssItem : rssItems) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_TITLE, rssItem.getTitle());
            cv.put(DatabaseHelper.COLUMN_DESCR, rssItem.getDescription());
            cv.put(DatabaseHelper.COLUMN_PUBDATE, rssItem.getPubDate().getTime());
            cv.put(DatabaseHelper.COLUMN_LINK, rssItem.getLink());
            cv.put(DatabaseHelper.COLUMN_IMAGEURL, rssItem.getImageUrl());
            database.insert(DatabaseHelper.TABLE, null, cv);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public long delete(long noteId){
        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(noteId)};
        return database.delete(DatabaseHelper.TABLE, whereClause, whereArgs);
    }

    public long update(RssItem rssItem){
        String whereClause = DatabaseHelper.COLUMN_ID + "=" + String.valueOf(rssItem.getId());
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TITLE, rssItem.getTitle());
        cv.put(DatabaseHelper.COLUMN_DESCR, rssItem.getDescription());
        cv.put(DatabaseHelper.COLUMN_PUBDATE, rssItem.getPubDate().getTime());
        cv.put(DatabaseHelper.COLUMN_LINK, rssItem.getLink());
        cv.put(DatabaseHelper.COLUMN_IMAGEURL, rssItem.getImageUrl());
        return database.update(DatabaseHelper.TABLE, cv, whereClause, null);
    }

    public void clear() {
        database.execSQL("DELETE FROM " + dbHelper.TABLE + ";");
    }

}
