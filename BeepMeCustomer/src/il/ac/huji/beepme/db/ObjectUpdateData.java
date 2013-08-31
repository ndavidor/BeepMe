package il.ac.huji.beepme.db;

import java.util.ArrayList;

import org.json.JSONObject;

import com.parse.ParsePush;

public abstract class ObjectUpdateData {

	public abstract JSONObject toJson();
	
	/**
	 * push a notification with this data
	 * @param expTime the expiration time of notification, in second. If less than or equals 0, then only send to online device.
	 * @param channels the channels of notifcations.
	 */
	public void pushNotification(long expTime, String... channels){
		ParsePush push = new ParsePush();
		
		if(channels.length == 1)
			push.setChannel(channels[0]);
		else{
			ArrayList<String> temp = new ArrayList<String>(channels.length);
			for(String channel : channels)
				temp.add(channel);
			push.setChannels(temp);
		}
		push.setExpirationTimeInterval(expTime);
		push.setData(toJson());
		push.sendInBackground();
	}
	
}
