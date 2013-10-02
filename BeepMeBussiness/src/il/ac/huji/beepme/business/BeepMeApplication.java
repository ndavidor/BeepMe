package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.EmployeeAdapter;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;


import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseObject;


public class BeepMeApplication extends Application {

	public static QueueAdapter adapter_queue = new QueueAdapter();
	public static EmployeeAdapter adapter_employee = new EmployeeAdapter();
	public static int station;
	public static Queue workingQueue;
	public static ParseObject manager;
	
	public static String businessID;
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int SIGNIFICANTLY_DISTANCE = 50;
	public static Location currentBestLocation;
		
	public void onCreate(){
		super.onCreate();
		adapter_queue.setContext(getApplicationContext());
		adapter_employee.setContext(getApplicationContext());
		
		init();
		adapter_queue.loadData();
	}
	
	private void init(){
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
		    String parseAppID = bundle.getString("il.ac.huji.beepme.parse.APPID");
		    String parseClientKey = bundle.getString("il.ac.huji.beepme.parse.CLIENTKEY");
		    businessID = bundle.getString("il.ac.huji.beepme.business.ID");
		    
		    Parse.initialize(getApplicationContext(), parseAppID, parseClientKey);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	public static boolean updateLocation(Location location){
		if(location == null)
			return false;
		
		boolean result = false;
		
		if(isBetterLocation(location, currentBestLocation)){
			result = (currentBestLocation == null || currentBestLocation.distanceTo(location) > SIGNIFICANTLY_DISTANCE);			
			currentBestLocation = location;
		}
		
		return result;
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) 
	        return true;	    

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) 
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    else if (isSignificantlyOlder)
	        return false;	    

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate)
	        return true;
	    else if (isNewer && !isLessAccurate) 
	        return true;
	    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
	        return true;
	    
	    return false;
	}
	
	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) 
	      return provider2 == null;
	    
	    return provider1.equals(provider2);
	}
}
