package il.ac.huji.actionbar.item;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.AdapterView;

public class ActionBarOverflowButton extends ActionBarButton {

	private DropdownListMenu dropdownMenu;
	private OverflowMenuAdapter menuAdapter;
	private int dropdown_anim_tr_id;
	private int dropdown_anim_tl_id;
	private int dropdown_anim_br_id;
	private int dropdown_anim_bl_id;
	
	public ActionBarOverflowButton(Context context) {
		this(context, null);
	}
	
	public ActionBarOverflowButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarOverflowButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		menuAdapter = new OverflowMenuAdapter();
		menuAdapter.setLayoutInflater(context);
		
		dropdownMenu = new DropdownListMenu(context);
		dropdownMenu.setAdapter(menuAdapter);
	}
	
	public void setDropdownSize(int width, int height){
		dropdownMenu.setSize(width, height);
	}
	
	public void setDropdownItemHeight(int itemHeight){
		menuAdapter.setItemHeight(itemHeight);
	}
	
	public void setDropdownBackgroundDrawable(Drawable drawable){
		dropdownMenu.setBackgroundDrawable(drawable);
	}
	
	public void setDropdownBackgroundResource(int id){
		dropdownMenu.setBackgroundResource(id);
	}
	
	public void setDropdownBackgroundColor(int color){
		dropdownMenu.setBackgroundColor(color);
	}
	
	public void setDropdownDividerResource(int id){
		dropdownMenu.setDividerResource(id);
	}
	
	public void setDropdownSelectorResource(int id){
		dropdownMenu.setSelectorResource(id);
	}
	
	public void setDropdownTextAppearance(int id){
		menuAdapter.setTextAppearance(id);
	}
	
	public void setDropdownAnimationStyle(int top_right_id, int top_left_id, int bottom_right_id, int bottom_left_id){
		dropdown_anim_tr_id = top_right_id;
		dropdown_anim_tl_id = top_left_id;
		dropdown_anim_br_id = bottom_right_id;
		dropdown_anim_bl_id = bottom_left_id;
	}
	
	public void setCorner(int corner){
		super.setCorner(corner);
		switch (corner) {
			case CORNER_TOP_RIGHT:
				dropdownMenu.setAnimationStyle(dropdown_anim_tr_id);
				break;
			case CORNER_TOP_LEFT:
				dropdownMenu.setAnimationStyle(dropdown_anim_tl_id);
				break;
			case CORNER_BOTTOM_RIGHT:
				dropdownMenu.setAnimationStyle(dropdown_anim_br_id);
				break;
			case CORNER_BOTTOM_LEFT:
				dropdownMenu.setAnimationStyle(dropdown_anim_bl_id);
				break;	
		}
	}
			
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		dropdownMenu.setOnItemClickListener(listener);
	}
	
	public int getItemCount(){
		return menuAdapter.getCount();
	}
	
	public ActionBarButton getItem(int index){
		return (ActionBarButton)menuAdapter.getItem(index);
	}
	
	public ActionBarButton getItemById(int id){
		return menuAdapter.getItemById(id);
	}
	
	public void addItem(ActionBarButton button){
		menuAdapter.addItem(button);
	}
	
	public void removeItem(ActionBarButton button){
		menuAdapter.removeItem(button);
	}
	
	public void clearAll(){
		menuAdapter.clearAll();
		menuAdapter.notifyDataSetChanged();
	}
	
	public void showDropdownMenu(){
		dropdownMenu.show(this);
	}
	
	public boolean isVisible(){
		return true;
	}
	
	public void setVisible(boolean visible){
	}
}
