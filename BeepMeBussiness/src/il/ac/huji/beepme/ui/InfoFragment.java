package il.ac.huji.beepme.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class InfoFragment extends DialogFragment {

	private static final String ARG_TITLE = "TITLE";
	private static final String ARG_MESSAGE = "MESSAGE";
	private static final String ARG_OKTEXT = "OKTEXT";
	private static final String ARG_ICON = "ICON";
	
	public static InfoFragment newInstance(String title, int iconID, String message, String okText) {
		InfoFragment f = new InfoFragment();
		
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_OKTEXT,okText);
        args.putInt(ARG_ICON, iconID);
        f.setArguments(args);

        return f;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);     
        
        ImageView iv_icon = (ImageView)v.findViewById(R.id.info_iv_icon);
        TextView tv_title = (TextView)v.findViewById(R.id.info_tv_title);
        TextView tv_text = (TextView)v.findViewById(R.id.info_tv_text);
        Button bt_ok = (Button)v.findViewById(R.id.info_bt_ok);
        
        bt_ok.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				InfoFragment.this.dismiss();
			}
		});
        
        int id = getArguments().getInt(ARG_ICON, -1);
		if(id >= 0)
			iv_icon.setImageResource(id);
		
		tv_title.setText(getArguments().getString(ARG_TITLE));
		tv_text.setText(getArguments().getString(ARG_MESSAGE));
		bt_ok.setText(getArguments().getString(ARG_OKTEXT));
        
        return v;
    }
	
}
