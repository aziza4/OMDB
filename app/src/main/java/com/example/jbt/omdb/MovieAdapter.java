package com.example.jbt.omdb;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public static class ViewHolder {
        public final ImageView posterIV;
        public final TextView subjectTV;
        public final TextView bodyTV;
        public final RatingBar ratingRatingBar;


        public ViewHolder(View view) {
            posterIV = (ImageView) view.findViewById(R.id.movieImageView);
            subjectTV = (TextView)view.findViewById(R.id.subjectTextView);
            bodyTV = (TextView)view.findViewById(R.id.bodyTextView);
            ratingRatingBar = (RatingBar) view.findViewById(R.id.restRatingBar);
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {

        Movie movie = Utility.getMovieFromCursor(c);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Bitmap imageNA = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_na);
        Bitmap image = movie.getImage() != null ? movie.getImage() : imageNA;

        viewHolder.posterIV.setImageBitmap(image);
        viewHolder.subjectTV.setText(movie.getSubject());
        viewHolder.bodyTV.setText(movie.getBody());
        viewHolder.ratingRatingBar.setRating(movie.getRating());
    }
}
