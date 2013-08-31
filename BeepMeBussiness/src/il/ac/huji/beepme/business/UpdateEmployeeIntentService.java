package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.Employee;

import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class UpdateEmployeeIntentService extends IntentService {

	public static final String ACTION_LOAD_EMPLOYEE = "il.ac.huji.beepme.LOAD_EMPLOYEE";
	
	public static final String TAG = UpdateEmployeeIntentService.class.getSimpleName();
		
    private static final String WAKELOCK_KEY = "LOCK_UPDATE_EMPLOYEE";
    private static PowerManager.WakeLock sWakeLock;

    private static final Object LOCK = UpdateEmployeeIntentService.class;
    
    private Handler mHandler;
	    
	public UpdateEmployeeIntentService() {
		super(TAG);
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		mHandler = new Handler();
	}
		
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			loadEmployee();
        } finally {
            synchronized (LOCK) {
                if (sWakeLock != null) 
                    sWakeLock.release();
            }
        }
	}
	
	private void loadEmployee(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Employee");
		
		List<ParseObject> list = null;
		try {
			list = query.whereEqualTo("businessid", BeepMeApplication.businessID)
				.addAscendingOrder("username")			
				.find();
		} catch (ParseException e) {}
				
		if(list == null || list.isEmpty())
			BeepMeApplication.adapter_employee.addEmployees(mHandler, null);
		else{
			Employee[] employees = new Employee[list.size()];
			for(int i = 0; i < employees.length; i++)
				employees[i] = new Employee(list.get(i));
			
			BeepMeApplication.adapter_employee.addEmployees(mHandler, employees);
		}	
	}
	
    public static void runIntentInService(Context context, Intent intent) {
        synchronized (LOCK) {
            if (sWakeLock == null) {
                // This is called from BroadcastReceiver, there is no init.
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
            }
        }
        
        Log.v(TAG, "Acquiring wakelock");
        sWakeLock.acquire();
        intent.setClassName(context, UpdateEmployeeIntentService.class.getName());
        context.startService(intent);
    }

}
