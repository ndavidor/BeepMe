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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class InputFragment extends DialogFragment implements View.OnClickListener {

	private static final String ARG_TITLE = "TITLE";
	private static final String ARG_MESSAGE = "MESSAGE";
	private static final String ARG_ICON = "ICON";
	
	private EditText et_info;
	
	public interface InputListener{
		public void input(String text);
	}
	
	private WeakReference<InputListener> listener;
	
	public static InputFragment newInstance(String title, int iconID, String message) {
		InputFragment f = new InputFragment();
		
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
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
        View v = inflater.inflate(R.layout.fragment_input, container, false);     
        
        ImageView iv_icon = (ImageView)v.findViewById(R.id.input_iv_icon);
        TextView tv_title = (TextView)v.findViewById(R.id.input_tv_title);
        TextView tv_text = (TextView)v.findViewById(R.id.input_tv_text);
        et_info = (EditText)v.findViewById(R.id.input_et);
        Button bt_ok = (Button)v.findViewById(R.id.input_bt_ok);
        Button bt_cancel = (Button)v.findViewById(R.id.input_bt_cancel);
        
        bt_ok.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        
        int id = getArguments().getInt(ARG_ICON, -1);
		if(id >= 0)
			iv_icon.setImageResource(id);
		
		tv_title.setText(getArguments().getString(ARG_TITLE));
		tv_text.setText(getArguments().getString(ARG_MESSAGE));
        
        return v;
    }
		
	public void setListener(InputListener listener){
		this.listener = new WeakReference<InputListener>(listener);
	}
		
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.input_bt_ok:
				bt_okClicked();
				break;
			case R.id.input_bt_cancel:
				bt_cancelClicked();
				break;
		}
	}
	
	private void bt_okClicked(){		
		if(listener != null && listener.get() != null)
			listener.get().input(et_info.getText().toString());
		
		this.dismiss();
	}
	
	private void bt_cancelClicked(){
		this.dismiss();
	}	
}
