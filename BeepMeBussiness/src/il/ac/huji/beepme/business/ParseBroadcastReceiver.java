package il.ac.huji.beepme.business;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ParseBroadcastReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		UpdateQueueIntentService.runIntentInService(context, intent);    
	}	
}
