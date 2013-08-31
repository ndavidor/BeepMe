package il.ac.huji.beepme.db;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseInstallation;

public class CustomerUpdateData extends ObjectUpdateData{
		
	public static final int TYPE_TURN = 0;
	public static final int TYPE_DONE = 1;
	
	private String deviceID;
	private int type;
	private String businessID;
	private String queueName;
	private int number;
	private int station;
	
	public static CustomerUpdateData build(int type, String businessID, String queueName, int number, int station){
		return new CustomerUpdateData(type, businessID, queueName, number, station);
	}
	
	public CustomerUpdateData(String json){
		try {
			JSONObject data = new JSONObject(json);
			deviceID = data.getString("deviceid");
			type = data.getInt("type");
			businessID = data.getString("businessid");
			queueName = data.getString("queuename");
			number = data.getInt("number");
			station = data.getInt("station");			
		} catch (JSONException e) {}		
	}
	
	public CustomerUpdateData(int type, String businessID, String queueName, int number, int station){
		this.deviceID = ParseInstallation.getCurrentInstallation().getObjectId();
		this.type = type;
		this.businessID = businessID;
		this.queueName = queueName;
		this.number = number;
		this.station = station;
	}
	
	public String getDeviceID(){
		return deviceID;
	}
	
	public int getType(){
		return type;
	}
	
	public String getBusinessID(){
		return businessID;
	}
	
	public String getQueueName(){
		return queueName;
	}
	
	public int getNumber(){
		return number;
	}
	
	public int getStation(){
		return station;
	}
	
	public JSONObject toJson(){
		JSONObject data = new JSONObject();
				
		try {
			data.put("action", "il.ac.huji.beepme.UPDATE_CUSTOMER");
			data.put("deviceid", deviceID);
			data.put("type", type);
			data.put("number", number);
			data.put("station", station);
			data.put("businessid", businessID);
			data.put("queuename", queueName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return data;
	}
}
