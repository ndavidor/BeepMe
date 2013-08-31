package il.ac.huji.beepme.business;

import il.ac.huji.beepme.ui.EmployeeFragment;
import il.ac.huji.beepme.ui.ManagerFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.business.R;

public class ManagerActivity extends FragmentActivity implements ActionBarHolder, ActionBarListener {

	private ActionBarLayout layout_ab;
	private FrameLayout layout_content;
	
	private static final String TAG_MANAGER = "MANAGER";
	private static final String TAG_EMPLOYEE = "EMPLOYEE";
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_manager);
		
		layout_ab = (ActionBarLayout)findViewById(R.id.manager_abl);
		layout_content = layout_ab.getLayoutContent();
		layout_ab.registerActionBarListener(this);		
		layout_ab.setTitleClickable(false);
		
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				int count = getSupportFragmentManager().getBackStackEntryCount();
				layout_ab.setTitleClickable(count > 0);
			}
		});
		
		if(savedInstanceState == null)
			showFragment(getManagerFragment(), TAG_MANAGER, true);
		
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
	
	protected ManagerFragment getManagerFragment(){
		ManagerFragment fragment = (ManagerFragment)getSupportFragmentManager().findFragmentByTag(TAG_MANAGER);
		if(fragment == null)
			fragment = ManagerFragment.newInstance();
		return fragment;		
	}
	
	protected EmployeeFragment getEmployeeFragment(){
		EmployeeFragment fragment = (EmployeeFragment)getSupportFragmentManager().findFragmentByTag(TAG_EMPLOYEE);
		if(fragment == null)
			fragment = EmployeeFragment.newInstance();
		return fragment;		
	}

	@Override
	public ActionBarLayout getActionBarLayout() {
		return layout_ab;
	}

	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		if(id == R.id.ab_bt_employee)
			showFragment(getEmployeeFragment(), TAG_EMPLOYEE, false);
		else if(id == R.id.ab_bt_title)
			onBackPressed();
	}

	@Override
	public void contextualModeChanged(boolean mode) {
	}
}
