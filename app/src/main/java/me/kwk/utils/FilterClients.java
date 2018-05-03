package me.kwk.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.kwik.bl.KwikDevice;

/**
 * Created by Daniel on 5/3/2018.
 */

public class FilterClients {
    public static Map<String,  List<KwikDevice>> filterClients (List<KwikDevice> allDevices) {

        Map<String,  List<KwikDevice>> map =  new HashMap<String,  List<KwikDevice>>();

        for (KwikDevice tmpDevice : allDevices) {

            String tmpClient = tmpDevice.getClient();
            if (map.containsKey(tmpClient)){

                map.get(tmpClient).add(tmpDevice);

            }else{

                List<KwikDevice> newEntry = new ArrayList<KwikDevice>();
                newEntry.add(tmpDevice);
                map.put(tmpClient, newEntry);

            }

        }

        return map;
    }

}
