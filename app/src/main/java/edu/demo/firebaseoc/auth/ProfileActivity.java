package edu.demo.firebaseoc.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.OnClick;
import edu.demo.firebaseoc.R;
import edu.demo.firebaseoc.base.BaseActivity;

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
        }
    }

    // --------------
    // ACTIONS
    // --------------
    @OnClick(R.id.profile_activity_button_update)
    public void onClickUpdateButton(){}

    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignOutButton(){}

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton(){}
}
