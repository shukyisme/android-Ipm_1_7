package me.kwk.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import me.kwik.appsquare.BaseActivity;
import me.kwik.appsquare.R;
import me.kwik.appsquare.TrapDetailsActivity;
import me.kwik.bl.KwikDevice;


/**
 * Created by Daniel on 5/3/2018.
 */

public class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
    List<KwikDevice> devices = new ArrayList<KwikDevice>();
    Map<String, List<KwikDevice>> filtered;
    List<ListViewItemBase> allItems;

    private LayoutInflater mInflater;
    private Context mContext;
    private List<CustomArrayAdapterItem> mClients;

    public CustomAdapter(Context context, List<KwikDevice> devices, List<CustomArrayAdapterItem> clients) {
        mContext = context;
        mClients = clients;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.filtered = FilterClients.filterClients(devices);

        int startIdx = 0;
        allItems = new ArrayList<ListViewItemBase>();
        int sectionId = 0;
        for (Map.Entry<String, List<KwikDevice>> entry : filtered.entrySet()){

            ListViewItemSection section = new ListViewItemSection();
            section.setSection(true);
            section.setName(entry.getKey());
            allItems.add(section);

            for(KwikDevice device: entry.getValue()){

                ListViewItemCell cell = new ListViewItemCell();
                cell.setSection(false);
                cell.setDevice(device);
                allItems.add(cell);
            }

        }
    }

    @Override
    public int getItemViewType(int position) {

        ListViewItemBase item = allItems.get(position);
        if(item.isSection()){
            return TYPE_HEADER;
        }else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {

        return 2;
    }

    @Override
    public int getCount() {

        int cnt = allItems.size();

        return cnt;

    }

    @Override
    public ListViewItemBase getItem(int position) {

        return allItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.traps_list_item, parent, false);
                    holder.trapName = (TextView)convertView.findViewById(R.id.trap_item_name_TextView);
                    holder.siteName = (TextView)convertView.findViewById(R.id.trap_item_site_name_TextView);
                    holder.trapSerialNumber = (TextView)convertView.findViewById(R.id.trap_item_sn_TextView);
                    holder.trapImageView = (ImageView)convertView.findViewById(R.id.trap_item_image_ImageView);
                    holder.lowBatteryImageView = (ImageView)convertView.findViewById(R.id.trap_item_low_battery_ImageView);


                    break;
                case TYPE_HEADER:
                    convertView = mInflater.inflate( R.layout.traps_list_section, parent, false );
                    holder.sectionName = (TextView)convertView.findViewById(R.id.trap_section_name_TextView);
                    break;
            }
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        if(rowType == TYPE_ITEM){
            final ListViewItemCell cell = (ListViewItemCell) this.getItem(position);
            holder.trapName.setText(cell.getDevice().getName());
            holder.siteName.setText(cell.getDevice().getSiteName());

            try {
                holder.trapSerialNumber.setText(mContext.getString(R.string.SN) + cell.getDevice().getId());
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            try {
                Drawable dr = null;
                if (cell.getDevice().getStatus().equals(KwikDevice.STATUS_ALERT)) {
                    dr = ContextCompat.getDrawable(mContext, R.drawable.kwik_orange);
                }else if(cell.getDevice().getStatus().equals(KwikDevice.STATUS_READY)) {
                    dr = ContextCompat.getDrawable(mContext, R.drawable.ipm_button_ready_icon);
                }else if(cell.getDevice().getStatus().equals(KwikDevice.STATUS_AVAILABLE)) {
                    dr = ContextCompat.getDrawable(mContext, R.drawable.ipm_button_ready_icon);
                }else if(cell.getDevice().getStatus().equals(KwikDevice.STATUS_NOT_AVAILABLE)) {
                    dr = ContextCompat.getDrawable(mContext, R.drawable.grey_button);
                }else if(cell.getDevice().getStatus().equals(KwikDevice.STATUS_DISABLED)) {
                    dr = ContextCompat.getDrawable(mContext, R.drawable.grey_button);
                }

                if(dr != null){
                    holder.trapImageView.setImageDrawable(dr);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            if(cell.getDevice().getBatteryStatus() != null && cell.getDevice().getBatteryStatus().equalsIgnoreCase("Replace")){
                holder.lowBatteryImageView.setVisibility(View.VISIBLE);
            }else{
                holder.lowBatteryImageView.setVisibility(View.INVISIBLE);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext,TrapDetailsActivity.class);
                    i.putExtra("serial_number",cell.getDevice().getId());
                    i.putExtra("client",cell.getDevice().getClient());
                    mContext.startActivity(i);
                }
            });
        }else if(rowType == TYPE_HEADER){
            if(this.mClients != null) {
                ListViewItemSection section = (ListViewItemSection) this.getItem(position);
                String clientId = section.getName();
                String clientName = "NA";
                for (CustomArrayAdapterItem client : this.mClients) {
                    if (clientId.equals(client.getId())) {
                        clientName = client.getLabel();
                        break;
                    }
                }
                holder.sectionName.setText(clientName);
            }
        }


        return convertView;
    }

    public static class ViewHolder {
        public TextView sectionName;

        public TextView trapName;
        public TextView siteName;
        public TextView trapSerialNumber;
        public ImageView trapImageView;
        public ImageView lowBatteryImageView;
    }

}
