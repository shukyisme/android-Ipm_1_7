package me.kwik.square;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class TrapDetailsActivity extends BaseActivity {

    @BindView(R.id.trap_details_activity_edit_ImageButton)
    ImageButton mEditImageButton;

    @BindView(R.id.trap_details_activity_name_EditText)
    EditText mNameEditText;

    @BindView(R.id.trap_details_activity_name_TextView)
    TextView mNameTextView;

    @BindView(R.id.trap_details_activity_finish_editing_TextView)
    TextView mFinishEditingTextView;

    @BindView(R.id.trap_details_activity_description_edit_ImageButton)
    ImageButton mEditDescriptionImageButton;

    @BindView(R.id.trap_details_activity_edit_description_EditText)
    EditText mDescriptionEditText;

    @BindView(R.id.trap_details_activity_edit_description_TextView)
    TextView mDescriptionTextView;

    @BindView(R.id.trap_details_activity_description_finish_editing_TextView)
    TextView mFinishEditingDescriptionTextView;


    private static final int EDITING_STATUS = 0;
    private static final int EDITED_STATUS  = 1;
    private static int NAME_STATUS = EDITED_STATUS;
    private static int DESCRIPTION_STATUS = EDITED_STATUS;

    private String mSerial;
    private KwikDevice mTrap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trap_details);
        mActionBarTitle.setText("Trap details");
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSerial = getIntent().getStringExtra("serial_number");
        //TODO: remove comment after fixing the api
//        if(mSerial == null){
//            showOneButtonErrorDialog("","UnKnown error, Please go back and try again");
//            return;
//        }
        showProgressBar();
        KwikMe.getKwikDevices(mSerial,null, null, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(List<KwikDevice> buttons) {
                hideProgressBar();
                mTrap = buttons.get(0);
                if (buttons == null || buttons.size() == 0) {
                    showOneButtonErrorDialog("","Button was not found");
                    //Utils.playAudioFile( ClientOverviewActivity.this, "add_first_button", 0, 5 );
                    return;
                }
                update();
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }

    private void update() {
        mNameEditText.setText(mTrap.getName());
        mNameTextView.setText(mTrap.getName());

//        Logger.e("EEEEEE",mTrap.getName());

    }

    public void editClicked(View view) {
        if(NAME_STATUS == EDITED_STATUS){
            NAME_STATUS = EDITING_STATUS;
            mNameTextView.setVisibility(View.INVISIBLE);
            mNameEditText.setVisibility(View.VISIBLE);
            mNameEditText.setText(mNameTextView.getText().toString());
            mFinishEditingTextView.setVisibility(View.VISIBLE);
        }
    }


    public void finishEditingClick(View view) {
        if(NAME_STATUS == EDITING_STATUS){
            NAME_STATUS = EDITED_STATUS;
            mNameTextView.setVisibility(View.VISIBLE);
            mNameEditText.setVisibility(View.INVISIBLE);
            mNameTextView.setText(mNameEditText.getText().toString());
            mFinishEditingTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void finishEditingDescriptionClick(View view) {
        if(DESCRIPTION_STATUS == EDITING_STATUS){
            DESCRIPTION_STATUS = EDITED_STATUS;
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mDescriptionEditText.setVisibility(View.INVISIBLE);
            mDescriptionTextView.setText(mDescriptionEditText.getText().toString());
            mFinishEditingDescriptionTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void editDescriptionClicked(View view) {
        if(DESCRIPTION_STATUS == EDITED_STATUS){
            DESCRIPTION_STATUS = EDITING_STATUS;
            mDescriptionTextView.setVisibility(View.INVISIBLE);
            mDescriptionEditText.setVisibility(View.VISIBLE);
            mDescriptionEditText.setText(mDescriptionTextView.getText().toString());
            mFinishEditingDescriptionTextView.setVisibility(View.VISIBLE);
        }
    }
}
