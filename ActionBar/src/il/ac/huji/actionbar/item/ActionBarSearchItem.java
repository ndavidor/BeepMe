package il.ac.huji.actionbar.item;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import il.ac.huji.actionbar.R;

public class ActionBarSearchItem extends ActionBarCollapseItem implements View.OnFocusChangeListener, View.OnKeyListener, AdapterView.OnItemClickListener, View.OnClickListener{

	private Context context;
	private RelativeLayout rl_container;
	private CustomAutoCompleteTextView tv_input;
	private ImageButton bt_clear;
	private ImageButton bt_voice;
	
	public interface SearchListener{
		
		public void startSearch(String info);
		
		public boolean supportVoiceRecognizer();
		
		public void startVoiceRecognizer();
	}
	
	private WeakReference<SearchListener> listener;
	
	public ActionBarSearchItem(Context context) {
		this(context, null);
	}
	
	public ActionBarSearchItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarSearchItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.context = context;
		View v = LayoutInflater.from(context).inflate(R.layout.item_search, null);
		tv_input = (CustomAutoCompleteTextView)v.findViewById(R.id.item_search_tv_input);
		rl_container = (RelativeLayout)v.findViewById(R.id.item_search_rl_container);
		bt_clear = (ImageButton)v.findViewById(R.id.item_search_bt_clear);
		bt_voice = (ImageButton)v.findViewById(R.id.item_search_bt_voice);
		
		tv_input.setOnFocusChangeListener(this);
		tv_input.setOnKeyListener(this);
		tv_input.setOnItemClickListener(this);
				
		bt_clear.setVisibility(View.GONE);
		bt_voice.setVisibility(View.GONE);
		bt_clear.setOnClickListener(this);
		bt_voice.setOnClickListener(this);
		
		setContentView(v);
	}	
	
	private boolean isVoiceSupport(){
		if(listener == null || listener.get() == null)
			return true;
		
		return listener.get().supportVoiceRecognizer();
	}
	
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter){
		tv_input.setAdapter(adapter);
	}
	
	public void setText(String text){
		tv_input.setText(text);
	}
	
	public void setHint(String hint){
		tv_input.setHint(hint);
	}
	
	public void setThreshold(int threshold){
		tv_input.setThreshold(threshold);
	}
	
	public void setDropDownWidth(int width){
		tv_input.setDropDownWidth(width);
	}
	
	public void setDropDownHeight(int height){
		tv_input.setDropDownHeight(height);
	}
	
	public void setDropDownBackgroundDrawable(Drawable d){
		tv_input.setDropDownBackgroundDrawable(d);
	}
	
	public void setDropDownBackgroundResource(int id){
		tv_input.setDropDownBackgroundResource(id);
	}
	
	public void setDropDownHorizontalOffset(int offset){
		tv_input.setDropDownHorizontalOffset(offset);
	}
	
	public void setDropDownVerticalOffset(int offset){
		tv_input.setDropDownVerticalOffset(offset);
	}
	
	public void setButtonBackgroundResource(int id){
		bt_clear.setBackgroundResource(id);
	}
	
	public void setButtonBackgroundDrawable(Drawable d){
		bt_clear.setBackgroundDrawable(d);
	}
	
	public void setButtonBackgroundColor(int color){
		bt_clear.setBackgroundColor(color);
	}
	
	public void setCollapseMode(boolean mode){
		if(collapse != mode){
			this.collapse = mode;
			requestLayout();	
			
			if(listener_collapse != null && listener_collapse.get() != null)
				listener_collapse.get().onCollapseChanged(mode);
			
			if(!collapse){
				requestTextFocus();				
			}
			else{
				tv_input.setText("");
				clearTextFocus();
				bt_clear.setVisibility(View.GONE);
				bt_voice.setVisibility(View.GONE);
			}
		}
	}

	public void sendVoiceRecognitionResult(String result, boolean searchImmediately){
		if(result == null)
			return;
		
		tv_input.setText(result);
		
		if(searchImmediately){
			if(listener != null && listener.get() != null)
				listener.get().startSearch(result);		
			clearTextFocus();
		}
		else
			requestTextFocus();
	}
	
	private void requestTextFocus(){
		tv_input.requestFocus();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);		
	}
	
	private void clearTextFocus(){
		tv_input.clearFocus();
		rl_container.requestFocus();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tv_input.getWindowToken(), 0); 
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		rl_container.setBackgroundResource(hasFocus ? R.drawable.bg_search_input_focused : R.drawable.bg_search_input_normal);
		
		if(hasFocus || TextUtils.isEmpty(tv_input.getText().toString()))
			bt_clear.setVisibility(View.GONE);
		else 
			bt_clear.setVisibility(View.VISIBLE);
		
		if(hasFocus && isVoiceSupport())
			bt_voice.setVisibility(View.VISIBLE);
		else
			bt_voice.setVisibility(View.GONE);		
	}
	
	public void setListener(SearchListener listener){
		if(listener == null)
			this.listener = null;
		else
			this.listener = new WeakReference<SearchListener>(listener);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
			String info = tv_input.getText().toString();
			
			if(!TextUtils.isEmpty(info)){
				if(listener != null && listener.get() != null)
					listener.get().startSearch(info);	
				
				clearTextFocus(); 
			}	
		}
		
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		String info = parentView.getAdapter().getItem(position).toString();
		
		if(!TextUtils.isEmpty(info)){
			if(listener != null && listener.get() != null)
				listener.get().startSearch(info);	
			
			clearTextFocus();			
		}
	}

	@Override
	public void onClick(View v) {
		if(v == bt_clear){
			tv_input.setText("");			
			requestTextFocus();
		}
		else if(v == bt_voice){
			if(tv_input.hasFocus())
				if(listener != null && listener.get() != null)
					listener.get().startVoiceRecognizer();
		}
	}
	
}

class CustomAutoCompleteTextView extends AutoCompleteTextView{

	public CustomAutoCompleteTextView(Context context) {
		super(context);
	}
	
	public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) 
	    	this.clearFocus();	    
	    
	    return super.onKeyPreIme(keyCode, event);
	}
	
}
