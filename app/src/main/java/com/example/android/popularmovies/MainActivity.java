package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.popularmovies.model.MovieInfoObject;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback
{

    private final String LOG_TAG=MainActivity.class.getSimpleName();
    private static final String MOVIEINFORMATIONFRAGMENT_TAG="MIFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.movie_detail_container)!=null)
        {
            mTwoPane=true;
            if (savedInstanceState==null)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container,new MovieInformationActivityFragment())
                        .commit();
            }
        }
        else
        {
            mTwoPane=false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MovieInfoObject movie)
    {
        if(mTwoPane)
        {
            Bundle args=new Bundle();
            args.putParcelable("movieObj",movie);

            MovieInformationActivityFragment fragment=new MovieInformationActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,fragment,MOVIEINFORMATIONFRAGMENT_TAG)
                    .commit();
        }
        else
        {
            Intent intent=new Intent(this,MovieInformationActivity.class).putExtra("movieObj",movie);
            startActivity(intent);
        }
    }
}
