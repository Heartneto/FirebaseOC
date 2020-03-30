package edu.demo.firebaseoc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import edu.demo.firebaseoc.auth.ProfileActivity;
import edu.demo.firebaseoc.base.BaseActivity;

public class MainActivity extends BaseActivity {

    // FOR DESIGN
    @BindView(R.id.main_activity_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.main_activity_button_login)
    Button buttonLogin;

    // FOR DATA
    // 1 - Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResponseAfterSignIn(requestCode, resultCode,data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI when activity is resuming
        updateUIWhenResuming();
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_main;
    }

    // ------------
    // NAVIGATION
    // ------------
    // Launch Sign-in Activity
    @SuppressLint("WrongConstant")
    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo_auth)
                        .build(),
                RC_SIGN_IN
        );
    }

    // Launch Profile Activity
    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    //  -------
    // UI
    // --------
    private void showSnackbar(CoordinatorLayout pCoordinatorLayout, String message){
        Snackbar.make(pCoordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    // Update UI when activity is resuming
    private void updateUIWhenResuming(){
        buttonLogin.setText(isCurrentUserLogged() ?
                getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }

    // -----------
    // UTILS
    // -----------
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackbar(coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(coordinatorLayout, getString(R.string.error_authentication_canceled));
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(coordinatorLayout, getString(R.string.error_no_internet));
                    return;
                }

                showSnackbar(coordinatorLayout,getString(R.string.error_unknown_error));
                Log.e("TAG", "Sign-in error: ", response.getError());
            }
        }
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.main_activity_button_login)
    public void onClickLoginButton() {
        // Start appropriate activity
        if (isCurrentUserLogged())
            this.startProfileActivity();
        else {
            startSignInActivity();
        }
    }
}
