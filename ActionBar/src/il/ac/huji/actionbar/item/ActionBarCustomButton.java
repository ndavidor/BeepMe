package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
public class ActionBarCustomButton extends FrameLayout implements IActionBarItem {

	private int category;
	private int corner;
	private String title;
	private View mContentView;
	private int supportWidth;
	private boolean visible = true;
	private boolean contextual = false;
	
	public ActionBarCustomButton(Context context) {
		this(context, null);
	}
	
	public ActionBarCustomButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarCustomButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}	
	
	public void setContentView(View v){
		removeAllViews();
		mContentView = v;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mContentView.setLayoutParams(params);
		addView(mContentView);
	}
	
	public View getContentView(){
		return mContentView;
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
		category = value;
	}

	@Override
	public int getCorner() {
		return corner;
	}

	@Override
	public void setCorner(int corner) {
		this.corner = corner;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int getSupportWidth() {
		return supportWidth;
	}
	
	public void setSupportWidth(int width){
		this.supportWidth = width;
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
