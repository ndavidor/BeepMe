package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.EmployeeAdapter;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseObject;
import il.ac.huji.beepme.business.R;

// acralyzer URL: https://rey5137.cloudant.com/acralyzer/_design/acralyzer/index.html
@ReportsCrashes(
        formKey = "",
        formUri = "https://rey5137.cloudant.com/acra-beepme/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="innothaldnturthersurside",
        formUriBasicAuthPassword="fO6Rf1wJqppj5CJQtYaKV5pq",
        
        mode = ReportingInteractionMode.NOTIFICATION,
        resToastText = R.string.crash_toast_text, 
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resNotifIcon = android.R.drawable.stat_notify_error, 
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogOkToast = R.string.crash_dialog_ok_toast      
)
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
		ACRA.init(this);
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
