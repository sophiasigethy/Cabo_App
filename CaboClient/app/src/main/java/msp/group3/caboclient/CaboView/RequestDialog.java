package msp.group3.caboclient.CaboView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import msp.group3.caboclient.CaboModel.Player;
import msp.group3.caboclient.R;

public class RequestDialog {

    private boolean wantToAccept = false;
    private Button dialogButtonAccept;
    private Button dialogButtonDecline;
    private Dialog dialog = null;
    private TextView text;
    private ImageView image;

    @SuppressLint("SetTextI18n")
    public void showDialog(Activity activity, Player sender){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.request_dialog);

        image = (ImageView) dialog.findViewById(R.id.dialog_image);
        text = (TextView) dialog.findViewById(R.id.text_dialog);
        if(sender!=null){
            text.setText(activity.getApplicationContext().getResources().getString(R.string.friend_request_received)
                    + ": " + sender.getNick());
        }

        dialogButtonAccept = (Button) dialog.findViewById(R.id.btn_dialog_accept);

        dialogButtonDecline = (Button) dialog.findViewById(R.id.btn_dialog_decline);
        dialogButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    public boolean wantToAccept(){
        return wantToAccept;
    }

    public Button getDialogButtonAccept(){
        return dialogButtonAccept;
    }

    public Button getDialogButtonDecline(){
        return dialogButtonDecline;
    }

    public Dialog getDialog(){
        return dialog;
    }

    public TextView getText(){
        return text;
    }

    public ImageView getImage(){
        return image;
    }
}
