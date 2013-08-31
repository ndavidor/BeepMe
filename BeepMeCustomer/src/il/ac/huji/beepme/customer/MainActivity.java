package il.ac.huji.beepme.customer;

import il.ac.huji.beepme.db.Channels;
import il.ac.huji.beepme.db.Customer;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;
import il.ac.huji.beepme.db.QueueUpdateData;
import il.ac.huji.beepme.ui.ConfirmFragment;
import il.ac.huji.beepme.ui.InfoFragment;
import il.ac.huji.beepme.ui.QueueFragment;
import il.ac.huji.beepme.ui.TicketFragment;

import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.FrameLayout;

import com.google.zxing.client.android.CaptureFragment;
import com.google.zxing.client.android.Intents;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;
import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.customer.R;

public class MainActivity extends FragmentActivity implements ActionBarHolder, ActionBarListener, QueueFragment.OnQueueSelectionListener, CaptureFragment.CaptureListener, ConfirmFragment.ConfirmListener, QueueAdapter.OnQueueListener{

	private ActionBarLayout layout_ab;
	private FrameLayout layout_content;
	
	private static final String TAG_QUEUE = "QUEUE";
	private static final String TAG_CAPTURE = "CAPTURE";
	private static final String TAG_TICKET = "TICKET";
	
	private static final int ID_REPLACE = 1;
	private static final int ID_RETRY = 2;
	private static final int ID_INIT = 3;
	
