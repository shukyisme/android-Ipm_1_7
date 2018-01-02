package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import me.kwik.data.KwikAddress;

/**
 * Created by Farid Abu Salih on 23/04/17.
 * farid@kwik.me
 */

public class AddressAdapter extends ArrayAdapter<KwikAddress> {
    private Context context;
    private List<KwikAddress> addresses;

    public boolean isShowEditIcon() {
        return showEditIcon;
    }

    public void setShowEditIcon(boolean showEditIcon) {
        this.showEditIcon = showEditIcon;
    }

    private boolean showEditIcon = false;
    public AddressAdapter(Context context, List<KwikAddress> addresses, boolean showEditIcon) {
        super( context, -1,addresses );
        this.context = context;
        this.addresses = addresses;
        this.showEditIcon = showEditIcon;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.address_item, parent, false);
        TextView addressText = (TextView) rowView.findViewById( R.id.address_item_textView );
        addressText.setText(this.addresses.get( position ).getDisplayText());
        final String addressId = this.addresses.get( position ).getId();

        ImageButton editIcon = (ImageButton)rowView.findViewById( R.id.address_item_imageButton );

        if(this.showEditIcon){
            editIcon.setVisibility( View.VISIBLE );
            editIcon.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent( context,AddressActivity.class );
                    i.putExtra( "mode",AddressActivity.EDIT_MODE );
                    i.putExtra( "addressId", addressId );
                    context.startActivity( i );
                }
            } );
            rowView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent( context,AddressActivity.class );
                    i.putExtra( "mode",AddressActivity.EDIT_MODE );
                    i.putExtra( "addressId", addressId );
                    context.startActivity( i );
                }
            } );
        }else{
            editIcon.setVisibility( View.INVISIBLE );
        }

        return rowView;
    }
}
