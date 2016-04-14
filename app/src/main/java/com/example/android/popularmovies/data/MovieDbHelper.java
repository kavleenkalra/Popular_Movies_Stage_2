package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by kakalra on 4/3/2016.
 */
public class MovieDbHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION=1;

    static final String DATABASE_NAME="movie.db";

    public MovieDbHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String SQL_CREATE_MOVIE_TABLE="CREATE TABLE "+MovieEntry.TABLE_NAME+" ("+
                MovieEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                MovieEntry.COLUMN_ID+" INTEGER NOT NULL,"+
                MovieEntry.COLUMN_TITLE+" TEXT NOT NULL,"+
                MovieEntry.COLUMN_POSTER_URL+" TEXT NOT NULL,"+
                MovieEntry.COLUMN_OVERVIEW+" TEXT NOT NULL,"+
                MovieEntry.COLUMN_RELEASE_DATE+" TEXT NOT NULL,"+
                MovieEntry.COLUMN_RATING+" TEXT NOT NULL "+
                ");";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
