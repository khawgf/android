package com.example.duolingoapp.bocauhoi;

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
import com.example.duolingoapp.premium.PremiumActivity;
import com.example.duolingoapp.premium.Premium;
import com.example.duolingoapp.trangthai.Status;

import java.util.List;

public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.QuestionViewHolder> {
    private Context context;
    private int layout;
    private List<QuestionList> questionList;
    private List<Status> statusList;
    private List<Premium> premiumList;
    private AlertDialog alertDialog;
    private String nameScreen;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public QuestionListAdapter(Context context, int layout, List<QuestionList> questionList, List<Status> statusList, List<Premium> premiumList, String nameScreen) {
        this.context = context;
        this.layout = layout;
        this.questionList = questionList;
        this.statusList = statusList;
        this.premiumList = premiumList;
        this.nameScreen = nameScreen;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new QuestionViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionList BoHT = questionList.get(position);
        holder.txtTenBo_ENG.setText(BoHT.getStt() + ". " + BoHT.getTenBo_ENG());
        holder.txtTenBo_VIE.setText(BoHT.getStt() + ". " + BoHT.getTenBo_VIE());
        Bitmap img= BitmapFactory.decodeByteArray(BoHT.getImg(),0,BoHT.getImg().length);
        holder.subjectImage.setImageBitmap(img);

        // Kiểm tra ID_Bo của mỗi item có trong statusList không và thiết lập backgroundTint
        boolean isCompleted = isBoCompleted(BoHT.getIdBo());
        if (isCompleted) {
            holder.layoutItem.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F83DD844")));
            holder.txtTenBo_ENG.setTextColor(Color.WHITE);
            holder.txtTenBo_VIE.setTextColor(Color.WHITE);
        } else {
            holder.layoutItem.setBackgroundTintList(null);
            holder.txtTenBo_ENG.setTextColor(Color.BLACK);
            holder.txtTenBo_VIE.setTextColor(Color.BLACK);
        }

        // Kiểm tra ID_Bo của mỗi item có trong premiumList không
        boolean isUnclocked = isBoUnclocked(BoHT.getIdBo());
        if (isUnclocked) {
            holder.txtUnlockClass.setVisibility(View.GONE);
        } else {
            holder.txtUnlockClass.setVisibility(View.VISIBLE);
        }



        // Thêm OnClickListener cho txtUnlockClass
        holder.txtUnlockClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate layout của dialog_opencourses.xml
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_opencourses, null);

                // Tạo dialog
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(dialogView);

                // Tìm các thành phần trong layout dialog_opencourses.xml
                TextView txtCloseCourse = dialogView.findViewById(R.id.txtCloseCourse);
                Button btnOpenCourse = dialogView.findViewById(R.id.btnOpenCourse);

                // Tạo và hiển thị dialog
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                // Gắn OnClickListener cho txtCloseCourse
                txtCloseCourse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss(); // Đóng dialog
                    }
                });

                // Gắn OnClickListener cho btnOpenCourse
                btnOpenCourse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PremiumActivity.class);
                        intent.putExtra("idBo", BoHT.getIdBo());
                        intent.putExtra("screenCurrent", nameScreen);
                        intent.putExtra("tenBo_ENG", BoHT.getTenBo_ENG());
                        context.startActivity(intent);
                        alertDialog.dismiss(); // Đóng dialog
                    }
                });
            }
        });

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    private boolean isBoCompleted(int idBo) {
        for (Status status : statusList) {
            if (status.getIdBo() == idBo) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoUnclocked(int idBo) {
        for (Premium premium : premiumList) {
            if (premium.getIdBo() == idBo) {
                return true;
            }
        }
        return false;
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView txtTenBo_ENG, txtTenBo_VIE;
        TextView txtUnlockClass;

        ImageView subjectImage;

        LinearLayout layoutItem;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            txtTenBo_ENG = itemView.findViewById(R.id.txtSubjectName_ENG);
            txtTenBo_VIE = itemView.findViewById(R.id.txtSubjectName_VIE);
            subjectImage = itemView.findViewById(R.id.ivSubjectImage);
            txtUnlockClass = itemView.findViewById(R.id.txtUnlockClass_ItemSubject);
            layoutItem = itemView.findViewById(R.id.linerLayoutItem);
        }
    }
}