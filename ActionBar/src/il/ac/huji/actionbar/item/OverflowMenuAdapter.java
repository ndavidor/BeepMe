package il.ac.huji.actionbar.item;

import java.util.ArrayList;
import il.ac.huji.actionbar.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverflowMenuAdapter extends BaseAdapter {

	private ArrayList<ActionBarButton> list_item = new ArrayList<ActionBarButton>();
	private Context mContext;
	private LayoutInflater mInflater;
	private int textAppearanceId;
	private int itemHeight;

	public void setLayoutInflater(Context context){
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	
	public void setTextAppearance(int resId){
		textAppearanceId = resId;
	}
	
	public void setItemHeight(int height){
		itemHeight = height;
	}
	
	public ActionBarButton getItemById(int id){
		for(ActionBarButton button : list_item)
			if(button.getId() == id)
				return button;
		
		return null;
	}
	
	public void addItem(ActionBarButton button){
		if(!list_item.contains(button)){	
			list_item.add(button);
			notifyDataSetChanged();
		}
	}
	
	public void removeItem(ActionBarButton button){
		if(list_item.remove(button))
			notifyDataSetChanged();
	}
		
	public void clearAll(){
		list_item.clear();
	}
	
	@Override
	public int getCount() {
		return list_item.size();
	}

	@Override
	public Object getItem(int position) {
		return list_item.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ActionBarButton button = (ActionBarButton)getItem(position);
		
		View v = convertView;
		if(v == null)
			v = mInflater.inflate(R.layout.row_overflow, null);	
		
		if(button != null){			
			ImageView iv = (ImageView)v.findViewById(R.id.row_overflow_iv_icon);
			if(itemHeight > 0){
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemHeight, itemHeight);
				iv.setLayoutParams(params);
			}
			iv.setImageDrawable(button.getDrawable());
			
			TextView tv = (TextView)v.findViewById(R.id.row_overflow_tv_title);
			if(itemHeight > 0){
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
				tv.setLayoutParams(params);
			}
			tv.setText(button.getTitle());
			if(textAppearanceId > 0)
				tv.setTextAppearance(mContext, textAppearanceId);			
		}	
		
		return v;
	}


}
