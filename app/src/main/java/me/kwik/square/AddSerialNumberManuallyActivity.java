package me.kwik.square;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.listeners.AttachUserListener;
import me.kwik.listeners.GetProjectListener;
import me.kwk.utils.Utils;

public class AddSerialNumberManuallyActivity extends BaseActivity {

    private Application mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_serial_number_manualy);

        mActionBarTitle.setText(R.string.add_serial_manually_activity_title);
        mApp = (Application)getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.playAudioFile(AddSerialNumberManuallyActivity.this, "enter_the_serial_number", 0, 5);
    }

    public void enterSerialNumberClicked(View enterSerialNumber){
        AlertDialog.Builder alert = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        final EditText edittext = new EditText(this);
        edittext.setHint(R.string.enter_serial_number_dialog_edit_text_hint);

        alert.setTitle(R.string.enter_serial_number_dialog_title);

        alert.setView(edittext);

        alert.setPositiveButton(R.string.enter_serial_number_dialog_submit_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String serialNumber = edittext.getText().toString();
                if(serialNumber.length() != 7){
                    showOneButtonErrorDialog(getString(R.string.enter_serial_number_dialog_error_header), getString(R.string.enter_serial_number_error_length_error));
                    dialog.dismiss();
                    return;
                }

                try {
                    double d = Double.parseDouble(serialNumber);
                }
                catch(NumberFormatException nfe) {
                    showOneButtonErrorDialog(getString(R.string.enter_serial_number_dialog_error_header), getString(R.string.enter_serial_number_error_numbers_only_error));
                    dialog.dismiss();
                    return;
                }

                KwikButtonDevice button = new KwikButtonDevice();
                button.setId(serialNumber);
                button.setUser(KwikMe.USER_ID);

                KwikMe.attachUser(button, new AttachUserListener() {
                    @Override
                    public void attachUserDone(KwikButtonDevice button) {
                        final KwikButtonDevice finalButton = button;
                        if(mApp.getButtons() == null){
                            mApp.setButtons(new ArrayList<KwikButtonDevice>() );
                        }
                        if(mApp.getButton(serialNumber) == null) {

                            mApp.getButtons().add(button);
                        }else{
                            mApp.updateButton(serialNumber,button);
                        }
                        KwikMe.getProject(button.getProject(), new GetProjectListener() {
                            @Override
                            public void getProjectDone(KwikProject project) {
                                mApp.addProject(project);
                                Intent i = new Intent(AddSerialNumberManuallyActivity.this, GoodJobActivity.class);
                                i.putExtra("buttonId",serialNumber);
                                startActivity(i);
                                finish();
                            }

                            @Override
                            public void getProjectError(KwikServerError error) {

                                showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), getString( android.R.string.ok ), "",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AddSerialNumberManuallyActivity.this.finish();
                                            }
                                        },null);
                            }
                        });
                    }
                    @Override
                    public void attachUserError(KwikServerError error) {
                        showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), getString( android.R.string.ok ), "",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AddSerialNumberManuallyActivity.this.finish();
                                    }
                                },null);
                    }
                });

            }
        });

        alert.setNegativeButton(R.string.enter_serial_number_error_cancel_button, null);

        final AlertDialog dialog = alert.create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                      @Override
                                      public void onShow(DialogInterface arg0) {
                                          dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.kwik_me_orange));
                                          dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.kwik_me_orange));

                                      }
                                  });

        dialog.show();
    }
}
