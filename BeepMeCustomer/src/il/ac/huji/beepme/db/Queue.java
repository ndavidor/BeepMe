package il.ac.huji.beepme.db;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.location.Location;
import android.location.LocationManager;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;


public class Queue {
	
	public boolean selected = false;
	private String name;
	private int total;
	private int current;
	private long totalTime; //in second
	private String businessID;
	private Location location;
	
	private Customer customer;
	private ParseObject object;
			
	public interface OnQueueStatusChangedListener{
		
		public void onQueueTotalChanged(int total);
		
		public void onQueueCurrentChanged(int current);
		
		public void onQueueTotalTimeChanged(long time);
				
	}
	
	private ArrayList<WeakReference<OnQueueStatusChangedListener>> listeners = new ArrayList<WeakReference<OnQueueStatusChangedListener>>();
		
	public Queue(ParseObject obj){
		this.object = obj;

		name = obj.getString("name");
		total = obj.getInt("total");
		current = obj.getInt("current");
		totalTime = obj.getLong("totaltime");
		businessID = obj.getString("businessid");
		
		ParseGeoPoint point = obj.getParseGeoPoint("location");
		if(point != null){
			location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(point.getLatitude());
			location.setLongitude(point.getLongitude());
		}
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
	
	public double getAvgTime(){
		if(current == 0)
			return 0f;
		
		return (double)totalTime / (float)current;
	}
	
	public double getAvgMinuteTime(){
		return getAvgTime() / 60;
	}
	
	public double getWaitMinuteTime(){
		return getWaitTime() / 60;
	}
	
	public double getWaitTime(){
		if(current == 0)
			return 0f;
		
		return ((double)totalTime / (float)current * (customer.number - current) * 9 / 10);
	}
	
	public long getTotalTime(){
		return totalTime;
	}
	
	public Queue setTotalTime(long totalTime){
		if(this.totalTime != totalTime){
			this.totalTime = totalTime;
			dispatchTotalTimeChanged(totalTime);
		}
		
		return this;
	}
	
	public String getBusinessID(){
		return businessID;
	}
	
	public String getName(){
		return name;
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
		if(this.current != current){
			this.current = current;
			dispatchCurrentChanged(current);
		}
		
		return this;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public Queue setLocation(Location location){
		if(location != null)
			this.location = location;
		
		return this;
	}
	
	public void setCustomer(Customer customer){
		this.customer = customer;
	}
	
	public Customer getCustomer(){
		return customer;
	}
	
	public int getYourNumber(){
		return customer.number;
	}
	
	public String getYourUID(){
		return customer.uid;
	}
	
	public int getYourStation(){
		return customer.getStation();
	}
	
	public void reset(){		
		setTotal(0);
		setCurrent(0);
		setTotalTime(0);
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
	
	private void dispatchCurrentChanged(int current){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnQueueStatusChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null)
				listeners.remove(i);
			else
				ref.get().onQueueCurrentChanged(current);
		}
	}
	
	private void dispatchTotalTimeChanged(long time){
		for(int i = listeners.size() - 1; i >= 0; i--){
			WeakReference<OnQueueStatusChangedListener> ref = listeners.get(i);
			if(ref == null || ref.get() == null)
				listeners.remove(i);
			else
				ref.get().onQueueTotalTimeChanged(time);
		}
	}	
}
