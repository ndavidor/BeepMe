package il.ac.huji.beepme.ui;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class EmployeeAccountFragment extends DialogFragment implements View.OnClickListener{

	private TextView tv_title;
	private EditText et_username;
	private EditText et_password;
	private EditText et_repassword;
	
	private Button bt_ok;
	private Button bt_cancel;
	
	private boolean isNew = false;
	
	private static final String ARG_TITLE = "title";
	private static final String ARG_NEW = "new";
	private static final String ARG_USERNAME = "username";
	
	public interface InputListener{
		public void inputDone(String username, String password, boolean isNew);
		
		public void inputCancel();
	}
	
	private WeakReference<InputListener> listener;
	
	public static EmployeeAccountFragment newInstance(String title, boolean isNew, String username){
		EmployeeAccountFragment fragment = new EmployeeAccountFragment();
		
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putBoolean(ARG_NEW, isNew);
		args.putString(ARG_USERNAME, username);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setListener(InputListener listener){
		this.listener = new WeakReference<InputListener>(listener);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_employee_account, container, false);     
        
        tv_title = (TextView)v.findViewById(R.id.employee_account_tv_title);
        et_username = (EditText)v.findViewById(R.id.employee_account_et_username);
        et_password = (EditText)v.findViewById(R.id.employee_account_et_password);
        et_repassword = (EditText)v.findViewById(R.id.employee_account_et_repassword);
        bt_ok = (Button)v.findViewById(R.id.employee_account_bt_ok);
        bt_cancel = (Button)v.findViewById(R.id.employee_account_bt_cancel);
                
        bt_ok.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        
        if(savedInstanceState != null){
        	isNew = savedInstanceState.getBoolean(ARG_NEW, true);
        	tv_title.setText(savedInstanceState.getString(ARG_TITLE));
        	et_username.setText(savedInstanceState.getString(ARG_USERNAME));
        }
        else  if(getArguments() != null){
        	isNew = getArguments().getBoolean(ARG_NEW, true);
        	tv_title.setText(getArguments().getString(ARG_TITLE));
        	et_username.setText(getArguments().getString(ARG_USERNAME));
        }
        
        if(!isNew){
        	et_username.setVisibility(View.GONE);
        	et_password.setHint("New password");
        }
                                
        return v;
    }
	
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putBoolean(ARG_NEW, isNew);
		outState.putString(ARG_TITLE, tv_title.getText().toString());
		outState.putString(ARG_USERNAME, et_username.getText().toString());
	}
	
	protected void showInfoDialog(String title, int iconID, String message, String okText){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
	    ft.add(dialog, InfoFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.employee_account_bt_ok:
				bt_okClicked();
				break;
			case R.id.employee_account_bt_cancel:
				bt_cancelClicked();
				break;
		}
	}
	
	private void bt_okClicked(){
		final String username = et_username.getText().toString();
		final String password = et_password.getText().toString();
		final String repassword = et_repassword.getText().toString();
		
		if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || (!isNew && TextUtils.isEmpty(repassword))){
			showInfoDialog("Missing value", android.R.drawable.ic_dialog_alert, "Please enter all the value", "OK");
			return;
		}
				
		if(!password.equals(repassword)){
			showInfoDialog("Mismatch", android.R.drawable.ic_dialog_alert, "Password is mismatch", "OK");
			return;
		}
		
		if(listener != null && listener.get() != null)
			listener.get().inputDone(username, password, isNew);
		
		this.dismiss();
	}
	
	private void bt_cancelClicked(){
		if(listener != null && listener.get() != null)
			listener.get().inputCancel();
		
		this.dismiss();
	}
	
}
