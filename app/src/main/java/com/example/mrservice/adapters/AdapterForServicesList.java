package com.example.mrservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.R;
import com.example.mrservice.interfaces.OnServiceSelectedI;
import com.example.mrservice.models.TaskCat;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterForServicesList extends RecyclerView.Adapter<AdapterForServicesList.Holder> {

    Context context;
    List<TaskCat> taskCatList;
    OnServiceSelectedI serviceSelectedI;

    public AdapterForServicesList(Object object, Context context, List<TaskCat> taskCatList) {
        this.context = context;
        this.taskCatList = taskCatList;
        serviceSelectedI = (OnServiceSelectedI) object;
    }

    @NonNull
    @Override
    public AdapterForServicesList.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_cat_bottom_sheet, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterForServicesList.Holder holder, int position) {

        final TaskCat cat = taskCatList.get(holder.getAdapterPosition());
        if (cat.getCategoryImageUrl() != null)
            Picasso.get()
                    .load(cat.getCategoryImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerInside()
                    .fit()
                    .into(holder.serviceImage);

        holder.serviceName.setText(cat.getCategoryName());
        holder.cardServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceSelectedI.onServiceSelected(cat.getCategoryId(),cat.getCategoryName());
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
