package com.example.mrservice.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrservice.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LoginActivity.class.getName();
    private Context context;

    private EditText inputPhoneNumber, input_otp;
    private ProgressBar progressBar;
    private Button btnLogin, btn_verify, login_button_fb, login_button_google;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String code;

    private FirebaseAuth mAuth;
    private CountryCodePicker countryCodePicker;

    private CallbackManager mCallbackManager;

    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)
            moveToHome();
        setContentView(R.layout.activity_login);


        findViewsById();
        initFacebookLogin();
        initGoogleSignIn();
    }

    private void findViewsById() {
        inputPhoneNumber = (EditText) findViewById(R.id.input_phone_number);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        countryCodePicker = (CountryCodePicker) findViewById(R.id.ccp);
        btnLogin = findViewById(R.id.btn_login);
        login_button_fb = findViewById(R.id.login_button_fb);
        login_button_google = findViewById(R.id.login_button_google);
        setClickListeners();
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(this);
        login_button_fb.setOnClickListener(this);
        login_button_google.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view == btnLogin) {

            if (inputPhoneNumber.length() == 10) {
                progressBar.setVisibility(View.VISIBLE);
                requestVerificationCode(countryCodePicker.getSelectedCountryCodeWithPlus() + inputPhoneNumber.getText().toString().trim());
            } else
                inputPhoneNumber.setError("Please Enter valid phone number!");

        }
        if (view == login_button_fb){
            loginWithFacebook();
        }

        if (view == login_button_google){
            signInWithGoogle();
        }

    }

    private void getAdminLoginDialog() {

        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.layout_otp_dialog, null);
        input_otp = (EditText) view.findViewById(R.id.input_otp);
        btn_verify = (Button) view.findViewById(R.id.btn_verify);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input_otp.length() != 6) {
                    input_otp.setError("Enter Valid Code");
                    input_otp.setFocusable(true);
                } else {
                    verifyVerificationCode(input_otp.getText().toString().trim());
                }
            }
        });
    }


    // phone number authentication
    private void requestVerificationCode(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                            // Phone number to verify
                60,                                   // Timeout duration
                TimeUnit.SECONDS,                       // Unit of timeout
                this,                            // Activity (for callback binding)
                mCallbacks);                            // OnVerificationStateChangedCallbacks
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeAutoRetrievalTimeOut(String verificationId) {
            super.onCodeAutoRetrievalTimeOut(verificationId);

            progressBar.setVisibility(View.GONE);
            notifyUserAndRetry("Phone Number Verification failed, Please try again!");

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            String code = credential.getSmsCode();
            Log.d(TAG, "onVerificationCompleted:" + credential);
            Log.d(TAG, "onVerificationCompleted: CODE === " + code);
            progressBar.setVisibility(View.INVISIBLE);
            if (code != null) {
                if (input_otp != null)
                    input_otp.setText(code);
                verifyVerificationCode(code);
            } else
                signInWithCredentials(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "onVerificationFailed", e);

            progressBar.setVisibility(View.GONE);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "FirebaseAuthInvalidCredentialsException" + e);
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Log.e(TAG, "FirebaseTooManyRequestsException" + e);
            }

            notifyUserAndRetry("Your Phone Number Verification failed, please try again!");
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent: Verification code == " + verificationId);
            mVerificationId = verificationId;
            mResendToken = token;

            progressBar.setVisibility(View.INVISIBLE);
            getAdminLoginDialog();
        }

    };
    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithCredentials(credential);
    }

    // facebook authentication
    private void initFacebookLogin() {
        // Initialize Facebook Login button

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                signInWithCredentials(credential);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }
    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_photos", "email", "public_profile")
        );
    }

    // google login
    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))//you can also use R.string.default_web_client_id
                .requestEmail()
                .build();


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }
    private void signInWithGoogle() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }
    private void handleGoogleSignInResult(GoogleSignInResult result) {
        progressBar.setVisibility(View.VISIBLE);

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                signInWithCredentials(credential);
            }
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. " + result);
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
        }

    }

    private void signInWithCredentials(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null)
                                moveToHome();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }


    private void notifyUserAndRetry(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = null;
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();

    }

    private void moveToHome() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
            googleApiClient.disconnect();
        } else // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void getHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
