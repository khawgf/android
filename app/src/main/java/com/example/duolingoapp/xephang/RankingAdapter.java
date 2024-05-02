package com.example.duolingoapp.xephang;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionList;
import com.example.duolingoapp.premium.Premium;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.taikhoan.User;
import com.example.duolingoapp.trangthai.Status;

import com.example.duolingoapp.bocauhoi.QuestionListAdapter;

import java.util.HashMap;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder>{
    private Context context;
    private int layout;
    private List<Status> statusList;
    private List<User> userList;
    private HashMap<Integer, Integer> topResources = new HashMap<>();

    private DatabaseAccess DB;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private QuestionListAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(QuestionListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void initTopResources() {
        topResources.put(1, R.drawable.top1);
        topResources.put(2, R.drawable.top2);
        topResources.put(3, R.drawable.top3);
    }
    public RankingAdapter(Context context, int layout, List<Status> statusList, List<User> userList) {
        this.context = context;
        this.layout = layout;
        this.statusList = statusList;
        this.userList = userList;
        initTopResources(); // Khởi tạo HashMap
        DB = DatabaseAccess.getInstance(context);
    }

    @NonNull
    @Override
    public RankingAdapter.RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new RankingAdapter.RankingViewHolder(view);
    }

    @SuppressLint({"RecyclerView", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RankingAdapter.RankingViewHolder holder, int position) {
        Status status = statusList.get(position);
        User user = getUser(status.getIdUser());
        int stt = position + 1;
        if (topResources.containsKey(stt)) {
            holder.txtRankingNumber.setVisibility(View.GONE);
            holder.imgTopRanking.setVisibility(View.VISIBLE);
            int resourceId = topResources.get(stt);
            Bitmap imgTop = BitmapFactory.decodeResource(context.getResources(), resourceId);
            holder.imgTopRanking.setImageBitmap(imgTop);
        } else {
            holder.imgTopRanking.setVisibility(View.GONE);
            holder.txtRankingNumber.setVisibility(View.VISIBLE);
            holder.txtRankingNumber.setText(stt + ". ");
        }
        if (user!=null){
            holder.txtUserName.setText(user.getHoTen());
            User user1 = DB.getUser(user.getIduser());
            if (user1!=null){
                byte[] imgSQLite = user1.getImg();
                if (imgSQLite == null || imgSQLite.length == 0) {
                    // Nếu không có dữ liệu, gán hình ảnh mặc định
                    holder.imgUser.setImageResource(R.drawable.icon_user);
                } else {
                    Bitmap img = BitmapFactory.decodeByteArray(imgSQLite, 0, imgSQLite.length);
                    holder.imgUser.setImageBitmap(img);
                }
            } else {
                holder.imgUser.setImageResource(R.drawable.icon_user);
            }

        }
        holder.txtUserScore.setText(String.valueOf(status.getScore()));



    }

    private User getUser(String idUser) {
        for (User user : userList) {
            if (user.getIduser().equals(idUser)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (statusList != null) {
            return statusList.size();
        } else {
            return 0;
        }
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView txtRankingNumber, txtUserName, txtUserScore;
        ImageView imgUser, imgTopRanking;

        public RankingViewHolder(View itemView) {
            super(itemView);
            txtRankingNumber = itemView.findViewById(R.id.txtRankingNumber);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserScore = itemView.findViewById(R.id.txtUserScore);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgTopRanking = itemView.findViewById(R.id.imgTopRanking);
        }
    }
}