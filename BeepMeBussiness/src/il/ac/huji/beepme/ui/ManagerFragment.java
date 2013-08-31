package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.business.BeepMeApplication;
import il.ac.huji.beepme.db.Channels;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;
import il.ac.huji.beepme.db.QueueUpdateData;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.business.R;

public class ManagerFragment extends Fragment implements ActionBarListener, QueueAdapter.OnSelectionChangedListener, ConfirmFragment.ConfirmListener, InputFragment.InputListener{

	private ActionBarLayout layout_ab;
	private IActionBarItem[] ab_items;
	
	private ListView lv_queue;
	private QueueAdapter adapter;
	
	private static final int ID_RESET = 1;
	private static final int ID_DELETE = 2;
	private static final int ID_LOGOUT = 3;
	
	private static final int EXP_NOTIF = 60 * 30;
	
	public static ManagerFragment newInstance(){
		ManagerFragment fragment = new ManagerFragment();
		
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_manager, container, false);
		
		lv_queue = (ListView)v.findViewById(R.id.manager_lv);
		adapter = BeepMeApplication.adapter_queue;
		adapter.setEnableSelection(true);
		adapter.setOnSelectionChangedListener(this);
		lv_queue.setAdapter(adapter);
						
		return v;
	}
	
	public void onResume(){
		super.onResume();
		
		if(layout_ab == null){
			if(getActivity() instanceof ActionBarHolder)
				layout_ab = ((ActionBarHolder)getActivity()).getActionBarLayout();
		}
		
		if(layout_ab != null){			
			layout_ab.setTitle(R.drawable.beepmelogogeneral, "Queue Manager", null);
			ab_items = layout_ab.addItemFromXml(R.menu.menu_manager);									
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

	protected void showInfoDialog(String title, int iconID, String message, String okText){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
	    ft.add(dialog, InfoFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	protected void showInputDialog(String title, int iconID, String message){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    InputFragment dialog = InputFragment.newInstance(title, iconID, message);
	    dialog.setListener(this);
	    ft.add(dialog, InputFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	protected void showConfirmDialog(int id, String title, int iconID, String message){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    ConfirmFragment dialog = ConfirmFragment.newInstance(id, title, iconID, message);
	    dialog.setListener(this);
	    ft.add(dialog, ConfirmFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	@Override
	public void actionBarItemClicked(int id, IActionBarItem item) {
		switch (id) {
			case R.id.ab_bt_select_all:
				adapter.setAllSelected();
				break;
			case R.id.ab_bt_reset:
				showConfirmDialog(ID_RESET, "Reset Queue", android.R.drawable.ic_dialog_info, "Do you want to reset selected queues?");
				break;
			case R.id.ab_bt_delete:
				showConfirmDialog(ID_DELETE, "Delete Queue", android.R.drawable.ic_dialog_info, "Do you want to delete selected queues?");
				break;
			case R.id.ab_bt_logout:
				showConfirmDialog(ID_LOGOUT, "Log out", android.R.drawable.ic_dialog_info, "Do you want to log out?");
				break;
			case R.id.ab_bt_add:
				showInputDialog("New Queue", android.R.drawable.ic_dialog_info, "Please enter a name:");
				break;
			case R.id.ab_bt_refresh:
				BeepMeApplication.adapter_queue.loadData();
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
	public void confirm(ConfirmFragment dialog, boolean yes) {
		if(!yes)
			return;
		
		if(dialog.getConfirmId() == ID_RESET){
			ResetQueueTask task = new ResetQueueTask(getActivity());
			task.start();
		}
		else if(dialog.getConfirmId() == ID_DELETE){
			DeleteQueueTask task = new DeleteQueueTask(getActivity());
			task.start();
		}
		else if(dialog.getConfirmId() == ID_LOGOUT)
			getActivity().finish();
	}
		
	/**
	 * Remove all Customer object related with a queue from db and push notification.
	 * @param businessID The businessID of queue.
	 * @param queueName The name of queue.
	 */
	private String removeAllCustomer(String businessID, String queueName){		
		//query and remove all customer object
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Customer");
		query.whereEqualTo("businessid", businessID)
			.whereEqualTo("queuename", queueName)
			.selectKeys(new ArrayList<String>());
		try {
			List<ParseObject> list = query.find();
			for(ParseObject obj : list)
				obj.delete();
		} catch (ParseException e) {
			return e.getMessage() == null ? "Unknown error" : e.getMessage();
		}
		
		//push notification
		QueueUpdateData.build(QueueUpdateData.TYPE_REMOVE_CUSTOMER, businessID)
			.put(queueName)
			.pushNotification(EXP_NOTIF, Channels.getQueueChannel(businessID, queueName));
		
		return null;
	}

	@Override
	public void input(String text) {
		if(!TextUtils.isEmpty(text)){
			if(adapter.getQueue(text) != null){
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "The name already exist!", "OK");
			}
			else{
				Queue queue = new Queue(text, 0, 0, 0, BeepMeApplication.businessID);
				AddQueueTask task = new AddQueueTask(getActivity(), queue);
				task.start();
			}
		}
	}
	
	private class AddQueueTask extends AsyncTask<Void, Integer, String>{

		private Queue queue;
		private Context mContext;
		private ProgressDialog dialog;
		
		public AddQueueTask(Context context, Queue queue){
			this.queue = queue;
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
				queue.getParseObject().save();
			} catch (ParseException e) {
				return e.getMessage() == null ? "Unknown error" : e.getMessage();
			}
			
			//push notification				
			QueueUpdateData.build(QueueUpdateData.TYPE_UPDATE, BeepMeApplication.businessID)
				.put(queue.getName())
				.pushNotification(EXP_NOTIF, BeepMeApplication.businessID);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to add queue!\nMessage: " + result, "OK");
			else
				adapter.addQueue(queue);
		}
		
	}
	
	private class ResetQueueTask extends AsyncTask<Void, Integer, String>{

		private Queue[] queues;
		private Context mContext;
		private ProgressDialog dialog;
		
		public ResetQueueTask(Context context){
			this.queues = adapter.getSelected();
			this.mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);
		}
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Resetting ...");
			dialog.setCancelable(false);
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... value){
			int index = value[0].intValue();
			Queue queue = queues[index];
			queue.setTotal(0)
				.setCurrent(0)
				.setTotalTime(0);
			BeepMeApplication.adapter_queue.notifyDataSetInvalidated();
			dialog.setProgress(index * 100 / queues.length);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			QueueUpdateData data = QueueUpdateData.build(QueueUpdateData.TYPE_UPDATE, BeepMeApplication.businessID);
			
			String error = null;
			for(int i = 0; i < queues.length; i++){
				Queue queue = queues[i];
				
				error = removeAllCustomer(queue.getBussinessID(), queue.getName());
				if(error != null)
					break;
				
				ParseObject object = queue.getParseObject();
				object.put("total", 0);
				object.put("current", 0);
				object.put("totaltime", 0);
				try {
					object.save();
				} catch (ParseException e) {
					error = e.getMessage() == null ? "Unknown error" : e.getMessage();
					break;
				}
				
				data.put(queue.getName());				
				publishProgress(Integer.valueOf(i));
			}
			
			//push notification
			if(data.count() > 0)				
				data.pushNotification(EXP_NOTIF, Channels.getBusinessChannel(BeepMeApplication.businessID));
			
			return error;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to reset queue!\nMessage: " + result, "OK");
		}
		
	}
	
	private class DeleteQueueTask extends AsyncTask<Void, Integer, String>{

		private Queue[] queues;
		private Context mContext;
		private ProgressDialog dialog;
		
		public DeleteQueueTask(Context context){
			this.queues = adapter.getSelected();
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
			Queue queue = queues[index];
			BeepMeApplication.adapter_queue.removeQueue(queue);
			dialog.setProgress(index * 100 / queues.length);
		}
		
		@Override
		protected String doInBackground(Void... params) {			
			QueueUpdateData data = QueueUpdateData.build(QueueUpdateData.TYPE_DELETE, BeepMeApplication.businessID);
			
			String error = null;
			for(int i = 0; i < queues.length; i++){
				Queue queue = queues[i];
				
				error = removeAllCustomer(queue.getBussinessID(), queue.getName());
				if(error != null)
					break;
				
				ParseObject object = queue.getParseObject();
				try {
					object.delete();
				} catch (ParseException e) {
					error = e.getMessage() == null ? "Unknown error" : e.getMessage();
					break;
				}
				
				data.put(queue.getName());				
				publishProgress(Integer.valueOf(i));
			}
			
			//push notification
			if(data.count() > 0)				
				data.pushNotification(EXP_NOTIF, Channels.getBusinessChannel(BeepMeApplication.businessID));
			
			return error;
		}
		
		@Override
		protected void onPostExecute(String result){
			dialog.dismiss();
			
			if(result != null)
				showInfoDialog("Error", android.R.drawable.ic_dialog_alert, "Failed to delete queue!\nMessage: " + result, "OK");
			else if(adapter.isEmpty())
				layout_ab.hideContextualActionBar();
		}
		
	}
}
