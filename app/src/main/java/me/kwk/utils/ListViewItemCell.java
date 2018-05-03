package me.kwk.utils;

import me.kwik.bl.KwikDevice;

/**
 * Created by Daniel on 5/3/2018.
 */

public class ListViewItemCell extends ListViewItemBase {
    private KwikDevice device;

    public KwikDevice getDevice() {
        return device;
    }

    public void setDevice(KwikDevice device) {
        this.device = device;
    }
}
