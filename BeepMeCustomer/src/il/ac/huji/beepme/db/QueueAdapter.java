package il.ac.huji.beepme.db;

import il.ac.huji.beepme.customer.BeepMeApplication;
import il.ac.huji.beepme.customer.LocationWatcherService;
import il.ac.huji.beepme.customer.MainActivity;
import il.ac.huji.beepme.customer.UpdateQueueIntentService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import il.ac.huji.beepme.customer.R;

public class QueueAdapter extends BaseAdapter implements View.OnClickListener {

	private ArrayList<Queue> items = new ArrayList<Queue>();
	private Context mContext;
	private LayoutInflater mInflater;
	
	private boolean loaded = false;
	
	private Handler mHandler;
	
	public interface OnSelectionChangedListener{
		
		public void onSelectionChanged(int index, boolean selected);
	}
	
	public interface OnItemClickListener{
		
		public void onItemClicked(int index);
	}
		
	public interface OnQueueListener{
		
		public void onTimeBelow2Min(Queue queue);
		
		public void onNearTurn(Queue queue);
		
	}
	
	private WeakReference<OnSelectionChangedListener> listener_selection;
	
	private WeakReference<OnItemClickListener> listener_click;
	
	private WeakReference<OnQueueListener> listener_queue;
	
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
	
	public void setOnNearTurnListener(OnQueueListener listener){
		if(listener == null)
			this.listener_queue = null;
		else
			this.listener_queue = new WeakReference<OnQueueListener>(listener);
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
			if(items.get(i).selected)
				list.add(items.get(i));
		}
		
		if(list.isEmpty())
			return null;
		
