package com.example.android.popularmovies.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.MovieTrailer;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by kakalra on 4/2/2016.
 */
public class TrailerAdapter extends ArrayAdapter<MovieTrailer>
{
    private Context context;
    private static final String LOG_TAG=TrailerAdapter.class.getSimpleName();

    public TrailerAdapter(Activity context,List<MovieTrailer> trailerList)
    {
        super(context,0,trailerList);
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        MovieTrailer movieTrailerObject=getItem(position);
        if (convertView==null)
        {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer,parent,false);
        }
        ImageView iconView=(ImageView)convertView.findViewById(R.id.trailer_imageview);
        Picasso.with(context).load("http://img.youtube.com/vi/"+movieTrailerObject.getTrailerKey()+"/0.jpg").into(iconView);

        TextView textView=(TextView)convertView.findViewById(R.id.trailer_textview);
        textView.setText(movieTrailerObject.getTrailerName());

        return convertView;
    }
}
