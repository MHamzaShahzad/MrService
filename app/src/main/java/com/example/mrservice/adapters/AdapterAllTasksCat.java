package com.example.mrservice.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.admin.FragmentCatCRUD;
import com.example.mrservice.fragments.FragmentGetTaskDetail;
import com.example.mrservice.models.TaskCat;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterAllTasksCat extends RecyclerView.Adapter<AdapterAllTasksCat.Holder> {

    private static final String TAG = AdapterAllTasksCat.class.getName();
    private Context context;
    private List<TaskCat> taskCatList;
    private boolean isAdmin;

    public AdapterAllTasksCat(Context context, List<TaskCat> taskCatList, boolean isAdmin) {

        this.context = context;
        this.taskCatList = taskCatList;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_cat, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        final TaskCat cat = taskCatList.get(holder.getAdapterPosition());

        if (cat.getCategoryImageUrl() != null && !cat.getCategoryImageUrl().equals("") && !cat.getCategoryImageUrl().equals("null"))
            Picasso.get()
                    .load(cat.getCategoryImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .centerInside().fit()
                    .into(holder.serviceImage);

        holder.serviceName.setText(cat.getCategoryName());

        holder.cardServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.STRING_TASKS_CAT_OBJ, taskCatList.get(holder.getAdapterPosition()));

                if (isAdmin) {
                    FragmentCatCRUD fragmentCatCRUD = FragmentCatCRUD.getInstance();
                    fragmentCatCRUD.setArguments(bundle);
                    fragmentCatCRUD.show(((FragmentActivity) context).getSupportFragmentManager(), "");
                } else {
                    Log.e(TAG, "onClick: NOT_ADMIN" );
                    ((FragmentActivity) context)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(android.R.id.content, FragmentGetTaskDetail.getInstance(bundle), Constants.TITLE_TASK_DETAIL)
                            .addToBackStack(Constants.TITLE_TASK_DETAIL)
                            .commit();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return taskCatList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        CardView cardServices;
        ImageView serviceImage;
        TextView serviceName;

        public Holder(@NonNull View itemView) {
            super(itemView);

            cardServices = itemView.findViewById(R.id.cardServices);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);

        }
    }
}
