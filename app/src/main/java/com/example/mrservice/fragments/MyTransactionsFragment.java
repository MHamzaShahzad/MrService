package com.example.mrservice.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterTransactionsHistory;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.models.TransactionModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyTransactionsFragment extends Fragment {

    private static final String TAG = MyTransactionsFragment.class.getName();
    private Context context;
    private View view;

    private List<TransactionModel> transactionModelList;
    private AdapterTransactionsHistory adapterTransactionsHistory;
    private RecyclerView recyclerTransactionsHistory;
    private ValueEventListener transactionsEventListener;
    private Bundle arguments;

    private FirebaseUser firebaseUser;

    public static MyTransactionsFragment getInstance(Bundle arguments) {
        return new MyTransactionsFragment(arguments);
    }

    private MyTransactionsFragment(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        context = container.getContext();
        transactionModelList = new ArrayList<>();
        adapterTransactionsHistory = new AdapterTransactionsHistory(context, transactionModelList);

        if (view == null) {

            view = inflater.inflate(R.layout.fragment_my_transactions, container, false);

            recyclerTransactionsHistory = view.findViewById(R.id.recyclerTransactionsHistory);
            recyclerTransactionsHistory.setHasFixedSize(true);
            recyclerTransactionsHistory.setLayoutManager(new GridLayoutManager(context, 1));
            recyclerTransactionsHistory.setAdapter(adapterTransactionsHistory);

            initTransactionsEventListener();

        }
        return view;
    }

    private void initTransactionsEventListener() {
        if (transactionsEventListener == null)
            transactionsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    transactionModelList.clear();

                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                        try {

                            Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                            for (DataSnapshot snapshot : snapshots) {
                                Iterable<DataSnapshot> innerSnapshots = snapshot.getChildren();
                                for (DataSnapshot innerSnapshot : innerSnapshots) {
                                    TransactionModel model = innerSnapshot.getValue(TransactionModel.class);
                                    if (model != null) {
                                        if (model.getCreditedTo().equals(firebaseUser.getUid()) || model.getDeductedFrom().equals(firebaseUser.getUid()))
                                            transactionModelList.add(model);

                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapterTransactionsHistory.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
    }

    private void loadTransactionHistory() {
        removeTransactionsEventListener();
        MyFirebaseDatabase.TRANSACTIONS_REFERENCE.addValueEventListener(transactionsEventListener);
    }

    private void removeTransactionsEventListener() {
        if (transactionsEventListener != null)
            MyFirebaseDatabase.TRANSACTIONS_REFERENCE.removeEventListener(transactionsEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactionHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeTransactionsEventListener();
    }

}
