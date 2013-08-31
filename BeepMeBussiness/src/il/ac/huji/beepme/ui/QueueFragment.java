package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.business.BeepMeApplication;
import il.ac.huji.beepme.db.Queue;
import il.ac.huji.beepme.db.QueueAdapter;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class QueueFragment extends Fragment implements QueueAdapter.OnItemClickListener{

	private TextView tv_title;
	private ListView lv_queue;
	private QueueAdapter adapter;
	
	private static final String ARG_TITLE = "TITLE";
	
	public interface OnQueueSelectionListener{		
		public void onQueueSelected(Queue queue, int index);
	}
	
	private WeakReference<OnQueueSelectionListener> listener;
		
	public static QueueFragment newInstance(String title){
		QueueFragment fragment = new QueueFragment();
		
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_queue, container, false);
		
		tv_title = (TextView)v.findViewById(R.id.queue_tv_title);
		lv_queue = (ListView)v.findViewById(R.id.queue_lv);
		
		adapter = BeepMeApplication.adapter_queue;
		adapter.setEnableSelection(false);
		adapter.setOnItemClickListener(this);
		lv_queue.setAdapter(adapter);
						
		tv_title.setText(getArguments().getString(ARG_TITLE));	
		return v;
	}
	
	public void onResume(){
		super.onResume();		
	}
		
	public void onPause(){
		super.onPause();			
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		if(activity instanceof OnQueueSelectionListener)
			listener = new WeakReference<OnQueueSelectionListener>((OnQueueSelectionListener)activity);
	}

	@Override
	public void onItemClicked(int index) {
		Queue queue = (Queue)adapter.getItem(index);
		if(listener != null && listener.get() != null)
			listener.get().onQueueSelected(queue, index);
	}
}
