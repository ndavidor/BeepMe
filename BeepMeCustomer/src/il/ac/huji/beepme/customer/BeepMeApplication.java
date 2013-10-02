package il.ac.huji.beepme.customer;

import il.ac.huji.beepme.db.QueueAdapter;


import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.parse.Parse;


public class BeepMeApplication extends Application {

	public static QueueAdapter adapter = new QueueAdapter();
		
	public void onCreate(){
		super.onCreate();
		adapter.setContext(getApplicationContext());
		init();
		
	}
	
	private void init(){
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
		    String parseAppID = bundle.getString("il.ac.huji.beepme.parse.APPID");
		    String parseClientKey = bundle.getString("il.ac.huji.beepme.parse.CLIENTKEY");
		    
		    Parse.initialize(getApplicationContext(), parseAppID, parseClientKey);
		    
		} catch (NameNotFoundException e) {}
	}	
}
