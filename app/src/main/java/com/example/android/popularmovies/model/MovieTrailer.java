package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kakalra on 4/2/2016.
 */
public class MovieTrailer implements Parcelable
{
    private String trailerKey;
    private String trailerName;
    private String trailerSite;

    public MovieTrailer(String trailerKey,String trailerName,String trailerSite)
    {
        this.trailerKey=trailerKey;
        this.trailerName=trailerName;
        this.trailerSite=trailerSite;
    }

    private MovieTrailer(Parcel in)
    {
        trailerKey=in.readString();
        trailerName=in.readString();
        trailerSite=in.readString();
    }

    public String getTrailerKey()
    {
        return trailerKey;
    }

    public String getTrailerName()
    {
        return trailerName;
    }

    public String getTrailerSite()
    {
        return trailerSite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i)
    {
        p.writeString(trailerKey);
        p.writeString(trailerName);
        p.writeString(trailerSite);
    }

    public static final Parcelable.Creator<MovieTrailer> CREATOR=new Parcelable.Creator<MovieTrailer>()
    {
        @Override
        public MovieTrailer createFromParcel(Parcel source)
        {
            return new MovieTrailer(source);
        }

        @Override
        public MovieTrailer[] newArray(int i)
        {
            return new MovieTrailer[i];
        }
    };
}
