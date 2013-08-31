package il.ac.huji.beepme.business;

import harmony.java.awt.Color;
import il.ac.huji.beepme.db.Channels;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueUpdateData;
import il.ac.huji.beepme.ui.LoginFragment;
import il.ac.huji.beepme.ui.QrCodeFragment;
import il.ac.huji.beepme.ui.QueueFragment;
import il.ac.huji.beepme.ui.TicketFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.business.R;

public class EntranceActivity extends FragmentActivity implements LocationListener, ActionBarListener, QueueFragment.OnQueueSelectionListener, LoginFragment.LoginListener, QrCodeFragment.QrCodeListener {

	private ActionBarLayout layout_ab;
	private IActionBarItem[] ab_items;
	private FrameLayout layout_content;
	
	private static final String TAG_QUEUE = "QUEUE";
	private static final String TAG_QRCODE = "QRCODE";
	private static final String TAG_TICKET = "TICKET";
	
	private LocationManager locationManager;
	
	private BaseFont font;
	
	private static final int EXP_NOTIF = 60 * 30;
			
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_entrance);
		
		layout_ab = (ActionBarLayout)findViewById(R.id.entrance_abl);
		layout_content = layout_ab.getLayoutContent();
		
		layout_ab.setTitle(R.drawable.beepmelogogeneral, "Entrance Ticket", null);
		layout_ab.setTitleClickable(false);
		layout_ab.registerActionBarListener(this);
		ab_items = layout_ab.addItemFromXml(R.menu.menu_entrance);
				
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				int count = getSupportFragmentManager().getBackStackEntryCount();
				if(count == 0){
					layout_ab.setTitle(R.drawable.beepmelogogeneral, "Entrance Ticket", null);
					ab_items = layout_ab.addItemFromXml(R.menu.menu_entrance);
				}
				else{
					layout_ab.setTitle(R.drawable.beepmelogogeneral, BeepMeApplication.workingQueue.getName(), null);
					layout_ab.removeActionBarItems(ab_items);
				}
			}
		});
		
		if(savedInstanceState == null)
			showFragment(getQueueFragment(), TAG_QUEUE, true);	
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100, this);
		
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		UpdateLocationTask task = new UpdateLocationTask(lastKnownLocation);
		task.start();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		locationManager.removeUpdates(this);
	}
		
	public void onBackPressed(){		
		if(getSupportFragmentManager().getBackStackEntryCount() > 0)
			super.onBackPressed();
		
		//turn off back function
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
			
			try{
				transaction.commit();
			}
			catch(Exception ex){}
		}
	}
	
	protected QueueFragment getQueueFragment(){
		QueueFragment fragment = (QueueFragment)getSupportFragmentManager().findFragmentByTag(TAG_QUEUE);
		if(fragment == null)
			fragment = QueueFragment.newInstance(getString(R.string.select_queue_entrance));
		return fragment;		
	}
	
	protected QrCodeFragment getQrCodeFragment(){
		QrCodeFragment fragment = (QrCodeFragment)getSupportFragmentManager().findFragmentByTag(TAG_QRCODE);
		if(fragment == null)
			fragment = QrCodeFragment.newInstance();
		return fragment;
	}
	
	protected TicketFragment getTicketFragment(String name, int number, String uid){
		TicketFragment fragment = TicketFragment.newInstance(name, number, uid);
		return fragment;
	}
	
	protected void showLoginDialog(){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
	    LoginFragment dialog = LoginFragment.newInstance(true);
	    dialog.setListener(this);
	    ft.add(dialog, LoginFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
		
	private Font getFont(float size, int style, int red, int green, int blue) throws Exception{
		if(font == null){
			InputStream is = getResources().openRawResource(R.raw.roboto_regular);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			font = BaseFont.createFont("roboto_regular.ttf", BaseFont.IDENTITY_H, true, false, buffer, null);
		}
		
		return new Font(font, size, style, new Color(red, green, blue));
	}
	
	private void createPDF(File file, String queueName, int number, String uid){
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            Paragraph paragraph1 = new Paragraph(queueName, getFont(20f, Font.BOLD, 0, 0, 0));
            paragraph1.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph1.setSpacingAfter(10f);             
            document.add(paragraph1);
            
            Paragraph paragraph2 = new Paragraph("Thank you!\nYour number in queues is:", getFont(20f, Font.NORMAL, 0, 0, 0));
            paragraph2.setAlignment(Paragraph.ALIGN_CENTER); 
            document.add(paragraph2);
            
            Paragraph paragraph3 = new Paragraph(String.valueOf(number), getFont(40f, Font.BOLD, 0, 0, 255));
            paragraph3.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph3.setSpacingAfter(10f);  
            document.add(paragraph3);
            
            Paragraph paragraph4 = new Paragraph("Your UserID is:", getFont(20f, Font.NORMAL, 0, 0, 0));
            paragraph4.setAlignment(Paragraph.ALIGN_CENTER); 
            document.add(paragraph4);
            
            Paragraph paragraph5 = new Paragraph(uid, getFont(40f, Font.BOLD, 191, 191, 191));
            paragraph5.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph5.setSpacingAfter(30f);
            document.add(paragraph5);
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beepmelogo);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);            
            Image image = Image.getInstance(stream.toByteArray());
            image.setAlignment(Image.ALIGN_CENTER);
            document.add(image);            
            
        } catch (Exception e) {
//        	e.printStackTrace();
        }
        
        document.close();
    }
	
	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		if(id == R.id.ab_bt_logout)
			showLoginDialog();
		else if(id == R.id.ab_bt_refresh)
			BeepMeApplication.adapter_queue.loadData();
	}

	@Override
	public void contextualModeChanged(boolean mode) {		
	}

	@Override
	public void onQueueSelected(Queue queue, int index) {
		BeepMeApplication.workingQueue = queue;
		showFragment(getQrCodeFragment(), TAG_QRCODE, false);
	}

	@Override
	public void loginSuccess(String username, String password, String station) {
		finish();
	}

	@Override
	public void loginCancel() {
	}

	@Override
	public void printTicket(String name, int number, String uid) {
		File file = new File(android.os.Environment.getExternalStorageDirectory() + java.io.File.separator + "ticket.pdf");
		createPDF(file, name, number, uid);
		showFragment(getTicketFragment(name, number, uid), TAG_TICKET, false);
		
		Intent printIntent = new Intent(this, PrintDialogActivity.class);
		printIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
		printIntent.putExtra("title", "Ticket");
		startActivity(printIntent);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		UpdateLocationTask task = new UpdateLocationTask(location);
		task.start();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
	
	private class UpdateLocationTask extends AsyncTask<Void, Void, Void>{

		private Location location;
		
		public UpdateLocationTask(Location location){
			this.location = location;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Queue[] queues = BeepMeApplication.adapter_queue.getAll();
			if(queues == null)
				return null;
			
			if(!BeepMeApplication.updateLocation(location) || BeepMeApplication.currentBestLocation == null)
				return null;
						
			for(Queue queue : queues){
				ParseObject object = queue.getParseObject();
				ParseGeoPoint point = object.getParseGeoPoint("location");
				if(point == null)
					point = new ParseGeoPoint();
				
				point.setLatitude(BeepMeApplication.currentBestLocation.getLatitude());
				point.setLongitude(BeepMeApplication.currentBestLocation.getLongitude());
				
				object.put("location", point);
								
				try {
					object.save();
				} catch (ParseException e) {
					object.saveEventually();
				}
				
				QueueUpdateData data = new QueueUpdateData(QueueUpdateData.TYPE_UPDATE, BeepMeApplication.businessID);
				data.put(queue.getName())
					.pushNotification(EXP_NOTIF, Channels.getQueueChannel(BeepMeApplication.businessID, queue.getName()));
			}
			
			return null;
		}
		
	}
}
