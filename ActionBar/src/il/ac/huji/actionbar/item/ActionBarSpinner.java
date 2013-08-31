package il.ac.huji.actionbar.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

public class ActionBarSpinner extends ActionBarMenu {

	private int selectedIndex = -1;
	private BaseAdapter adapter;
	private DropdownListMenu menu;
	
	public interface OnSelectedItemChangedListener{
		
		public void selectedItemChanged(int selected);
	}
	
	private OnSelectedItemChangedListener listener;
	
	public ActionBarSpinner(Context context) {
		this(context, null);
	}
	
	public ActionBarSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		menu = new DropdownListMenu(context);
		setPopupMenu(menu);
	}
	
	public void setOnSelectedItemChanged(OnSelectedItemChangedListener listener){
		this.listener = listener;
	}
	
	public void setAdapter(BaseAdapter adapter){
		this.adapter = adapter;
		menu.setAdapter(adapter);		
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setSelectedIndex(position);
				if(listener != null)
					listener.selectedItemChanged(position);
			}
		});
		setSupportWidth(calcPreferredWidth());
		setSelectedIndex(0);		
	}
		
	public int getSelectedIndex(){
		return selectedIndex;
	}
	
	public Object getSelectedItem(){
		return adapter == null ? null : adapter.getItem(selectedIndex);
	}
	
	public void setSelectedIndex(int index){
		if(index >= 0 && index < adapter.getCount()){
			selectedIndex = index;
			if(getContentView() == null){
				View v = adapter.getView(selectedIndex, null, null);
				setContentView(v);
			}
			else
				adapter.getView(selectedIndex, getContentView(), null);
		}
	}

	public void setDropdownSize(int width, int height){
		menu.setSize(width, height);
	}
	
	public void setDropdownBackgroundColor(int color){
		menu.setBackgroundColor(color);
	}
	
	public void setDropdownBackgroundResource(int id){
		menu.setBackgroundResource(id);
	}
	
	public void setDropdownDividerResource(int id){
		menu.setDividerResource(id);
	}	
	
	public void setDropdownSelectorResource(int id){
		menu.setSelectorResource(id);
	}
	
	private int calcPreferredWidth(){		
		if(adapter == null)
			return 0;
		
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int result = 0;
		for(int i = 0, count = adapter.getCount(); i < count; i++){
			setContentView(adapter.getView(i, getContentView(), null));
			this.measure(spec, spec);
			result = Math.max(result, getMeasuredWidth());
		}
		
		return result;
	}
}
