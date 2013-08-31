package il.ac.huji.beepme.db;

import il.ac.huji.beepme.business.BeepMeApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Queue {
	
	public boolean selected = false;
	private String name;
	private int total;
	private int current;
	private long totalTime; //in second
	private String businessID;
	
	private Customer customer;
	
	private ParseObject object;
			
	public interface OnQueueStatusChangedListener{
		
		public void onQueueTotalChanged(int total);
		
		public void onQueueDeleted();
		
	}
	
	private ArrayList<WeakReference<OnQueueStatusChangedListener>> listeners = new ArrayList<WeakReference<OnQueueStatusChangedListener>>();
		
	public Queue(ParseObject obj){
		this.object = obj;
		
		name = obj.getString("name");
		total = obj.getInt("total");
		current = obj.getInt("current");
		totalTime = obj.getLong("totaltime");
		businessID = obj.getString("businessid");
	}
	
	public Queue(String name, int total, int current, long totalTime, String businessID){
		object = new ParseObject("Queue");
		object.put("name", name);
		object.put("total", total);
		object.put("current", current);
		object.put("totaltime", totalTime);
		object.put("businessid", businessID);		
		
		this.name = name;
		this.total = total;
		this.current = current;
		this.totalTime = totalTime;
		this.businessID = businessID;
	}
		
	public void addOnQueueStatusChangedListener(OnQueueStatusChangedListener listener){
		listeners.add(new WeakReference<OnQueueStatusChangedListener>(listener));		
	}
	
	public void removeOnQueueStatusChangedListener(OnQueueStatusChangedListener listener){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnQueueStatusChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == listener)
				listeners.remove(i);
		}
	}
	
	public ParseObject getParseObject(){
		return object;
	}
	
	public Queue setParseObject(ParseObject object){
		this.object = object;
		
		return this;
	}
	
	public double getAvgMinuteTime(){
		if(current == 0)
			return 0f;
		
		return (double)totalTime / current / 60;
	}
	
	public long getTotalTime(){
		return totalTime;
	}
	
	public Queue setTotalTime(long totalTime){
		if(this.totalTime != totalTime)
			this.totalTime = totalTime;
		
		return this;
	}
	
	public String getName(){
		return name;
	}
	
	public String getBussinessID(){
		return businessID;
	}
	
	public int getTotal(){
		return total;
	}
	
	public Queue setTotal(int total){
		if(this.total != total){
			this.total = total;
			dispatchTotalChanged(total);
		}
		
		return this;
	}
	
	public int getCurrent(){
		return current;
	}
	
	public Queue setCurrent(int current){
		if(this.current != current)
			this.current = current;				
		
		return this;
	}
	
	public void reset(){
		current = 0;
		totalTime = 0;
		setTotal(0);
	}
	
	private void dispatchTotalChanged(int total){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnQueueStatusChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null)
				listeners.remove(i);
			else
				ref.get().onQueueTotalChanged(total);
		}
	}
	
	public void dispatchDeleted(){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnQueueStatusChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null)
				listeners.remove(i);
			else
				ref.get().onQueueDeleted();
		}
	}
	
	public String findNextCustomer(){	
		if(customer != null){
			//increment totalTime
			object.increment("totaltime", (System.currentTimeMillis() - customer.getCreatedTime()) / 1000);
			
			try {
				object.save();
			} catch (ParseException e) {
				object.saveEventually();
			}
			
			customer.delete();
			
			//push this customer is served notification
			CustomerUpdateData.build(CustomerUpdateData.TYPE_DONE, customer.businessID, customer.queueName, customer.number, -1)
				.pushNotification(60 * 30, Channels.getQueueChannel(customer.businessID, customer.queueName));
			
			customer = null;
		}
		
		while(true){
			object.increment("current");
			try {
				object.save();
			} catch (ParseException e) {
				return e.getMessage() == null ? "Unknown error" : e.getMessage();
			}
			
			int current = object.getInt("current");
			int total = object.getInt("total");
			
			if(current > total){
				object.increment("current", -1);
				setTotal(total).setCurrent(total);
				try {
					object.save();
				} catch (ParseException e) {
					object.saveEventually();
				}
				return null;
			}
			else
				setTotal(total).setCurrent(current);			
						
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Customer");
			ParseObject obj = null;
			try {
				List<ParseObject> list = query.whereEqualTo("businessid", businessID)
											.whereEqualTo("queuename", name)
											.whereEqualTo("number", current)
											.addDescendingOrder("createdAt")
											.find();
				
				if(!list.isEmpty()){
					obj = list.get(0);
					
					//remove all outdate customer
					for(int i = 1, count = list.size(); i < count; i++)
						try{
							list.get(i).delete();
						}
						catch(ParseException ex){
							list.get(i).deleteEventually();
						}
				}
				
			} catch (ParseException e) {
				return e.getMessage() == null ? "Unknown error" : e.getMessage();
			}
			
			if(obj != null){		
				customer = new Customer(obj);				
				customer.setStation(BeepMeApplication.station);
								
				//push update queue notification
				QueueUpdateData.build(QueueUpdateData.TYPE_UPDATE, businessID)
					.put(name)
					.pushNotification(60 * 30, Channels.getAllChannels(businessID, name));
				
				//push customer notification
				CustomerUpdateData.build(CustomerUpdateData.TYPE_TURN, businessID, name, customer.number, BeepMeApplication.station)
					.pushNotification(60 * 30, Channels.getQueueChannel(businessID, name));
				
				return null;
			}
		}
	}
	
	public Customer getCustomer(){
		return customer;
	}
	
}
