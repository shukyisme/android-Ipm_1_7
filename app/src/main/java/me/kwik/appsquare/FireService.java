package me.kwik.appsquare;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import me.kwik.utils.Logger;

/**
 * Created by Farid Abu Salih on 19/03/17.
 * farid@kwik.me
 */

public class FireService extends FirebaseInstanceIdService {
    private static final String TAG = "FireService";
    public static String PUSH_NOTIFICATION_KEY;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Logger.e(TAG, "Refreshed token: " + refreshedToken);

        PUSH_NOTIFICATION_KEY = refreshedToken;
        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(refreshedToken);
    }
}
