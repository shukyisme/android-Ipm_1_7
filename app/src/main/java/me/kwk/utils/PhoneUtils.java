package me.kwk.utils;

import java.util.Locale;

/**
 * @author Farid Abu Salih (Farid@kwik.me)
 *
 * Jan 7, 2016
 * PhoneUtils.java
 */
public abstract class PhoneUtils {
	
	public static boolean  isAsusDevice(){
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer.toLowerCase(Locale.ENGLISH).contains("asus");
    }
}
