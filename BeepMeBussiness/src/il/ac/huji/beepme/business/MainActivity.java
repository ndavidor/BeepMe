package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.Channels;
import il.ac.huji.beepme.ui.LoginFragment;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;
import il.ac.huji.beepme.business.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener, LoginFragment.LoginListener{

	private Button bt_manager;
	private Button bt_employee;
	private Button bt_entrace;
	
	private static final int CODE_UPDATE = 1389;
	private static final int INTERVAL = 60 * 1000; //miliseconds
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		bt_manager = (Button)findViewById(R.id.main_bt_manager);
		bt_employee = (Button)findViewById(R.id.main_bt_employee);
		bt_entrace = (Button)findViewById(R.id.main_bt_entrance);
		
		bt_manager.setOnClickListener(this);
		bt_employee.setOnClickListener(this);
		bt_entrace.setOnClickListener(this);
		
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
		
		PushService.subscribe(this, Channels.getBusinessChannel(BeepMeApplication.businessID), MainActivity.class);
		
		ParseAnalytics.trackAppOpened(getIntent());
		
		scheduleUpdateQueueTask();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		
		unscheduleUpdateQueueTask();
	}
	
	private void scheduleUpdateQueueTask(){
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(UpdateQueueIntentService.ACTION_LOAD_QUEUE);
		
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), CODE_UPDATE, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, INTERVAL, pi);
	}
	
	private void unscheduleUpdateQueueTask(){
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(UpdateQueueIntentService.ACTION_LOAD_QUEUE);
		
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), CODE_UPDATE, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(pi);
	}
	
	protected void showLoginDialog(boolean isManager){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
	    LoginFragment dialog = LoginFragment.newInstance(isManager);
	    dialog.setListener(this);
	    ft.add(dialog, LoginFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.main_bt_manager:
				bt_managerClicked();
				break;
			case R.id.main_bt_employee:
				bt_employeeClicked();
				break;
			case R.id.main_bt_entrance:
				bt_entranceClicked();
				break;
		}
	}
	
	private void bt_managerClicked(){
		showLoginDialog(true);
	}
	
	private void bt_employeeClicked(){
		showLoginDialog(false);
	}

	private void bt_entranceClicked(){
		Intent intent = new Intent(this, EntranceActivity.class);
		startActivity(intent);
	}

	@Override
	public void loginSuccess(String username, String password, String station) {
		if(TextUtils.isEmpty(station)){
			Intent intent = new Intent(this, ManagerActivity.class);
			startActivity(intent);
		}
		else{
			BeepMeApplication.station = Integer.parseInt(station);
			Intent intent = new Intent(this, EmployeeActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void loginCancel() {
	}
}
