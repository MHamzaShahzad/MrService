package com.example.mrservice.admin;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.controllers.MyFirebaseDatabase;
import com.example.mrservice.controllers.MyFirebaseStorage;
import com.example.mrservice.models.TaskCat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCatCRUD extends DialogFragment implements View.OnClickListener {

    private static final String TAG = FragmentCatCRUD.class.getName();
    private Context context;

    private TaskCat oldTaskCat;

    private static ProgressDialog progressDialog;
    private static final int GALLERY_REQUEST = 1;
    private Uri filePath;

    private ImageView catImageView;
    private ImageButton btnUpdateCatImage;
    private EditText catNameInput;
    private Button btnSubmitCat, btnDelete, btnCancel;

    public static FragmentCatCRUD getInstance() {
        return new FragmentCatCRUD();
    }

    private FragmentCatCRUD() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_cat_crud, null);
        initDialogWidgets(view);
        builder.setView(view);

        initProgressDialog();
        getPreviousCat();

        return builder.create();

    }

    private void initDialogWidgets(View view) {
        catImageView = view.findViewById(R.id.catImageView);
        btnUpdateCatImage = view.findViewById(R.id.btnUpdateCatImage);
        catNameInput = view.findViewById(R.id.catNameInput);
        btnSubmitCat = view.findViewById(R.id.btnSubmitCat);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);

        initClickListeners();
    }

    private void initClickListeners(){
        btnUpdateCatImage.setOnClickListener(this);
        btnSubmitCat.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private void getPreviousCat() {

        Bundle argumentBundle = getArguments();
        if (argumentBundle != null) {

            try {
                oldTaskCat = (TaskCat) argumentBundle.getSerializable(Constants.STRING_TASKS_CAT_OBJ);
                if (oldTaskCat != null) {
                    if (oldTaskCat.getCategoryImageUrl() != null && !oldTaskCat.getCategoryImageUrl().equals("") && !oldTaskCat.getCategoryImageUrl().equals("null"))
                        Picasso.get()
                                .load(oldTaskCat.getCategoryImageUrl())
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .centerInside().fit()
                                .into(catImageView);

                    catNameInput.setText(oldTaskCat.getCategoryName());
                    catNameInput.setSelection(0, oldTaskCat.getCategoryName().length());

                    btnDelete.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void submitCat() {
        if (catNameInput.length() == 0) {
            catNameInput.setError("Field is required");
            return;
        }

        if (filePath == null && (oldTaskCat == null || oldTaskCat.getCategoryImageUrl() == null)) {
            Toast.makeText(context, "Select Image!", Toast.LENGTH_SHORT).show();
        } else {
            if (filePath != null) {
                progressDialog.show();
                uploadCatToFirebase();
            } else {
                if (oldTaskCat != null && oldTaskCat.getCategoryImageUrl() != null) {
                    progressDialog.show();
                    uploadCatToFirebase(oldTaskCat.getCategoryImageUrl());
                }
            }
        }
    }

    private void deleteCat() {
        MyFirebaseDatabase.TASKS_CAT.child(oldTaskCat.getCategoryId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    FragmentCatCRUD.this.dismiss();
                else
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private TaskCat buildCategoryInstance(String imageUrl, String id) {

        return new TaskCat(

                id,
                catNameInput.getText().toString().trim(),
                imageUrl

        );
    }

    private void loadImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
    }

    private void uploadCatToFirebase() {
        MyFirebaseStorage.TASKS_CAT_PICS.child(filePath.getLastPathSegment() + ".jpg").putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //progressDialog.dismiss();
                Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show();

                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        uploadCatToFirebase(uri.toString());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Toast.makeText(context, "Can't load image url " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dismissProgressDialog();
                Toast.makeText(context, "Image Uploading Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void uploadCatToFirebase(String imageUri) {

        progressDialog.setMessage("Uploading to database!");
        String id = (oldTaskCat != null) ? oldTaskCat.getCategoryId() : UUID.randomUUID().toString();

        MyFirebaseDatabase.TASKS_CAT.child(id).setValue(buildCategoryInstance(imageUri, id)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dismissProgressDialog();
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Category submitted successfully!", Toast.LENGTH_SHORT).show();
                    FragmentCatCRUD.this.dismiss();
                } else
                    Toast.makeText(context, (task.getException() != null) ? task.getException().getMessage() : "Something went wrong, please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            filePath = data.getData();
            if (filePath != null)
                catImageView.setImageURI(filePath);

            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);
                catImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {
            case R.id.btnCancel:
                FragmentCatCRUD.this.dismiss();
                break;
            case R.id.btnDelete:
                deleteCat();
                break;
            case R.id.btnSubmitCat:
                submitCat();
                break;
            case R.id.btnUpdateCatImage:
                loadImageFromGallery();
                break;
        }

    }

}
