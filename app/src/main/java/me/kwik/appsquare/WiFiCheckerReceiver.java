package me.kwik.appsquare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import me.kwik.bl.KwikMe;
import me.kwik.utils.Logger;


/**
 * Created by Farid Abu Salih on 13/06/16.
 * farid@kwik.me
 */
public class WiFiCheckerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Logger.e("WifiReceiver" , "WifiReceiver");
        String ssid = wifiInfo.getSSID();
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Logger.e("WifiReceiver" , "intent.getAction()= %s",intent.getAction() );
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Logger.e("WifiReceiver" , "intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO) = %s",networkInfo.isConnected() );
            if(networkInfo.isConnected()) {
                if (ssid.contains("kwik_") || ssid.contains("ipm_button")) {
                    Intent i = new Intent(context,ButtonToApActivity.class);
                    i.setAction(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);

                    final Notification n  = new Notification.Builder(context)
                            .setContentTitle("IPM")
                            .setContentText(context.getString(R.string.connected_notification_message))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pIntent)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setAutoCancel(true).build();

                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notificationManager.notify(0, n);


                            MediaPlayer mediaPlayer = new MediaPlayer();



                            if(KwikMe.LOCAL.equals(Application.LOCAL_HE_IL) || KwikMe.LOCAL.equals(Application.LOCAL_IW_IL)) {
                                mediaPlayer = MediaPlayer.create(context, R.raw.click_notification_above_he);
                                mediaPlayer.start();
                            }else if (KwikMe.LOCAL.startsWith( "en" )){
                                mediaPlayer = MediaPlayer.create(context, R.raw.click_notification_above_en);
                                mediaPlayer.start();
                            }
                        }
                    }, 4000);



                    try {
                        context.unregisterReceiver(WiFiCheckerReceiver.this);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }
}
