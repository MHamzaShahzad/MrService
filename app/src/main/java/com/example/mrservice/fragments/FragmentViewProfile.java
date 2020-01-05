package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import com.example.mrservice.CommonFunctionsClass;
import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.interfaces.FragmentInteractionListener;
import com.example.mrservice.models.UserProfileModel;
import com.firebase.ui.auth.data.model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentViewProfile extends Fragment {

    private static final String TAG = FragmentViewProfile.class.getName();
    private Context context;
    private View view;

    private CircleImageView user_profile_photo;
    private RatingBar user_profile_rating;
    private TextView user_profile_name, user_rating_counts, user_email, user_mobile, user_address, user_type, user_about;

    private FragmentInteractionListener mListener;
    private Bundle arguments;

    public static FragmentViewProfile getInstance(Bundle arguments) {
        return new FragmentViewProfile(arguments);
    }

    public FragmentViewProfile(Bundle arguments) {
        // Required empty public constructor
        this.arguments = arguments;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_USER_PROFILE_VIEW);
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_view_profile, container, false);

            initLayoutWidgets();
            getProfileData();
        }
        return view;
    }

    private void initLayoutWidgets() {

        user_profile_photo = view.findViewById(R.id.user_profile_photo);
        user_profile_rating = view.findViewById(R.id.user_profile_rating);
        user_rating_counts = view.findViewById(R.id.user_rating_counts);
        user_profile_name = view.findViewById(R.id.user_profile_name);
        user_email = view.findViewById(R.id.user_email);
        user_mobile = view.findViewById(R.id.user_mobile);
        user_address = view.findViewById(R.id.user_address);
        user_type = view.findViewById(R.id.user_type);
        user_about = view.findViewById(R.id.user_about);

    }

    private void getProfileData() {

        if (arguments != null) {

            final UserProfileModel userProfileModel = (UserProfileModel) arguments.getSerializable(Constants.STRING_USER_PROFILE_OBJECT);
            if (userProfileModel != null) {

                if (userProfileModel.getUserImageUrl() != null && !userProfileModel.getUserImageUrl().equals("") && !userProfileModel.getUserImageUrl().equals("null")) {
                    Picasso.get()
                            .load(userProfileModel.getUserImageUrl())
                            .placeholder(R.drawable.image_avatar)
                            .error(R.drawable.image_avatar)
                            .centerInside().fit()
                            .into(user_profile_photo);
                    user_profile_photo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, FragmentLargePicture.getInstance(userProfileModel.getUserImageUrl())).addToBackStack(null).commit();
                        }
                    });
                }

                user_profile_name.setText(userProfileModel.getUserName());
                user_mobile.setText(userProfileModel.getUserMobileNumber());
                user_email.setText(userProfileModel.getUserEmailAddress());
                user_address.setText(userProfileModel.getUserAddress());
                user_type.setText(CommonFunctionsClass.getUserType(userProfileModel.getUserType()));
                user_about.setText(userProfileModel.getAbout());
                user_profile_rating.setRating(userProfileModel.getUserRating());
                user_rating_counts.setText("(" + userProfileModel.getRatingCounts() + ")");

            }

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement FragmentInteractionListenerInterface.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_USER_PROFILE_VIEW);
    }

}
