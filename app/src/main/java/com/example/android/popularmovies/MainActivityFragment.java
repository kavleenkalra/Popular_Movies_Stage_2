package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.model.MovieInfoObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
{
    private final String MAIN_TAG=MainActivityFragment.class.getSimpleName();

    private MovieAdapter movieAdapter;
    MovieInfoObject movieInfoArray[];

    private ArrayList<MovieInfoObject> movieList;

    private static final String[] MOVIE_COLUMNS={
        MovieContract.MovieEntry._ID,
        MovieContract.MovieEntry.COLUMN_ID,
        MovieContract.MovieEntry.COLUMN_TITLE,
        MovieContract.MovieEntry.COLUMN_POSTER_URL,
        MovieContract.MovieEntry.COLUMN_OVERVIEW,
        MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
        MovieContract.MovieEntry.COLUMN_RATING
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_POSTER_PATH = 3;
    public static final int COL_OVERVIEW = 4;
    public static final int COL_RELEASE_DATE = 5;
    public static final int COL_RATING = 6;

    public interface Callback
    {
        void onItemSelected(MovieInfoObject movie);
    }

    public MainActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState==null || !savedInstanceState.containsKey("movies"))
        {
            movieList=new ArrayList<MovieInfoObject>();
        }
        else
        {
            movieList=savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        movieAdapter=new MovieAdapter(getActivity(), movieList);
        GridView gridView=(GridView)rootView.findViewById(R.id.mainGrid);
        gridView.setAdapter(movieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfoObject movieObject=movieAdapter.getItem(position);
                ((Callback)getActivity()).onItemSelected(movieObject);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    //this function is responsible for calling the background thread functions.
    private void updateMovies()
    {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType=prefs.getString(getString(R.string.pref_sort_key),getString(R.string.pref_way_default));
        Log.v(MAIN_TAG,"sort type is:"+sortType);
        if (sortType.contentEquals("favourite"))
        {
            Log.v(MAIN_TAG,"in favourite");
            FetchFavouriteMovieTask favouriteMovieTask=new FetchFavouriteMovieTask(getActivity());
            favouriteMovieTask.execute();
        }
        else
        {
            Log.v(MAIN_TAG,"in else");
            FetchMovieTask movieTask=new FetchMovieTask();
            movieTask.execute(sortType);//sortType parameter signifies which sorting criteria to use.
        }
    }

    public class FetchMovieTask extends AsyncTask<String,Void,MovieInfoObject[]>
    {
        private final String  LOG_TAG=FetchMovieTask.class.getSimpleName();

        private MovieInfoObject[] getMovieDataFromJsonStr(String movieJsonStr)throws JSONException
        {
            final String OWM_RESULT="results";
            final String OWM_ID="id";
            final String OWM_TITLE="original_title";
            final String OWM_OVERVIEW="overview";
            final String OWM_RELEASEDATE="release_date";
            final String OWM_POSTERPATH="poster_path";
            final String OWM_RATING="vote_average";

            JSONObject movieJson=new JSONObject(movieJsonStr);
            JSONArray jsonMovieArray=movieJson.getJSONArray(OWM_RESULT);

            movieInfoArray=new MovieInfoObject[jsonMovieArray.length()];

            for (int i=0;i<jsonMovieArray.length();i++)
            {
                JSONObject movieObject=jsonMovieArray.getJSONObject(i);
                int id=movieObject.getInt(OWM_ID);
                String title=movieObject.getString(OWM_TITLE);
                String posterPath=movieObject.getString(OWM_POSTERPATH);
                String overview=movieObject.getString(OWM_OVERVIEW);
                String releaseDate=movieObject.getString(OWM_RELEASEDATE);
                String rating=movieObject.getString(OWM_RATING);

                movieInfoArray[i]=new MovieInfoObject(id,title,posterPath,overview,releaseDate,rating);

            }
            return movieInfoArray;
        }

        @Override
        protected MovieInfoObject[] doInBackground(String... params)
        {
            HttpURLConnection urlConnection=null;
            BufferedReader reader=null;

            String movieJsonStr=null;

            String sort_criteria=params[0];

            try
            {
                final String BASE_URL="http://api.themoviedb.org/3/movie/";
                final String API_KEY="api_key";

                Uri builtUri=Uri.parse(BASE_URL).buildUpon()
                        .appendPath(sort_criteria)
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                        .build();

                URL url=new URL(builtUri.toString());

                Log.v(LOG_TAG,"url is:"+url.toString());
                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();
                if (inputStream==null)
                    return null;

                reader=new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line=reader.readLine())!=null)
                    buffer.append(line+"\n");

                if (buffer.length()==0)
                    return null;

                movieJsonStr=buffer.toString();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error", e);
                return null;
            }
            finally
            {
                if (urlConnection!=null)
                    urlConnection.disconnect();
                if (reader!=null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        Log.e(LOG_TAG,"Error closing stream",e);
                    }
                }
            }

            try
            {
                return getMovieDataFromJsonStr(movieJsonStr);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG,"Error in parsing Json string",e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieInfoObject[] result)
        {
            if(result!=null)
            {
                movieAdapter.clear();
                Log.v(LOG_TAG,"in postExecute");
                for (MovieInfoObject obj:result)
                    movieAdapter.add(obj);
            }
        }
    }

    public class FetchFavouriteMovieTask extends AsyncTask<Void,Void,MovieInfoObject[]>
    {
        private Context mContext;
        private final String LOG_TAG=FetchFavouriteMovieTask.class.getSimpleName();

        public FetchFavouriteMovieTask(Context context)
        {
            this.mContext=context;
        }

        private MovieInfoObject[] getFavouriteMovieDataFromCursor(Cursor cursor)
        {
            movieInfoArray=new MovieInfoObject[cursor.getCount()];
            int i=0;
            if (cursor!=null && cursor.moveToFirst())
            {
                do
                {
                    int id=cursor.getInt(COL_MOVIE_ID);
                    String title=cursor.getString(COL_TITLE);
                    String posterPath=cursor.getString(COL_POSTER_PATH);
                    String overview=cursor.getString(COL_OVERVIEW);
                    String releaseDate=cursor.getString(COL_RELEASE_DATE);
                    String rating=cursor.getString(COL_RATING);

                    movieInfoArray[i]=new MovieInfoObject(id,title,posterPath,overview,releaseDate,rating);
                    i++;
                }while (cursor.moveToNext());
                cursor.close();
            }
            return movieInfoArray;
        }

        @Override
        protected MovieInfoObject[] doInBackground(Void... params)
        {
            Cursor cursor=mContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,null
            );
            return getFavouriteMovieDataFromCursor(cursor);
        }

        @Override
        protected void onPostExecute(MovieInfoObject[] movieInfoObjects)
        {
            movieAdapter.clear();
            Log.v(LOG_TAG,"in onPostExecute");
            for (MovieInfoObject obj:movieInfoObjects)
                movieAdapter.add(obj);
        }
    }
}
