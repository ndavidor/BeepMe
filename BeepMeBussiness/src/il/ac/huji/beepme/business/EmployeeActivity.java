package il.ac.huji.beepme.business;

import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.ui.ConfirmFragment;
import il.ac.huji.beepme.ui.QueueFragment;
import il.ac.huji.beepme.ui.StationFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.business.R;

public class EmployeeActivity extends FragmentActivity implements ActionBarListener, ConfirmFragment.ConfirmListener, QueueFragment.OnQueueSelectionListener {

	private ActionBarLayout layout_ab;
	private IActionBarItem[] ab_items;
	private FrameLayout layout_content;
	
	private static final String TAG_QUEUE = "QUEUE";
	private static final String TAG_STATION = "STATION";
	
	private static final int ID_LOGOUT = 1;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_employee);
		
		layout_ab = (ActionBarLayout)findViewById(R.id.employee_abl);
		layout_content = layout_ab.getLayoutContent();
		
		layout_ab.setTitle(R.drawable.beepmelogogeneral, "Station: " + BeepMeApplication.station, null);
		layout_ab.registerActionBarListener(this);
		ab_items = layout_ab.addItemFromXml(R.menu.menu_employee);
				
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				int count = getSupportFragmentManager().getBackStackEntryCount();
				layout_ab.setTitleClickable(count > 0);
				if(count == 0){
					layout_ab.setTitle(R.drawable.beepmelogogeneral, "Station: " + BeepMeApplication.station, null);
					ab_items = layout_ab.addItemFromXml(R.menu.menu_employee);
				}
				else{
					layout_ab.setTitle(R.drawable.beepmelogogeneral, BeepMeApplication.workingQueue.getName(), "Station: " + BeepMeApplication.station);
					layout_ab.removeActionBarItems(ab_items);
				}
			}
		});
		
		if(savedInstanceState == null)
			showFragment(getQueueFragment(), TAG_QUEUE, true);	
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
			fragment = QueueFragment.newInstance(getString(R.string.select_queue));
		return fragment;		
	}
	
	protected StationFragment getStationFragment(){
		StationFragment fragment = (StationFragment)getSupportFragmentManager().findFragmentByTag(TAG_STATION);
		if(fragment == null)
			fragment = StationFragment.newInstance();
		return fragment;
	}

	protected void showConfirmDialog(int id, String title, int iconID, String message){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
	    ConfirmFragment dialog = ConfirmFragment.newInstance(id, title, iconID, message);
	    dialog.setListener(this);
	    ft.add(dialog, ConfirmFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	@Override
	public void confirm(ConfirmFragment dialog, boolean yes) {
		if(!yes)
			return;
		
		if(dialog.getConfirmId() == ID_LOGOUT)
			finish();		
	}
	
	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		if(id == R.id.ab_bt_logout)
			showConfirmDialog(ID_LOGOUT, "Log out", android.R.drawable.ic_dialog_info, "Do you want to log out?");
		else if(id == R.id.ab_bt_title)
			onBackPressed();
		else if(id == R.id.ab_bt_refresh)
			BeepMeApplication.adapter_queue.loadData();
	}

	@Override
	public void contextualModeChanged(boolean mode) {
	}

	@Override
	public void onQueueSelected(Queue queue, int index) {
		if(queue.getCurrent() < queue.getTotal()){
			BeepMeApplication.workingQueue = queue;
			showFragment(getStationFragment(), TAG_STATION, false);
		}
	}

}
