package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;

import com.example.android.popularmovies.data.MovieContract;

/**
 * Created by kakalra on 4/3/2016.
 */
public class Utility
{
    public static int isSetToFavourite(Context context,int id)
    {
        Cursor cursor=context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_ID+" = ?",
                new String []{Integer.toString(id)},
                null
        );
        int numOfRows=cursor.getCount();
        cursor.close();
        return numOfRows;
    }
}
