package fyp.hkust.facet;

import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.one.EmojiOneProvider;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class MyApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

//        _instance = this;
        FirebaseApp.getApps(this);
        //enable the offline capability for firebase
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        EmojiManager.install(new EmojiOneProvider());
        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
//        built.setIndicatorsEnabled(false);
//        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

    }
}
