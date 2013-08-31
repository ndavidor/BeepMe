package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.customer.BeepMeApplication;
import il.ac.huji.beepme.db.Customer;
import il.ac.huji.beepme.db.Queue;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import il.ac.huji.actionbar.ActionBarHolder;
import il.ac.huji.actionbar.ActionBarLayout;
import il.ac.huji.beepme.customer.R;

public class TicketFragment extends Fragment implements Queue.OnQueueStatusChangedListener, Customer.OnCustomerStationChangedListener{

	private ActionBarLayout layout_ab;
	
	private LinearLayout ll_waitting;
	private LinearLayout ll_up;
	
	private TextView tv_number;
	private TextView tv_cur;
	private TextView tv_info;
		
	private TextView tv_station;
	private TextView tv_uid;
	
	private int index;
	private Queue queue;
	private Customer customer;
	
	private Handler mHandler;
	
	public static final String ARG_INDEX = "INDEX";
		
	public static TicketFragment newInstance(int index){
		TicketFragment fragment = new TicketFragment();
				
		Bundle args = new Bundle();
		args.putInt(ARG_INDEX, index);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ticket, container, false);

		ll_waitting = (LinearLayout)v.findViewById(R.id.ticket_ll_waitting);
		ll_up = (LinearLayout)v.findViewById(R.id.ticket_ll_up);
		
		tv_number = (TextView)v.findViewById(R.id.ticket_tv_number);
		tv_cur = (TextView)v.findViewById(R.id.ticket_tv_cur);
		tv_info = (TextView)v.findViewById(R.id.ticket_tv_info);		
		
		tv_station = (TextView)v.findViewById(R.id.ticket_tv_station);
		tv_uid = (TextView)v.findViewById(R.id.ticket_tv_uid);
		
		index = getArguments().getInt(ARG_INDEX);
						
		mHandler = new Handler();
		
		return v;
	}
	
	public void onResume(){
		super.onResume();	
		
		if(layout_ab == null){
			if(getActivity() instanceof ActionBarHolder)
				layout_ab = ((ActionBarHolder)getActivity()).getActionBarLayout();
		}
		
		queue = (Queue)BeepMeApplication.adapter.getItem(index);
		queue.addOnQueueStatusChangedListener(this);
		customer = queue.getCustomer();
		customer.addOnCustomerStationChangedListener(this);
		
		if(customer.getStation() < 0){
			ll_up.setVisibility(View.GONE);
			ll_waitting.setVisibility(View.VISIBLE);
			
			tv_number.setText(String.valueOf(customer.number));
			tv_cur.setText(String.valueOf(queue.getCurrent()));
			setInfoText(customer.number - queue.getCurrent(), queue.getAvgMinuteTime() * (customer.number - queue.getCurrent()));
		}
		else{
			ll_up.setVisibility(View.VISIBLE);
			ll_waitting.setVisibility(View.GONE);
			
			tv_station.setText(String.valueOf(customer.getStation()));
			tv_uid.setText(String.valueOf(customer.uid));
		}
		
		if(layout_ab != null)	
			layout_ab.setTitle(R.drawable.beepmelogogeneral, queue.getName(), null);		
	}
		
	public void onPause(){
		super.onPause();
		
		queue.removeOnQueueStatusChangedListener(this);
		customer.removeOnCustomerStationChangedListener(this);
	}

	@Override
	public void onCustomerStationChanged(final int station) {
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				if(station > 0){
					ll_up.setVisibility(View.VISIBLE);
					ll_waitting.setVisibility(View.GONE);
					
					tv_station.setText(String.valueOf(customer.getStation()));
					tv_uid.setText(String.valueOf(customer.uid));
				}
			}
		});
	}

	@Override
	public void onQueueTotalChanged(int total) {
	}

	@Override
	public void onQueueCurrentChanged(final int current) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(ll_waitting.getVisibility() == View.VISIBLE){	
					tv_cur.setText(String.valueOf(current));
					setInfoText(customer.number - current, queue.getWaitMinuteTime());
				}
			}
		});
		
	}

	@Override
	public void onQueueTotalTimeChanged(long time) {
		mHandler.post(new Runnable() {			
			@Override
			public void run() {
				if(ll_waitting.getVisibility() == View.VISIBLE)	
					setInfoText(customer.number - queue.getCurrent(), queue.getWaitMinuteTime());
			}
		});		
	}	
	
	private void setInfoText(int number, double time){		
		String temp = String.format("There are %s people before you.\nYou have about %.1f minutes left.", number, time).replace(".0", "");
		SpannableString text = new SpannableString(temp);  
		int start = temp.indexOf("There are ") + 10;
		int end = temp.indexOf("people before you");
		text.setSpan(new ForegroundColorSpan(0xFFFF00FF), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);  
		start = temp.indexOf("You have about ") + 15;
		end = temp.indexOf("minutes left");
		text.setSpan(new ForegroundColorSpan(0xFFFF00FF), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_info.setText(text, BufferType.SPANNABLE);
	}
	
}
