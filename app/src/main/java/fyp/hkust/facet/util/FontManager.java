package fyp.hkust.facet.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ClementNg on 17/1/2017.
 */

public class FontManager {

    public static final String ROOT = "fonts/",
            CUSTOM_FONT = "HiraginoSansGBW3.ttf",
            APP_FONT = ROOT + "HiraginoSansGBW3.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child,typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }


}
