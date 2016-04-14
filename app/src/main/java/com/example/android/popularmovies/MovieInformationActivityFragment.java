package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.ShareActionProvider;

import com.example.android.popularmovies.Adapters.TrailerAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.model.MovieInfoObject;
import com.example.android.popularmovies.model.MovieTrailer;
import com.squareup.picasso.Picasso;

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
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieInformationActivityFragment extends Fragment {

    private final String LOG_TAG=MovieInformationActivityFragment.class.getSimpleName();

    ArrayAdapter movieReviewAdapter;
    String movieReviewArray[];
    ArrayList<String> reviewDataList;

    MovieInfoObject movieObject;

    TrailerAdapter movieTrailerAdapter;
    MovieTrailer movieTrailerArray[];
    ArrayList<MovieTrailer> trailerList;

    public static final String VIDEOS="videos";
    public static final String REVIEWS="reviews";

    private ShareActionProvider mShareActionProvider;

    private MovieTrailer mTrailer;

    public MovieInformationActivityFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null || !savedInstanceState.containsKey("movie_review_array_list") || !savedInstanceState.containsKey("movie_trailer_array_list"))
        {
            reviewDataList=new ArrayList<String>();
            trailerList=new ArrayList<MovieTrailer>();
        }
        else
        {
            reviewDataList=savedInstanceState.getStringArrayList("movie_review_array_list");
            trailerList=savedInstanceState.getParcelableArrayList("movie_trailer_array_list");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putStringArrayList("movie_review_array_list",reviewDataList);
        outState.putParcelableArrayList("movie_trailer_array_list", trailerList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        if (movieObject!=null)
        {
            inflater.inflate(R.menu.menu_movie_fragment_detail,menu);
            final MenuItem favourite=menu.findItem(R.id.favourite_item);
            MenuItem share=menu.findItem(R.id.action_share);

            new AsyncTask<Void,Void,Integer>()
            {
                @Override
                protected Integer doInBackground(Void... params)
                {
                    return Utility.isSetToFavourite(getActivity(),movieObject.getMovieId());
                }

                @Override
                protected void onPostExecute(Integer integer)
                {
                    if(integer==1)
                        favourite.setIcon(R.drawable.star_on);
                    else
                        favourite.setIcon(R.drawable.star_off);
                }
            }.execute();

            mShareActionProvider=(ShareActionProvider) MenuItemCompat.getActionProvider(share);
            if(mTrailer!=null)
            {
                mShareActionProvider.setShareIntent(shareTrailerIntent());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        int itemId=item.getItemId();
        switch (itemId)
        {
            case R.id.favourite_item:
                if (movieObject!=null)
                {
                    // to check if movie is set to a favourite movie.
                    new AsyncTask<Void,Void,Integer>()
                    {
                        @Override
                        protected Integer doInBackground(Void... params)
                        {
                            return Utility.isSetToFavourite(getActivity(),movieObject.getMovieId());

                        }

                        @Override
                        protected void onPostExecute(Integer integer)
                        {
                            if(integer==1)
                            {
                                //movie is already in favourites. Delete it from favorites.
                                new AsyncTask<Void,Void,Integer>()
                                {
                                    @Override
                                    protected Integer doInBackground(Void... params)
                                    {
                                        int rowsDeleted=getActivity().getContentResolver().delete(
                                                MovieContract.MovieEntry.CONTENT_URI,
                                                MovieContract.MovieEntry.COLUMN_ID+" = ?",
                                                new String[]{Integer.toString(movieObject.getMovieId())}
                                        );
                                        return rowsDeleted;
                                    }

                                    @Override
                                    protected void onPostExecute(Integer integer)
                                    {
                                        item.setIcon(R.drawable.star_off);
                                        Toast toast=Toast.makeText(getContext(),R.string.removed_from_favourites,Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }.execute();
                            }
                            else
                            {
                                //movie is not in favourites. Add it to favourites.
                                new AsyncTask<Void,Void,Uri>()
                                {
                                    @Override
                                    protected Uri doInBackground(Void... params)
                                    {
                                        ContentValues contentValues=new ContentValues();

                                        contentValues.put(MovieContract.MovieEntry.COLUMN_ID,movieObject.getMovieId());
                                        contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE,movieObject.getMovieOriginalTitle());
                                        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL,movieObject.getMoviePosterPath());
                                        contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,movieObject.getMovieOverview());
                                        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movieObject.getMovieReleaseDate());
                                        contentValues.put(MovieContract.MovieEntry.COLUMN_RATING,movieObject.getMovieRating());

                                        return getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,contentValues);
                                    }

                                    @Override
                                    protected void onPostExecute(Uri uri)
                                    {
                                        item.setIcon(R.drawable.star_on);
                                        Toast toast=Toast.makeText(getContext(),R.string.added_to_favourites,Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }.execute();
                            }
                        }
                    }.execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Bundle arguements=getArguments();
        if(arguements != null)
        {
            movieObject=arguements.getParcelable("movieObj");
        }
        View rootView=inflater.inflate(R.layout.fragment_movie_information, container, false);

        TextView movieTitleTextView = ((TextView) rootView.findViewById(R.id.movie_title_textView));
        ImageView moviePosterView = ((ImageView) rootView.findViewById(R.id.movie_poster_imageView));
        TextView movieRatingTextView = ((TextView) rootView.findViewById(R.id.movie_rating_textView));
        TextView movieOverviewTextView = ((TextView) rootView.findViewById(R.id.movie_overview_textView));
        TextView movieReleaseDateTextView = ((TextView) rootView.findViewById(R.id.movie_releaseDate_textView));

        //loading trailer into a customized view.
        ListView movieTrailerListView = ((ListView) rootView.findViewById(R.id.movie_trailer_listView));
        movieTrailerAdapter = new TrailerAdapter(getActivity(), trailerList);
        movieTrailerListView.setAdapter(movieTrailerAdapter);

        movieTrailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieTrailer trailer = movieTrailerAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getTrailerKey()));
                startActivity(intent);
            }
        });

        movieReviewAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.movie_review_list_item,
                R.id.movie_review_item_textview,
                reviewDataList
        );

        //loading the movie reviews into a list view.
        ListView movieReviewListView = ((ListView) rootView.findViewById(R.id.movie_review_listView));
        movieReviewListView.setAdapter(movieReviewAdapter);

        if(movieObject != null) {

            //loading image title into text view.
            movieTitleTextView.setText(movieObject.getMovieOriginalTitle());

            //loading image into image view.
            Picasso.with(getContext())
                    .load(movieObject.getMoviePosterPath())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(moviePosterView);

            //loading rating into text view.
            movieRatingTextView.setText("Rating : " + movieObject.getMovieRating() + "/10");

            //loading overview into text view.
            movieOverviewTextView.setText(movieObject.getMovieOverview());

            //loading release date into text view
            movieReleaseDateTextView.setText("Release Date : " + movieObject.getMovieReleaseDate());
        }
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(movieObject!=null)
        {
            fetchTrailer();
            fetchReview();
        }
    }

    private void fetchReview()
    {
        FetchMovieReviewTask reviewTask=new FetchMovieReviewTask();
        reviewTask.execute(movieObject.getMovieId());
    }

    private void fetchTrailer()
    {
        FetchMovieTrailerTask trailerTask=new FetchMovieTrailerTask();
        trailerTask.execute(movieObject.getMovieId());
    }

    private Intent shareTrailerIntent()
    {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,movieObject.getMovieOriginalTitle()+": "+"http://www.youtube.com/watch?v=" + mTrailer.getTrailerKey());
        return intent;
    }

    public class FetchMovieReviewTask extends AsyncTask<Integer,Void,String[]>
    {
        private String[] getMovieReviewFromJson(String movieReviewJsonString)throws JSONException
        {
            final String OWM_RESULT="results";
            final String OWM_CONTENT="content";

            JSONObject movieReviewJson=new JSONObject(movieReviewJsonString);
            JSONArray jsonMovieReviewArray=movieReviewJson.getJSONArray(OWM_RESULT);

            movieReviewArray=new String[jsonMovieReviewArray.length()];

            for(int i=0;i<jsonMovieReviewArray.length();i++)
            {
                JSONObject reviewObject=jsonMovieReviewArray.getJSONObject(i);
                String content=reviewObject.getString(OWM_CONTENT);

                movieReviewArray[i]=new String(content);
            }

            return movieReviewArray;
        }

        @Override
        protected String[] doInBackground(Integer... params)
        {
            HttpURLConnection urlConnection=null;
            BufferedReader reader=null;

            String movieReviewJsonString=null;
            int movieId=params[0];

            try
            {
                final String BASE_URL="http://api.themoviedb.org/3/movie/";
                final String API_KEY="api_key";

                Uri builtUri=Uri.parse(BASE_URL).buildUpon()
                        .appendPath(String.valueOf(movieId))
                        .appendPath(REVIEWS)
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

                movieReviewJsonString=buffer.toString();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG,"Error",e);
                Toast toast=Toast.makeText(getContext(),R.string.review_fetch_failed,Toast.LENGTH_SHORT);
                toast.show();
                return null;
            }
            finally
            {
                if (urlConnection != null)
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
                return getMovieReviewFromJson(movieReviewJsonString);
            }
            catch(JSONException e)
            {
                Log.e(LOG_TAG, "Error in parsing Json string", e);
                Toast toast=Toast.makeText(getContext(),R.string.review_fetch_failed,Toast.LENGTH_SHORT);
                toast.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result)
        {
            if(result!=null)
            {
                movieReviewAdapter.clear();
                Log.v(LOG_TAG,"in postExecute");
                for(String str:result)
                    movieReviewAdapter.add(str);
            }
        }
    }

    public class FetchMovieTrailerTask extends AsyncTask<Integer,Void,MovieTrailer[]>
    {
        private MovieTrailer[] getMovieTrailerDetailsFromJson(String movieTrailerJsonString)throws JSONException
        {
            final String OWM_RESULT="results";
            final String OWM_KEY="key";
            final String OWM_NAME="name";
            final String OWM_SITE="site";

            JSONObject movieTrailerJson=new JSONObject(movieTrailerJsonString);
            JSONArray jsonMovieTrailerArray=movieTrailerJson.getJSONArray(OWM_RESULT);

            movieTrailerArray=new MovieTrailer[jsonMovieTrailerArray.length()];
            for(int i=0;i<jsonMovieTrailerArray.length();i++)
            {
                JSONObject trailerObject=jsonMovieTrailerArray.getJSONObject(i);
                String key=trailerObject.getString(OWM_KEY);
                String name=trailerObject.getString(OWM_NAME);
                String site=trailerObject.getString(OWM_SITE);

                movieTrailerArray[i]=new MovieTrailer(key,name,site);
            }
            return movieTrailerArray;
        }

        @Override
        protected MovieTrailer[] doInBackground(Integer... params)
        {
            HttpURLConnection urlConnection=null;
            BufferedReader reader=null;

            String movieTrailerJsonString=null;
            int movieId=params[0];

            try
            {
                final String BASE_URL="http://api.themoviedb.org/3/movie/";
                final String API_KEY="api_key";

                Uri builtUri=Uri.parse(BASE_URL).buildUpon()
                        .appendPath(String.valueOf(movieId))
                        .appendPath(VIDEOS)
                        .appendQueryParameter(API_KEY,BuildConfig.MOVIE_API_KEY)
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

                movieTrailerJsonString=buffer.toString();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG,"Error",e);
                Toast toast=Toast.makeText(getContext(),R.string.trailer_fetch_failed,Toast.LENGTH_SHORT);
                toast.show();
                return null;
            }
            finally
            {
                if (urlConnection != null)
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
                return getMovieTrailerDetailsFromJson(movieTrailerJsonString);
            }
            catch(JSONException e)
            {
                Log.e(LOG_TAG,"Error in parsing Json string",e);
                Toast toast=Toast.makeText(getContext(),R.string.trailer_fetch_failed,Toast.LENGTH_SHORT);
                toast.show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MovieTrailer[] movieTrailers)
        {
            movieTrailerAdapter.clear();
            Log.v(LOG_TAG,"in onPostExecute");
            for(MovieTrailer trailerObject:movieTrailers)
                movieTrailerAdapter.add(trailerObject);

            mTrailer=movieTrailers[0];
            if(mShareActionProvider!=null)
            {
                mShareActionProvider.setShareIntent(shareTrailerIntent());
            }
        }
    }
}
