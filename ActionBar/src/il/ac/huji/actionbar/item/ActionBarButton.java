package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ActionBarButton extends ImageButton implements IActionBarItem {

	private int category;
	private int corner;
	private String title;
	private int supportWidth;
	private boolean visible = true;
	private boolean contextual = false;
	
	public ActionBarButton(Context context) {
		this(context, null);
	}
	
	public ActionBarButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.CENTER);
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
