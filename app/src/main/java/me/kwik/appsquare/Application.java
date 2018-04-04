package me.kwik.appsquare;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.uk.rushorm.android.AndroidInitializeConfig;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.RushSearch;
import me.kwik.bl.KwikApp;
import me.kwik.bl.KwikMe;
import me.kwik.data.IpmClient;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikLocale;
import me.kwik.data.KwikNetwork;
import me.kwik.data.KwikProject;
import me.kwik.data.KwikUser;
import me.kwik.data.MobileDevice;
import me.kwik.utils.Logger;
import me.kwk.utils.FontsOverride;
import me.kwk.utils.Utils;

/**
 * Created by Farid on 1/15/2016.
 * farid@kwik.me
 */
public final class Application extends android.app.Application {

    public static final String SUPPORT_EMAIL = "service@kwik.me";
    public static String SERVER_URL;
    public static final String DEV_SERVER_URL = "https://devinternal.kwik.me";
    public static final String INTEGRATION_SERVER_URL = "https://integration.kwik.me";
    public static final String PRODUCTION_SERVER_URL = "https://api.kwik.me";

    public static enum PrefType {
        LOCALE, FIRST_BUTTON_IN_THE_APP
    }

    ;
    private Map<String, String> countriesCodes = new HashMap<String, String>();
    private static final String TAG = "Application";

    private List<KwikProject> projects = new ArrayList<KwikProject>();
    private KwikUser user = new KwikUser();
    private List<KwikButtonDevice> buttons = new ArrayList<KwikButtonDevice>();
    private List<IpmClient> mClients = new ArrayList<IpmClient>();
    private MobileDevice mMobileDevice;
    public static KwikLocale locale;
    public static final int LAUNCH_MODE_NORMAL = 0;
    public static final int LAUNCH_MODE_EDIT = 1;
    public static int LAUNCH_MODE;
    public static final String GOOGLE_ANALYTICS_CATEGORY_POPUP_FOR_USER = "Popup for user";
    public static final String GOOGLE_ANALYTICS_CATEGORY_USER_ACTION = "User action";
    public static final String GOOGLE_ANALYTICS_CATEGORY_WIFI_TEACH = "Wifi teach";
    public static final String GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION = "Server action";
    public static final String LOCAL_HE_IL = "he_IL";
    public static final String LOCAL_EN_US = "en_US";
    public static final String LOCAL_IW_IL = "iw_IL";
    public static final String LOCAL_RU_RU = "ru_RU";
    public static final String LOCAL_FR_FR = "fr_FR";
    public static final String LOCAL_JA_JP = "ja_JP";
    public static final String LOCAL_PT_BR = "pt_BR";
    public static final String LOCAL_IW = "iw";
    public static final String LOCAL_EN = "en";
    public static final String LOCAL_RU = "ru";
    public static final String LOCAL_FR = "fr";
    public static final String LOCAL_JA = "ja";
    public static final String LOCAL_PT = "pt";
    public static final int MINIMUM_AUDIO_LEVEL = 9;


    public static final String IMAGES_FOLDER_NAME = "kwikImages";


    @Override
    public void onCreate() {
        super.onCreate();
        new KwikMe(getApplicationContext());
        FontsOverride.setDefaultFont(this, "MONOSPACE", "HelveticaNeueLTStd-Lt.otf");
        SERVER_URL = PRODUCTION_SERVER_URL;

        mMobileDevice = new MobileDevice(getApplicationContext());


        KwikApp.LifeCyclePhase = KwikMe.LifeCyclePhase.DEBUG;
        KwikMe.LOCAL = Locale.getDefault().toString();

        if (getStringPref(Application.PrefType.LOCALE).equals("0")) {
            KwikMe.LOCAL = LOCAL_EN_US;
            Utils.setLocale(getApplicationContext(), LOCAL_EN);

        } else if (getStringPref(Application.PrefType.LOCALE).equals("1")) {
            Utils.setLocale(getApplicationContext(), LOCAL_IW);
            KwikMe.LOCAL = LOCAL_HE_IL;
        } else if (getStringPref(Application.PrefType.LOCALE).equals("2")) {
            Utils.setLocale(getApplicationContext(), LOCAL_FR);
            KwikMe.LOCAL = LOCAL_FR_FR;
        } else if (getStringPref(Application.PrefType.LOCALE).equals("3")) {
            Utils.setLocale(getApplicationContext(), LOCAL_RU);
            KwikMe.LOCAL = LOCAL_RU_RU;
        } else if (getStringPref(Application.PrefType.LOCALE).equals("4")) {
            Utils.setLocale(getApplicationContext(), LOCAL_JA);
            KwikMe.LOCAL = LOCAL_JA_JP;
        }

        if (KwikMe.LOCAL.equals(LOCAL_IW_IL)) {
            KwikMe.LOCAL = LOCAL_HE_IL;
        }


        List<Class<? extends Rush>> classes = new ArrayList<>();

        classes.add(KwikUser.class);
        classes.add(KwikNetwork.class);

        AndroidInitializeConfig rushConfig = new AndroidInitializeConfig(getApplicationContext());
        rushConfig.setClasses(classes);
        RushCore.initialize(rushConfig);
        try {
            List<KwikUser> objects = new RushSearch().find(KwikUser.class);
            if (objects != null && objects.size() > 0) {
                user = objects.get(0);
            }
        } catch (NullPointerException e) {
            Logger.e(TAG, "Exception %s", e.getMessage());
        }

        //projects.add(new KwikProject(""));


    }

