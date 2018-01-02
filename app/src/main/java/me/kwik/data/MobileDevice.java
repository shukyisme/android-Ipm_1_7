package me.kwik.data;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Farid Abu Salih on 10/03/16.
 * farid@kwik.me
 */
public class MobileDevice {

    public static final String XXXHDPI = "xxxhdpi";
    public static final String XXHDPI  = "xxhdpi";
    public static final String XHDPI   = "xhdpi";

    public static String DENSITY;

    public MobileDevice(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        DENSITY = XXXHDPI;

        if (metrics.density >= 2 && metrics.density < 2.5){
            DENSITY = XHDPI;
        }else if(metrics.density >=2.5 && metrics.density < 3.5){
            DENSITY = XXHDPI;
        }
    }
}
