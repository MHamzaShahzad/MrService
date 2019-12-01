package com.example.mrservice.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.interfaces.FragmentInteractionListener;



public class FragmentAboutUs extends Fragment implements View.OnClickListener {
    TextView facebook,youtube,playstore,instagram,contact,share;
    View rootView;


    private FragmentInteractionListener mListener;
    private Context context;

    public static FragmentAboutUs getInstance(){
        return new FragmentAboutUs();
    }

    private FragmentAboutUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = container.getContext();

        if (mListener != null)
            mListener.onFragmentInteractionListener(Constants.TITLE_ABOUT_US);


        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_about_us, container, false);

            youtube = rootView.findViewById(R.id.watchOnYoutube);
            youtube.setOnClickListener(this);

            contact = rootView.findViewById(R.id.contact);
            contact.setOnClickListener(this);

            playstore = rootView.findViewById(R.id.rateOnPlaystore);
            playstore.setOnClickListener(this);

            instagram = rootView.findViewById(R.id.followOnInstagram);
            instagram.setOnClickListener(this);

            facebook = rootView.findViewById(R.id.likeOnFacebook);
            facebook.setOnClickListener(this);

            share = rootView.findViewById(R.id.share);
            share.setOnClickListener(this);

        }
        return rootView;

    }



    public void addFacebook(String id) {


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        if (isPackageInstalled(getContext(), "com.facebook.katana")) {
            intent.setPackage("com.facebook.katana");
            int versionCode = 0;
            try {
                versionCode = getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (versionCode >= 3002850) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + "http://m.facebook.com/" + id);
                intent.setData(uri);
            } else {
                Uri uri = Uri.parse("fb://page/" + id);
                intent.setData(uri);
            }
        } else {
            intent.setData(Uri.parse("http://m.facebook.com/" + id));
        }
        startActivity(intent);
    }


    public boolean isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){

            case R.id.likeOnFacebook:
                Log.e("test", "onClick: ");
                //addFacebook("prize.bond.7359447");
                break;
            case R.id.watchOnYoutube:
                Log.e("test", "onClick: ");
                //addYoutube("UCGwYIdJM15kMm5p9KjyGw5Q");
                break;
            case R.id.followOnInstagram:
                Log.e("test", "onClick: ");
                //addInstagram("prizebondmonitoring");
                break;
            case R.id.contact:
                Log.e("test", "onClick: ");
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, FragmentContactUs.getInstance()).addToBackStack(null).commit();
                break;
            case R.id.share:
                /*Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Check Out PrizeBond Monitroing, I use it to store and manage my prize bond. Get it for free at https://http://prizebondmonitoring.com/";

                sharingIntent.putExtra(Intent.EXTRA_TEXT,shareBody);

                startActivity(Intent.createChooser(sharingIntent,"Invite a friend via..."));*/

                break;
            case R.id.rateOnPlaystore:
                Toast.makeText(context,"Soon you'll see our app on play store :) ",Toast.LENGTH_LONG).show();
                break;

        }
    }


    public void addYoutube(String id) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(String.format("http://youtube.com/channel/%s", id)));

        if (isPackageInstalled(getContext(), "com.google.android.youtube")) {
            intent.setPackage("com.google.android.youtube");
        }

        startActivity(intent);

    }

    public void addInstagram(String id) {


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(String.format("http://instagram.com/_u/" + id)));

        if (isPackageInstalled(getContext(),  "com.instagram.android")) {
            intent.setPackage("com.instagram.android");
        }

        startActivity(intent);

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
            mListener.onFragmentInteractionListener(Constants.TITLE_HOME);
        }
        mListener = null;
    }
}
