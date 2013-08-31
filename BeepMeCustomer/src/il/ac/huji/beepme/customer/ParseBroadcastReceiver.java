package il.ac.huji.beepme.customer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ParseBroadcastReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {		
		String action = intent.getAction();
		
		if(action.equals("android.intent.action.BOOT_COMPLETED"))
			UpdateQueueIntentService.runIntentInService(context, new Intent(UpdateQueueIntentService.ACTION_LOAD_QUEUE));
		else if(action.equals(UpdateCustomerIntentService.ACTION_UPDATE_CUSTOMER))
			UpdateCustomerIntentService.runIntentInService(context, intent);
		else
			UpdateQueueIntentService.runIntentInService(context, intent);
	}

}
