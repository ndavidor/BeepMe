package il.ac.huji.beepme.db;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class Customer {

	public final String uid;
	public final int number;
	public final String queueName;
	public final String businessID;
	private int station = -1;
	private ParseObject object;
		
	public interface OnCustomerStationChangedListener{
		
		public void onCustomerStationChanged(int station);
				
	}
	
	private ArrayList<WeakReference<OnCustomerStationChangedListener>> listeners = new ArrayList<WeakReference<OnCustomerStationChangedListener>>();
	
	public Customer(ParseObject object){
		this.object = object;
		this.queueName = object.getString("queuename");
		this.businessID = object.getString("businessid");
		this.number = object.getInt("number");
		this.uid = object.getString("uid");
		this.station = object.getInt("station");
	}
	
	public Customer(String queueName, String businessID, int number, String uid){
		this.queueName = queueName;
		this.businessID = businessID;
		this.number = number;
		this.uid = uid;
		this.station = -1;
		
		object = new ParseObject("Customer");
		object.put("businessid", businessID);
		object.put("queuename", queueName);
		object.put("number", number);
		object.put("uid", uid);
		object.put("station", station);
		object.put("device", ParseInstallation.getCurrentInstallation().getObjectId());
	}
	
	public ParseObject getParseObject(){
		return object;
	}
		
	public void setParseObject(ParseObject object){
		this.object = object;
	}
	
	public void delete(){
		try{
			object.delete();
		} catch (ParseException e) {
			object.deleteEventually();
		}
	}
	
	public void addOnCustomerStationChangedListener(OnCustomerStationChangedListener listener){
		listeners.add(new WeakReference<OnCustomerStationChangedListener>(listener));		
	}
	
	public void removeOnCustomerStationChangedListener(OnCustomerStationChangedListener listener){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnCustomerStationChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null || ref.get() == listener)
				listeners.remove(i);
		}	
	}
	
	public int getStation(){
		return station;
	}
	
	public void setStation(int station){
		if(this.station != station){
			this.station = station;
			dispatchStationChanged(station);
		}
	}
	
	private void dispatchStationChanged(int total){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnCustomerStationChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null)
				listeners.remove(i);
			else
				ref.get().onCustomerStationChanged(station);
		}
	}	
}
