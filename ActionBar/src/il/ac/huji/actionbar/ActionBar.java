package il.ac.huji.actionbar;

import il.ac.huji.actionbar.item.ActionBarButton;
import il.ac.huji.actionbar.item.ActionBarCollapseItem;
import il.ac.huji.actionbar.item.ActionBarOverflowButton;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.actionbar.item.TitleItem;

import java.util.ArrayList;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import il.ac.huji.actionbar.R;

public class ActionBar extends FrameLayout {

	private ActionBarOverflowButton overflowButton;
	private boolean hasOverflowButton;	
	private boolean adjustMode;
	
	private int barWidth = -1; 
	private int barHeight = -1;
	private IActionBarItem item_collapse;
	private boolean hasIfRoomItem;
		
	private ArrayList<Integer> list_visible = new ArrayList<Integer>();
	private ArrayList<Integer> list_overflow = new ArrayList<Integer>();
	
	private int adjust_width;
		
	public ActionBar(Context context) {
		this(context, null);
	}
	
	public ActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}	
	
	public ActionBarLayout getActionBarLayout(){
		if(getParent() != null && getParent() instanceof ActionBarLayout)
			return (ActionBarLayout)getParent();
		
		return null;
	}
	
	public void setAdjustMode(boolean b){
		this.adjustMode = b;
	}
	
	public boolean hasVisibleItem(){
		return !list_visible.isEmpty();
	}
	
	public void setOverflowButton(ActionBarOverflowButton button, boolean hasOverflowButton){
		overflowButton = button;
		this.hasOverflowButton = hasOverflowButton;
		if(hasOverflowButton)
			addView(overflowButton, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		else
			removeView(overflowButton);
	}
		
	protected void addToOverflowMenu(IActionBarItem item){
		if(item instanceof ActionBarButton && item.isVisible()){
			if(overflowButton != null)
				overflowButton.addItem((ActionBarButton)item);
		}
	}
	
	protected void removeFromOverflowMenu(IActionBarItem item){
		if(item instanceof ActionBarButton){
			if(overflowButton != null)
				overflowButton.removeItem((ActionBarButton)item);
		}
	}
	
	public IActionBarItem getItem(int id){
		for(int i = getChildCount() - 1; i >= 0; i--){
			IActionBarItem item = (IActionBarItem)getChildAt(i);
			if(item.getItemID() == id)
				return item;		
		}						
		return null;
	}
	
	public int indexOfItem(IActionBarItem item){
		return indexOfChild((View)item);
	}
	
	public void addItem(IActionBarItem item){
		if(getItem(item.getItemID()) != null)
			return;
		
		addView((View)item, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}
	
	public void addItem(int index, IActionBarItem item){
		addView((View)item, index, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}
	
	public boolean removeItem(IActionBarItem item){
		for(int i = getChildCount() - 1; i >= 0; i--){
			IActionBarItem temp = (IActionBarItem)getChildAt(i);
			if(temp == item){
				removeViewAt(i);
				removeFromOverflowMenu(item);
				return true;
			}
		}	
		return false;
	}
	
	public boolean removeItem(int id){
		for(int i = getChildCount() - 1; i >= 0; i--){
			IActionBarItem temp = (IActionBarItem)getChildAt(i);
			if(temp.getItemID() == id){
				removeViewAt(i);
				removeFromOverflowMenu(temp);
				return true;
			}
		}	
		return false;
	}
		
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){		
		int left_child = 0;
		int right_child = right - left;
		
		for(Integer index : list_visible){
			View item = getChildAt(index.intValue());
			int itemWidth = 0;
			if(item == item_collapse)
				itemWidth = right_child - left_child;
			else
				itemWidth = ((IActionBarItem)item).getSupportWidth() + adjust_width;
			
			ViewGroup.LayoutParams params = item.getLayoutParams();
			if(params == null)
				params = new ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT);			
			else
				params.width = itemWidth;
			
			item.setLayoutParams(params);
			removeFromOverflowMenu((IActionBarItem)item);

			if(item == item_collapse)
				item.layout(left_child, 0, right_child, barHeight);
			else if(isLeftItem((IActionBarItem)item)){
				item.layout(left_child, 0, left_child + itemWidth, barHeight);
				left_child += itemWidth;
			}
			else{
				item.layout(right_child - itemWidth, 0, right_child, barHeight);
				right_child -= itemWidth;
			}						
		}
				
		for(Integer index : list_overflow){
			View item = getChildAt(index.intValue());
			if(item != overflowButton)
				addToOverflowMenu((IActionBarItem)item);
			item.layout(0, 0, 0, 0);
		}
			
					
	}
	
	private void addToOverflowList(Integer index){
		for(int i = list_overflow.size() - 1; i >= 0; i--){
			if(index.intValue() < list_overflow.get(i).intValue()){
				list_overflow.add(i, index);
				return;
			}
		}
		
		list_overflow.add(index);
	}
		
	public void preLayout(int left, int top, int right, int bottom){
		barWidth = right - left;
		barHeight = bottom - top;
		int remainWidth = barWidth;
		hasIfRoomItem = false;
		item_collapse = null;		
		
		list_visible.clear();
		list_overflow.clear();
		
		for(int i = 0, count = getChildCount(); i < count; i++)
			remainWidth = preLayoutItem(remainWidth, i);	
		
		if(overflowButton != null && hasOverflowButton){
			if(overflowButton.getItemCount() > 0 || !list_overflow.isEmpty()){
				int overflowWidth = overflowButton.getSupportWidth();
				
				if(remainWidth < overflowWidth){
					int extraWidth = overflowWidth;
					ArrayList<Integer> list = new ArrayList<Integer>();	
					
					//looking to remove ifRoom items
					for(int i = list_visible.size() - 1; i >= 0; i--){
						Integer index = list_visible.get(i);
						IActionBarItem item = (IActionBarItem)getChildAt(index.intValue());
						if(item.getCategory() == IActionBarItem.CATEGORY_IFROOM){
							extraWidth -= item.getSupportWidth();
							list.add(index);
							if(extraWidth <= 0)
								break;
						}
					}
					
					//if not enough looking to remove another items
					if(extraWidth > 0){
						for(int i = list_visible.size() - 1; i >= 0; i--){
							Integer index = list_visible.get(i);
							IActionBarItem item = (IActionBarItem)getChildAt(index.intValue());
							if(item == item_collapse){
								break;
							}
							else if(item.getCategory() != IActionBarItem.CATEGORY_IFROOM){
								extraWidth -= item.getSupportWidth();
								list.add(index);
								if(extraWidth <= 0)
									break;				
							}
						}	
					}
					
					//if have enough width for overflow
					if(extraWidth <= 0){
						for(Integer index : list){
							list_visible.remove(index);
							addToOverflowList(index);
						}
						
						remainWidth -= extraWidth;
					}
					
					list.clear();
					list = null;
					
				}
							
				if(list_visible.isEmpty())
					list_visible.add(Integer.valueOf(0));
				else
					list_visible.add(0, Integer.valueOf(0));
			}
			else
				list_overflow.add(Integer.valueOf(0));
		}				
		
		adjust_width = 0;
		if(adjustMode && remainWidth > 0 && !list_visible.isEmpty())		
			adjust_width = remainWidth / list_visible.size();			
	}	
	
	private int preLayoutItem(int remainWidth, int index){
		IActionBarItem item = (IActionBarItem) getChildAt(index);
		
		if(item == overflowButton)
			return remainWidth;
		
		if(!item.isVisible()){
			addToOverflowList(Integer.valueOf(index));
			return remainWidth;
		}
		
		if(item instanceof ActionBarCollapseItem){		
			boolean collapse = ((ActionBarCollapseItem)item).isCollapseMode();
			boolean overlap = ((ActionBarCollapseItem)item).isOverlapMode();
			if(!collapse && remainWidth > 0){				
				if(overlap){					
					for(int i = list_visible.size() - 1; i >= 0; i--){
						Integer temp_id = list_visible.get(i);
						IActionBarItem temp = (IActionBarItem)getChildAt(temp_id.intValue());
						int id = temp.getItemID();
						if(id != R.id.ab_bt_overflow && id != R.id.ab_bt_overflow_contextual && id != R.id.ab_bt_title && id != R.id.ab_bt_done_contextual){							
							list_visible.remove(i);
							addToOverflowList(temp_id);
						}					
					}
				}
				
				item_collapse = item;		
				list_visible.add(Integer.valueOf(index));
				remainWidth = 0;
				return remainWidth;
			}			
		}
			
		if(item instanceof TitleItem){
			((View)item).measure(MeasureSpec.makeMeasureSpec(barWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY));
		}
		
		if(item.getCategory() == IActionBarItem.CATEGORY_NEVER){
			addToOverflowList(Integer.valueOf(index));
			return remainWidth;
		}
		
		int itemWidth = item.getSupportWidth();
		
		if(itemWidth <= remainWidth){
			list_visible.add(Integer.valueOf(index));
			remainWidth -= itemWidth;
			if(item.getCategory() == IActionBarItem.CATEGORY_IFROOM)
				hasIfRoomItem = true;
		}
		else if(item.getCategory() == IActionBarItem.CATEGORY_ALWAYS && hasIfRoomItem){			
			//looking to remove ifRooms item
			int extraWidth = itemWidth;
			ArrayList<Integer> list = new ArrayList<Integer>();	
			hasIfRoomItem = false;
			for(int i = list_visible.size() - 1; i >= 0; i--){
				Integer temp_id = list_visible.get(i);
				IActionBarItem temp = (IActionBarItem)getChildAt(temp_id.intValue());
				if(temp.getCategory() == IActionBarItem.CATEGORY_IFROOM){
					if(extraWidth > 0){
						extraWidth -= temp.getSupportWidth();
						list.add(temp_id);
					}
					else{
						hasIfRoomItem = true;
						break;
					}
				}
			}
			
			//if have enough width for item
			if(extraWidth <= 0){
				for(Integer temp: list){
					list_visible.remove(temp);
					addToOverflowList(temp);
				}
				
				list_visible.add(Integer.valueOf(index));				
				remainWidth -= extraWidth;
			}
			
			list.clear();
			list = null;
		}
		else
			addToOverflowList(Integer.valueOf(index));
		
		return remainWidth;
	}
	
	protected boolean isLeftItem(IActionBarItem item){
		return item.getCorner() == IActionBarItem.CORNER_TOP_LEFT || item.getCorner() == IActionBarItem.CORNER_BOTTOM_LEFT;
	}
	
}