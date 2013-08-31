package il.ac.huji.beepme.db;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseInstallation;

public class QueueUpdateData extends ObjectUpdateData{
		
	public static final int TYPE_UPDATE = 0;
	public static final int TYPE_DELETE = 1;
	public static final int TYPE_REMOVE_CUSTOMER = 2;
	
	private String deviceID;
	private int type;
	private String businessID;
	private ArrayList<String> names = new ArrayList<String>();
	
	public static QueueUpdateData build(int type, String businessID){
		return new QueueUpdateData(type, businessID);
	}
	
	public QueueUpdateData(String json){
		try {
			JSONObject data = new JSONObject(json);
			deviceID = data.getString("deviceid");
			type = data.getInt("type");
			businessID = data.getString("businessid");
			JSONArray array_names = data.getJSONArray("names");			
			for(int i = 0; i < array_names.length(); i++)
				put(array_names.getString(i));			
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	
	public QueueUpdateData(int type, String businessID){
		this.deviceID = ParseInstallation.getCurrentInstallation().getObjectId();
		this.type = type;
		this.businessID = businessID;
	}
	
	public QueueUpdateData put(String name){
		names.add(name);
		
		return this;
	}
	
	public String getDeviceID(){
		return deviceID;
	}
	
	public int getType(){
		return type;
	}
	
	public int count(){
		return names.size();
	}
	
	public String getBusinessID(){
		return businessID;
	}
	
	public String[] getNames(){
		if(names.isEmpty())
			return null;
		
		return names.toArray(new String[names.size()]);
	}
	
	public ArrayList<String> getNamesList(){
		return names;
	}
		
	public JSONObject toJson(){
		JSONObject data = new JSONObject();
		
		JSONArray array_names = new JSONArray();
		for(String name : names)
			array_names.put(name);		
		
		try {
			data.put("action", "il.ac.huji.beepme.UPDATE_QUEUE");
			data.put("deviceid", deviceID);
			data.put("type", type);
			data.put("businessid", businessID);
			data.put("names", array_names);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}
}
