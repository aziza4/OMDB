        package com.example.jbt.omdb;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.view.ActionMode;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.RatingBar;
        import android.widget.TextView;
        import android.widget.Toast;
        import java.util.ArrayList;


        class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.MovieViewHolder> {

            private final Context mContext;
            private final MoviesDBHelper mDbHelper;
            private final FragmentHelper mFragmentHelper;
            private ArrayList<Movie> mMovies;
            private final MainFragment.OnMainFragListener mListener;


            public MovieRecyclerAdapter(Context context, MainFragment.OnMainFragListener listener, boolean isTabletMode) {
                mContext = context;
                mMovies = null;
                mListener = listener;
                mDbHelper = new MoviesDBHelper(mContext);
                mFragmentHelper = new FragmentHelper((AppCompatActivity)mContext, isTabletMode);
            }


            public void setData(ArrayList<Movie> movies) // called whenever refresh is needed
            {
                mMovies = movies;
                notifyDataSetChanged();
            }


            public void clearData() {

                int size = mMovies.size();

                if (size == 0)
                    return;

                for (int i = 0; i < size; i++)
                    mMovies.remove(0);

                notifyItemRangeRemoved(0, size);
            }


            private void removeItem(Movie movie)
            {
                mMovies.remove(movie);
                notifyDataSetChanged();
            }


            @Override
            public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                LayoutInflater inflater = LayoutInflater.from(mContext);

                @SuppressLint("InflateParams")
                View view = inflater.inflate(R.layout.main_list_item, null);
                return new MovieViewHolder(view);
            }


            @Override
            public void onBindViewHolder(MovieViewHolder holder, int position) {

                holder.bind(mMovies.get(position));
            }


            @Override
            public int getItemCount() {
                return mMovies.size();
            }



            class MovieViewHolder extends RecyclerView.ViewHolder {

                public final ImageView posterIV;
                public final TextView subjectTV;
                public final TextView bodyTV;
                public final RatingBar ratingRatingBar;

                private Movie mMovie;

                public MovieViewHolder(View view) {
                    super(view);

                    posterIV = (ImageView) view.findViewById(R.id.movieImageView);
                    subjectTV = (TextView)view.findViewById(R.id.subjectTextView);
                    bodyTV = (TextView)view.findViewById(R.id.bodyTextView);
                    ratingRatingBar = (RatingBar) view.findViewById(R.id.restRatingBar);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.onMovieEdit(mMovie);
                        }
                    });

                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ((AppCompatActivity) mContext)
                                    .startSupportActionMode(new ActionMode.Callback() {
                                @Override
                                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                    MenuInflater inflater = mode.getMenuInflater();
                                    inflater.inflate(R.menu.main_context_menu, menu);
                                    return true;
                                }

                                @Override
                                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                                    return false;
                                }

                                @Override
                                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                                    switch (item.getItemId())
                                    {
                                        case R.id.editMenuItem:
                                            mListener.onMovieEdit(mMovie);
                                            mode.finish();
                                            return true;

                                        case R.id.deleteMenuItem:
                                            if ( mDbHelper.deleteMovie(mMovie.getId())) {
                                                removeItem(mMovie);
                                                String movieDeletedMsg = mContext.getString(R.string.movie_deleted_msg);
                                                Toast.makeText(mContext, movieDeletedMsg, Toast.LENGTH_SHORT).show();
                                                mFragmentHelper.onMovieDelete();

                                            }
                                            mode.finish();
                                            return true;

                                        default:
                                            return false;
                                    }
                                }

                                @Override
                                public void onDestroyActionMode(ActionMode mode) { }
                            });

                            return true;
                        }
                    });
                }


                public void bind(Movie movie)
                {
                    mMovie = movie;

                    bodyTV.setText(movie.getBody());
                    Bitmap imageNA = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_na);
                    Bitmap image = movie.getImage() != null ? movie.getImage() : imageNA; // placeholder image if no image
                    posterIV.setImageBitmap(image);
                    subjectTV.setText(movie.getSubject());
                    bodyTV.setText(movie.getBody());
                    ratingRatingBar.setRating(movie.getRating());
                }
            }
        }