package com.example.mrservice.adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.models.ChatModel;
import com.example.mrservice.models.UserProfileModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.Gravity.END;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Holder> {

    private static final String TAG = AdapterChat.class.getName();
    private Context context;
    private List<ChatModel> chatModelList;
    private FirebaseUser firebaseUser;

    private LinearLayout.LayoutParams params;

    public AdapterChat(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatModel chatModel = chatModelList.get(holder.getAdapterPosition());

        Log.e(TAG, "onBindViewHolder: " + chatModel.getMessage() );

        if (chatModel.getSenderId().equals(firebaseUser.getUid())) {
            holder.cardChat.setBackgroundColor(context.getResources().getColor(R.color.myMessage));
            params.gravity = END;
        } else {
            holder.cardChat.setBackgroundColor(context.getResources().getColor(R.color.otherMessage));
            params.gravity = Gravity.START;
        }

        //holder.cardChat.setLayoutParams(params);


        holder.textMessage.setText(chatModel.getMessage());
        holder.textDateTimeSent.setText(chatModel.getDateTimeSent());
        loadUserProfile(holder, chatModel.getSenderId());

    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private CardView cardChat;
        private RatingBar ratingBar;
        private TextView textMessage, textDateTimeSent, textUserName, ratingCounts;

        public Holder(@NonNull View itemView) {
            super(itemView);

            cardChat = itemView.findViewById(R.id.cardChat);
            profileImage = itemView.findViewById(R.id.profileImage);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingCounts = itemView.findViewById(R.id.ratingCounts);
            textMessage = itemView.findViewById(R.id.textMessage);
            textUserName = itemView.findViewById(R.id.textUserName);
            textDateTimeSent = itemView.findViewById(R.id.textDateTimeSent);
        }
    }

    private void loadUserProfile(final Holder holder, final String userId) {
        MyFirebaseDatabase.USERS_REFERENCE.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {

                        UserProfileModel userProfileModel = dataSnapshot.getValue(UserProfileModel.class);
                        if (userProfileModel != null) {

                            if (userProfileModel.getUserImageUrl() != null && !userProfileModel.getUserImageUrl().equals("null") && !userProfileModel.getUserImageUrl().equals(""))
                                Picasso.get()
                                        .load(userProfileModel.getUserImageUrl())
                                        .error(R.drawable.image_avatar)
                                        .placeholder(R.drawable.image_avatar)
                                        .centerInside().fit()
                                        .into(holder.profileImage);

                            holder.textUserName.setText(userProfileModel.getUserName());
                            holder.ratingBar.setRating(userProfileModel.getUserRating());
                            holder.ratingCounts.setText("(" + userProfileModel.getRatingCounts() + ")");

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
