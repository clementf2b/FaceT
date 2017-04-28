package fyp.hkust.facet.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.EasySplashScreen;

/**
 * Created by bentley on 28/4/2017.
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainMenuActivity.class)
                .withSplashTimeOut(4500)
                .withBackgroundResource(R.color.myLightBlue)
                .withHeaderText("")
                .withFooterText("")
                .withBeforeLogoText("")
                .withLogo(R.drawable.splash04)
                .withAfterLogoText("FaceT");


        //set your own animations
        TextViewAnimation(config.getAfterLogoTextView());

        //customize all TextViews
        Typeface lobsterFont = Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf");
        config.getAfterLogoTextView().setTypeface(lobsterFont);
        config.getAfterLogoTextView().setTextColor(getResources().getColor(R.color.myGreen));

//
//        config.getHeaderTextView().setTextColor(Color.WHITE);
//        config.getFooterTextView().setTextColor(Color.WHITE);

        //create the view
        View easySplashScreenView = config.create();

        setContentView(easySplashScreenView);
    }

    private void myCustomTextViewAnimation(TextView tv) {
        Animation animation = new TranslateAnimation(0, 0, 480, 0);
        animation.setDuration(3000);
        tv.startAnimation(animation);
    }

    private void TextViewAnimation(TextView tv) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setStartOffset(1000);
        animation.setDuration(4500);
        tv.startAnimation(animation);
    }
}