    public void clearApplicationData() {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        KwikMe.USER_ID = null;
        KwikMe.USER_TOKEN = null;
        KwikMe.USER_EMAIL = null;
        KwikMe.USER_PASSWROD = null;
        pref.edit().clear().commit();
    }

    public static boolean deleteFile(File file) {

        boolean deletedAll = true;

        if (file != null) {

            if (file.isDirectory()) {

                String[] children = file.list();

                for (int i = 0; i < children.length; i++) {

                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;

                }

            } else {

                deletedAll = file.delete();

            }

        }

        return deletedAll;

    }

    private void initCountriesCodes() {
        String jsonString = Utils.loadJSONFromAsset(this, "CountriesCodes");
        JSONArray arr = null;
        JSONObject jObj = null;
        try {
            arr = new JSONArray(jsonString);
            for (int i = 0; i < arr.length(); i++) {
                jObj = arr.getJSONObject(i);
                countriesCodes.put(jObj.getString("alpha2Code"), jObj.getString("callingCodes"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Logger.d(TAG, "CountriesCodes %s", jsonString);
        Utils.printMap(countriesCodes);

    }

    public List<KwikButtonDevice> getButtons() {
        return buttons;
    }

    public void setButtons(List<KwikButtonDevice> buttons) {
        this.buttons = buttons;
    }

    public void addButton(KwikButtonDevice button) {
        if (buttons != null && buttons.size() > 0) {
            for (int i = 0; i < buttons.size(); i++) {
                if (buttons.get(i).getId() != null && !buttons.get(i).getId().isEmpty() && buttons.get(i).getId().equals(button.getId())) {
                    buttons.add(i, button);
                    return;
                }
            }
        } else {

            this.buttons.add(button);
        }

    }

    public List<KwikProject> getProjects() {
        return projects;
    }

    public void addProject(KwikProject project) {
        int i = 0;
        for (KwikProject p : projects) {
            if (p.getId().equals(project.getId())) {
                projects.set(i, project);
                return;
            }
            i++;
        }
        projects.add(project);
    }

    public KwikUser getUser() {
        return user;
    }

    public void setUser(KwikUser user) {
        this.user = user;
    }



    public void setPref(PrefType type, String value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = pref.edit();
        switch (type) {
            case LOCALE:
                e.putString(getBaseContext().getString(R.string.kwik_me_locale_id_key), value);
                break;

            default:
                break;
        }
        e.commit();
    }

    public String getStringPref(PrefType type) {
        String ans = "-1";
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        switch (type) {

            case LOCALE:
                ans = pref.getString(getBaseContext().getString(R.string.kwik_me_locale_id_key), "-1");
                break;

            default:
                break;
        }

        return ans;
    }

    public boolean getBooleanPref(PrefType type) {
        boolean ans = false;
//        SharedPreferences pref = PreferenceManager
//                .getDefaultSharedPreferences(getBaseContext());
//        switch (type) {
//
//
//
//            default:
//                break;
//        }

        return ans;
    }

    @Nullable
    public KwikButtonDevice getButton(String buttonId) {
        //No need to check if getButtons return null
        if (buttonId != null && buttonId.startsWith("kwik_")) {
            buttonId = buttonId.substring(5);
        }
        if (buttons!= null && buttons.size() > 0) {
            for (KwikButtonDevice device : buttons) {
                if (device.getId().equals(buttonId)) {
                    return device;

                }
            }
        }
        return null;
    }

    public boolean updateButton(String mKwikButtonSerial, KwikButtonDevice button) {
        if (mKwikButtonSerial.startsWith("kwik_")) {
            mKwikButtonSerial = mKwikButtonSerial.substring(5);
        }
        if (buttons.size() > 0) {
            int i = 0;
            for (KwikButtonDevice device : buttons) {
                if (device.getId().equals(mKwikButtonSerial)) {
                    buttons.set(i, button);
                    return true;

                }
                i++;
            }
        }
        return false;
    }


    public KwikProject getProject(String projectId) {
        if (projects.size() > 0) {
            for (KwikProject p : projects) {
                if (p.getId().equals(projectId)) {
                    return p;
                }
            }
        }
        return null;
    }

    public KwikProject addProject(String projectId) {
        if (projects.size() > 0) {
            for (KwikProject project : projects) {
                if (project.getId().equals(projectId)) {
                    return project;
                }
            }
        }
        KwikProject p = new KwikProject("");
        p.setId(projectId);
        projects.add(p);
        return p;
    }

    public void removeButton(String buttonId) {
        if (buttonId.startsWith("kwik_")) {
            buttonId = buttonId.substring(5);
        }

        if (buttonId.startsWith("ipm_")) {
            buttonId = buttonId.substring(4);
        }
        if (buttons.size() > 0) {
            int i = 0;
            for (KwikButtonDevice device : buttons) {
                if (device.getId().equals(buttonId)) {
                    buttons.remove(i);
                    return;
                }
                i++;
            }
        }
    }

    public List<IpmClient> getmClients() {
        return mClients;
    }

    public IpmClient getClient(String id){

        for(IpmClient c:mClients){
            if(c.getId().trim().equals(id.trim())){
                return c;
            }
        }
        return null;
    }

    public void setmClients(List<IpmClient> mClients) {
        this.mClients = mClients;
    }
}