		return list.toArray(new Queue[list.size()]);
	}
	
	public Queue[] getAll(){		
		return items.toArray(new Queue[items.size()]);
	}
	
	public void deleteSelected(){
		final ArrayList<Queue> items_new = new ArrayList<Queue>(items);
		for(int i = items_new.size() - 1; i >= 0; i--){
			Queue queue = items_new.get(i);
			
			if(queue.selected){		
				items_new.remove(i);
				
				//delete customer
				queue.getCustomer().delete();
				
				unsubscribeChannel(items_new, queue);				
			}
			else if(items_new.isEmpty())
				items_new.add(queue);
			else
				items_new.add(0, queue);			
		}
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				items = items_new;
				notifyDataSetChanged();
			}
		});		
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
	
	public Queue getQueue(String businessID, String name){
		for(int i = items.size() - 1; i >= 0; i--){
			Queue queue = items.get(i);
			if(queue.getBusinessID().equals(businessID) && queue.getName().equals(name))
				return queue;
		}
		
		return null;
	}
	
	public void addQueues(Queue[] queues, boolean update){
		final ArrayList<Queue> items_new;
		if(queues != null){
			items_new = new ArrayList<Queue>(items);	
			
			boolean[] in_date = null;
			
			if(!update){
				in_date = new boolean[items_new.size()];			
				for(int i = 0; i < in_date.length; i++)
					in_date[i] = false;
			}
			
			
			for(int i = 0; i < queues.length; i++){
				Queue queue = null;
				
				for(int j = items_new.size() - 1; j >= 0; j--){
					Queue temp = items_new.get(j);
					if(temp.getBusinessID().equals(queues[i].getBusinessID()) && temp.getName().equals(queues[i].getName())){
						queue = temp;
						//mark this queue is in date
						if(!update)
							in_date[j] = true;						
						break;
					}
				}
				
				boolean currentChange = false;
				
				//only add queue at first load
				if(queue == null && !loaded){
					if(queues[i].getCustomer() == null)
						continue;
					queue = queues[i];
					
					items_new.add(queue);
					currentChange = true;
					
					if(queue.getCustomer() != null){
						final Customer customer = queue.getCustomer();							
						if(customer.getStation() > 0){
							mHandler.post(new Runnable() {						
								@Override
								public void run() {
									Toast.makeText(mContext, "Your turn at queue: " + customer.queueName + " has come!", Toast.LENGTH_SHORT).show();
								}								
							});	
							
							buildNotification(customer.businessID, customer.queueName, customer.getStation());
						}
					}	
				}
				else{
					currentChange = queue.getCurrent() != queues[i].getCurrent();
					
					queue.setTotal(queues[i].getTotal())
						.setCurrent(queues[i].getCurrent())
						.setTotalTime(queues[i].getTotalTime())
						.setLocation(queues[i].getLocation())
						.setParseObject(queues[i].getParseObject());
					
					if(queues[i].getCustomer() != null){
						final Customer customer = queue.getCustomer();
						customer.setParseObject(queues[i].getCustomer().getParseObject());
						
						final int new_station = queues[i].getCustomer().getStation();						
						if(customer.getStation() != new_station){
							mHandler.post(new Runnable() {						
								@Override
								public void run() {
									if(customer.getStation() < 0)
										Toast.makeText(mContext, "Your turn at queue: " + customer.queueName + " has come!", Toast.LENGTH_SHORT).show();	
									
									customer.setStation(new_station);
									BeepMeApplication.adapter.notifyDataSetInvalidated();
									
									buildNotification(customer.businessID, customer.queueName, new_station);
								}
							});	
						}
					}					
				}				
				
				//subscribe notification channel
				subscribeChannel(queue);
				
				//notify your waitting time below 2 minutes
				if(currentChange && queue.getWaitMinuteTime() < 2f && queue.getYourNumber() < queue.getCurrent()){
					if(listener_queue != null && listener_queue.get() != null)
						listener_queue.get().onTimeBelow2Min(queue);
					
					final String meassge_time = String.format("%s: You only need to wait %.1f minutes more!", queue.getName(), queue.getWaitMinuteTime()).replace(".0", "");					
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if(mContext != null)
								Toast.makeText(mContext, meassge_time, Toast.LENGTH_SHORT).show();
						}
					});			
				}
				
				//notify your turn is near
				if(currentChange && queue.getYourNumber() - queue.getCurrent() == 1){
					if(listener_queue != null && listener_queue.get() != null)
						listener_queue.get().onNearTurn(queue);
					
					final String message_near = queue.getName() + ": your turn is near!";					
					mHandler.post(new Runnable() {			
						@Override
						public void run() {
							if(mContext != null)
								Toast.makeText(mContext, message_near, Toast.LENGTH_SHORT).show();
						}
					});					
				}
					
			}	
			
			//remove any queue is out of date
			if(!update){
				for(int i = in_date.length - 1; i >= 0; i--)
					if(!in_date[i]){
						Queue queue = items_new.remove(i);
						unsubscribeChannel(items_new, queue);
					}
			}
		}
		else if(update)
			items_new = null;
		else
			items_new = new ArrayList<Queue>();
		
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				if(items_new != null)
					items = items_new;
				
				loaded = true;
				notifyDataSetChanged();
				
				//start or stop location checking service if needed
				if(mContext != null){
					Intent t = new Intent(mContext, LocationWatcherService.class);
					if(items.isEmpty())
						mContext.stopService(t);
					else
						mContext.startService(t);			
				}
				
			}
		});
	}
		
	public void addQueue(final Queue queue){
		loaded = true;
		
		//notify your waitting time below 2 minutes
		if(queue.getWaitMinuteTime() < 2f && queue.getYourNumber() < queue.getCurrent()){
			if(listener_queue != null && listener_queue.get() != null)
				listener_queue.get().onTimeBelow2Min(queue);
			
			final String meassge_time = String.format("%s: You only need to wait %.1f minutes more!", queue.getName(), queue.getWaitMinuteTime()).replace(".0", "");					
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mContext != null)
						Toast.makeText(mContext, meassge_time, Toast.LENGTH_SHORT).show();
				}
			});			
		}
		
		//notify your turn is near
		if(queue.getYourNumber() - queue.getCurrent() == 1){
			if(listener_queue != null && listener_queue.get() != null)
				listener_queue.get().onNearTurn(queue);
			
			final String message_near = queue.getName() + ": your turn is near!";					
			mHandler.post(new Runnable() {			
				@Override
				public void run() {
					if(mContext != null)
						Toast.makeText(mContext, message_near, Toast.LENGTH_SHORT).show();
				}
			});		
		}
		
		if(items.contains(queue)){
			mHandler.post(new Runnable() {			
				@Override
				public void run() {					
					notifyDataSetInvalidated();
				}
			});				
			return ;
		}
		
		//subscribe notification channel
		subscribeChannel(queue);
		
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
		
		//start location checking service
		if(mContext != null){
			Intent t = new Intent(mContext, LocationWatcherService.class);
			mContext.startService(t);
		}		
	}
	
	public void removeQueue(final Queue queue){
		if(queue == null)
			return;				
		
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				items.remove(queue);
				notifyDataSetChanged();
				
				//stop location checking service if needed
				if(mContext != null && items.isEmpty()){
					Intent t = new Intent(mContext, LocationWatcherService.class);
					mContext.stopService(t);
				}
			}
		});
		
		unsubscribeChannel(items, queue);
	}
	
	private void subscribeChannel(Queue queue){
		ParseInstallation.getCurrentInstallation().addUnique("channels", Channels.getQueueChannel(queue.getBusinessID(), queue.getName()));
		try {
			ParseInstallation.getCurrentInstallation().save();
		} catch (ParseException e) {
			ParseInstallation.getCurrentInstallation().saveEventually();
		}	
	}
	
	private void unsubscribeChannel(ArrayList<Queue> queues, Queue queue){
		//check if have queue with same businessID		
		for(int i = queues.size() - 1; i >= 0; i --){
			if(queues.get(i).getBusinessID().equals(queue.getBusinessID()) && !queues.get(i).getName().equals(queue.getName())){
				return;
			}
		}
				
		//if not, unsubscribe notification channel
		ParseInstallation.getCurrentInstallation().removeAll("channels", Arrays.asList(Channels.getQueueChannel(queue.getBusinessID(), queue.getName())));
		try {
			ParseInstallation.getCurrentInstallation().save();
		} catch (ParseException e) {
			ParseInstallation.getCurrentInstallation().saveEventually();
		}	
	}
	
	private void buildNotification(String businessID, String queueName, int station){
		Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.beepmeringtone);
				
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(mContext)
		        .setSmallIcon(R.drawable.beepmelogogeneral)
		        .setContentTitle("Your turn is up")
		        .setContentText(queueName + ": Please go to the station " + station)
		        .setSound(uri)
		        .setAutoCancel(true)
		        .setVibrate(new long[]{0, 1000, 250, 1000, 250, 1000, 250, 1000, 250, 1000});
		
		Intent resultIntent = new Intent(mContext, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
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
			holder.rl_content = (RelativeLayout)v.findViewById(R.id.queue_rl_content);
			holder.cb_selected = (CheckBox)v.findViewById(R.id.queue_cb_selected);
			holder.tv_name = (TextView)v.findViewById(R.id.queue_tv_name);
			holder.tv_avg = (TextView)v.findViewById(R.id.queue_tv_avg);
			holder.tv_left = (TextView)v.findViewById(R.id.queue_tv_left);
			holder.tv_number = (TextView)v.findViewById(R.id.queue_tv_number);
			holder.tv_info = (TextView)v.findViewById(R.id.queue_tv_info);	
			holder.rl_content.setOnClickListener(this);
			holder.cb_selected.setOnClickListener(this);
			holder.cb_selected.setTag(holder);
			holder.rl_content.setTag(holder);
			v.setTag(holder);
		}
		
		Queue queue = (Queue)getItem(position);
		
		if(queue != null){
			holder.ll_empty.setVisibility(View.GONE);
			holder.ll_loading.setVisibility(View.GONE);
			holder.ll_container.setVisibility(View.VISIBLE);
			
			holder.ll_container.setBackgroundColor(mContext.getResources().getColor(queue.selected ? R.color.bg_queue_selected : (position & 1) == 1 ? R.color.bg_queue_normal_even : R.color.bg_queue_normal_odd));		
			holder.tv_name.setText(queue.getName());
			String time_avg = String.format(mContext.getResources().getString(R.string.avg_time), queue.getAvgMinuteTime());
			holder.tv_avg.setText(time_avg.replace(".0", ""));
			holder.tv_number.setText(String.format(mContext.getResources().getString(R.string.your_number), queue.getYourNumber()));
					
			if(queue.getCustomer().getStation() < 0){
				String time_left = String.format(mContext.getResources().getString(R.string.left_time), queue.getWaitMinuteTime());
				holder.tv_left.setText(time_left.replace(".0", ""));
				if(queue.getTotal() == 0)
					holder.tv_info.setText("Empty");
				else
					holder.tv_info.setText(Math.min(queue.getCurrent(), queue.getTotal()) + " / " + queue.getTotal());
			}
			else{
				String time_left = String.format(mContext.getResources().getString(R.string.left_time), 0f);
				holder.tv_left.setText(time_left.replace(".0", ""));
				holder.tv_info.setText(String.format(mContext.getResources().getString(R.string.your_turn), queue.getCustomer().getStation()));			
			}
			
			
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
		RelativeLayout rl_content;
		CheckBox cb_selected;
		TextView tv_name;
		TextView tv_avg;
		TextView tv_left;
		TextView tv_number;
		TextView tv_info;
		
		int position;
	}
	
}
