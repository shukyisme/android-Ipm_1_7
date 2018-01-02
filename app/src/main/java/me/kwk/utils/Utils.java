package me.kwk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.kwik.bl.KwikMe;
import me.kwik.square.Application;
import me.kwik.square.SetupActivity;
import me.kwik.utils.Logger;

/**
 * Created by Farid Abu Salih on 25/01/16.
 * farid@kwik.me
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    public static MediaPlayer mAudioInstructionPlayer ;
    static int count = 0;
    public static int mVolumeLevel;
    public static AudioManager audioManager;
    static final String AUDIO_PATH =
            "http://yourHost/play.mp3";
    private static MediaPlayer mediaPlayer;
    private int playbackPosition=0;

    public static AutoCompleteTextView setAutoCompleteTextViewAdapter(AutoCompleteTextView phoneNumberPrefix, final Context context){
        final AutoCompleteTextView t = phoneNumberPrefix;
        ArrayList<String> sortList = new ArrayList<String>();

        Set<String> countriesCodeSet = PhoneNumberUtil.getInstance().getSupportedRegions();
        Set<String> countryPhoneCodesSet = new HashSet<>();
        Iterator it = countriesCodeSet.iterator();
        String defaultCode = null;

        String[] arr = countriesCodeSet.toArray(new String[countriesCodeSet.size()]);

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();

        for (int i = 0; i < countriesCodeSet.size(); i++) {
            if(countryCode != null) {
                if (arr[i].equals(countryCode.toUpperCase())) {
                    defaultCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i]);
                }
            }else{
                if (Locale.getDefault().getCountry().equals(arr[i])) {
                    defaultCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i]);
                }
            }

        }

        while(it.hasNext()) {
            countryPhoneCodesSet.add("+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion((String)it.next()));
        }

        it = countryPhoneCodesSet.iterator();

        while(it.hasNext()){
            sortList.add((String)it.next());
        }



        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,sortList);
        t.setAdapter(adapter);
        t.setText(defaultCode);
        t.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
            if(!b) {
            // on focus off
            String str = t.getText().toString();

            ListAdapter listAdapter = t.getAdapter();
            for(int i = 0; i < listAdapter.getCount(); i++) {
                String temp = listAdapter.getItem(i).toString();
                if(str.compareTo(temp) == 0) {
                    return;
                }
            }

                try {
                    if (!t.getText().toString().startsWith( "+" )) {
                        t.setText( "+" + t.getText().toString() );
                    }
                }catch (NullPointerException e){
                    //Do nothing
                }


        }
    }
});
        return t;
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


    public static String loadJSONFromAsset(Context context,String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Logger.i(mp.getClass().getSimpleName() ,"%s = %s ",pair.getKey() , pair.getValue());
            it.remove();
        }
    }


    public static void showHelp(Context context){
        String url = Application.SERVER_URL + "/static/kwikmeHelp_en.html";
        if(KwikMe.LOCAL == Application.LOCAL_HE_IL){
            url = Application.SERVER_URL + "/static/kwikmeHelp_he.html";
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void setLocale(Context context, String newLocale ){
        Locale locale = new Locale( newLocale );
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static Map<String, List<String>> splitQuery(URL url)
            throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(
                    pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder
                    .decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public static Object checkVolumeLevel(final Activity activity) {
        if(!(KwikMe.LOCAL.startsWith("en") || KwikMe.LOCAL.startsWith("he"))){
            return null;
        }
        audioManager = (AudioManager) activity.getSystemService(activity.AUDIO_SERVICE);
        mVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Intent i = null;
        if (mVolumeLevel <= Application.MINIMUM_AUDIO_LEVEL) {
            i = new Intent(activity, SetupActivity.class);
            i.putExtra("sender",activity.getClass().getSimpleName());
            activity.startActivity(i);
        }
        return i;
    }

    public static void  playAudioFile(final Context context,String fileName,final int numberOfRepeats, int interval) {
        count = 0;
        AssetFileDescriptor afd = null;

        if(KwikMe.LOCAL.equals(Application.LOCAL_HE_IL) || KwikMe.LOCAL.equals(Application.LOCAL_IW_IL)) {
            fileName = fileName + "_he.aac";
        }else if (KwikMe.LOCAL.startsWith( "en" )){
            fileName = fileName + "_en.aac";
        }else{
            return;
        }

        try {
            if(mAudioInstructionPlayer != null){
                mAudioInstructionPlayer.stop();
                mAudioInstructionPlayer.release();
            }
            afd = context.getAssets().openFd(fileName);

            mAudioInstructionPlayer = new MediaPlayer();
            mAudioInstructionPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mAudioInstructionPlayer.prepare();
            mAudioInstructionPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public static void stopPlaying() {
        if (mAudioInstructionPlayer != null) {
            try {
                mAudioInstructionPlayer.stop();
                mAudioInstructionPlayer.release();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
            mAudioInstructionPlayer = null;
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static void playAudio(String url) throws Exception {
        killMediaPlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private static void killMediaPlayer() {
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
