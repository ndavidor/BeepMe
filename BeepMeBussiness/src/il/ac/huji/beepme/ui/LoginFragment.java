package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.business.BeepMeApplication;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import il.ac.huji.beepme.business.R;

public class LoginFragment extends DialogFragment implements View.OnClickListener{

	private EditText et_username;
	private EditText et_password;
	private EditText et_number;
	
	private Button bt_ok;
	private Button bt_cancel;
	
	private boolean isManager = false;
	
	private static final String ARG_MANAGER = "manager";
	
	public interface LoginListener{
		public void loginSuccess(String username, String password, String station);
		
		public void loginCancel();
	}
	
	private WeakReference<LoginListener> listener;
	
	public static LoginFragment newInstance(boolean isManager){
		LoginFragment fragment = new LoginFragment();
		
		Bundle args = new Bundle();
		args.putBoolean(ARG_MANAGER, isManager);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setListener(LoginListener listener){
		this.listener = new WeakReference<LoginListener>(listener);
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
        View v = inflater.inflate(R.layout.fragment_login, container, false);     
        
        et_username = (EditText)v.findViewById(R.id.login_et_username);
        et_password = (EditText)v.findViewById(R.id.login_et_password);
        et_number = (EditText)v.findViewById(R.id.login_et_number);
        bt_ok = (Button)v.findViewById(R.id.login_bt_ok);
        bt_cancel = (Button)v.findViewById(R.id.login_bt_cancel);
                
        bt_ok.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        
        if(savedInstanceState != null)
        	isManager = savedInstanceState.getBoolean(ARG_MANAGER, false);
        else  if(getArguments() != null)
        	isManager = getArguments().getBoolean(ARG_MANAGER, false);
        
        et_number.setVisibility(isManager ? View.GONE : View.VISIBLE);
                                
        return v;
    }
	
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putBoolean(ARG_MANAGER, isManager);			
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
			case R.id.login_bt_ok:
				bt_okClicked();
				break;
			case R.id.login_bt_cancel:
				bt_cancelClicked();
				break;
		}
	}
	
	private void bt_okClicked(){
		final String username = et_username.getText().toString();
		final String password = et_password.getText().toString();
		final String number = et_number.getText().toString();
		
		if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || (!isManager && TextUtils.isEmpty(number))){
			showInfoDialog("Missing value", android.R.drawable.ic_dialog_alert, "Please enter all the value", "OK");
			return;
		}
		
		final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Please wait ...", true, false);
		
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){

			@Override
			protected String doInBackground(Void... params) {
				if(isManager){
					if(BeepMeApplication.manager == null){
						ParseQuery<ParseObject> query = ParseQuery.getQuery("Manager");
						try {
							ParseObject object = query.whereEqualTo("businessid", BeepMeApplication.businessID).getFirst();
							if(object == null)
								return "No manager account found for this business!";
							
							BeepMeApplication.manager = object;				
						} catch (ParseException e) {
							return "No manager account found for this business!";
						}
					}
					
					if(!BeepMeApplication.manager.getString("username").equals(username) || !BeepMeApplication.manager.getString("password").equals(password))
						return "Username or password mismatch!";
					
					return null;
				} 
				else{
					ParseQuery<ParseObject> query = ParseQuery.getQuery("Employee");
					try {
						ParseObject object = query.whereEqualTo("businessid", BeepMeApplication.businessID)
								.whereEqualTo("username", username)
								.whereEqualTo("password", password)
								.getFirst();
						
						if(object == null)
							return "Username or password mismatch!";
														
					} catch (ParseException e) {
						return "Username or password mismatch!";	
					}					
				}
				
				return null;
			}
			
			@Override
			protected void onPostExecute(String error){
				dialog.dismiss();
				
				if(error == null){					
					if(listener != null && listener.get() != null)
						listener.get().loginSuccess(username, password, number);
					
					LoginFragment.this.dismiss();
				}
				else
					showInfoDialog("Error", android.R.drawable.ic_dialog_alert, error, "OK");
			}
			
		};
		
		task.execute(new Void[0]);		
	}
	
	private void bt_cancelClicked(){
		if(listener != null && listener.get() != null)
			listener.get().loginCancel();
		
		this.dismiss();
	}
	
}
