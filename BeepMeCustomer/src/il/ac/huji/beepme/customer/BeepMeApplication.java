package il.ac.huji.beepme.customer;

import il.ac.huji.beepme.db.QueueAdapter;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.parse.Parse;
import il.ac.huji.beepme.customer.R;


//acralyzer URL: https://rey5137.cloudant.com/acralyzer/_design/acralyzer/index.html
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

	public static QueueAdapter adapter = new QueueAdapter();
		
	public void onCreate(){
		super.onCreate();
		adapter.setContext(getApplicationContext());
		init();
		
		ACRA.init(this);
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
