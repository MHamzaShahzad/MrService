package com.example.mrservice.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.adapters.AdapterForServicesList;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.MyFirebaseStorage;
import com.example.mrservice.interfaces.OnServiceSelectedI;
import com.example.mrservice.models.TaskCat;
import com.example.mrservice.models.UserProfileModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class FragmentCreateEditProfile extends Fragment implements View.OnClickListener, OnServiceSelectedI {

    private static final String TAG = FragmentCreateEditProfile.class.getName();
    private static final int GALLERY_REQUEST_CODE = 189;
    private Context context;
    private View view;

    private CircleImageView user_profile_photo;
    private ImageButton btn_update_profile;
    private RatingBar user_profile_rating;
    private Button btn_submit_profile;
    private EditText user_profile_name, user_email, user_mobile, user_address, user_category, user_about;
    private TextView user_rating_counts;
    private CheckBox btn_buyer, btn_seller;

    private UserProfileModel userProfileModelPrevious;

    private Uri imageUri;
    private ProgressDialog progressDialog;
    private BottomSheetDialog mBottomSheetDialog;

    private BottomSheetBehavior sheetBehavior;
    private String selectedServiceId;
    private LatLng taskLocationLatLng;

    private FirebaseUser firebaseUser;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 108;

    public static FragmentCreateEditProfile getInstance() {
        return new FragmentCreateEditProfile();
    }

    private FragmentCreateEditProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_create_edit_profile, container, false);


            initLayoutWidgets();
            initProgressDialog();
            initSheetBehaviour();
            initBottomSheetDialog();
            getProfileData();
        }
        return view;
    }

    private void initLayoutWidgets() {

        user_profile_photo = view.findViewById(R.id.user_profile_photo);
        btn_update_profile = view.findViewById(R.id.btn_update_profile);

        user_profile_rating = view.findViewById(R.id.user_profile_rating);
        user_rating_counts = view.findViewById(R.id.user_rating_counts);
        user_profile_name = view.findViewById(R.id.user_profile_name);
        user_email = view.findViewById(R.id.user_email);
        user_mobile = view.findViewById(R.id.user_mobile);
        user_address = view.findViewById(R.id.user_address);
        user_category = view.findViewById(R.id.user_category);
        user_about = view.findViewById(R.id.user_about);
        btn_submit_profile = view.findViewById(R.id.btn_submit_profile);

        btn_buyer = view.findViewById(R.id.btn_buyer);
        btn_seller = view.findViewById(R.id.btn_seller);

        initOnClickListener();

    }

    private void initOnClickListener() {
        user_category.setOnClickListener(this);
        user_address.setOnClickListener(this);
        btn_update_profile.setOnClickListener(this);
        btn_submit_profile.setOnClickListener(this);
    }

    private void getProfileData() {

        MyFirebaseDatabase.USERS_REFERENCE.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    try {

                        userProfileModelPrevious = dataSnapshot.getValue(UserProfileModel.class);

                        if (userProfileModelPrevious != null) {

                            if (userProfileModelPrevious.getUserImageUrl() != null && !userProfileModelPrevious.getUserImageUrl().equals("") && !userProfileModelPrevious.getUserImageUrl().equals("null")) {
                                Picasso.get()
                                        .load(userProfileModelPrevious.getUserImageUrl())
                                        .placeholder(R.drawable.image_avatar)
                                        .error(R.drawable.image_avatar)
                                        .centerInside().fit()
                                        .into(user_profile_photo);
                                user_profile_photo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, FragmentLargePicture.getInstance(userProfileModelPrevious.getUserImageUrl())).addToBackStack(null).commit();
                                    }
                                });
                            }

                            user_profile_name.setText(userProfileModelPrevious.getUserName());

                            if (firebaseUser.getPhoneNumber() == null && userProfileModelPrevious.getUserMobileNumber() == null) {
                                user_mobile.setFocusable(true);
                                user_mobile.setFocusableInTouchMode(true);
                                user_mobile.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.ic_edit_black_24dp), null);
                            } else {
                                if (userProfileModelPrevious.getUserMobileNumber() != null)
                                    user_mobile.setText(userProfileModelPrevious.getUserMobileNumber());
                                if (firebaseUser.getPhoneNumber() != null)
                                    user_mobile.setText((firebaseUser.getPhoneNumber() != null) ? firebaseUser.getPhoneNumber().replace("+92", "") : "");
                            }

                            user_email.setText(userProfileModelPrevious.getUserEmailAddress());
                            user_address.setText(userProfileModelPrevious.getUserAddress());
                            user_about.setText(userProfileModelPrevious.getAbout());
                            user_profile_rating.setRating(userProfileModelPrevious.getUserRating());
                            user_rating_counts.setText("(" + userProfileModelPrevious.getRatingCounts() + ")");
                            setUserType(userProfileModelPrevious.getUserType());

                            MyFirebaseDatabase.TASKS_CAT.child(userProfileModelPrevious.getUserServiceCatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                        try {

                                            TaskCat cat = dataSnapshot.getValue(TaskCat.class);
                                            if (cat != null) {
                                                FragmentCreateEditProfile.this.onServiceSelected(cat.getCategoryId(), cat.getCategoryName());
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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (firebaseUser.getPhoneNumber() == null || (firebaseUser.getPhoneNumber() != null && firebaseUser.getPhoneNumber().length() == 0)) {
                        Log.e(TAG, "onDataChange: NO_PHONE ");
                        user_mobile.setFocusable(true);
                        user_mobile.setFocusableInTouchMode(true);
                        user_mobile.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_edit_black_24dp), null);
                    } else {
                        Log.e(TAG, "onDataChange: _PHONE " + firebaseUser.getPhoneNumber());
                        user_mobile.setText((firebaseUser.getPhoneNumber() != null) ? firebaseUser.getPhoneNumber().replace("+92", "") : "");
                    }

                    if (firebaseUser.getEmail() == null || (firebaseUser.getEmail() != null && firebaseUser.getEmail().length() == 0)) {
                        user_email.setFocusable(true);
                        user_email.setFocusableInTouchMode(true);
                        user_email.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_edit_black_24dp), null);

                    } else {
                        Log.e(TAG, "onDataChange: _EMAIL " + firebaseUser.getEmail());
                        user_email.setText(firebaseUser.getEmail());
                    }
                    if (firebaseUser.getDisplayName() != null)
                        user_profile_name.setText(firebaseUser.getDisplayName());

                    if (firebaseUser.getPhotoUrl() != null) {
                        Picasso.get()
                                .load(firebaseUser.getPhotoUrl())
                                .error(R.drawable.image_avatar)
                                .placeholder(R.drawable.image_avatar)
                                .centerInside().fit()
                                .into(user_profile_photo);
                        user_profile_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(android.R.id.content, FragmentLargePicture.getInstance(firebaseUser.getPhotoUrl().toString())).addToBackStack(null).commit();
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        user_mobile.setText((firebaseUser.getPhoneNumber() != null) ? firebaseUser.getPhoneNumber().replace("+92", "") : "");
    }

    private void initSheetBehaviour() {
        sheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet));
        // callback for do something
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    public void initBottomSheetDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.services_bottom_up_sheet, null);

        mBottomSheetDialog = new BottomSheetDialog(context,
                R.style.MaterialDialogSheet);

        RecyclerView recyclerView = dialogView.findViewById(R.id.listServices);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        recyclerView.setHasFixedSize(true);

        loadServicesList(recyclerView);

        mBottomSheetDialog.setContentView(dialogView);
        mBottomSheetDialog.setCancelable(true);
        if (mBottomSheetDialog.getWindow() != null) {
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    private void loadServicesList(final RecyclerView recyclerView) {
        MyFirebaseDatabase.TASKS_CAT.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<TaskCat> list = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots) {
                        try {
                            TaskCat model = snapshot.getValue(TaskCat.class);
                            if (model != null)
                                list.add(model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                recyclerView.setAdapter(new AdapterForServicesList(FragmentCreateEditProfile.this, context, list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isFormValid() {
        return true;
    }

    private void pickImageForCropping() {
        //CropImage.startPickImageActivity(getActivity());
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(getActivity());
    }

    private void loadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void selectLocationDialog() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build((Activity) context), PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void setUpdateProfileData() {
        showProgressDialog();
        if (imageUri != null)
            uploadImageAndData();
        else
            uploadData(null);
    }

    private void uploadImageAndData() {
        MyFirebaseStorage.USER_PROFILE_PICS.child(firebaseUser.getUid() + ".jpg").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        uploadData(uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                e.printStackTrace();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });
    }

    private void uploadData(String imageUrl) {
        MyFirebaseDatabase.USERS_REFERENCE.child(firebaseUser.getUid()).setValue(getUserProfileInstance(imageUrl)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressDialog();
                if (task.isSuccessful())
                    ((FragmentActivity) context).getSupportFragmentManager().popBackStack();
                else
                    Toast.makeText(context, "Uploading Failed : " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getUserType() {
        if (btn_buyer.isChecked() && btn_seller.isChecked())
            return Constants.USER_PROFILE_BUYER_SELLER;
        else if (btn_buyer.isChecked())
            return Constants.USER_PROFILE_BUYER;
        else if (btn_seller.isChecked())
            return Constants.USER_PROFILE_SELLER;
        else return null;
    }

    private void setUserType(String userType) {
        switch (userType) {
            case Constants.USER_PROFILE_BUYER:
                btn_buyer.setChecked(true);
                break;
            case Constants.USER_PROFILE_SELLER:
                btn_seller.setChecked(true);
                break;
            case Constants.USER_PROFILE_BUYER_SELLER:
                btn_buyer.setChecked(true);
                btn_seller.setChecked(true);
                break;
        }
    }

    private UserProfileModel getUserProfileInstance(String imageUrl) {
        return new UserProfileModel(
                user_profile_name.getText().toString(),
                (imageUrl == null) ? ((userProfileModelPrevious == null) ? null : userProfileModelPrevious.getUserImageUrl()) : imageUrl,
                user_email.getText().toString().trim(),
                user_mobile.getText().toString(),
                user_address.getText().toString(),
                (taskLocationLatLng == null) ? (userProfileModelPrevious == null) ? null : userProfileModelPrevious.getUserAddressLatLng() : taskLocationLatLng.latitude + "-" + taskLocationLatLng.longitude,
                getUserType(),
                (selectedServiceId == null) ? (userProfileModelPrevious == null) ? null : userProfileModelPrevious.getUserServiceCatId() : selectedServiceId,
                user_about.getText().toString(),
                (userProfileModelPrevious == null) ? 0 : userProfileModelPrevious.getUserRating(),
                (userProfileModelPrevious == null) ? 0 : userProfileModelPrevious.getRatingCounts()

        );
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    // Override Functions...
    @Override
    public void onServiceSelected(String serviceId, String serviceName) {
        user_category.setText(serviceName);
        selectedServiceId = serviceId;
        if (mBottomSheetDialog != null)
            mBottomSheetDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.user_category:
                if (mBottomSheetDialog != null)
                    mBottomSheetDialog.show();
                break;
            case R.id.user_address:
                selectLocationDialog();
                break;
            case R.id.btn_update_profile:
                pickImageForCropping();
                //loadImageFromGallery();
                break;
            case R.id.btn_submit_profile:
                if (isFormValid())
                    setUpdateProfileData();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e(TAG, "onActivityResult: " + requestCode);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = PlacePicker.getPlace(data, context);
                if (place != null) {

                    if (place.getLatLng() != null) {

                        Log.e(TAG, "onActivityResult: " + place.getLatLng());
                        taskLocationLatLng = place.getLatLng();
                    }
                    user_address.setText(place.getName() + "-" + place.getAddress());
                }
            } else {
                Toast.makeText(context, "You haven't picked an address!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                imageUri = data.getData();
                user_profile_photo.setImageURI(imageUri);
            } else {
                Toast.makeText(context, "You haven't picked Image!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                user_profile_photo.setImageURI(imageUri);
            } else {
                Toast.makeText(context, "You haven't picked Image!", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "onActivityResult: UNKNOWN_ON_ACTIVITY_RESULT");
        }
        super.onActivityResult(requestCode, resultCode, data);

    } // onActivityResult Ended...

}
