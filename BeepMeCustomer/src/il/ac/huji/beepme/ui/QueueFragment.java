package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.customer.BeepMeApplication;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;

import java.lang.ref.WeakReference;

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

import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.actionbar.ActionBarLayout.ActionBarListener;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.beepme.customer.R;

public class QueueFragment extends Fragment implements ActionBarListener, QueueAdapter.OnItemClickListener, QueueAdapter.OnSelectionChangedListener, ConfirmFragment.ConfirmListener{

	private ActionBarLayout layout_ab;
	private IActionBarItem[] ab_items;
	
	private ListView lv_queue;
	private QueueAdapter adapter;
		
	private static final int ID_CANCEL = 1;
	
	public interface OnQueueSelectionListener{		
		public void onQueueSelected(Queue queue, int index);
	}
	
	private WeakReference<OnQueueSelectionListener> listener;
		
	public static QueueFragment newInstance(){
		QueueFragment fragment = new QueueFragment();
				
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_queue, container, false);
		
		lv_queue = (ListView)v.findViewById(R.id.queue_lv);
		
		adapter = BeepMeApplication.adapter;
		adapter.setOnItemClickListener(this);
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
			layout_ab.setTitle(R.drawable.beepmelogogeneral, "Your queue", null);
			ab_items = layout_ab.addItemFromXml(R.menu.menu_queue);									
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
		
		if(activity instanceof OnQueueSelectionListener)
			listener = new WeakReference<OnQueueSelectionListener>((OnQueueSelectionListener)activity);
	}
	
	public void onDetach(){
		super.onDetach();
		listener = null;
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
			case R.id.ab_bt_refresh:
				BeepMeApplication.adapter.loadData();
				break;
			case R.id.ab_bt_select_all:
				adapter.setAllSelected();
				break;
			case R.id.ab_bt_delete:
				showConfirmDialog(ID_CANCEL, "Cancel Queue", android.R.drawable.ic_dialog_info, "Do you want to cancel selected queues?");
				break;
		}
	}

	@Override
	public void contextualModeChanged(boolean mode) {
		if(mode == false)
			adapter.clearSelected();
		
	}

	@Override
	public void onSelectionChanged(int index, final boolean selected) {
		getActivity().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				if(selected){
					if(!layout_ab.isContextualMode())
						layout_ab.showContextualActionBar();
				}
				else{
					if(adapter.getSelectedCount() == 0)
						layout_ab.hideContextualActionBar();
				}
			}
		});
	}
	
	@Override
	public void onItemClicked(int index) {
		Queue queue = (Queue)adapter.getItem(index);
		if(listener != null && listener.get() != null)
			listener.get().onQueueSelected(queue, index);
	}

	@Override
	public void confirm(ConfirmFragment dialog, boolean yes) {
		if(!yes)
			return;
		
		if(dialog.getConfirmId() == ID_CANCEL){
			DeleteQueueTask task = new DeleteQueueTask(getActivity());
			task.start();
		}
	}
	
	private class DeleteQueueTask extends AsyncTask<Void, Void, Void>{

		private Context mContext;
		private ProgressDialog dialog;
		
		public DeleteQueueTask(Context context){
			mContext = context;
		}
		
		public void start(){
			this.execute(new Void[0]);			
		}
		
		@Override
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Canceling ...");
			dialog.setCancelable(false);
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			adapter.deleteSelected();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			dialog.dismiss();
			
			if(adapter.isEmpty())
				layout_ab.hideContextualActionBar();
		}
		
	}
	
}
