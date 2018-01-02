package me.kwik.data;

import co.uk.rushorm.core.RushObject;

/**
 * Created by Farid Abu Salih on 08/05/16.
 * farid@kwik.me
 */
public class KwikNetwork extends RushObject {

    private String mSsid;
    private String mPassword;


    public KwikNetwork() {
    }

    public KwikNetwork(String ssid, String password) {
        this.mSsid = ssid;
        this.mPassword = password;
    }

    public String getSsid() {
        return mSsid;
    }

    public void setSsid(String mSsid) {
        this.mSsid = mSsid;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }
}
