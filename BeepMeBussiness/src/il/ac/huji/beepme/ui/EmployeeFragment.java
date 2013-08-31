package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.business.BeepMeApplication;
import il.ac.huji.beepme.db.Employee;
import il.ac.huji.beepme.db.EmployeeAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.business.R;

public class EmployeeFragment extends Fragment implements ActionBarListener, EmployeeAdapter.OnSelectionChangedListener, EmployeeAdapter.OnButtonClickListener, ConfirmFragment.ConfirmListener, EmployeeAccountFragment.InputListener{

	private ActionBarLayout layout_ab;
	private IActionBarItem[] ab_items;
	
	private ListView lv_employee;
	private EmployeeAdapter adapter;
	
	private static final int ID_DELETE = 1;
	
	public static EmployeeFragment newInstance(){
		EmployeeFragment fragment = new EmployeeFragment();
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_employee, container, false);
		
		lv_employee = (ListView)v.findViewById(R.id.employee_lv);
		
		adapter = BeepMeApplication.adapter_employee;
		adapter.setOnSelectionChangedListener(this);
		adapter.setOnButtonClickListener(this);
		adapter.loadData();
		lv_employee.setAdapter(adapter);
						
		return v;
	}
	
	public void onResume(){
		super.onResume();	
		
		if(layout_ab == null){
			if(getActivity() instanceof ActionBarHolder)
				layout_ab = ((ActionBarHolder)getActivity()).getActionBarLayout();
		}
		
		if(layout_ab != null){			
			layout_ab.setTitle(R.drawable.beepmelogogeneral, "Employee Manager", null);
			ab_items = layout_ab.addItemFromXml(R.menu.menu_manager_employee);									
			layout_ab.registerActionBarListener(this);		
		}
	}
		
	public void onPause(){
		super.onPause();
		
		if(layout_ab != null){	
			layout_ab.removeActionBarItems(ab_items);
			layout_ab.unregisterActionBarListener(this);
			ab_items = null;			
		}
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}
	
	protected void showInfoDialog(String title, int iconID, String message, String okText){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
	    ft.add(dialog, InfoFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	protected void showConfirmDialog(int id, String title, int iconID, String message){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    ConfirmFragment dialog = ConfirmFragment.newInstance(id, title, iconID, message);
	    dialog.setListener(this);
	    ft.add(dialog, ConfirmFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	protected void showEmployeeAccountDialog(String title, boolean isNew, String username){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		EmployeeAccountFragment dialog = EmployeeAccountFragment.newInstance(title, isNew, username);
	    dialog.setListener(this);
	    ft.add(dialog, EmployeeAccountFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		switch (id) {
		case R.id.ab_bt_select_all:
			adapter.setAllSelected();
			break;
		case R.id.ab_bt_delete:
			showConfirmDialog(ID_DELETE, "Delete Employee", android.R.drawable.ic_dialog_info, "Do you want to delete selected employees?");
			break;
		case R.id.ab_bt_add:
			showEmployeeAccountDialog("New account", true, null);
			break;
		case R.id.ab_bt_refresh:
			BeepMeApplication.adapter_employee.loadData();
			break;
	}
	}

	@Override
	public void contextualModeChanged(boolean mode) {
		if(mode == false)
			adapter.clearSelected();
	}

	@Override
	public void onSelectionChanged(int index, boolean selected) {
		if(selected){
			if(!layout_ab.isContextualMode())
				layout_ab.showContextualActionBar();
		}
		else{
			if(adapter.getSelectedCount() == 0)
				layout_ab.hideContextualActionBar();
		}
	}
	
	@Override
	public void onButtonClicked(int index) {
		Employee employee = (Employee)adapter.getItem(index);
		if(employee != null)
			showEmployeeAccountDialog("Change password", false, employee.getUsername());
	}
	
	@Override
	public void confirm(ConfirmFragment dialog, boolean yes) {
		if(!yes)
			return;
		
		if(dialog.getConfirmId() == ID_DELETE){
			DeleteEmployeeTask task = new DeleteEmployeeTask(getActivity(), adapter.getSelected());
			task.start();
		}
	}
	
	@Override
	public void inputDone(String username, String password, boolean isNew) {
		if(isNew){
			Employee employee = adapter.getEmployee(username);
			if(employee != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "This username already exists!", "OK");
			else{
				employee = new Employee(username, password);
				AddEmployeeTask task = new AddEmployeeTask(getActivity(), employee);
				task.start();
			}
		}
		else{
			Employee employee = adapter.getEmployee(username);
			if(employee != null){				
				ChangePasswordTask task = new ChangePasswordTask(getActivity(), employee, password);
				task.start();
			}
		}
	}

	@Override
	public void inputCancel() {		
	}
	
	private class AddEmployeeTask extends AsyncTask<Void, Integer, String>{

		private Employee employee;
		private Context mContext;
		private ProgressDialog dialog;
		
		public AddEmployeeTask(Context context, Employee employee){
			this.employee = employee;
			this.mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Adding ...");
			dialog.setCancelable(false);
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
				
		@Override
		protected String doInBackground(Void... params) {
			try {
				employee.getParseObject().save();
			} catch (ParseException e) {
				return e.getMessage() == null ? "Unknown error" : e.getMessage();
			}			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to add employee!\nMessage: " + result, "OK");
			else
				adapter.addEmployee(employee);
		}
		
	}
	
	private class ChangePasswordTask extends AsyncTask<Void, Integer, String>{

		private Employee employee;
		private String password;
		private Context mContext;
		private ProgressDialog dialog;
		
		public ChangePasswordTask(Context context, Employee employee, String password){
			this.employee = employee;
			this.password = password;
			this.mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Saving ...");
			dialog.setCancelable(false);
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
				
		@Override
		protected String doInBackground(Void... params) {
			try {
				ParseObject object = employee.getParseObject();
				object.put("password", password);
				object.save();
			} catch (ParseException e) {
				return e.getMessage() == null ? "Unknown error" : e.getMessage();
			}		
			
			employee.setPassword(password);
			return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to saving!\nMessage: " + result, "OK");
		}
		
	}
	
	private class DeleteEmployeeTask extends AsyncTask<Void, Integer, String>{

		private Employee[] employees;
		private Context mContext;
		private ProgressDialog dialog;
		
		public DeleteEmployeeTask(Context context, Employee[] employees){
			this.employees = employees;
			this.mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Deleting ...");
			dialog.setCancelable(false);
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... value){
			int index = value[0].intValue();
			Employee employee = employees[index];
			BeepMeApplication.adapter_employee.removeEmployee(employee);
			dialog.setProgress(index * 100 / employees.length);
		}
		
		@Override
		protected String doInBackground(Void... params) {			
			String error = null;
			for(int i = 0; i < employees.length; i++){
				Employee employee = employees[i];
								
				ParseObject object = employee.getParseObject();
				try {
					object.delete();
				} catch (ParseException e) {
					error = e.getMessage() == null ? "Unknown error" : e.getMessage();
					break;
				}
						
				publishProgress(Integer.valueOf(i));
			}
						
			return error;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to delete employee!\nMessage: " + result, "OK");
			else if(adapter.isEmpty())
				layout_ab.hideContextualActionBar();
				
		}
		
	}
	
}
