package il.ac.huji.beepme.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class TicketFragment extends Fragment {
	
	public static final String ARG_NAME = "NAME";
	public static final String ARG_NUMBER = "NUMBER";
	public static final String ARG_UID = "UID";
		
	public static TicketFragment newInstance(String name, int number, String uid){
		TicketFragment fragment = new TicketFragment();
				
		Bundle args = new Bundle();
		args.putString(ARG_NAME, name);
		args.putInt(ARG_NUMBER, number);
		args.putString(ARG_UID, uid);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ticket, container, false);

		TextView tv_name = (TextView)v.findViewById(R.id.ticket_tv_queue);
		TextView tv_number = (TextView)v.findViewById(R.id.ticket_tv_number);
		TextView tv_uid = (TextView)v.findViewById(R.id.ticket_tv_uid);
		
		tv_name.setText(getArguments().getString(ARG_NAME));
		tv_number.setText(String.valueOf(getArguments().getInt(ARG_NUMBER)));
		tv_uid.setText(getArguments().getString(ARG_UID));
						
		return v;
	}
	
	public void onResume(){
		super.onResume();	
		
	}
		
	public void onPause(){
		super.onPause();			
	}	
}
