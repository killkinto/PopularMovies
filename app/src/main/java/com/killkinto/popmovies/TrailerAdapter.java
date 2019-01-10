package com.killkinto.popmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.killkinto.popmovies.model.Trailer;


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private List<Trailer> mTrailers;
    private TrailerAdapterListener mTrailerListener;
    private final Context mContext;

    interface TrailerAdapterListener {
        void onClick(Trailer trailer);
    }

    public TrailerAdapter(@NonNull Context context, TrailerAdapterListener listener) {
        mTrailerListener = listener;
        mContext = context;
    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.trailer_item, parent, false);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder holder, int position) {
        holder.mNameTextView.setText(mTrailers.get(position).mName);
    }

    public void swapTrailers(List<Trailer> trailers) {
        mTrailers = trailers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTrailers != null ? mTrailers.size() : 0;
    }

    class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView mNameTextView;

        TrailerAdapterViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.tv_trailer_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mTrailerListener.onClick(mTrailers.get(getAdapterPosition()));
        }
    }
}
