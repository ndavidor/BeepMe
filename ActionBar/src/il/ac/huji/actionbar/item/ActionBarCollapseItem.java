package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;
import il.ac.huji.actionbar.item.ActionBarSearchItem.SearchListener;

import java.lang.ref.WeakReference;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ActionBarCollapseItem extends FrameLayout implements IActionBarItem {

	protected int category;
	protected int corner;
	protected String title;
	protected View mContentView;
	protected ActionBarCollapseButton mButton; 
	protected boolean collapse = true;
	protected boolean overlap = false;
	protected boolean onTop = true;
	protected int memoIndex;
	protected boolean visible = true;
	protected boolean contextual = false;	
	
	public interface OnCollapseChangedListener{		
		public void onCollapseChanged(boolean collapse);
	}
	
	protected WeakReference<OnCollapseChangedListener> listener_collapse;
	
	public ActionBarCollapseItem(Context context) {
		this(context, null);
	}
	
	public ActionBarCollapseItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarCollapseItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}	
	
	public void setOnCollapseChangedListener(OnCollapseChangedListener listener){
		if(listener == null)
			listener_collapse = null;
		else
			listener_collapse = new WeakReference<OnCollapseChangedListener>(listener);
	}
	
	public void setContentView(View v){
		if(mContentView != null)
			removeView(mContentView);
		
		mContentView = v;
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mContentView, params);
	}
	
	public void setButton(ActionBarCollapseButton button){	
		if(mButton != null)
			removeView(mButton);
		
		mButton = button;
		mButton.setCorner(corner);
		mButton.setCategory(category);
		mButton.setTitle(title);
		mButton.setCollapseItem(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(button.getSupportWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mButton, params);
	}
	
	public ActionBarCollapseButton getButton(){
		return mButton;
	}
	
	public View getContentView(){
		return mContentView;
	}
	
	public boolean isCollapseMode(){
		return collapse;
	}
	
	public void setCollapseMode(boolean mode){
		if(collapse != mode){
			this.collapse = mode;
			requestLayout();
			
			if(listener_collapse != null && listener_collapse.get() != null)
				listener_collapse.get().onCollapseChanged(mode);
		}
	}
	
	public boolean isOverlapMode(){
		return overlap;
	}
	
	public void setOverlapMode(boolean mode){
		this.overlap = mode;
	}
	
	public boolean isOnTop(){
		return onTop;
	}
	
	public void setOnTop(boolean b){
		onTop = b;
	}
	
	public int getMenoIndex(){
		return memoIndex;
	}
	
	public void setMemoIndex(int index){
		memoIndex = index;
	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
		int content_left = 0;
		int content_right = right - left;
		int content_top = 0;
		int content_bottom = bottom - top;
		
		if(collapse){
			mButton.layout(content_left, content_top, content_right, content_bottom);
			mContentView.layout(0, 0, 0, 0);
		}
		else{
			ViewGroup.LayoutParams params = mContentView.getLayoutParams();
			if(params.width != content_right){
				params.width = content_right;
				mContentView.measure(MeasureSpec.makeMeasureSpec(content_right, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(content_bottom, MeasureSpec.EXACTLY));
			}
			
			mContentView.layout(content_left, content_top, content_right, content_bottom);
			mButton.layout(0, 0, 0, 0);			
		}
	}
	
	public int getItemID(){
		return this.getId();
	}
	
	@Override
	public int getCategory() {
		return category;
	}

	@Override
	public void setCategory(int value) {
		this.category = value;
		mButton.setCategory(category);
	}

	@Override
	public int getCorner() {
		return corner;
	}

	@Override
	public void setCorner(int corner) {
		this.corner = corner;
		mButton.setCorner(corner);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		mButton.setTitle(title);
	}
	
	@Override
	public int getSupportWidth() {
		if(collapse)
			return mButton.getSupportWidth();
		else
			return getMeasuredWidth();
	}
	
	public void setSupportWidth(int width){
	}

	public boolean isVisible(){
		return visible;
	}
	
	public void setVisible(boolean visible){
		if(this.visible != visible){
			this.visible = visible;
			requestLayout();
		}
	}
	
	public boolean isContextual(){
		return contextual;
	}
	
	public void setContextual(boolean contextual){
		this.contextual = contextual;
	}
		
	public ActionBar getActionBar(){
		if(getParent() != null && getParent() instanceof ActionBar)
			return (ActionBar)getParent();
		
		return null;
	}
}
