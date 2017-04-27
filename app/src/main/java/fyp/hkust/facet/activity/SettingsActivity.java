package fyp.hkust.facet.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.CustomTypeFaceSpan;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.TypefaceSpan;

/**
 * Created by ClementNg on 2/4/2017.
 */


public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getDelegate().getSupportActionBar().setHomeButtonEnabled(true);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        // 所的的值将会自动保存到SharePreferences
        SpannableString s = new SpannableString(getTitle().toString());
        s.setSpan(new TypefaceSpan(SettingsActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getDelegate().getSupportActionBar().setTitle(s);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsActivity.MyPreferenceFragment()).commit();

    }

    private void convertPreferenceToUseCustomFont(Preference somePreference) {
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);

        CustomTypeFaceSpan customTypefaceSpan = new CustomTypeFaceSpan("", fontType);

        SpannableStringBuilder ss;
        if (somePreference.getTitle() != null) {
            ss = new SpannableStringBuilder(somePreference.getTitle().toString());
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, ss.length(), 0);
            ss.setSpan(customTypefaceSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            somePreference.setTitle(ss);
        }

        if (somePreference.getSummary() != null) {
            ss = new SpannableStringBuilder(somePreference.getSummary().toString());
            ss.setSpan(customTypefaceSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            somePreference.setSummary(ss);
        }
    }

    public class MyPreferenceFragment extends PreferenceFragment {

        private final static String TAG = "MyPreferenceFragment ";
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;

        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            if(isAdded()){
                getResources().getString(R.string.app_name);
            }
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out. Haven't logged in before");
                    }
                    if (firebaseAuth.getCurrentUser() != null) {
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in user_id:" + firebaseAuth.getCurrentUser().getUid());
                    }
                }
            };
            mAuth.addAuthStateListener(mAuthListener);

            PreferenceCategory category1 = (PreferenceCategory)findPreference("userControls");
            convertPreferenceToUseCustomFont(category1);
            PreferenceCategory category2 = (PreferenceCategory)findPreference("about");
            convertPreferenceToUseCustomFont(category2);

            Preference notificationReceiveButton = findPreference("notificationReceiveButton");
            convertPreferenceToUseCustomFont(notificationReceiveButton);

            Preference openSourceLicenses = findPreference("openSourceLicenses");
            convertPreferenceToUseCustomFont(openSourceLicenses);

            Preference aboutButton = findPreference("aboutUs");
            convertPreferenceToUseCustomFont(aboutButton);
            aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    startActivity(new Intent(getActivity(),AboutUsActivity.class));
                    return true;
                }
            });

            Preference ratingButton = findPreference("ratingButton");
            convertPreferenceToUseCustomFont(ratingButton);
            ratingButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    return true;
                }
            });

            Preference logoutButton = findPreference("logoutButton");
            convertPreferenceToUseCustomFont(logoutButton);
            logoutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    logout();
                    startActivity(new Intent(getActivity(), MainMenuActivity.class));
                    return true;
                }
            });
        }

        private void logout() {
            mAuth.signOut();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub

//        if(preference.getKey().equals("EditTextP")) //根據不同的key來辨別不同的Preference
//            preference.setSummary("EditTextPreference Value:" +newValue);
//        else if(preference.getKey().equals("ListP"))
//            preference.setSummary("ListPreference Value:" +((ListPreference) preference).getEntries()[Integer.parseInt(newValue.toString())]);
//            //從Entry中對應顯示使用者選擇的item value
//        else if(preference.getKey().equals("RingtoneP"))
//            preference.setSummary("RingtonePreference Value:" +newValue);
//        else if(preference.getKey().equals("SPCEditTextP"))
//            preference.setSummary("EditTextPreference Value:" +newValue);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }


    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}