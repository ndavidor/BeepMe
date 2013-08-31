package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.R;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class DropdownListMenu extends PopupMenu {

	protected ScrollView sv_container;
	protected LinearLayout layout_content;
	protected int dividerId;
	protected int selectorId;
	protected BaseAdapter adapter;
	
	private AdapterView.OnItemClickListener listener;
	
	protected View.OnClickListener listener_item = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			itemClicked(v);
		}
	};
	
	public DropdownListMenu(Context context){
		super(context);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mContentView = inflater.inflate(R.layout.menu_dropdown, null);
		sv_container = (ScrollView)mContentView.findViewById(R.id.menu_dropdown_sv_container);
		layout_content = (LinearLayout)mContentView.findViewById(R.id.menu_dropdown_layout_content);
				
		setContentView(mContentView);
		setOutsideTouchable(true);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}
	
	public void setSize(int width, int height){
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
		sv_container.setLayoutParams(params);
	}
	
	public void setBackgroundColor(int color){
		getContentView().setBackgroundColor(color);
	}
	
	public void setBackgroundResource(int id){
		getContentView().setBackgroundResource(id);
	}
	
	public void setDividerResource(int id){
		dividerId = id;
	}	
	
	public void setSelectorResource(int id){
		selectorId = id;
	}
	
	public void setAdapter(BaseAdapter adapter){
		this.adapter = adapter;
		adapter.registerDataSetObserver(new DataSetObserver() {
			
			public void onChanged() {
				apdaterChanged();
		    }

		    public void onInvalidated() {
		    	adapterInvalidated();
		    }
		});
		layoutContent();
	}
	
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		this.listener = listener;
	}
	
	protected void itemClicked(View v){
		int index = layout_content.indexOfChild(v);
		if(dividerId > 0){
			if(listener != null)
				listener.onItemClick(null, v, index / 2, 0);
		}
		else{
			if(listener != null)
				listener.onItemClick(null, v, index, 0);
		}
		dismiss();
	}
	
	protected void layoutContent(){
		layout_content.removeAllViews();
		if(adapter == null)
			return;
				
		for(int i = 0, count = adapter.getCount(); i < count; i++){
			View v = adapter.getView(i, null, null);
			if(v == null)
				continue;
			
			v.setOnClickListener(listener_item);
			if(selectorId > 0){
				v.setBackgroundResource(selectorId);
				v.setClickable(true);
			}
			layout_content.addView(v);
			
			if(dividerId > 0 && i != count - 1){
				View divider = new View(mContext);
				divider.setBackgroundResource(dividerId);
				layout_content.addView(divider);
			}
		}
	}
	
	protected void apdaterChanged(){
		layoutContent();
	}
	
	protected void adapterInvalidated(){
		layoutContent();
	}
	
}
