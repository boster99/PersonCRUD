package com.ctoddcook.android.personcrud;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A login screen that offers login via email/password.
 * <p>
 * Created by C. Todd Cook on 9/15/2016.<br>
 * ctodd@ctoddcook.com
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private String mEmail;
    private String mPassword;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    /**
     * Basic setup of the screen when the activity is created.
     *
     * @param savedInstanceState  If the activity is being re-initialized after previously being
     *                            shut down then this Bundle contains the data it most recently
     *                            supplied in onSaveInstanceState(Bundle). Otherwise it is
     *                            null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get references to on-screen fields
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Setup listeners for Log In and Register buttons
        Button mLogInButton = (Button) findViewById(R.id.log_in_button);
        mLogInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        // Firebase authorization
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // user is now logged in
                    Log.d(TAG, "onAuthStateChanged:logged in:" + user.getUid());
                    launchMain();
                } else {  // user is now logged out
                    Log.d(TAG, "onAuthStateChanged:logged out");
                }
            }
        };
    }

    /**
     * Attempts to sign in the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin() {
        getEmailAndPassword();

        // When logging in, check for the existence of email and password entries. We don't worry
        // here about whether they're valid entries, as the registration process takes care of
        // that validation. (And if the entries when logging in don't match the registration
        // values, then the user can't log in.)
        if (mEmail.isEmpty() || mPassword.isEmpty()) {
            Snackbar.make(findViewById(R.id.email_login_form), "Gee, we really need an email " +
                "and password before we can try to log you in", Snackbar.LENGTH_LONG).show();
            return;
        }

        showProgress(getString(R.string.progress_login));

        mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signInWithEmailAndPassword:onCompeteListener:" + task
                        .isSuccessful());

                    // If login fails, display a message.
                    if (!task.isSuccessful()) {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        Log.w(TAG, "signInWithEmailAndPassword:failed" + task.getException());
                        //noinspection ThrowableResultOfMethodCallIgnored
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed) +
                            ": " + task.getException(), Toast.LENGTH_LONG).show();
                    }

                    hideProgress();
                }
            });
    }

    /**
     * Attempts to register a new account with the provided email and password.
     * If there are missing or invalid items, the errors are communicated
     * and no attempt is made.
     */
    private void attemptRegistration() {
        getEmailAndPassword();

        // When registering, check for valid email and password entries
        if (!isEmailValid() || !doesPasswordMeetRequirements()) {
            return;
        }

        showProgress(getString(R.string.progress_registration));

        mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPassword)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "createUserWithEmailAndPassword:OnCompleteListener:" + task
                        .isSuccessful());

                    // If registration fails, display a message to the user. If it succeeds,
                    // the auth state listener will be notified.
                    if (!task.isSuccessful()) {
                        //noinspection ThrowableResultOfMethodCallIgnored,ConstantConditions
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                    }

                    hideProgress();
                }
            });
    }

    private void getEmailAndPassword() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the registration attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
    }

    /**
     * Check for a valid email address
     *
     * @return true if the email looks valid
     */
    private boolean isEmailValid() {
        // Make sure the user entered at least SOMETHING, before going on to a more pertinent test
        if (mEmail.isEmpty()) {
            mEmailView.setError(getString(R.string.error_email_required));
            mEmailView.requestFocus();
            return false;
        }

        // Check for the basic elements of an email address. We also look for the email to be at
        // least 7 characters long. a@b.com would pass these requirements.
        if (!(mEmail.contains("@") && mEmail.contains(".") && (mEmail.length() >= 7))) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Tests the password for required minimum length, and also to see whether it contains all of
     * the following:
     * <ul>
     * <li>At least one lower-case alpha character</li>
     * <li>At least one upper-case alpha character</li>
     * <li>At least one numeric digit</li>
     * <li>At least one symbol</li>
     * </ul>
     *
     * @return true if the password passes all tests
     */
    private boolean doesPasswordMeetRequirements() {
        int requiredLength = 8;

        // Check for length. If it's not long enough, show the user a message about the length.
        if (mPassword.isEmpty() || mPassword.length() < requiredLength) {
            mPasswordView.setError(getString(R.string.error_short_password));
            mPasswordView.requestFocus();
            return false;
        }

        // Check for required elements. If the password doesn't contain all of the required
        // elements, show the user a message explaining those elements.
        String patternUpper = ".*[A-Z]+.*",
            patternLower = ".*[a-z]+.*",
            patternDigit = ".*\\d+.*",
            patternSymbol = ".*[~`!@#$%^&\\*()\"':;_\\-+={}\\\\|\\[\\],<.>/\\?]+.*";

        boolean matchesPatterns = mPassword.matches(patternUpper)
            && mPassword.matches(patternLower)
            && mPassword.matches(patternDigit)
            && mPassword.matches(patternSymbol);

        if (!matchesPatterns) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
            return false;
        }

        // If we made it this far, the password looks good.
        return true;
    }

    private void launchMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Shows the progress indicator.
     */
    private void showProgress(@Nullable String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    /**
     * Hides the progress indicator.
     */
    private void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        hideProgress();
    }
}

