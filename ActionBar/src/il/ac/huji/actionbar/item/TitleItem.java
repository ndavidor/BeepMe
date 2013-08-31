package il.ac.huji.actionbar.item;

import il.ac.huji.actionbar.ActionBar;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import il.ac.huji.actionbar.R;

public class TitleItem extends LinearLayout implements IActionBarItem {

	private LinearLayout layout_button;
	private LinearLayout layout_title;
	private ImageView iv_button;
	private ImageView iv_icon;
	private TextView tv_title;
	private TextView tv_sub_title;
	private int corner;
	private int animDuration;
	private float hidingPercent;
	
	private WeakReference<OnClickListener> listener;
		
	public TitleItem(Context context) {
		this(context, null);
	}
	
	public TitleItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.item_title, this, true);
		layout_button = (LinearLayout)v.findViewById(R.id.item_title_layout_button);
		layout_title = (LinearLayout)v.findViewById(R.id.item_title_layout_title);
		layout_button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(listener != null && listener.get() != null)
					listener.get().onClick(TitleItem.this);
			}
		});
		iv_button = (ImageView)v.findViewById(R.id.item_title_iv_button);
		iv_icon = (ImageView)v.findViewById(R.id.item_title_iv_icon);
		tv_title = (TextView)v.findViewById(R.id.item_title_tv_title);
		tv_sub_title = (TextView)v.findViewById(R.id.item_title_tv_sub_title);
	}
	
	public void setBackgroundResource(int id){
		layout_button.setBackgroundResource(id);
	}
	
	public void setBackgroundColor(int color){
		layout_button.setBackgroundColor(color);
	}
	
	public void setOnClickListener(OnClickListener listener){
		this.listener = new WeakReference<View.OnClickListener>(listener);
	}
		
	public void setButtonVisibile(boolean visible){
		if(visible)
			iv_button.setVisibility(View.VISIBLE);
		else
			iv_button.setVisibility(View.INVISIBLE);
	}
		
	public void setIconVisibile(boolean visible){
		if(visible)
			iv_icon.setVisibility(View.VISIBLE);
		else
			iv_icon.setVisibility(View.GONE);
	}
		
	public void setTitleText(String title, String subTitle){
		tv_title.setText(title);
		tv_title.setVisibility(title == null ? View.GONE : View.VISIBLE);
		tv_title.setSelected(true);
		
		tv_sub_title.setText(subTitle);
		tv_sub_title.setVisibility(subTitle == null ? View.GONE : View.VISIBLE);
		tv_sub_title.setSelected(true);
	}
	
	public void setAnimDuration(int animDuration){
		this.animDuration = animDuration;
	}
	
	public void setHidingPercent(float percent){
		this.hidingPercent = Math.abs(percent);
	}
	
	public void hideDrawerButton(){
		TranslateAnimation anim = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_SELF, -hidingPercent, 
				TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0);
		anim.setDuration(animDuration);
		anim.setFillAfter(true);
		
		iv_button.clearAnimation();
		iv_button.startAnimation(anim);		
	}
	
	public void showDrawerButton(){
		TranslateAnimation anim = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, -hidingPercent, TranslateAnimation.RELATIVE_TO_SELF, 0f, 
				TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0);
		anim.setDuration(animDuration);
		anim.setFillAfter(true);
		
		iv_button.clearAnimation();
		iv_button.startAnimation(anim);		
	}
	
	public void hideText(){
		if(layout_title.getVisibility() != View.GONE){
			layout_title.setVisibility(View.GONE);
			requestLayout();
		}
	}
	
	public void unhideText(){
		if(layout_title.getVisibility() != View.VISIBLE){
			layout_title.setVisibility(View.VISIBLE);
			requestLayout();
		}
	}
	
	@Override
	public int getCategory() {
		return CATEGORY_ALWAYS;
	}

	@Override
	public void setCategory(int value) {
	}

	public int getItemID(){
		return this.getId();
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
		return tv_title.getText().toString();
	}

	@Override
	public void setTitle(String title) {
	}
	
	public void setButtonResource(int resId){
		iv_button.setImageResource(resId);
	}
	
	public void setIconResource(int resId){
		iv_icon.setImageResource(resId);
	}
	
	public void setIconDrawable(Drawable drawable){
		iv_icon.setImageDrawable(drawable);
	}

	public void setTitleTextAppearance(int id){
		tv_title.setTextAppearance(getContext(), id);
	}
	
	public void setSubTitleTextAppearance(int id){
		tv_sub_title.setTextAppearance(getContext(), id);
	}
	
	public void setTextWidth(int width){
		tv_title.setMaxWidth(width);
		tv_sub_title.setMaxWidth(width);
	}
	
	@Override
	public int getSupportWidth() {
		return getMeasuredWidth();
	}
	
	public void setSupportWidth(int width){
	}
	
	public boolean isVisible(){
		return true;
	}
	
	public void setVisible(boolean visible){		
	}
	
	public boolean isContextual(){
		return false;
	}
	
	public void setContextual(boolean contextual){		
	}
	
	public ActionBar getActionBar(){
		if(getParent() != null && getParent() instanceof ActionBar)
			return (ActionBar)getParent();
		
		return null;
	}
}
