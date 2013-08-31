package il.ac.huji.actionbar.item;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.widget.PopupWindow;

public class PopupMenu extends PopupWindow {

	protected Context mContext;
	protected WindowManager mWindowManager;
	
	public PopupMenu(Context context){
		super(context);
		mContext = context;
						
		setOutsideTouchable(true);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}
			
	public Context getContext(){
		return mContext;
	}
	
	public void show (View anchor) {
		int[] location = new int[2];				
		anchor.getLocationOnScreen(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
			
		View mContentView = getContentView();
		mContentView.forceLayout();
		mContentView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int contentViewWidth = mContentView.getMeasuredWidth();
		int contentViewHeight = mContentView.getMeasuredHeight();
		
		int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
		
		int xPos = 0;
		int yPos = 0;
		
		if(anchorRect.bottom + contentViewHeight < screenHeight)
			yPos = anchorRect.bottom;
		else
			yPos = anchorRect.top - contentViewHeight;
		 
		if(anchorRect.left + contentViewWidth < screenWidth)
			xPos = anchorRect.left;
		else
			xPos = screenWidth - contentViewWidth;
		
		showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
}
