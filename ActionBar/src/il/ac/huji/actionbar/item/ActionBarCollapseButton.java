package il.ac.huji.actionbar.item;

import android.content.Context;
import android.util.AttributeSet;

public class ActionBarCollapseButton extends ActionBarButton {

	private ActionBarCollapseItem item;
	
	public ActionBarCollapseButton(Context context) {
		this(context, null);
	}
	
	public ActionBarCollapseButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarCollapseButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setCollapseItem(ActionBarCollapseItem item){
		this.item = item;
	}
	
	public ActionBarCollapseItem getCollapseItem(){
		return item;
	}
}
