package il.ac.huji.beepme.db;

import com.parse.ParseException;
import com.parse.ParseObject;


public class Customer {

	public final String uid;
	public final int number;
	public final String queueName;
	public final String businessID;
	
	private ParseObject object;
		
	public Customer(ParseObject object){
		this.queueName = object.getString("queuename");
		this.businessID = object.getString("businessid");
		this.number = object.getInt("number");
		this.uid = object.getString("uid");		
		this.object = object;
	}
	
	public void setStation(int station){
		object.put("station", station);
		object.saveEventually();
	}
	
	public void delete(){
		try {
			object.delete();
		} catch (ParseException e) {
			object.deleteEventually();
		}		
	}
	
	public long getCreatedTime(){
		return object.getCreatedAt().getTime();
	}
}
