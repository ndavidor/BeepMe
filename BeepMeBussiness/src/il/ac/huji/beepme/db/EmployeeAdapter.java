package il.ac.huji.beepme.db;

import il.ac.huji.beepme.business.UpdateEmployeeIntentService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import il.ac.huji.beepme.business.R;

public class EmployeeAdapter extends BaseAdapter implements View.OnClickListener {

	private ArrayList<Employee> items = new ArrayList<Employee>();
	private Context mContext;
	private LayoutInflater mInflater;
	
	private boolean loaded = false;
	
	public interface OnSelectionChangedListener{
		
		public void onSelectionChanged(int index, boolean selected);
	}
	
	public interface OnButtonClickListener{
		
		public void onButtonClicked(int index);
	}
	
	private WeakReference<OnSelectionChangedListener> listener_selection;
	
	private WeakReference<OnButtonClickListener> listener_click;
	
	public EmployeeAdapter(){
	}
	
	public void setContext(Context context){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	public void loadData(){
		if(mContext != null)
			UpdateEmployeeIntentService.runIntentInService(mContext, new Intent(UpdateEmployeeIntentService.ACTION_LOAD_EMPLOYEE));
	}
	
	public void setOnSelectionChangedListener(OnSelectionChangedListener listener){
		if(listener == null)
			this.listener_selection = null;
		else
			this.listener_selection = new WeakReference<OnSelectionChangedListener>(listener);
	}
	
	public void setOnButtonClickListener(OnButtonClickListener listener){
		if(listener == null)
			this.listener_click = null;
		else
			this.listener_click = new WeakReference<OnButtonClickListener>(listener);
	}
		
	public int getSelectedCount(){
		int result = 0;
		for(int i = items.size() - 1; i >= 0; i--){
			if(items.get(i).selected)
				result ++;
		}
		
		return result;
	}
	
	public Employee[] getSelected(){
		ArrayList<Employee> list = new ArrayList<Employee>();
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
		
		return list.toArray(new Employee[list.size()]);
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
		
	public Employee getEmployee(String username){
		for(int i = items.size() - 1; i >= 0; i--){
			Employee employee = items.get(i);
			if(employee.getUsername().equals(username))
				return employee;
		}
		
		return null;
	}
	
	public void addEmployees(Handler handler, Employee[] employees){
		final ArrayList<Employee> items_new;
		if(employees != null){
			items_new = new ArrayList<Employee>(items);
			for(int i = 0; i < employees.length; i++){
				Employee employee = getEmployee(employees[i].getUsername());
				
				if(employee == null)
					items_new.add(employees[i]);
				else{
					employee.setUsername(employees[i].getUsername())
						.setPassword(employees[i].getPassword())
						.setParseObject(employees[i].getParseObject());
				}
			}
		}
		else
			items_new = null;
		
		handler.post(new Runnable() {			
			@Override
			public void run() {
				if(items_new != null)
					items = items_new;
				
				loaded = true;
				notifyDataSetChanged();
			}
		});
	}
	
	public void addEmployee(Employee employee){
		for(int i = items.size() - 1; i >= 0; i--){
			Employee temp = items.get(i);
			if(temp.getUsername().compareTo(employee.getUsername()) < 0){
				if(i == items.size() - 1)
					items.add(employee);
				else
					items.add(i + 1, employee);
				notifyDataSetChanged();
				return;
			}
		}
		
		if(items.isEmpty())
			items.add(employee);
		else
			items.add(0, employee);
		
		notifyDataSetChanged();
	}
	
	public void removeEmployee(Employee employee){
		items.remove(employee);
		notifyDataSetChanged();
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
			v = mInflater.inflate(R.layout.row_employee, null);
			holder = new Holder();
			holder.ll_empty = (LinearLayout)v.findViewById(R.id.employee_ll_empty);
			holder.ll_loading = (LinearLayout)v.findViewById(R.id.employee_ll_loading);
			holder.ll_container = (LinearLayout)v.findViewById(R.id.employee_ll_container);
			holder.cb_selected = (CheckBox)v.findViewById(R.id.employee_cb_selected);
			holder.tv_name = (TextView)v.findViewById(R.id.employee_tv_name);
			holder.bt_change = (Button)v.findViewById(R.id.employee_bt_change);
			
			holder.bt_change.setOnClickListener(this);
			holder.bt_change.setTag(holder);
			
			holder.cb_selected.setOnClickListener(this);
			holder.cb_selected.setTag(holder);
			
			v.setTag(holder);
		}
		
		Employee employee = (Employee)getItem(position);
		
		if(employee != null){
			holder.ll_empty.setVisibility(View.GONE);
			holder.ll_loading.setVisibility(View.GONE);
			holder.ll_container.setVisibility(View.VISIBLE);
						
			holder.ll_container.setBackgroundColor(mContext.getResources().getColor(employee.selected ? R.color.bg_queue_selected : (position & 1) == 1 ? R.color.bg_queue_normal_even : R.color.bg_queue_normal_odd));		
			holder.tv_name.setText(employee.getUsername());
			
			holder.cb_selected.setChecked(employee.selected);
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
		if(v.getId() == R.id.employee_cb_selected){
			Holder holder = (Holder)v.getTag();
			
			Employee employee = (Employee)getItem(holder.position);
			employee.selected = !employee.selected;
			
			if(listener_selection != null && listener_selection.get() != null)
				listener_selection.get().onSelectionChanged(holder.position, employee.selected);
			
			notifyDataSetInvalidated();
		}
		else if(v.getId() == R.id.employee_bt_change){
			Holder holder = (Holder)v.getTag();
			
			if(listener_click != null && listener_click.get() != null)
				listener_click.get().onButtonClicked(holder.position);
		}
		
	}	
	
	static class Holder{
		LinearLayout ll_empty;
		LinearLayout ll_loading;
		LinearLayout ll_container;
		CheckBox cb_selected;
		TextView tv_name;
		Button bt_change;
		
		int position;
	}
	
}
