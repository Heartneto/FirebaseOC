package edu.demo.firebaseoc.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.OnClick;
import edu.demo.firebaseoc.R;
import edu.demo.firebaseoc.api.UserHelper;
import edu.demo.firebaseoc.base.BaseActivity;
import edu.demo.firebaseoc.models.User;

public class ProfileActivity extends BaseActivity {

    // FOR DESIGN
    @BindView(R.id.profile_activity_imageview_profile)
    ImageView imageViewProfile;
    @BindView(R.id.profile_activity_edit_text_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.profile_activity_text_view_email)
    TextView textViewEmail;
    @BindView(R.id.profile_activity_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.profile_activity_check_box_is_mentor)
    CheckBox checkBoxIsMentor;

    // FOR DATA
    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30; // (update username)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureToolbar();
        updateUIWhenCreating();
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_profile;
    }

    // ----------
    // UI
    // ----------
    private void updateUIWhenCreating(){
        if (getCurrentUser() != null){
            // Get Picture URL from Firebase
            if (getCurrentUser().getPhotoUrl() != null){
                Glide.with(this)
                        .load(getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }
            // Get email & username from firebase
            String email = TextUtils.isEmpty(getCurrentUser().getEmail()) ?
                    getString(R.string.info_no_email_found) : getCurrentUser().getEmail();
            String username = TextUtils.isEmpty(getCurrentUser().getDisplayName()) ?
                    getString(R.string.info_no_username_found) : getCurrentUser().getDisplayName();
            // Update views with data
            textInputEditTextUsername.setText(username);
            textViewEmail.setText(email);

            // Get additional data from firestore (isMentor & username)
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User currentUser = documentSnapshot.toObject(User.class);
                            String username = TextUtils.isEmpty(currentUser.getUsername()) ?
                                    getString(R.string.info_no_username_found) : getCurrentUser().getDisplayName();
                            checkBoxIsMentor.setChecked(currentUser.getIsMentor());
                            textInputEditTextUsername.setText(username);;
                        }
                    }
            );
        }
    }

    // Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterResTRequestsCompleted(final  int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                    case DELETE_USER_TASK:
                        finish();
                    // Hiding Progress bar after request completed
                    case UPDATE_USERNAME:
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    // --------------
    // ACTIONS
    // --------------
    @OnClick(R.id.profile_activity_button_update)
    public void onClickUpdateButton(){
        updateUsernameInFirebase();
    }

    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignOutButton(){ signOutUserFromFirebase(); }

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserFromFirebase();
                            }
                        })
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    @OnClick(R.id.profile_activity_check_box_is_mentor)
    public void onClickCheckBoxIsMentor(){
        updateUserIsMentor();
    }

    // -----------------
    // REST REQUESTS
    // -----------------
    // Create http request (Signout & delete)
    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, updateUIAfterResTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase(){
        if (getCurrentUser() != null){
            // We also delete user from firestore storage
            UserHelper
                    .deleteUser(getCurrentUser().getUid())
                    .addOnFailureListener(onFailureListener());
            // Delete user from firebase
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, updateUIAfterResTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    // Update User Mentor (is or not)
    private void updateUserIsMentor(){
        if (getCurrentUser() != null){
            UserHelper
                    .updateIsMentor(
                            getCurrentUser().getUid(),
                            checkBoxIsMentor.isChecked())
                    .addOnFailureListener(onFailureListener());
        }
    }

    // Update User Username
    private void updateUsernameInFirebase(){
        progressBar.setVisibility(View.VISIBLE);
        String username = textInputEditTextUsername.getText().toString();
        if (getCurrentUser() != null){
            if (!username.isEmpty() && !username.equals(getString(R.string.info_no_username_found))){
                UserHelper
                        .updateUsername(
                                username,
                                getCurrentUser().getUid())
                        .addOnFailureListener(onFailureListener())
                        .addOnSuccessListener(updateUIAfterResTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }
}
