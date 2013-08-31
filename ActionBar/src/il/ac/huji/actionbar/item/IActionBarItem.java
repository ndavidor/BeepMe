package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;

public interface IActionBarItem {

	public static final int CORNER_TOP_LEFT 		= 0x00000001;
	public static final int CORNER_TOP_RIGHT		= 0x00000002;
	public static final int CORNER_BOTTOM_LEFT		= 0x00000004;
	public static final int CORNER_BOTTOM_RIGHT		= 0x00000008;
	
	public static final int CATEGORY_ALWAYS 			= 0x00000001;
	public static final int CATEGORY_IFROOM 			= 0x00000002;
	public static final int CATEGORY_NEVER 				= 0x00000004;
	
	public int getItemID();
	
	public int getCategory();
	
	public void setCategory(int value);
	
	public int getCorner();
	
	public void setCorner(int corner);
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public int getSupportWidth();
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	public boolean isContextual();
	
	public void setContextual(boolean contextual);
	
	public ActionBar getActionBar();
}
