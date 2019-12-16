package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterChat;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.SendPushNotificationFirebase;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.ChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class FragmentChat extends Fragment {

    private static final String TAG = FragmentChat.class.getName();
    private Context context;
    private View view;

    private EditText inputTextMessage;
    private ImageButton btnSendMessage;
    private RecyclerView recyclerChat;
    private List<ChatModel> chatModelList;
    private AdapterChat adapterChat;

    private ValueEventListener chatEventListener;

    private Bundle arguments;
    private FirebaseUser firebaseUser;
    private FragmentInteractionListener mListener;


    public static FragmentChat getInstance(Bundle arguments) {
        return new FragmentChat(arguments);
    }

    private FragmentChat(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
        chatModelList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(this.getTag());
        context = container.getContext();
        adapterChat = new AdapterChat(context, chatModelList);
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_chat, container, false);

            initLayoutWidgets();
        }
        return view;
    }

    private void initLayoutWidgets() {
        recyclerChat = view.findViewById(R.id.recyclerChat);
        inputTextMessage = view.findViewById(R.id.inputTextMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        setRecyclerChat();
        setBtnSendMessage();
    }

    private void setBtnSendMessage(){
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(inputTextMessage.getText()))
                    sendMessage();
                else
                    inputTextMessage.setError("Please write some message to be send!");
            }
        });
    }

    private void sendMessage(){
        MyFirebaseDatabase.CHAT_REFERENCE.child(arguments.getString(Constants.CHAT_ID_REF)).push().setValue(buildChatInstance()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    recyclerChat.scrollToPosition(chatModelList.size() - 1);
                    if (arguments.getString(Constants.MESSAGE_RECEIVER_ID) != null && !arguments.getString(Constants.MESSAGE_RECEIVER_ID).equals(firebaseUser.getUid()))
                    SendPushNotificationFirebase.buildAndSendNotification(
                            context,
                            arguments.getString(Constants.MESSAGE_RECEIVER_ID),
                            "New Message",
                            inputTextMessage.getText().toString().trim()
                    );
                    inputTextMessage.setText("");
                }
            }
        });
    }

    private ChatModel buildChatInstance(){
        return new ChatModel(
                firebaseUser.getUid(),
                inputTextMessage.getText().toString().trim(),
                CommonFunctionsClass.getCurrentDateTime()
        );
    }

    private void setRecyclerChat() {
        recyclerChat.setHasFixedSize(true);
        recyclerChat.setLayoutManager(new GridLayoutManager(context, 1));
        recyclerChat.setAdapter(adapterChat);
    }

    private void initChatEventListener() {
        removeChatEventListener();
        chatEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: " + dataSnapshot );
                chatModelList.clear();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots)
                        try {
                            ChatModel chatModel = snapshot.getValue(ChatModel.class);
                            if (chatModel != null)
                                chatModelList.add(chatModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
                adapterChat.notifyDataSetChanged();
                recyclerChat.scrollToPosition(chatModelList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        MyFirebaseDatabase.CHAT_REFERENCE.child(arguments.getString(Constants.CHAT_ID_REF)).addValueEventListener(chatEventListener);

    }

    private void removeChatEventListener() {
        if (chatEventListener != null)
            MyFirebaseDatabase.CHAT_REFERENCE.child(arguments.getString(Constants.CHAT_ID_REF)).removeEventListener(chatEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        initChatEventListener();
        if (mListener != null)
            mListener.onFragmentInteractionListener(this.getTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeChatEventListener();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mListener != null) {
            mListener.onFragmentInteractionListener(this.getTag());
        }
        mListener = null;
    }
}
