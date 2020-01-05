package com.example.mrservice.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.fragments.FragmentTaskDescription;
import com.example.mrservice.models.TaskModel;
import com.example.mrservice.models.TransactionModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AdapterTransactionsHistory extends RecyclerView.Adapter<AdapterTransactionsHistory.Holder> {

    private static final String TAG = AdapterTransactionsHistory.class.getName();
    private Context context;
    private List<TransactionModel> transactionModelList;
    private Bundle bundle;
    private FirebaseUser firebaseUser;

    public AdapterTransactionsHistory(Context context, List<TransactionModel> transactionModelList) {
        this.context = context;
        this.transactionModelList = transactionModelList;
        bundle = new Bundle();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_transactions_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TransactionModel transactionModel = transactionModelList.get(holder.getAdapterPosition());

        holder.placeTransactionAmount.setText(transactionModel.getTotalAmount());

        if (transactionModel.getDeductedFrom().equals(firebaseUser.getUid())) {
            holder.placeTransactionHeldOn.setText(transactionModel.getDeductedAtDateTime());
            holder.placeTransactionType.setText("Debited");
        }

        if (transactionModel.getCreditedTo().equals(firebaseUser.getUid())) {

            if (transactionModel.getTransactionStatus().equals(Constants.TRANSACTION_STATUS_PENDING))
                holder.placeTransactionHeldOn.setText("-");
            else
                holder.placeTransactionHeldOn.setText(transactionModel.getSubmitAtDateTime());

            holder.placeTransactionType.setText("Credited");
        }

        holder.placeTransactionStatus.setText((transactionModel.getTransactionStatus().equals(Constants.TRANSACTION_STATUS_COMPLETED) ? "Completed" : "Pending"));

    }

    @Override
    public int getItemCount() {
        return transactionModelList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private CardView cardTransactionHistory;
        private TextView placeTransactionHeldOn, placeTransactionAmount, placeTransactionType, placeTransactionStatus;

        public Holder(@NonNull View itemView) {
            super(itemView);

            cardTransactionHistory = itemView.findViewById(R.id.cardTransactionHistory);

            placeTransactionHeldOn = itemView.findViewById(R.id.placeTransactionHeldOn);
            placeTransactionAmount = itemView.findViewById(R.id.placeTransactionAmount);
            placeTransactionType = itemView.findViewById(R.id.placeTransactionType);
            placeTransactionStatus = itemView.findViewById(R.id.placeTransactionStatus);

            cardTransactionHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TransactionModel transactionModel = transactionModelList.get(getAdapterPosition());
                    bundle.putString(UserProfileModel.STRING_USER_PROFILE_ID_REF, transactionModel.getDeductedFrom());
                    bundle.putString(TaskModel.STRING_TASK_ID_REF, transactionModel.getTaskId());
                    ((FragmentActivity) context)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(android.R.id.content, FragmentTaskDescription.getInstance(bundle), Constants.TITLE_TASK_DESCRIPTION)
                            .addToBackStack(Constants.TITLE_TASK_DESCRIPTION).commit();
                }
            });
        }
    }
}
