package il.ac.huji.beepme.customer;

import il.ac.huji.beepme.db.CustomerUpdateData;
import il.ac.huji.beepme.db.Queue;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseInstallation;
import il.ac.huji.beepme.customer.R;

public class UpdateCustomerIntentService extends IntentService {

	public static final String TAG = UpdateCustomerIntentService.class.getSimpleName();
	
	public static final String ACTION_UPDATE_CUSTOMER = "il.ac.huji.beepme.UPDATE_CUSTOMER";
		
    private static final String WAKELOCK_KEY = "LOCK_UPDATE_CUSTOMER";
    private static PowerManager.WakeLock sWakeLock;

    private static final Object LOCK = UpdateCustomerIntentService.class;
	    
    private Handler mHandler;
    
	public UpdateCustomerIntentService() {
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
			String action = intent.getAction();
			
			if(action.equals(ACTION_UPDATE_CUSTOMER)){
				String data = intent.getExtras().getString("com.parse.Data");
	            updateCustomer(new CustomerUpdateData(data));
			}
        } finally {
            synchronized (LOCK) {
                if (sWakeLock != null) 
                    sWakeLock.release();
            }
        }
	}
		
	private void updateCustomer(final CustomerUpdateData data){
		//skip update if notification come from this device
		if(data.getDeviceID().equals(ParseInstallation.getCurrentInstallation().getObjectId()))
			return;
		
		final Context context = getApplicationContext();
		final Queue queue = BeepMeApplication.adapter.getQueue(data.getBusinessID(), data.getQueueName());	
						
		switch (data.getType()) {		
			case CustomerUpdateData.TYPE_TURN:														
				if(queue != null && queue.getYourNumber() == data.getNumber()){
					mHandler.post(new Runnable() {						
						@Override
						public void run() {
							Toast.makeText(context, "Your turn at queue: " + data.getQueueName() + " has come!", Toast.LENGTH_SHORT).show();	
							
							queue.getCustomer().setStation(data.getStation());
							BeepMeApplication.adapter.notifyDataSetInvalidated();
						}
					});			
					
					buildNotification(data.getBusinessID(), data.getQueueName(), data.getStation());
				}
				break;
			case CustomerUpdateData.TYPE_DONE:				
				if(queue != null && queue.getYourNumber() == data.getNumber()){
					mHandler.post(new Runnable() {						
						@Override
						public void run() {
							Toast.makeText(context, "Your turn at queue: " + data.getQueueName() + " has end!", Toast.LENGTH_SHORT).show();						
						}
					});	
					
					BeepMeApplication.adapter.removeQueue(queue);	
				}
				break;	
		}
		
	}
	
	private void buildNotification(String businessID, String queueName, int station){
		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beepmeringtone);
		
		Context context  = getApplicationContext();
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.beepmelogogeneral)
		        .setContentTitle("Your turn is up")
		        .setContentText(queueName + ": Please go to the station " + station)
		        .setSound(uri)
		        .setAutoCancel(true)
		        .setVibrate(new long[]{0, 1000, 250, 1000, 250, 1000, 250, 1000, 250, 1000});
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
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
        intent.setClassName(context, UpdateCustomerIntentService.class.getName());
        context.startService(intent);
    }

}