	private static final int EXP_NOTIF = 60 * 30;
	
	
	private Queue memoQueue;
	private Customer memoCustomer;
			
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		layout_ab = (ActionBarLayout)findViewById(R.id.main_abl);
		layout_content = layout_ab.getLayoutContent();
		layout_ab.registerActionBarListener(this);
		layout_ab.setTitle(R.drawable.ic_launcher, "BeepMe", null);
				
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {			
			@Override
			public void onBackStackChanged() {
				int count = getSupportFragmentManager().getBackStackEntryCount();
				layout_ab.setTitleClickable(count > 0);
			}
		});
		
		if(savedInstanceState == null)
			showFragment(getQueueFragment(), TAG_QUEUE, true);	
		
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseAnalytics.trackAppOpened(getIntent());	
		
		BeepMeApplication.adapter.setOnNearTurnListener(this);
	}
		
	protected void onResume(){
		super.onResume();
		
		if(ParseInstallation.getCurrentInstallation().getObjectId() == null)
			initUser();
		else
			BeepMeApplication.adapter.loadData();
	}
	
	protected void onPause(){
		super.onPause();		
	}
	
	protected void onDestroy(){
		super.onDestroy();
		layout_ab.unregisterActionBarListener(this);
		BeepMeApplication.adapter.setOnNearTurnListener(null);		
	}
			
	protected void showFragment(Fragment fragment, String tag, boolean addNew){
		if(!fragment.isVisible()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();	
			
			if(addNew)
				transaction.add(layout_content.getId(), fragment, tag);			
			else{
				transaction.replace(layout_content.getId(), fragment, tag);
				transaction.addToBackStack(null);
			}
			
			transaction.commit();
		}
	}
	
	protected QueueFragment getQueueFragment(){
		QueueFragment fragment = (QueueFragment)getSupportFragmentManager().findFragmentByTag(TAG_QUEUE);
		if(fragment == null)
			fragment = QueueFragment.newInstance();
		return fragment;
	}
	
	protected CaptureFragment getCaptureFragment(){
		CaptureFragment fragment = (CaptureFragment)getSupportFragmentManager().findFragmentByTag(TAG_CAPTURE);
		if(fragment == null)
			fragment = CaptureFragment.newInstance(true, layout_content.getMeasuredWidth(), layout_content.getMeasuredHeight(), false, "Please place a QrCode below the red line!", -1);
		return fragment;
	}
	
	protected TicketFragment getTicketFragment(int index){
		TicketFragment fragment = TicketFragment.newInstance(index);
		return fragment;
	}
	
	protected void showInfoDialog(String title, int iconID, String message, String okText){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
	    ft.add(dialog, InfoFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	protected void showConfirmDialog(int id, String title, int iconID, String message){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
	    ConfirmFragment dialog = ConfirmFragment.newInstance(id, title, iconID, message);
	    dialog.setListener(this);
	    ft.add(dialog, ConfirmFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	private void initUser(){
		InitUserTask task_init = new InitUserTask(this);
		task_init.start();
	}
		
	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		if(id == R.id.ab_bt_add)		
			showFragment(getCaptureFragment(), TAG_CAPTURE, false);		
		else if(id == R.id.ab_bt_title)
			onBackPressed();
	}

	@Override
	public void contextualModeChanged(boolean mode) {
	}
	
	@Override
 	public void onQueueSelected(Queue queue, int index) {
		showFragment(getTicketFragment(index), TAG_TICKET, false);
	}

	@Override
	public ActionBarLayout getActionBarLayout() {
		return layout_ab;
	}

	@Override
	public void codeCaptured(Intent intent) {
		String code = intent.getStringExtra(Intents.Scan.RESULT);
		String format = intent.getStringExtra(Intents.Scan.RESULT_FORMAT);	
		
		if(!format.equals("QR_CODE")){
			showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "This is not a QrCode!", "OK");
			return;
		}
		
		String[] data = code.split(":", 4);
		if(data.length != 4){
			showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "The format is mismatch!", "OK");
			return;
		}
		
		int number = 0;
		String uid = data[1];
		String businessID = data[2];
		String queueName = data[3];
		try{
			number = Integer.parseInt(data[0]);
		}
		catch(Exception ex){
			showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "The format is mismatch!", "OK");
		}
		
		onBackPressed();
		
		Queue queue = BeepMeApplication.adapter.getQueue(businessID, queueName);
		if(queue == null){
			Customer customer = new Customer(queueName, businessID, number, uid);
			queueCustomer(null, customer);
		}
		else{
			memoCustomer = new Customer(queueName, businessID, number, uid);
			showConfirmDialog(ID_REPLACE, "Duplicate Queue", android.R.drawable.ic_dialog_info, "You already take your number. Do you want to replace with this one?");
		}
	}
	
	private void queueCustomer(Queue queue, Customer customer){		
		QueueCustomerTask task_queue = new QueueCustomerTask(this, queue, customer);
		task_queue.start();
	}
		
	@Override
	public void confirm(ConfirmFragment dialog, boolean yes) {
		if(!yes){
			if(dialog.getConfirmId() == ID_INIT)
				finish();
			else
				return;
		}
		
		if(dialog.getConfirmId() == ID_REPLACE){
			Queue queue = BeepMeApplication.adapter.getQueue(memoCustomer.businessID, memoCustomer.queueName);
			queueCustomer(queue, memoCustomer);
			memoCustomer = null;
		}
		else if(dialog.getConfirmId() == ID_RETRY){
			queueCustomer(memoQueue, memoCustomer);
			memoQueue = null;
			memoCustomer = null;
		}
		else if(dialog.getConfirmId() == ID_INIT)
			initUser();		
	}

	@Override
	public void onTimeBelow2Min(Queue queue) {
		buildNotification(this, String.format("%s: You only need to wait %.1f minutes more!", queue.getName(), queue.getWaitMinuteTime()).replace(".0", ""));					
	}
	
	@Override
	public void onNearTurn(Queue queue) {
		buildNotification(this, queue.getName() + ": your turn is near!");
	}
	
	private void buildNotification(Context context, String message){
		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beepmeringtone);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.beepmelogogeneral)
		        .setContentTitle("Your turn is near")
		        .setContentText(message)
		        .setSound(uri)
		        .setAutoCancel(true);
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());
	}
	
	private class QueueCustomerTask extends AsyncTask<Void, Void, String>{

		private Queue queue;
		private Customer customer;
		private Context mContext;
		private ProgressDialog dialog;
		
		public QueueCustomerTask(Context context, Queue queue, Customer customer){
			this.mContext = context;
			this.queue = queue;
			this.customer = customer;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = ProgressDialog.show(mContext, "", "Please wait ...", true, false);
		}
		
		@Override
		protected String doInBackground(Void... params) {			
			if(queue == null){
				ParseQuery<ParseObject> query_queue = ParseQuery.getQuery("Queue");
				try {
					List<ParseObject> list = query_queue.whereEqualTo("businessid", customer.businessID)
						.whereEqualTo("name", customer.queueName)
						.find();
					if(list.isEmpty())
						return "Cannot get information about this queue!";					
					queue = new Queue(list.get(0));
				} catch (ParseException e) {
					return e.getMessage() == null ? "Unknown error" : e.getMessage();
				}
			}
			else{
				Customer old = queue.getCustomer();
				if(old != null)
					old.delete();
			}
			
			try{
				customer.getParseObject().save();
			}
			catch(ParseException ex){
				return ex.getMessage() == null ? "Unknown error" : ex.getMessage();
			}
			
			queue.setCustomer(customer);
			ParseObject object = queue.getParseObject();			
			object.increment("total", 1);
			try{
				object.save();
			}
			catch(ParseException ex){
				object.increment("total", -1);
				return ex.getMessage() == null ? "Unknown error" : ex.getMessage();
			}
			queue.setTotal(queue.getTotal() + 1);
			
			BeepMeApplication.adapter.addQueue(queue);
			
			//push queue update notification				
			QueueUpdateData.build(QueueUpdateData.TYPE_UPDATE, queue.getBusinessID())
				.put(queue.getName())
				.pushNotification(EXP_NOTIF, Channels.getAllChannels(queue.getBusinessID(), queue.getName()));
					
			return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();			
			
			if(result == null)				
				showInfoDialog("Thank you", android.R.drawable.ic_dialog_info, "Please wait to your turn!", "OK");			
			else{
				memoQueue = queue;
				memoCustomer = customer;
				showConfirmDialog(ID_RETRY, "Error", android.R.drawable.ic_dialog_alert, result + "\nDo you want to try again?");		
			}
			
			BeepMeApplication.adapter.loadData();
		}
		
	}

	private class InitUserTask extends AsyncTask<Void, Void, String>{
			
		private Context mContext;
		private ProgressDialog dialog;
		
		public InitUserTask(Context context){
			this.mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = ProgressDialog.show(mContext, "", "Creating user ...", true, false);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			try {
				ParseInstallation.getCurrentInstallation().save();
			} catch (ParseException e) {
				return e.getMessage();
			}
						
			return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
						
			if(result != null)
				showConfirmDialog(ID_INIT, "Cannot create user", android.R.drawable.ic_dialog_alert, result + "\nDo you want to try again?");
			else
				BeepMeApplication.adapter.loadData();
		}
		
	}
		
}
