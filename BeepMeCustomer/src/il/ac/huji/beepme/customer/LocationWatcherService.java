package il.ac.huji.beepme.customer;

import il.ac.huji.beepme.db.Queue;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import il.ac.huji.beepme.customer.R;

public class LocationWatcherService extends Service implements LocationListener{

	LocationManager locationManager;
	Handler handler;
	
	private static final int INTERVAL = 2 * 60 * 1000;
	private static final float DISTANCE = 0; 
	
	public static Location currentBestLocation;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		handler = new Handler();	
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);				
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, this);
		
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(isBetterLocation(lastKnownLocation, currentBestLocation)){
			currentBestLocation = lastKnownLocation;
			LocationCheckingTask.start(getApplicationContext(), handler, currentBestLocation);
		}
		
//		Toast.makeText(getApplicationContext(), "service start", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();		
		locationManager.removeUpdates(this);	
		
//		Toast.makeText(getApplicationContext(), "service destroy", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
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
	    boolean isSignificantlyNewer = timeDelta > INTERVAL;
	    boolean isSignificantlyOlder = timeDelta < -INTERVAL;
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
	
	@Override
	public void onLocationChanged(Location location) {			
		if(isBetterLocation(location, currentBestLocation)){
			currentBestLocation = location;
			LocationCheckingTask.start(getApplicationContext(), handler, currentBestLocation);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	private static class LocationCheckingTask extends AsyncTask<Void, Void, Void>{

		private Location location;
		private Handler mHanler;
		final private Context mContext;
		
		private static final float DEFAULT_SPEED = 1.4f; //default human walking speed		
		private static final float MIN_DISTANCE = 50f; 
		
		private LocationCheckingTask(Context context, Handler handler, Location location){
			mContext = context;
			mHanler = handler;
			this.location = location;
		}
		
		public static void start(Context context, Handler handler, Location location){
			if(location == null)
				return;
			
			LocationCheckingTask task = new LocationCheckingTask(context, handler, location);
			task.execute(new Void[0]);
		}
		
		private boolean isTooFar(Queue queue){
			if(queue.getLocation() == null || queue.getWaitTime() == 0f)
				return false;
			
			float speed = location.getSpeed() == 0f ? DEFAULT_SPEED : location.getSpeed();
			float distance  = queue.getLocation().distanceTo(location);
			double time = queue.getWaitTime() == 0 ? 60f : queue.getWaitTime();
			
			if(distance < MIN_DISTANCE)
				return false;
						
			return distance / speed > time;
		}
		
		private void buildNotification(String message){
			Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.beepmeringtone);
			
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(mContext)
			        .setSmallIcon(R.drawable.beepmelogogeneral)
			        .setContentTitle("You are too far")
			        .setContentText(message)
			        .setSound(uri)
			        .setAutoCancel(true);
			
			Intent resultIntent = new Intent(mContext, MainActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			
			NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mBuilder.build());
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Queue[] queues = BeepMeApplication.adapter.getAll();
			ArrayList<String> names = new ArrayList<String>();
			for(Queue queue : queues){
				if(isTooFar(queue))
					names.add(queue.getName());		
			}
			
			if(!names.isEmpty()){
				StringBuilder sb = new StringBuilder();
				sb.append("Please get back to queue ");
				for(int i = 0, count = names.size(); i < count; i++)
					sb.append(names.get(i)).append(i == count - 1 ? "!" : ", ");
				
				final String message = sb.toString();
				
				mHanler.post(new Runnable() {						
					@Override
					public void run() {
						Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
					}
				});
				
				buildNotification(message);
			}
				
			return null;
		}
		
		
	}
}
