package il.ac.huji.beepme.db;

import il.ac.huji.beepme.business.UpdateQueueIntentService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class QueueAdapter extends BaseAdapter implements View.OnClickListener {

	private ArrayList<Queue> items = new ArrayList<Queue>();
	private Context mContext;
	private LayoutInflater mInflater;
	private boolean enableSelection = true;
	private boolean loaded = false;
	
	private Handler mHandler;
	
	public interface OnSelectionChangedListener{
		
		public void onSelectionChanged(int index, boolean selected);
	}
	
	public interface OnItemClickListener{
		
		public void onItemClicked(int index);
	}
	
	private WeakReference<OnSelectionChangedListener> listener_selection;
	
	private WeakReference<OnItemClickListener> listener_click;
	
	public QueueAdapter(){
		mHandler = new Handler();
	}
	
	public void setContext(Context context){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	public void loadData(){
		if(mContext != null)
			UpdateQueueIntentService.runIntentInService(mContext, new Intent(UpdateQueueIntentService.ACTION_LOAD_QUEUE));
	}
	
	public void setOnSelectionChangedListener(OnSelectionChangedListener listener){
		if(listener == null)
			this.listener_selection = null;
		else
			this.listener_selection = new WeakReference<OnSelectionChangedListener>(listener);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		if(listener == null)
			this.listener_click = null;
		else
			this.listener_click = new WeakReference<OnItemClickListener>(listener);
	}
	
	public void setEnableSelection(boolean enable){
		if(enableSelection != enable){
			enableSelection = enable;
			notifyDataSetInvalidated();
		}
	}
	
	public int getSelectedCount(){
		int result = 0;
		for(int i = items.size() - 1; i >= 0; i--){
			if(items.get(i).selected)
				result ++;
		}
		
		return result;
	}
		
	public Queue[] getSelected(){
		ArrayList<Queue> list = new ArrayList<Queue>();
		for(int i = items.size() - 1; i >= 0; i--){
			if(items.get(i).selected){
				if(list.isEmpty())
					list.add(items.get(i));
				else
					list.add(0, items.get(i));
			}
		}
		
		if(list.isEmpty())
			return null;
		
		return list.toArray(new Queue[list.size()]);
	}
	
	public Queue[] resetSelected(){
		ArrayList<Queue> list = new ArrayList<Queue>();
		for(int i = items.size() - 1; i >= 0; i--){
			Queue queue = items.get(i);
			if(queue.selected){
				queue.reset();
				
				list.add(queue);
			}
		}
		
		notifyDataSetInvalidated();
		
		if(list.isEmpty())
			return null;
		
		return list.toArray(new Queue[list.size()]);
	}
	
	public Queue[] deleteSelected(){
		ArrayList<Queue> items_new = new ArrayList<Queue>();
		ArrayList<Queue> list = new ArrayList<Queue>();
		for(int i = items.size() - 1; i >= 0; i--){
			Queue queue = items.get(i);
			if(queue.selected)				
				list.add(queue);
			else if(items_new.isEmpty())
				items_new.add(queue);
			else
				items_new.add(0, queue);			
		}
		
		items = items_new;
		notifyDataSetChanged();
		
		if(list.isEmpty())
			return null;
		
		return list.toArray(new Queue[list.size()]);
	}
	
	public Queue[] getAll(){
		if(items.isEmpty())
			return null;
		
		return items.toArray(new Queue[items.size()]);
	}
	
	public void setAllSelected(){
		for(int i = items.size() - 1; i >= 0; i--)
			items.get(i).selected = true;
		
		notifyDataSetInvalidated();
	}
	
	public void clearSelected(){
		for(int i = items.size() - 1; i >= 0; i--)
			items.get(i).selected = false;
		
		notifyDataSetInvalidated();
	}
	
	public Queue getQueue(String name){
		for(int i = items.size() - 1; i >= 0; i--){
			Queue queue = items.get(i);
			if(queue.getName().equals(name))
				return queue;
		}
		
		return null;
	}
	
	public void addQueues(Queue[] queues){
		final ArrayList<Queue> items_new;
		if(queues != null){
			items_new = new ArrayList<Queue>(items);
			for(int i = 0; i < queues.length; i++){
				Queue queue = getQueue(queues[i].getName());
				
				if(queue == null)
					items_new.add(queues[i]);
				else{
					queue.setTotal(queues[i].getTotal())
						.setCurrent(queues[i].getCurrent())
						.setTotalTime(queues[i].getTotalTime())
						.setParseObject(queues[i].getParseObject());
				}
			}
		}
		else
			items_new = null;
		
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				if(items_new != null)
					items = items_new;
				
				loaded = true;
				notifyDataSetChanged();
			}
		});
	}
	
	public void addQueue(final Queue queue){
		int i;
		for(i = items.size() - 1; i >= 0 && items.get(i).getName().compareTo(queue.getName()) >= 0; i--){}		
		final int index = i;
		
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				if(index == items.size() - 1)
					items.add(queue);
				else
					items.add(index + 1, queue);	
				
				notifyDataSetChanged();
			}
		});	
	}
	
	public void removeQueue(Queue queue){
		items.remove(queue);
		notifyDataSetChanged();
	}
	
	public void deleteQueue(String[] names){
		final ArrayList<Queue> items_new  = new ArrayList<Queue>(items);
		
		for(String name : names){
			for(int i = items_new.size() - 1; i >= 0; i--){
				Queue queue = items_new.get(i);
				if(queue.getName().equals(name)){
					items_new.remove(i);
					queue.dispatchDeleted();
				}
			}
		}
		
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				items = items_new;		
				notifyDataSetChanged();
			}
		});
	}
	
	public boolean isEmpty(){
		return items.isEmpty();
	}
	
	@Override
	public int getCount() {
		if(items.isEmpty())
			return 1;
		
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		try{
			return items.get(position);
		}
		catch(Exception ex){}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		Holder holder = null;
		if(v != null)
			holder = (Holder)v.getTag();
		else{
			v = mInflater.inflate(R.layout.row_queue, null);
			holder = new Holder();
			holder.ll_empty = (LinearLayout)v.findViewById(R.id.queue_ll_empty);
			holder.ll_loading = (LinearLayout)v.findViewById(R.id.queue_ll_loading);
			holder.ll_container = (LinearLayout)v.findViewById(R.id.queue_ll_container);
			holder.rl_selected = (RelativeLayout)v.findViewById(R.id.queue_rl_selected);
			holder.cb_selected = (CheckBox)v.findViewById(R.id.queue_cb_selected);
			holder.tv_name = (TextView)v.findViewById(R.id.queue_tv_name);
			holder.tv_time = (TextView)v.findViewById(R.id.queue_tv_avg);
			holder.tv_info = (TextView)v.findViewById(R.id.queue_tv_info);	
			v.setOnClickListener(this);
			holder.cb_selected.setOnClickListener(this);
			holder.cb_selected.setTag(holder);
			v.setTag(holder);
		}
		
		Queue queue = (Queue)getItem(position);
		
		if(queue != null){
			holder.ll_empty.setVisibility(View.GONE);
			holder.ll_loading.setVisibility(View.GONE);
			holder.ll_container.setVisibility(View.VISIBLE);
			
			holder.rl_selected.setVisibility(enableSelection ? View.VISIBLE : View.GONE);
			
			holder.ll_container.setBackgroundColor(mContext.getResources().getColor(queue.selected ? R.color.bg_queue_selected : (position & 1) == 1 ? R.color.bg_queue_normal_even : R.color.bg_queue_normal_odd));		
			holder.tv_name.setText(queue.getName());
			
			String time = String.format(mContext.getResources().getString(R.string.avg_time), queue.getAvgMinuteTime());
			holder.tv_time.setText(time.replace(".0", ""));
			if(queue.getTotal() == 0)
				holder.tv_info.setText("Empty");
			else
				holder.tv_info.setText(Math.min(queue.getCurrent(), queue.getTotal()) + " / " + queue.getTotal());
			
			holder.cb_selected.setChecked(queue.selected);
			holder.position = position;
		}
		else{
			holder.ll_empty.setVisibility(loaded ? View.VISIBLE : View.GONE);
			holder.ll_loading.setVisibility(loaded ? View.GONE : View.VISIBLE);
			holder.ll_container.setVisibility(View.GONE);
		}
		
		return v;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.queue_cb_selected){
			Holder holder = (Holder)v.getTag();
			
			Queue queue = (Queue)getItem(holder.position);
			queue.selected = !queue.selected;
			
			if(listener_selection != null && listener_selection.get() != null)
				listener_selection.get().onSelectionChanged(holder.position, queue.selected);
			
			notifyDataSetInvalidated();
		}
		else{
			Holder holder = (Holder)v.getTag();
			
			if(listener_click != null && listener_click.get() != null)
				listener_click.get().onItemClicked(holder.position);
		}
		
	}	
	
	static class Holder{
		LinearLayout ll_empty;
		LinearLayout ll_loading;
		LinearLayout ll_container;
		RelativeLayout rl_selected;
		CheckBox cb_selected;
		TextView tv_name;
		TextView tv_time;
		TextView tv_info;
		
		int position;
	}
	
}
