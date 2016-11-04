package com.project.markpollution.CustomAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.markpollution.Objects.PollutionPoint;
import com.project.markpollution.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Hung on 04-Nov-16.
 */

public class FeedRecyclerViewAdapter extends RecyclerView.Adapter<FeedRecyclerViewAdapter.FeedRecyclerViewHolder> {
    private Context context;
    private List<PollutionPoint> listPo;

    public FeedRecyclerViewAdapter(Context context, List<PollutionPoint> listPo) {
        this.context = context;
        this.listPo = listPo;
    }

    @Override
    public FeedRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recyclerview_feed, parent, false);
        return new FeedRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedRecyclerViewHolder holder, int position) {
        PollutionPoint curPo = listPo.get(position);
        holder.tvTime.setText(formatDateTime(curPo.getTime()));
        Picasso.with(context).load(Uri.parse(curPo.getImage())).into(holder.ivPicture);
        holder.tvTitle.setText(curPo.getTitle());
        holder.tvDesc.setText(curPo.getDesc());
    }

    @Override
    public int getItemCount() {
        return listPo.size();
    }

    class FeedRecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivAvatar, ivPicture, ivComment, ivRate;
        TextView tvTitle, tvTime, tvDesc, tvComment, tvRate;

        public FeedRecyclerViewHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.imageViewAvatarFeed);
            ivPicture = (ImageView) itemView.findViewById(R.id.imageViewPictureFeed);
            ivComment = (ImageView) itemView.findViewById(R.id.imageViewCommentFeed);
            ivRate = (ImageView) itemView.findViewById(R.id.imageViewRateFeed);
            tvTitle = (TextView) itemView.findViewById(R.id.textViewTitleFeed);
            tvTime = (TextView) itemView.findViewById(R.id.textViewTimeFeed);
            tvDesc = (TextView) itemView.findViewById(R.id.textViewDescFeed);
            tvComment = (TextView) itemView.findViewById(R.id.textViewCommentFeed);
            tvRate = (TextView) itemView.findViewById(R.id.textViewRateFeed);
        }
    }

    private String formatDateTime(String time){
        SimpleDateFormat originFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat resultFormat = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");

        Date datetime = null;
        try {
            datetime = originFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultFormat.format(datetime);
    }
}
