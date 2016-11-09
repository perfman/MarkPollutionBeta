package com.project.markpollution.CustomAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.markpollution.Interfaces.OnItemClickListener;
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
//    private String url_CountCommentByPo = "http://indi.com.vn/dev/markpollution/CountCommentByPo.php?id_po=";
//    private String url_SumRateByPo = "http://indi.com.vn/dev/markpollution/SumRateByPo.php?id_po=";
    private OnItemClickListener onItemClickListener;

    public FeedRecyclerViewAdapter(Context context, List<PollutionPoint> listPo) {
        this.context = context;
        this.listPo = listPo;
    }

    @Override
    public FeedRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recyclerview_feed2, parent, false);
        return new FeedRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedRecyclerViewHolder holder, int position) {
        final PollutionPoint curPo = listPo.get(position);
        setCateIcon(curPo.getId_cate(), holder.ivCate);
        holder.tvTitle.setText(curPo.getTitle());
        holder.tvTime.setText(formatDateTime(curPo.getTime()));
        holder.tvDesc.setText(curPo.getDesc());
        Picasso.with(context).load(Uri.parse(curPo.getImage())).placeholder(R.drawable.placeholder).into(holder.ivPicture);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(curPo);
            }
        };
        holder.container.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return listPo.size();
    }

    class FeedRecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivCate, ivPicture;
        TextView tvTitle, tvTime, tvDesc;
        CardView container;

        public FeedRecyclerViewHolder(View itemView) {
            super(itemView);
            ivCate = (ImageView) itemView.findViewById(R.id.ivCateFeed);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivPictureFeed);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitleFeed);
            tvTime = (TextView) itemView.findViewById(R.id.tvTimeFeed);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescFeed);
            container = (CardView) itemView.findViewById(R.id.card_view_feed);
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

//    private void setCountCommentByPo(String id_po, final TextView ivHolder){
//        StringRequest strReq = new StringRequest(Request.Method.GET, url_CountCommentByPo +
//                id_po, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                if(!response.equals("Count comment failure")){
//                    ivHolder.setText(response);
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Volley.newRequestQueue(context).add(strReq);
//    }

//    private void setSumRateByPo(String id_po, final TextView tvHolder){
//        StringRequest strReq = new StringRequest(Request.Method.GET, url_SumRateByPo + id_po , new
//                Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        tvHolder.setText(response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Volley.newRequestQueue(context).add(strReq);
//    }

    private void setCateIcon(String cateID, ImageView holder){
        switch (cateID){
            case "1":
                holder.setImageResource(R.drawable.land_icon);
                break;
            case "2":
                holder.setImageResource(R.drawable.water);
                break;
            case "3":
                holder.setImageResource(R.drawable.air_icon);
                break;
            case "4":
                holder.setImageResource(R.drawable.thermal_icon);
                break;
            case "5":
                holder.setImageResource(R.drawable.light_icon);
                break;
            case "6":
                holder.setImageResource(R.drawable.noise_icon);
                break;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
