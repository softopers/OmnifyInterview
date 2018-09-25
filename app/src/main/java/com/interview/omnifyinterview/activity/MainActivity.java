package com.interview.omnifyinterview.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.util.SimpleTextWatcher;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.buttonSendOTP)
    AppCompatButton buttonSendOTP;
    @BindView(R.id.buttonLoginWithGoogle)
    AppCompatButton buttonLoginWithGoogle;
    @BindView(R.id.editTextMobileNumber)
    TextInputEditText editTextMobileNumber;
    @BindView(R.id.editTextOTP)
    TextInputEditText editTextOTP;
    @BindView(R.id.textInputLayoutMobileNumber)
    TextInputLayout textInputLayoutMobileNumber;
    @BindView(R.id.textInputLayoutOtp)
    TextInputLayout textInputLayoutOtp;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private GoogleSignInClient googleSignInClient;
    private ProgressDialog mProgressDialog;

    @Nullable
    public static PackageInfo getPackageInfo(@NonNull Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
            // Version info not included if exception thrown
        }
        return info;
    }

    @Nullable
    public static String getAppVersionName(@NonNull Context context) {
        PackageInfo info = getPackageInfo(context);
        if (info != null) {
            return info.versionName;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreate: merge");
        Log.d(TAG, "onCreate: merge tag");
        Log.d(TAG, "onCreate: " + getAppVersionName(getApplicationContext()));
        Log.d(TAG, "onCreate: new tag");

        buttonSendOTP.setOnClickListener(this);
        buttonLoginWithGoogle.setOnClickListener(this);

        editTextMobileNumber.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textInputLayoutMobileNumber.isErrorEnabled()) {
                    textInputLayoutMobileNumber.setError(null);
                    textInputLayoutMobileNumber.setErrorEnabled(false);
                }

                if (textInputLayoutOtp.getVisibility() == View.VISIBLE) {
                    buttonSendOTP.setText("Send OTP");
                    editTextOTP.setText("");
                    textInputLayoutOtp.setVisibility(View.GONE);
                    if (textInputLayoutOtp.isErrorEnabled()) {
                        textInputLayoutOtp.setError(null);
                        textInputLayoutOtp.setErrorEnabled(false);
                    }
                }
            }
        });

        editTextOTP.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textInputLayoutOtp.isErrorEnabled()) {
                    textInputLayoutOtp.setError(null);
                    textInputLayoutOtp.setErrorEnabled(false);
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
//                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    textInputLayoutMobileNumber.setError("Enter a valid mobile number with [+][country code]");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                MainActivity.this.verificationId = verificationId;
                textInputLayoutOtp.requestFocus();
                textInputLayoutOtp.setVisibility(View.VISIBLE);
                buttonSendOTP.setText("Verify OTP");
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSendOTP:
                if (buttonSendOTP.getText().toString().equalsIgnoreCase("Verify OTP")) {
                    if (!TextUtils.isEmpty(editTextOTP.getText().toString().trim())) {
                        verifyPhoneNumberWithCode(verificationId, editTextOTP.getText().toString().trim());
                    } else {
                        requestFocus(textInputLayoutOtp, "Enter OTP");
                    }
                } else {
                    if (isValidMobile(editTextMobileNumber.getText().toString().trim())) {
                        startPhoneNumberVerification(editTextMobileNumber.getText().toString());
                    } else {
                        requestFocus(textInputLayoutMobileNumber, "Enter a valid mobile number with [+][country code]");
                    }
                }
                break;
            case R.id.buttonLoginWithGoogle:
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 9001);
                break;
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    /* send message to a number */
    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    /* basic phone number validation */
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    /* login with google credentials */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                startActivity(new Intent(getApplicationContext(), StoryActivity.class));
                                finish();
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed", Snackbar.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    /* login with phone credentials */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showProgressDialog();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                startActivity(new Intent(getApplicationContext(), StoryActivity.class));
                                finish();
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                textInputLayoutOtp.setError("Invalid OTP");
                            }
                        }

                        hideProgressDialog();
                    }
                });
    }

    /* check if current user is logged in or not */

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(getApplicationContext(), StoryActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    private void requestFocus(TextInputLayout view, String appErrorMessage) {
        if (view.requestFocus()) {
            view.requestFocus();
            view.setError(appErrorMessage);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
