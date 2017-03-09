package fyp.hkust.facet.notificationservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ClementNg on 8/3/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d("Service"," started");
            Intent serviceIntent = new Intent("fyp.hkust.facet.notificationservice.MyService");
            context.startService(serviceIntent);
        }
    }
}


