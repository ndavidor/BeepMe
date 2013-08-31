package il.ac.huji.beepme.ui;

import java.lang.ref.WeakReference;

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

import il.ac.huji.beepme.customer.R;

public class ConfirmFragment extends DialogFragment implements View.OnClickListener {

	private static final String ARG_TITLE = "TITLE";
	private static final String ARG_MESSAGE = "MESSAGE";
	private static final String ARG_ICON = "ICON";
	private static final String ARG_ID  = "ID";
	
	private int id;
	
	public interface ConfirmListener{
		public void confirm(ConfirmFragment dialog, boolean yes);
	}
	
	private WeakReference<ConfirmListener> listener;
	
	public static ConfirmFragment newInstance(int id, String title, int iconID, String message) {
		ConfirmFragment f = new ConfirmFragment();
		
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putInt(ARG_ICON, iconID);
        args.putInt(ARG_ID, id);
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
        View v = inflater.inflate(R.layout.fragment_confirm, container, false);     
        
        ImageView iv_icon = (ImageView)v.findViewById(R.id.confirm_iv_icon);
        TextView tv_title = (TextView)v.findViewById(R.id.confirm_tv_title);
        TextView tv_text = (TextView)v.findViewById(R.id.confirm_tv_text);
        Button bt_yes = (Button)v.findViewById(R.id.confirm_bt_yes);
        Button bt_no = (Button)v.findViewById(R.id.confirm_bt_no);
        
        bt_yes.setOnClickListener(this);
        bt_no.setOnClickListener(this);
        
        int iconID = getArguments().getInt(ARG_ICON, -1);
		if(iconID >= 0)
			iv_icon.setImageResource(iconID);
		
		tv_title.setText(getArguments().getString(ARG_TITLE));
		tv_text.setText(getArguments().getString(ARG_MESSAGE));
        
		id = getArguments().getInt(ARG_ID);
		
        return v;
    }
		
	public void setListener(ConfirmListener listener){
		this.listener = new WeakReference<ConfirmListener>(listener);
	}
	
	public int getConfirmId(){
		return id;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.confirm_bt_yes:
				bt_yesClicked();
				break;
			case R.id.confirm_bt_no:
				bt_noClicked();
				break;
		}
	}
	
	private void bt_yesClicked(){		
		if(listener != null && listener.get() != null)
			listener.get().confirm(this, true);
		
		this.dismiss();
	}
	
	private void bt_noClicked(){
		if(listener != null && listener.get() != null)
			listener.get().confirm(this, false);
		
		this.dismiss();
	}	
}
