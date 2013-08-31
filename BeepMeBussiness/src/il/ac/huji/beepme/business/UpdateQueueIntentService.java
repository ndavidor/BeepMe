package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueUpdateData;

import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class UpdateQueueIntentService extends IntentService {

	public static final String ACTION_LOAD_QUEUE = "il.ac.huji.beepme.LOAD_QUEUE";
	public static final String ACTION_UPDATE_QUEUE = "il.ac.huji.beepme.UPDATE_QUEUE";
	
	public static final String TAG = UpdateQueueIntentService.class.getSimpleName();
		
    private static final String WAKELOCK_KEY = "LOCK_UPDATE_QUEUE";
    private static PowerManager.WakeLock sWakeLock;

    private static final Object LOCK = UpdateQueueIntentService.class;
    
    private Handler mHandler;
	    
	public UpdateQueueIntentService() {
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
			
			if(action.equals(ACTION_UPDATE_QUEUE)){
	    	    String data = intent.getExtras().getString("com.parse.Data");
	            updateQueue(new QueueUpdateData(data));
			}
			else if(action.equals(ACTION_LOAD_QUEUE))
				loadQueue();
        } finally {
            synchronized (LOCK) {
                if (sWakeLock != null) 
                    sWakeLock.release();
            }
        }
	}
	
	private void loadQueue(){		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Queue");
		
		List<ParseObject> list = null;
		try {
			list = query.whereEqualTo("businessid", BeepMeApplication.businessID)
				.addAscendingOrder("name")			
				.find();
		} catch (ParseException e) {}
				
		if(list == null || list.isEmpty())
			BeepMeApplication.adapter_queue.addQueues(null);
		else{
			Queue[] queues = new Queue[list.size()];
			for(int i = 0; i < queues.length; i++)
				queues[i] = new Queue(list.get(i));
			
			BeepMeApplication.adapter_queue.addQueues(queues);
		}	
	}
	
	private void updateQueue(final QueueUpdateData data){
		//skip update if notification come from this device
		if(data.getDeviceID().equals(ParseInstallation.getCurrentInstallation().getObjectId()))
			return;
		
		final Context context = getApplicationContext();
		
		//build Toast message
		String[] names = data.getNames();
		StringBuilder sb = new StringBuilder();
		sb.append(data.getType() == QueueUpdateData.TYPE_UPDATE ? "Update queue " : "Remove all customer of queue ");
		for(int i = 0; i < names.length; i++)
			sb.append(names[i]).append(i == names.length - 1 ? "!" : ", ");
		
		final String toastMessage = sb.toString();
				
		mHandler.post(new Runnable() {	
			@Override
			public void run() {
				Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
			}
		});		
			
		switch (data.getType()) {				
			case QueueUpdateData.TYPE_DELETE:	
				BeepMeApplication.adapter_queue.deleteQueue(data.getNames());
				break;	
			case QueueUpdateData.TYPE_UPDATE:	
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Queue");
				
				try {
					List<ParseObject> list = query.whereEqualTo("businessid", data.getBusinessID())
						.whereContainedIn("name", data.getNamesList())
						.find();
					
					if(list != null && !list.isEmpty()){								
						final Queue[] queues = new Queue[list.size()];
						for(int i = 0; i < queues.length; i ++)
							queues[i] = new Queue(list.get(i));
						
						BeepMeApplication.adapter_queue.addQueues(queues);					
					}	
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;						
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
        intent.setClassName(context, UpdateQueueIntentService.class.getName());
        context.startService(intent);
    }

}
