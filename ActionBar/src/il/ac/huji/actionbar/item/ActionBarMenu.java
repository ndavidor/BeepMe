package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class ActionBarMenu extends LinearLayout implements IActionBarItem {

	protected int category;
	protected int corner;
	protected String title;
	protected int supportWidth;
	protected boolean visible = true;
	protected boolean contextual = false;
	protected int dropdown_anim_top_id;
	protected int dropdown_anim_bottom_id;
	
	private PopupMenu popupMenu;
	
	public ActionBarMenu(Context context) {
		this(context, null);
	}
	
	public ActionBarMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		setClickable(true);
		setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(popupMenu != null)
					popupMenu.show(ActionBarMenu.this);
			}
		});		
		setGravity(Gravity.CENTER);
		setOrientation(LinearLayout.HORIZONTAL);
	}	
	
	public void setContentView(View view){
		removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(params);		
		addView(view);		
	}
	
	public View getContentView(){
		if(getChildCount() > 0)
			return getChildAt(0);
		
		return null;
	}
	
	public void setPopupMenu(PopupMenu popupMenu){
		this.popupMenu = popupMenu;
	}
	
	public PopupMenu getPopupMenu(){
		return popupMenu;
	}
	
	public void setDropdownAnimationStyle(int top_id, int bottom_id){
		dropdown_anim_top_id = top_id;
		dropdown_anim_bottom_id = bottom_id;
	}
	
	public int getItemID(){
		return this.getId();
	}
		
	public int getCategory(){
		return category;
	}
	
	public void setCategory(int value){
		category = value;
	}
	
	public int getCorner(){
		return corner;
	}
	
	public void setCorner(int corner){
		this.corner = corner;
		switch (corner) {
		case CORNER_TOP_RIGHT:
		case CORNER_TOP_LEFT:
			popupMenu.setAnimationStyle(dropdown_anim_top_id);
			break;
		case CORNER_BOTTOM_RIGHT:
		case CORNER_BOTTOM_LEFT:
			popupMenu.setAnimationStyle(dropdown_anim_bottom_id);
			break;	
	}
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
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
