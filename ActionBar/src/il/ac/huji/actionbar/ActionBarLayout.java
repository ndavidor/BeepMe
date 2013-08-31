package il.ac.huji.actionbar;

import il.ac.huji.actionbar.item.ActionBarButton;
import il.ac.huji.actionbar.item.ActionBarCollapseButton;
import il.ac.huji.actionbar.item.ActionBarCollapseItem;
import il.ac.huji.actionbar.item.ActionBarCustomButton;
import il.ac.huji.actionbar.item.ActionBarOverflowButton;
import il.ac.huji.actionbar.item.ActionBarSearchItem;
import il.ac.huji.actionbar.item.ActionBarSpinner;
import il.ac.huji.actionbar.item.IActionBarItem;
import il.ac.huji.actionbar.item.TitleItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import il.ac.huji.actionbar.R;

public class ActionBarLayout extends FrameLayout {
			
	private ActionBar ab_top;
	private int ab_top_height;
	private int ab_top_bgColor;
	private int ab_top_bgId;
	private boolean ab_top_overlay;
	private boolean ab_top_adjust;
	
	private ActionBar ab_top_contextual;
	private int ab_top_contextual_bgColor;
	private int ab_top_contextual_bgId;
	
	private ActionBar ab_bottom;	
	private int ab_bottom_height;
	private int ab_bottom_bgColor;
	private int ab_bottom_bgId;
	private boolean ab_bottom_overlay;
	private boolean ab_bottom_adjust;
	
	private ActionBar ab_bottom_contextual;
	private int ab_bottom_contextual_bgColor;
	private int ab_bottom_contextual_bgId;
	
	private FrameLayout ab_content;
	
	private int ab_animDuration = 400;
	
	private int actionButtonWidth;
	private int actionButton_bgColor;
	private int actionButton_bgId;
	
	private TitleItem titleItem;
	private int titleItem_textWidth;
	private int titleItem_corner;
	private int titleItem_textAppearanceId;
	private int titleItem_subTextAppearanceId;
	private boolean titleItem_clickable;
	private int titleItem_buttonSrc;
	private int titleItem_animDuration;
	private float titleItem_buttonHidingPercent;
		
	private ActionBarOverflowButton overflowButton;	
	private int overflowButton_corner;	
	private int overflowButton_iconId;
	
	private int overflowDropdown_width;
	private int overflowDropdown_height;
	private int overflowDropdown_itemHeight;
	private int overflowDropdown_bgColor;
	private int overflowDropdown_bgId;	
	private int overflowDropdown_dividerId;	
	private int overflowDropdown_selectorId;	
	private int overflowDropdown_textAppearanceId;	
	private int overflowDropdown_anim_tr_id;
	private int overflowDropdown_anim_tl_id;
	private int overflowDropdown_anim_br_id;
	private int overflowDropdown_anim_bl_id;
	
	private ActionBarOverflowButton contextualOverflowButton;
	private int contextualOverflowButton_corner;
	private int contextualOverflowButton_iconId;
	
	private ActionBarButton contextualDoneButton;
	private int contextualDoneButton_corner;
	private int contextualDoneButton_iconId;
	
	private int spinner_bgColor;
	private int spinner_bgId;
	private int spinnerDropdown_bgColor;
	private int spinnerDropdown_bgId;	
	private int spinnerDropdown_dividerId;	
	private int spinnerDropdown_selectorId;	
	private int spinnerDropdown_anim_top;
	private int spinnerDropdown_anim_bottom;
	
	private boolean contextualMode = false;
	private ArrayList<ActionBarCollapseItem> uncollapseItems = new ArrayList<ActionBarCollapseItem>();
	private boolean memo_titleClickable = false;
	
	
	
	private AdapterView.OnItemClickListener listener_overflow = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			overflowItemClicked(position);
		}
	};

	private AdapterView.OnItemClickListener listener_overflow_contextual = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ContextualOverflowItemClicked(position);
		}
	};
	
	private static final String XML_MENU = "menu";
	private static final String XML_ITEM = "item";
	
	private static final int TYPE_BUTTON = 1;
	private static final int TYPE_CUSTOM_BUTTON = 2;
	private static final int TYPE_COLLAPSE = 3;
	private static final int TYPE_SPINNER = 4;
	private static final int TYPE_SEARCH = 5;
	
	public interface ActionBarListener{
		
		public void actionBarItemClicked(int id, IActionBarItem item);
		
		public void contextualModeChanged(boolean mode);
	}
	
	private ListenerDispatcher dispatcher = new ListenerDispatcher();
	
	private View.OnClickListener listener_button = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			actionButtonClicked(v);
		}		
	};
	
	private View.OnLongClickListener listener_button_long = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			actionButtonLongClicked(v);
			return false;
		}
	};
	
	private View.OnClickListener listener_collapse_button = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			collapseButtonClicked(v);
		}
	};
	
	public ActionBarLayout(Context context) {
		this(context, null);		
	}

	public ActionBarLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(attrs != null){		
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBarStyle, defStyle, R.style.ActionBarStyleDefault);
			 
			for (int i = 0, count = a.getIndexCount(); i < count; i++){
			    int attr = a.getIndex(i);
			    
			    switch (attr){
			        case R.styleable.ActionBarStyle_actionBarTopHeight:
			            ab_top_height = a.getDimensionPixelSize(attr, 48);
			            break;
			        case R.styleable.ActionBarStyle_actionBarTopBackground:
			        	TypedValue value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		ab_top_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		ab_top_bgId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_actionBarTopOverlayMode:
			        	ab_top_overlay = a.getBoolean(attr, false);
			        	break;
			        case R.styleable.ActionBarStyle_actionBarTopAdjustMode:
			        	ab_top_adjust = a.getBoolean(attr, false);
			        	break;	
			        case R.styleable.ActionBarStyle_actionBarContextualTopBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		ab_top_contextual_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		ab_top_contextual_bgId = a.getResourceId(attr, 0);
			            break; 		
			        case R.styleable.ActionBarStyle_actionBarBottomHeight:
			            ab_bottom_height = a.getDimensionPixelSize(attr, 48);
			            break;
			        case R.styleable.ActionBarStyle_actionBarBottomBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		ab_bottom_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		ab_bottom_bgId = a.getResourceId(attr, 0);
			            break; 
			        case R.styleable.ActionBarStyle_actionBarBottomOverlayMode:
			        	ab_bottom_overlay = a.getBoolean(attr, false);
			        	break;
			        case R.styleable.ActionBarStyle_actionBarBottomAdjustMode:
			        	ab_bottom_adjust = a.getBoolean(attr, false);
			        	break;	
			        case R.styleable.ActionBarStyle_actionBarContextualBottomBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		ab_bottom_contextual_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		ab_bottom_contextual_bgId = a.getResourceId(attr, 0);
			            break; 	
			        case R.styleable.ActionBarStyle_actionBarAnimDuration:
			            ab_animDuration = a.getInteger(attr, android.R.integer.config_mediumAnimTime);
			            break;
			        case R.styleable.ActionBarStyle_actionButtonWidth:
			            actionButtonWidth = a.getDimensionPixelSize(attr, 48);
			            break;  
			        case R.styleable.ActionBarStyle_actionButtonBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		actionButton_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		actionButton_bgId = a.getResourceId(attr, 0);
			            break; 
			        case R.styleable.ActionBarStyle_overflowButtonCorner:
			            overflowButton_corner = a.getInteger(attr, IActionBarItem.CORNER_TOP_RIGHT);
			            break;   
			        case R.styleable.ActionBarStyle_overflowButtonIcon:
			        	overflowButton_iconId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_overflowDropdownBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		overflowDropdown_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		overflowDropdown_bgId = a.getResourceId(attr, 0);
			            break;  
			        case R.styleable.ActionBarStyle_overflowDropdownWidth:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		overflowDropdown_width = a.getDimensionPixelSize(attr, 240);
			        	else{
			        		overflowDropdown_width = a.getInteger(attr, -2);
			        		if(overflowDropdown_width == -1)
			        			overflowDropdown_width = ViewGroup.LayoutParams.MATCH_PARENT;
			        		else if(overflowDropdown_width == -2)
			        			overflowDropdown_width = ViewGroup.LayoutParams.WRAP_CONTENT;
			        	}			        			            		            
			            break; 
			        case R.styleable.ActionBarStyle_overflowDropdownHeight:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		overflowDropdown_height = a.getDimensionPixelSize(attr, 240);
			        	else{
			        		overflowDropdown_height = a.getInteger(attr, -2);
			        		if(overflowDropdown_height == -1)
			        			overflowDropdown_height = ViewGroup.LayoutParams.MATCH_PARENT;
			        		else if(overflowDropdown_height == -2)
			        			overflowDropdown_height = ViewGroup.LayoutParams.WRAP_CONTENT;
			        	}	            
			            break; 
			        case R.styleable.ActionBarStyle_overflowDropdownItemHeight:
			        	overflowDropdown_itemHeight = a.getDimensionPixelSize(attr, 0);	            
			            break; 
			        case R.styleable.ActionBarStyle_overflowDropdownDivider:
			        	overflowDropdown_dividerId = a.getResourceId(attr, 0);
			            break;   
			        case R.styleable.ActionBarStyle_overflowDropdownSelector:
			        	overflowDropdown_selectorId = a.getResourceId(attr, 0);
			            break;  
			        case R.styleable.ActionBarStyle_overflowDropdownTextAppearance:
			        	overflowDropdown_textAppearanceId = a.getResourceId(attr, 0);
			            break;    
			        case R.styleable.ActionBarStyle_overflowDropdownAnimationTopRight:
			        	overflowDropdown_anim_tr_id = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_overflowDropdownAnimationTopLeft:
			        	overflowDropdown_anim_tl_id = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_overflowDropdownAnimationBottomRight:
			        	overflowDropdown_anim_br_id = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_overflowDropdownAnimationBottomLeft:
			        	overflowDropdown_anim_bl_id = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_contextualOverflowButtonCorner:
			            contextualOverflowButton_corner = a.getInteger(attr, IActionBarItem.CORNER_TOP_RIGHT);
			            break;   
			        case R.styleable.ActionBarStyle_contextualOverflowButtonIcon:
			        	contextualOverflowButton_iconId = a.getResourceId(attr, 0);
			            break;    
			        case R.styleable.ActionBarStyle_contextualDoneButtonCorner:
			            contextualDoneButton_corner = a.getInteger(attr, IActionBarItem.CORNER_TOP_LEFT);
			            break;   
			        case R.styleable.ActionBarStyle_contextualDoneButtonIcon:
			        	contextualDoneButton_iconId = a.getResourceId(attr, 0);
			            break;     
			        case R.styleable.ActionBarStyle_titleTextWidth:
			            titleItem_textWidth = a.getDimensionPixelSize(attr, 128);
			            break; 
			        case R.styleable.ActionBarStyle_titleTextAppearance:
			            titleItem_textAppearanceId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_titleSubTextAppearance:
			            titleItem_subTextAppearanceId = a.getResourceId(attr, 0);
			            break;    
			        case R.styleable.ActionBarStyle_titleCorner:
			            titleItem_corner = a.getInteger(attr, IActionBarItem.CORNER_TOP_LEFT);
			            break;   
			        case R.styleable.ActionBarStyle_titleClickable:
			            titleItem_clickable = a.getBoolean(attr, true);
			            break;   
			        case R.styleable.ActionBarStyle_titleButtonSrc:
			            titleItem_buttonSrc = a.getResourceId(attr, 0);
			            break;    
			        case R.styleable.ActionBarStyle_titleAnimDuration:
			            titleItem_animDuration = a.getInteger(attr, android.R.integer.config_shortAnimTime);
			            break; 
			        case R.styleable.ActionBarStyle_titleButtonHidingPercent:
			            titleItem_buttonHidingPercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0.5f)));
			            break; 
			        case R.styleable.ActionBarStyle_spinnerBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		spinner_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		spinner_bgId = a.getResourceId(attr, 0);
			            break; 
			        case R.styleable.ActionBarStyle_spinnerDropdownBackground:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_INT_COLOR_ARGB8 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4)
			        		spinnerDropdown_bgColor = a.getColor(attr, 0xFF000000);
			        	else 
			        		spinnerDropdown_bgId = a.getResourceId(attr, 0);
			            break;  
			        case R.styleable.ActionBarStyle_spinnerDropdownDivider:
			        	spinnerDropdown_dividerId = a.getResourceId(attr, 0);
			            break;   
			        case R.styleable.ActionBarStyle_spinnerDropdownSelector:
			        	spinnerDropdown_selectorId = a.getResourceId(attr, 0);
			            break;  
			        case R.styleable.ActionBarStyle_spinnerDropdownAnimationTop:
			        	spinnerDropdown_anim_top = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.ActionBarStyle_spinnerDropdownAnimationBottom:
			        	spinnerDropdown_anim_bottom = a.getResourceId(attr, 0);
			            break;
			    }
			}
			a.recycle();	
		}
		
		ab_top = new ActionBar(context);
		ab_top.setId(R.id.ab_top);
		
		ab_top_contextual = new ActionBar(context);
		
		ab_bottom = new ActionBar(context);
		ab_bottom.setId(R.id.ab_bottom);
		
		ab_bottom_contextual = new ActionBar(context);
		
		ab_content = new FrameLayout(context, attrs);
		ab_content.setId(R.id.ab_content);
		
		setTopActionBar();
		setBottomActionBar();		
		setTopContextualActionBar();
		setBottomContextualActionBar();
		setContentView();
		setOverflowButton(context);
		setContextualOverflowButton(context);
		setContextualDoneButton(context);
		setTitleItem(context);		
	}
		
	public boolean dispatchKeyEvent(KeyEvent event){		
		if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			if(!uncollapseItems.isEmpty()){
				actionButtonClicked(titleItem);
				return true;
			}
			else if(contextualMode){
				hideContextualActionBar();
				return true;
			}
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	public void registerActionBarListener(ActionBarListener listener){
		dispatcher.registerListener(listener);
	}
	
	public void unregisterActionBarListener(ActionBarListener listener){
		dispatcher.unregisterListener(listener);
	}
	
	public FrameLayout getLayoutContent(){
		return ab_content;
	}
				
	public int getTopActionBarHeight(){
		return ab_top_height;
	}
	
	public int getBottomActionBarHeight(){
		return ab_bottom_height;
	}
	
	public boolean hasUncollapseItem(){
		return !uncollapseItems.isEmpty();
	}
	
	private void setTopActionBar(){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ab_top_height);
		ab_top.setLayoutParams(params);
		ab_top.setAdjustMode(ab_top_adjust);
		if(ab_top_bgId != 0)
			ab_top.setBackgroundResource(ab_top_bgId);
		else
			ab_top.setBackgroundColor(ab_top_bgColor);		
	}
	
	private void setTopContextualActionBar(){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ab_top_height);
		ab_top_contextual.setLayoutParams(params);
		ab_top_contextual.setAdjustMode(ab_top_adjust);
		if(ab_top_contextual_bgId != 0)
			ab_top_contextual.setBackgroundResource(ab_top_contextual_bgId);
		else
			ab_top_contextual.setBackgroundColor(ab_top_contextual_bgColor);		
	}
	
	private void setBottomActionBar(){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ab_bottom_height);
		ab_bottom.setLayoutParams(params);
		ab_bottom.setAdjustMode(ab_bottom_adjust);
		if(ab_bottom_bgId != 0)
			ab_bottom.setBackgroundResource(ab_bottom_bgId);
		else
			ab_bottom.setBackgroundColor(ab_bottom_bgColor);
	}
	
	private void setBottomContextualActionBar(){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ab_bottom_height);
		ab_bottom_contextual.setLayoutParams(params);
		ab_bottom_contextual.setAdjustMode(ab_bottom_adjust);
		if(ab_bottom_contextual_bgId != 0)
			ab_bottom_contextual.setBackgroundResource(ab_bottom_contextual_bgId);
		else
			ab_bottom_contextual.setBackgroundColor(ab_bottom_contextual_bgColor);		
	}
	
	public boolean isContextualMode(){
		return contextualMode;
	}
	
	public void showContextualActionBar(){
		if(!contextualMode){
			contextualMode = true;
			dispatcher.contextualModeChanged(contextualMode);
			if(ab_top.hasVisibleItem()){
				if(ab_top_contextual.hasVisibleItem()){
					TranslateAnimation anim_top = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, -ab_top_height, TranslateAnimation.ABSOLUTE, 0);
					anim_top.setDuration(ab_animDuration);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_top_contextual.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_top.setVisibility(View.INVISIBLE);
						}
					});				
					ab_top_contextual.startAnimation(anim_top);
				}
				else{
					SlideAnimation anim_top = new SlideAnimation(ab_top, ab_animDuration, SlideAnimation.SLIDE_OUT, SlideAnimation.MARGIN_TOP);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_top.setVisibility(View.INVISIBLE);
						}
					});				
					ab_top.startAnimation(anim_top);
				}
			}
			else{
				if(ab_top_contextual.hasVisibleItem()){
					TranslateAnimation anim = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, -ab_top_height, TranslateAnimation.ABSOLUTE, 0);
					anim.setDuration(ab_animDuration);
					anim.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_top_contextual.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_top.setVisibility(View.INVISIBLE);
						}
					});				
					ab_top_contextual.startAnimation(anim);
					
					SlideAnimation anim_top = new SlideAnimation(ab_top, ab_animDuration, SlideAnimation.SLIDE_IN, SlideAnimation.MARGIN_TOP);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_top.setVisibility(View.INVISIBLE);
						}
					});				
					ab_top.startAnimation(anim_top);
				}
			}
			
			
			if(ab_bottom.hasVisibleItem()){
				if(ab_bottom_contextual.hasVisibleItem()){
					TranslateAnimation anim_bottom = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, ab_bottom_height, TranslateAnimation.ABSOLUTE, 0);
					anim_bottom.setDuration(ab_animDuration);
					anim_bottom.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_bottom_contextual.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_bottom.setVisibility(View.INVISIBLE);
						}
					});			
					ab_bottom_contextual.startAnimation(anim_bottom);
				}
				else{
					SlideAnimation anim_bottom = new SlideAnimation(ab_bottom, ab_animDuration, SlideAnimation.SLIDE_OUT, SlideAnimation.MARGIN_BOTTOM);
					anim_bottom.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_bottom.setVisibility(View.INVISIBLE);
						}
					});				
					ab_bottom.startAnimation(anim_bottom);
				}
			}
			else{
				if(ab_bottom_contextual.hasVisibleItem()){
					TranslateAnimation anim = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, ab_bottom_height, TranslateAnimation.ABSOLUTE, 0);
					anim.setDuration(ab_animDuration);
					anim.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_bottom_contextual.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_bottom.setVisibility(View.INVISIBLE);
						}
					});			
					ab_bottom_contextual.startAnimation(anim);
					
					SlideAnimation anim_bottom = new SlideAnimation(ab_bottom, ab_animDuration, SlideAnimation.SLIDE_IN, SlideAnimation.MARGIN_BOTTOM);
					anim_bottom.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_bottom.setVisibility(View.INVISIBLE);
						}
					});				
					ab_bottom.startAnimation(anim_bottom);
				}
			}
		}
		
	}
	
	public void hideContextualActionBar(){
		if(contextualMode){			
			
			if(ab_top_contextual.hasVisibleItem()){
				if(ab_top.hasVisibleItem()){
					TranslateAnimation anim_top = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, -ab_top_height);
					anim_top.setDuration(ab_animDuration);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_top.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {					
							ab_top_contextual.setVisibility(View.INVISIBLE);
							contextualMode = false;
							dispatcher.contextualModeChanged(contextualMode);
						}
					});
					ab_top_contextual.startAnimation(anim_top);
				}
				else{
					TranslateAnimation anim = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, -ab_top_height);
					anim.setDuration(ab_animDuration);
					anim.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {					
							ab_top_contextual.setVisibility(View.INVISIBLE);
							contextualMode = false;
							dispatcher.contextualModeChanged(contextualMode);
						}
					});
					ab_top_contextual.startAnimation(anim);
					
					SlideAnimation anim_top = new SlideAnimation(ab_top, ab_animDuration, SlideAnimation.SLIDE_OUT, SlideAnimation.MARGIN_TOP);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_top.setVisibility(View.INVISIBLE);
						}
					});				
					ab_top.startAnimation(anim_top);
				}
			}
			else{
				if(ab_top.hasVisibleItem()){
					SlideAnimation anim_top = new SlideAnimation(ab_top, ab_animDuration, SlideAnimation.SLIDE_IN, SlideAnimation.MARGIN_TOP);
					anim_top.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_top.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							contextualMode = false;
							dispatcher.contextualModeChanged(contextualMode);
						}
					});				
					ab_top.startAnimation(anim_top);
				}
			}
			
			if(ab_bottom_contextual.hasVisibleItem()){
				if(ab_bottom.hasVisibleItem()){
					TranslateAnimation anim_bottom = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, ab_bottom_height);
					anim_bottom.setDuration(ab_animDuration);
					anim_bottom.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							ab_bottom.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {					
							ab_bottom_contextual.setVisibility(View.INVISIBLE);
						}
					});
					ab_bottom_contextual.startAnimation(anim_bottom);
				}
				else{
					TranslateAnimation anim = new TranslateAnimation(
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0, 
							TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, ab_bottom_height);
					anim.setDuration(ab_animDuration);
					anim.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {					
							ab_bottom_contextual.setVisibility(View.INVISIBLE);
						}
					});
					ab_bottom_contextual.startAnimation(anim);
					
					SlideAnimation anim_bottom = new SlideAnimation(ab_bottom, ab_animDuration, SlideAnimation.SLIDE_OUT, SlideAnimation.MARGIN_BOTTOM);
					anim_bottom.setAnimationListener(new Animation.AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {}
						
						@Override
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							ab_bottom.setVisibility(View.INVISIBLE);
						}
					});				
					ab_bottom.startAnimation(anim_bottom);
				}
			}
			else{
				SlideAnimation anim_bottom = new SlideAnimation(ab_bottom, ab_animDuration, SlideAnimation.SLIDE_IN, SlideAnimation.MARGIN_BOTTOM);
				anim_bottom.setAnimationListener(new Animation.AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						ab_bottom.setVisibility(View.VISIBLE);						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {}
				});				
				ab_bottom.startAnimation(anim_bottom);
			}			
			
		}
		
	}
	
	public void showContextualActionBarImmediately(){
		if(!contextualMode){
			contextualMode = true;
			
			if(ab_top_contextual.hasVisibleItem()){
				ab_top.setVisibility(View.INVISIBLE);
				ab_top_contextual.setVisibility(View.VISIBLE);
			}
			else if(ab_top.getVisibility() == View.VISIBLE)
				ab_top.setVisibility(View.INVISIBLE);
			
			if(ab_bottom_contextual.hasVisibleItem()){
				ab_bottom.setVisibility(View.INVISIBLE);
				ab_bottom_contextual.setVisibility(View.VISIBLE);
			}
			else if(ab_bottom.getVisibility() == View.VISIBLE)
				ab_bottom.setVisibility(View.INVISIBLE);
		}
	}
	
	public void hideContextualActionBarImmediately(){
		if(contextualMode){
			contextualMode = false;
			
			ab_top.setVisibility(ab_top.hasVisibleItem() ? View.VISIBLE : View.INVISIBLE);	
			ab_top_contextual.setVisibility(View.INVISIBLE);
			
			ab_bottom.setVisibility(ab_bottom.hasVisibleItem() ? View.VISIBLE : View.INVISIBLE);	
			ab_bottom_contextual.setVisibility(View.INVISIBLE);
		}
	}
		
	private void setContentView(){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		ab_content.setLayoutParams(params);		
		removeAllViews();		
		addView(ab_content);
		addView(ab_top);			
		addView(ab_bottom);				
		addView(ab_top_contextual);
		addView(ab_bottom_contextual);
	}
		
	private void setOverflowButton(Context context){
		overflowButton = new ActionBarOverflowButton(context);
		overflowButton.setSupportWidth(actionButtonWidth);
		overflowButton.setOnClickListener(listener_button);
		if(actionButton_bgId != 0)
			overflowButton.setBackgroundResource(actionButton_bgId);
		else
			overflowButton.setBackgroundColor(actionButton_bgColor);
		overflowButton.setImageResource(overflowButton_iconId);		
		overflowButton.setOnItemClickListener(listener_overflow);
		overflowButton.setDropdownSize(overflowDropdown_width, overflowDropdown_height);
		if(overflowDropdown_itemHeight > 0)
			overflowButton.setDropdownItemHeight(overflowDropdown_itemHeight);
		if(overflowDropdown_bgId != 0)
			overflowButton.setDropdownBackgroundResource(overflowDropdown_bgId);
		else
			overflowButton.setDropdownBackgroundColor(overflowDropdown_bgColor);
		overflowButton.setDropdownDividerResource(overflowDropdown_dividerId);
		overflowButton.setDropdownSelectorResource(overflowDropdown_selectorId);
		overflowButton.setDropdownTextAppearance(overflowDropdown_textAppearanceId);
		overflowButton.setDropdownAnimationStyle(overflowDropdown_anim_tr_id, overflowDropdown_anim_tl_id, overflowDropdown_anim_br_id, overflowDropdown_anim_bl_id);
		overflowButton.setCorner(overflowButton_corner);
				
		switch (overflowButton.getCorner()) {
			case IActionBarItem.CORNER_TOP_LEFT:
			case IActionBarItem.CORNER_TOP_RIGHT:
				ab_top.setOverflowButton(overflowButton, true);
				ab_bottom.setOverflowButton(overflowButton, false);				
				break;
			case IActionBarItem.CORNER_BOTTOM_LEFT:
			case IActionBarItem.CORNER_BOTTOM_RIGHT:
				ab_top.setOverflowButton(overflowButton, false);
				ab_bottom.setOverflowButton(overflowButton, true);	
				break;	
		}		
		
		overflowButton.setId(R.id.ab_bt_overflow);
	}
	
	private void setContextualOverflowButton(Context context){
		contextualOverflowButton = new ActionBarOverflowButton(context);
		contextualOverflowButton.setSupportWidth(actionButtonWidth);
		contextualOverflowButton.setOnClickListener(listener_button);
		if(actionButton_bgId != 0)
			contextualOverflowButton.setBackgroundResource(actionButton_bgId);
		else
			contextualOverflowButton.setBackgroundColor(actionButton_bgColor);
		contextualOverflowButton.setImageResource(contextualOverflowButton_iconId);		
		contextualOverflowButton.setOnItemClickListener(listener_overflow_contextual);
		contextualOverflowButton.setDropdownSize(overflowDropdown_width, overflowDropdown_height);
		if(overflowDropdown_itemHeight > 0)
			contextualOverflowButton.setDropdownItemHeight(overflowDropdown_itemHeight);
		if(overflowDropdown_bgId != 0)
			contextualOverflowButton.setDropdownBackgroundResource(overflowDropdown_bgId);
		else
			contextualOverflowButton.setDropdownBackgroundColor(overflowDropdown_bgColor);
		contextualOverflowButton.setDropdownDividerResource(overflowDropdown_dividerId);
		contextualOverflowButton.setDropdownSelectorResource(overflowDropdown_selectorId);
		contextualOverflowButton.setDropdownTextAppearance(overflowDropdown_textAppearanceId);
		contextualOverflowButton.setDropdownAnimationStyle(overflowDropdown_anim_tr_id, overflowDropdown_anim_tl_id, overflowDropdown_anim_br_id, overflowDropdown_anim_bl_id);
		contextualOverflowButton.setCorner(contextualOverflowButton_corner);
				
		switch (contextualOverflowButton.getCorner()) {
			case IActionBarItem.CORNER_TOP_LEFT:
			case IActionBarItem.CORNER_TOP_RIGHT:				
				ab_top_contextual.setOverflowButton(contextualOverflowButton, true);
				ab_bottom_contextual.setOverflowButton(contextualOverflowButton, false);
				break;
			case IActionBarItem.CORNER_BOTTOM_LEFT:
			case IActionBarItem.CORNER_BOTTOM_RIGHT:				
				ab_top_contextual.setOverflowButton(contextualOverflowButton, false);
				ab_bottom_contextual.setOverflowButton(contextualOverflowButton, true);
				break;	
		}		
		
		contextualOverflowButton.setId(R.id.ab_bt_overflow_contextual);
	}
		
	private void setContextualDoneButton(Context context){
		contextualDoneButton = addActionButton(contextualDoneButton_iconId, IActionBarItem.CATEGORY_ALWAYS, contextualDoneButton_corner, "Done", true, true, R.id.ab_bt_done_contextual);		
	}
	
	private void setTitleItem(Context context){
		titleItem = new TitleItem(context);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		titleItem.setLayoutParams(params);
		titleItem.setTextWidth(titleItem_textWidth);
		titleItem.setTitleTextAppearance(titleItem_textAppearanceId);
		titleItem.setSubTitleTextAppearance(titleItem_subTextAppearanceId);
		titleItem.setCorner(titleItem_corner);
		titleItem.setId(R.id.ab_bt_title);
		titleItem.setButtonResource(titleItem_buttonSrc);
		titleItem.setAnimDuration(titleItem_animDuration);
		titleItem.setHidingPercent(titleItem_buttonHidingPercent);
		if(titleItem_clickable){
			titleItem.setOnClickListener(listener_button);
			titleItem.setButtonVisibile(true);
			if(actionButton_bgId != 0)
				titleItem.setBackgroundResource(actionButton_bgId);
			else
				titleItem.setBackgroundColor(actionButton_bgColor);
		}
		else{
			titleItem.setOnClickListener(null);
			titleItem.setButtonVisibile(false);
			titleItem.setBackgroundResource(0);
		}
		
		addActionBarItem(titleItem);
	}
	
	public void setTitle(int resId, String title, String subTitle){
		if(resId > 0){
			titleItem.setIconResource(resId);
			titleItem.setIconVisibile(true);
		}
		else{
			titleItem.setIconVisibile(false);
		}
		titleItem.setTitleText(title, subTitle);
		requestLayout();
	}
	
	public void setTitleIcon(int resId){
		if(resId > 0){
			titleItem.setIconResource(resId);
			titleItem.setIconVisibile(true);
		}
		else{
			titleItem.setIconVisibile(false);
		}
		requestLayout();
	}
	
	public void setTitleText(String title, String subTitle){
		titleItem.setTitleText(title, subTitle);
		requestLayout();
	}
	
	public void setTitleClickable(boolean clickable){
		if(titleItem_clickable != clickable){
			titleItem_clickable = clickable;
			
			if(titleItem_clickable){
				titleItem.setOnClickListener(listener_button);
				titleItem.setButtonVisibile(true);
				if(actionButton_bgId != 0)
					titleItem.setBackgroundResource(actionButton_bgId);
				else
					titleItem.setBackgroundColor(actionButton_bgColor);
			}
			else{
				titleItem.setOnClickListener(null);
				titleItem.setButtonVisibile(false);
				titleItem.setBackgroundResource(0);
			}
		}
	}
	
	public void showTitleButton(){
		titleItem.showDrawerButton();
	}
	
	public void hideTitleButton(){
		titleItem.hideDrawerButton();
	}
	
	public void setOverflowButtonCorner(int corner){
		if(overflowButton.getCorner() != corner){
			overflowButton_corner = corner;
			overflowButton.setCorner(corner);
			
			switch (overflowButton.getCorner()) {
				case IActionBarItem.CORNER_TOP_LEFT:
				case IActionBarItem.CORNER_TOP_RIGHT:
					ab_top.setOverflowButton(overflowButton, true);
					ab_bottom.setOverflowButton(overflowButton, false);
					break;
				case IActionBarItem.CORNER_BOTTOM_LEFT:
				case IActionBarItem.CORNER_BOTTOM_RIGHT:
					ab_top.setOverflowButton(overflowButton, false);
					ab_bottom.setOverflowButton(overflowButton, true);
					break;	
			}				
			requestLayout();
		}
	}
	
	public void setContextualOverflowButtonCorner(int corner){
		if(contextualOverflowButton.getCorner() != corner){
			contextualOverflowButton_corner = corner;
			contextualOverflowButton.setCorner(corner);
			
			switch (contextualOverflowButton.getCorner()) {
				case IActionBarItem.CORNER_TOP_LEFT:
				case IActionBarItem.CORNER_TOP_RIGHT:
					ab_top_contextual.setOverflowButton(contextualOverflowButton, true);
					ab_bottom_contextual.setOverflowButton(contextualOverflowButton, false);
					break;
				case IActionBarItem.CORNER_BOTTOM_LEFT:
				case IActionBarItem.CORNER_BOTTOM_RIGHT:
					ab_top_contextual.setOverflowButton(contextualOverflowButton, false);
					ab_bottom_contextual.setOverflowButton(contextualOverflowButton, true);
					break;	
			}	
			requestLayout();
		}
	}
	
	public void setActionBarTopOverlayMode(boolean b){
		if(ab_top_overlay != b){
			ab_top_overlay = b;
			requestLayout();
		}
	}
	
	public void setActionBarBottomOverlayMode(boolean b){
		if(ab_bottom_overlay != b){
			ab_bottom_overlay = b;
			requestLayout();
		}
	}
	
	public void setActionBarTopAdjustMode(boolean b){
		if(ab_top_adjust != b){
			ab_top_adjust = b;
			ab_top.requestLayout();
		}
	}
	
	public void setActionBarBottomAdjustMode(boolean b){
		if(ab_bottom_adjust != b){
			ab_bottom_adjust = b;
			ab_bottom.requestLayout();
		}
	}
	
	public void setActionBarItemVisible(int id, boolean visible){
		IActionBarItem item = getActionBarItem(id);
		if(item != null && item.isVisible() != visible){
			item.setVisible(visible);
			requestLayout();
		}
	}
	
	public void setContextualActionBarItemVisible(int id, boolean visible){
		IActionBarItem item = getContextualActionBarItem(id);
		if(item != null && item.isVisible() != visible){
			item.setVisible(visible);
			requestLayout();
		}
	}
	
	public void uncollapseItem(int id){
		IActionBarItem item = getActionBarItem(id);
		if(item instanceof ActionBarCollapseItem){
			ActionBarCollapseItem item_collapse = (ActionBarCollapseItem)item;
			if(item_collapse.isCollapseMode())
				collapseButtonClicked(item_collapse.getButton());
		}
	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
		int content_left = 0;
		int content_right = right - left;
		int content_top = 0;
		int content_bottom = bottom - top;
						
		switch (contextualOverflowButton_corner) {
			case IActionBarItem.CORNER_TOP_LEFT:
			case IActionBarItem.CORNER_TOP_RIGHT:
				ab_bottom_contextual.preLayout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);
				ab_top_contextual.preLayout(content_left, 0, content_right, ab_top_height);
				break;
			case IActionBarItem.CORNER_BOTTOM_LEFT:
			case IActionBarItem.CORNER_BOTTOM_RIGHT:
				ab_top_contextual.preLayout(content_left, 0, content_right, ab_top_height);
				ab_bottom_contextual.preLayout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);					
				break;
		}
	
		switch (overflowButton_corner) {
			case IActionBarItem.CORNER_TOP_LEFT:
			case IActionBarItem.CORNER_TOP_RIGHT:
				ab_bottom.preLayout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);
				ab_top.preLayout(content_left, 0, content_right, ab_top_height);
				break;
			case IActionBarItem.CORNER_BOTTOM_LEFT:
			case IActionBarItem.CORNER_BOTTOM_RIGHT:
				ab_top.preLayout(content_left, 0, content_right, ab_top_height);
				ab_bottom.preLayout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);					
				break;
		}
		
		if(!contextualMode){						
			if(ab_top.hasVisibleItem()){
				ab_top.layout(content_left, 0, content_right, ab_top_height);
				if(!ab_top_overlay)
					content_top += ab_top_height;
			}
			else 
				ab_top.layout(content_left, 0, content_right, 0);			
						
			if(ab_bottom.hasVisibleItem()){
				ab_bottom.layout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);
				if(!ab_bottom_overlay)
					content_bottom -= ab_bottom_height;
			}
			else 
				ab_bottom.layout(content_left, content_bottom, content_right, content_bottom);
			
			if(ab_top_contextual.hasVisibleItem()){
				ab_top_contextual.layout(content_left, -ab_top_height, content_right, 0);
				ab_top_contextual.setVisibility(View.INVISIBLE);
			}
			else 
				ab_top_contextual.layout(content_left, 0, content_right, 0);
			
			if(ab_bottom_contextual.hasVisibleItem()){
				ab_bottom_contextual.layout(content_left, content_bottom, content_right, content_bottom + ab_bottom_height);
				ab_bottom_contextual.setVisibility(View.INVISIBLE);
			}
			else 
				ab_bottom_contextual.layout(content_left, content_bottom, content_right, content_bottom);
		}
		else{						
			if(ab_top_contextual.hasVisibleItem()){
				ab_top_contextual.layout(content_left, 0, content_right, ab_top_height);	
				ab_top.layout(content_left, 0, content_right, ab_top_height);	
				content_top += ab_top_height;
			}
			else {
				ab_top_contextual.layout(content_left, 0, content_right, 0);	
				ab_top.layout(content_left, 0, content_right, 0);	
			}
			
			if(ab_bottom_contextual.hasVisibleItem()){
				ab_bottom_contextual.layout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);
				ab_bottom.layout(content_left, content_bottom - ab_bottom_height, content_right, content_bottom);
				content_bottom -= ab_bottom_height;
			}
			else {
				ab_bottom_contextual.layout(content_left, content_bottom, content_right, content_bottom);
				ab_bottom.layout(content_left, content_bottom, content_right, content_bottom);
			}
			
		}	
		
		
		
		int height =  content_bottom - content_top;		
		ViewGroup.LayoutParams params = ab_content.getLayoutParams();
		if(params.height != height){
			params.height = height;
			ab_content.measure(MeasureSpec.makeMeasureSpec(content_right - content_left, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
		}				
		ab_content.layout(content_left, content_top, content_right, content_bottom);
	}
	
	public IActionBarItem getActionBarItem(int id){
		if(id == R.id.ab_bt_title || id == R.id.ab_bt_overflow)
			return null;
		
		IActionBarItem result = ab_top.getItem(id);
		
		if(result != null)
			return result;
		
		result = ab_bottom.getItem(id);
		return result;
	}
	
	public IActionBarItem getContextualActionBarItem(int id){
		if(id == R.id.ab_bt_overflow_contextual || id == R.id.ab_bt_done_contextual)
			return null;
		
		IActionBarItem result = ab_top_contextual.getItem(id);
		
		if(result != null)
			return result;
		
		result = ab_bottom_contextual.getItem(id);
		return result;
	}
		
	public IActionBarItem[] addItemFromXml(int id) {
        XmlResourceParser parser = null;
        IActionBarItem[] result = null;
        try {
            parser = getContext().getResources().getLayout(id);
            AttributeSet attrs = Xml.asAttributeSet(parser);            
            result = addItemFromXml(parser, attrs);
        } 
        catch (Exception e) {} 
        finally {
            if (parser != null) 
            	parser.close();
        }
        return result;
    }
	
	private IActionBarItem[] addItemFromXml(XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
        String tagName;
        boolean lookingForEndOfUnknownTag = false;
        String unknownTagName = null;
        ArrayList<IActionBarItem> list = new ArrayList<IActionBarItem>();

        // This loop will skip to the menu start tag
        do {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.getName();
                if (tagName.equals(XML_MENU)) {
                    eventType = parser.next();
                    break;
                }                
                throw new RuntimeException("Expecting menu, got " + tagName);
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
        
        boolean reachedEndOfMenu = false;
        while (!reachedEndOfMenu) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (lookingForEndOfUnknownTag) 
                        break;   
                    tagName = parser.getName();
                    if (tagName.equals(XML_ITEM)) {
                       IActionBarItem item = readItem(attrs);
                       if(item != null)
                    	   list.add(item);
                    }
                    else {
                        lookingForEndOfUnknownTag = true;
                        unknownTagName = tagName;
                    }
                    break;
                    
                case XmlPullParser.END_TAG:
                    tagName = parser.getName();
                    if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                        lookingForEndOfUnknownTag = false;
                        unknownTagName = null;
                    }                    
                    else if (tagName.equals(XML_MENU)) 
                        reachedEndOfMenu = true;
                    
                    break;
                    
                case XmlPullParser.END_DOCUMENT:
                    throw new RuntimeException("Unexpected end of document");
            }
            
            eventType = parser.next();
        }
        
        if(!list.isEmpty())
        	requestLayout();
        else
        	return null;
        
        IActionBarItem[] result = new IActionBarItem[list.size()];
        return list.toArray(result);
    }
	
	private IActionBarItem readItem(AttributeSet attrs){
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ActionBarItem);
		int itemType = TYPE_BUTTON;
		int itemId = 0;
		int itemIconId = 0;
		int itemCorner = IActionBarItem.CORNER_TOP_RIGHT;
		int itemCategory = IActionBarItem.CATEGORY_IFROOM;
		boolean itemVisible = true;		
		boolean contextual = false;
		String itemTitle = null;
		int itemResLayout = 0;
		String itemResClass = null;
		boolean collapse = true;		
		boolean overlap = false;
		boolean onTop = true;
		int dropdownWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
		int dropdownHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
		
		for (int i = 0, count = a.getIndexCount(); i < count; i++){
		    int attr = a.getIndex(i);
		    switch (attr) {
				case R.styleable.ActionBarItem_type:
					itemType = a.getInt(attr, TYPE_BUTTON);
					break;
				case R.styleable.ActionBarItem_id:
					itemId = a.getResourceId(attr, 0);
					break;
				case R.styleable.ActionBarItem_icon:
					itemIconId = a.getResourceId(attr, 0);
					break;
				case R.styleable.ActionBarItem_corner:
					itemCorner = a.getInt(attr, IActionBarItem.CORNER_TOP_RIGHT);
					break;
				case R.styleable.ActionBarItem_category:
					itemCategory = a.getInt(attr, IActionBarItem.CATEGORY_IFROOM);
					break;
				case R.styleable.ActionBarItem_visible:
					itemVisible = a.getBoolean(attr, true);	
					break;
				case R.styleable.ActionBarItem_contextual:
					contextual = a.getBoolean(attr, false);
					break;
				case R.styleable.ActionBarItem_title:
					TypedValue value = a.peekValue(attr);
					if(value.type == TypedValue.TYPE_STRING)
						itemTitle = a.getString(attr);
					else
						itemTitle = getContext().getResources().getString(a.getResourceId(attr, 0));	
					break;
				case R.styleable.ActionBarItem_resLayout:
					itemResLayout = a.getResourceId(attr, 0);
					break;
				case R.styleable.ActionBarItem_resClass:
					itemResClass = a.getString(attr);
					break;
				case R.styleable.ActionBarItem_collapse:
					collapse = a.getBoolean(attr, true);	
					break;					
				case R.styleable.ActionBarItem_overlap:
					overlap = a.getBoolean(attr, false);
					break;
				case R.styleable.ActionBarItem_onTop:
					onTop = a.getBoolean(attr, true);
					break;
				case R.styleable.ActionBarItem_dropdownWidth:
		        	value = a.peekValue(attr);
		        	if(value.type == TypedValue.TYPE_DIMENSION)
		        		dropdownWidth = a.getDimensionPixelSize(attr, 240);
		        	else{
		        		dropdownWidth = a.getInteger(attr, -2);
		        		if(dropdownWidth == -1)
		        			dropdownWidth = ViewGroup.LayoutParams.MATCH_PARENT;
		        		else if(overflowDropdown_width == -2)
		        			dropdownWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
		        	}			        			            		            
		            break; 
				case R.styleable.ActionBarItem_dropdownHeight:
		        	value = a.peekValue(attr);
		        	if(value.type == TypedValue.TYPE_DIMENSION)
		        		dropdownHeight = a.getDimensionPixelSize(attr, 240);
		        	else{
		        		dropdownHeight = a.getInteger(attr, -2);
		        		if(dropdownHeight == -1)
		        			dropdownHeight = ViewGroup.LayoutParams.MATCH_PARENT;
		        		else if(overflowDropdown_width == -2)
		        			dropdownHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
		        	}			        			            		            
		            break;     
			}
		}
		a.recycle();
				
		switch (itemType) {
			case TYPE_BUTTON:
				return addActionButton(itemIconId, itemCategory, itemCorner, itemTitle, itemVisible, contextual, itemId);				
			case TYPE_CUSTOM_BUTTON:
				if(itemResClass == null)
					return addActionBarCustomButton(itemResLayout, itemCategory, itemCorner, itemTitle, itemVisible, contextual, itemId);
				else{
					Class<?> aClass;
					try {
						aClass = Class.forName(itemResClass);
						if(ActionBarCustomButton.class.isAssignableFrom(aClass)){
							ActionBarCustomButton button = (ActionBarCustomButton)aClass.getConstructor(Context.class).newInstance(getContext());							
							return addActionBarCustomButton(button, itemCategory, itemCorner, itemTitle, itemVisible, contextual, itemId);
						}
					} catch (Exception e) {}
					
				}
				break;
			case TYPE_SPINNER:
				if(itemResClass == null)
					return addActionBarSpinner(dropdownWidth, dropdownHeight, itemCategory, itemCorner, itemTitle, itemVisible, contextual, itemId);
				else{
					Class<?> aClass;
					try {
						aClass = Class.forName(itemResClass);
						if(ActionBarSpinner.class.isAssignableFrom(aClass)){
							ActionBarSpinner spinner = (ActionBarSpinner)aClass.getConstructor(Context.class).newInstance(getContext());
							return addActionBarSpinner(spinner, dropdownWidth, dropdownHeight, itemCategory, itemCorner, itemTitle, itemVisible, contextual, itemId);
						}
					} catch (Exception e) {}
					
				}
				break;
			case TYPE_COLLAPSE:
				if(itemResClass == null)
					return addActionBarCollapseItem(itemResLayout, itemIconId, itemCategory, itemCorner, itemTitle, itemId, collapse, overlap, onTop, itemVisible, contextual);
				break;
			case TYPE_SEARCH:				
				return addActionBarSearchItem(itemIconId == 0 ? R.drawable.ab_bt_search : itemIconId, itemCategory, itemCorner, itemTitle, itemId, collapse, overlap, onTop, itemVisible, contextual);				
		}
		
		return null;
	}
		
	private void addActionBarItem(IActionBarItem item){
		if(!(item instanceof View))
			return;
		
		switch (item.getCorner()) {
			case IActionBarItem.CORNER_TOP_LEFT:
			case IActionBarItem.CORNER_TOP_RIGHT:
				if(item.isContextual())
					ab_top_contextual.addItem(item);				
				else
					ab_top.addItem(item);				
				break;
			case IActionBarItem.CORNER_BOTTOM_LEFT:
			case IActionBarItem.CORNER_BOTTOM_RIGHT:
				if(item.isContextual())
					ab_bottom_contextual.addItem(item);			
				else
					ab_bottom.addItem(item);				
				break;	
		}		
	}
			
	private ActionBarButton addActionButton(int iconId, int category, int corner, String title, boolean visible, boolean contextual, int id){
		ActionBarButton button = new ActionBarButton(getContext());
		button.setSupportWidth(actionButtonWidth);
		button.setImageResource(iconId);
		button.setCategory(category);
		button.setCorner(corner);
		button.setTitle(title);
		button.setId(id);
		button.setVisible(visible);
		button.setContextual(contextual);
		button.setOnClickListener(listener_button);
		button.setOnLongClickListener(listener_button_long);
		if(actionButton_bgId != 0)
			button.setBackgroundResource(actionButton_bgId);
		else
			button.setBackgroundColor(actionButton_bgColor);
		addActionBarItem(button);			
		return button;
	}
		
	private ActionBarCustomButton addActionBarCustomButton(int resId, int category, int corner, String title, boolean visible, boolean contextual, int id){
		ActionBarCustomButton button = new ActionBarCustomButton(getContext());
		LayoutInflater inflater = LayoutInflater.from(getContext());		
		View v = inflater.inflate(resId, null, false);
		button.setSupportWidth(actionButtonWidth);
		button.setContentView(v);
		button.setCategory(category);
		button.setCorner(corner);
		button.setTitle(title);
		button.setVisible(visible);
		button.setContextual(contextual);
		button.setId(id);
		button.setOnClickListener(listener_button);
		button.setOnLongClickListener(listener_button_long);
		if(actionButton_bgId != 0)
			button.setBackgroundResource(actionButton_bgId);
		else
			button.setBackgroundColor(actionButton_bgColor);
		addActionBarItem(button);
		return button;
	}
	
	private ActionBarCustomButton addActionBarCustomButton(ActionBarCustomButton button, int category, int corner, String title, boolean visible, boolean contextual, int id){
		button.setSupportWidth(actionButtonWidth);
		button.setCategory(category);
		button.setCorner(corner);
		button.setTitle(title);
		button.setVisible(visible);
		button.setContextual(contextual);
		button.setId(id);
		button.setOnClickListener(listener_button);
		button.setOnLongClickListener(listener_button_long);
		if(actionButton_bgId != 0)
			button.setBackgroundResource(actionButton_bgId);
		else
			button.setBackgroundColor(actionButton_bgColor);
		addActionBarItem(button);
		return button;
	}
	
	private ActionBarCollapseItem addActionBarCollapseItem(int resId, int iconId, int category, int corner, String title, int id, boolean collapse, boolean overlap, boolean onTop, boolean visible, boolean contextual){
		ActionBarCollapseItem item = new ActionBarCollapseItem(getContext());
		LayoutInflater inflater = LayoutInflater.from(getContext());		
		View v = inflater.inflate(resId, null, false);
		item.setContentView(v);
		item.setSupportWidth(actionButtonWidth);
		
		ActionBarCollapseButton button = new ActionBarCollapseButton(getContext());
		button.setSupportWidth(actionButtonWidth);
		button.setImageResource(iconId);
		button.setOnClickListener(listener_collapse_button);
		button.setOnLongClickListener(listener_button_long);
		if(actionButton_bgId != 0)
			button.setBackgroundResource(actionButton_bgId);
		else
			button.setBackgroundColor(actionButton_bgColor);
		item.setButton(button);
		
		item.setCategory(category);
		item.setCorner(corner);
		item.setTitle(title);
		item.setVisible(visible);
		item.setContextual(contextual);
		item.setId(id);
		item.setCollapseMode(collapse);
		item.setOverlapMode(overlap);
		item.setOnTop(onTop);
		addActionBarItem(item);
		return item;
	}
		
	private ActionBarSpinner addActionBarSpinner(int dropdownWidth, int dropdownHeight, int category, int corner, String title, boolean visible, boolean contextual, int id){
		ActionBarSpinner item = new ActionBarSpinner(getContext());
		
		if(spinner_bgId != 0)
			item.setBackgroundResource(spinner_bgId);
		else
			item.setBackgroundColor(spinner_bgColor);
		item.setDropdownSize(dropdownWidth, dropdownHeight);
		if(spinnerDropdown_bgId != 0)
			item.setDropdownBackgroundResource(spinnerDropdown_bgId);
		else
			item.setDropdownBackgroundColor(spinnerDropdown_bgColor);
		item.setDropdownDividerResource(spinnerDropdown_dividerId);
		item.setDropdownSelectorResource(spinnerDropdown_selectorId);
		item.setDropdownAnimationStyle(spinnerDropdown_anim_top, spinnerDropdown_anim_bottom);		
		
		item.setCategory(category);
		item.setCorner(corner);
		item.setTitle(title);
		item.setVisible(visible);
		item.setContextual(contextual);
		item.setId(id);
		addActionBarItem(item);
		
		return item;
	}
	
	private ActionBarSpinner addActionBarSpinner(ActionBarSpinner item, int dropdownWidth, int dropdownHeight, int category, int corner, String title, boolean visible, boolean contextual, int id){
		
		if(spinner_bgId != 0)
			item.setBackgroundResource(spinner_bgId);
		else
			item.setBackgroundColor(spinner_bgColor);
		
		item.setDropdownSize(dropdownWidth, dropdownHeight);
		if(spinnerDropdown_bgId != 0)
			item.setDropdownBackgroundResource(spinnerDropdown_bgId);
		else
			item.setDropdownBackgroundColor(spinnerDropdown_bgColor);
		item.setDropdownDividerResource(spinnerDropdown_dividerId);
		item.setDropdownSelectorResource(spinnerDropdown_selectorId);
		item.setDropdownAnimationStyle(spinnerDropdown_anim_top, spinnerDropdown_anim_bottom);
		
		item.setCategory(category);
		item.setCorner(corner);
		item.setTitle(title);
		item.setVisible(visible);
		item.setContextual(contextual);
		item.setId(id);
		addActionBarItem(item);
		
		return item;
	}
	
	private ActionBarSearchItem addActionBarSearchItem(int iconId, int category, int corner, String title, int id, boolean collapse, boolean overlap, boolean onTop, boolean visible, boolean contextual){
		ActionBarSearchItem item = new ActionBarSearchItem(getContext());
		item.setSupportWidth(actionButtonWidth);
		
		ActionBarCollapseButton button = new ActionBarCollapseButton(getContext());
		button.setSupportWidth(actionButtonWidth);
		button.setImageResource(iconId);
		button.setOnClickListener(listener_collapse_button);
		button.setOnLongClickListener(listener_button_long);
		if(actionButton_bgId != 0)
			button.setBackgroundResource(actionButton_bgId);
		else
			button.setBackgroundColor(actionButton_bgColor);
		item.setButton(button);
		
		item.setCategory(category);
		item.setCorner(corner);
		item.setTitle(title);
		item.setVisible(visible);
		item.setContextual(contextual);
		item.setId(id);
		item.setCollapseMode(collapse);
		item.setOverlapMode(overlap);
		item.setOnTop(onTop);
		if(actionButton_bgId != 0)
			item.setButtonBackgroundResource(actionButton_bgId);
		else
			item.setButtonBackgroundColor(actionButton_bgColor);		
		addActionBarItem(item);
		
		if(!item.isCollapseMode() && item.isOverlapMode()){
			if(uncollapseItems.isEmpty())
				memo_titleClickable = titleItem_clickable;
			
			titleItem.hideText();
		}
		
		return item;
	}
	
	public void removeItemFromXml(int id) {
        XmlResourceParser parser = null;
        try {
            parser = getContext().getResources().getLayout(id);
            AttributeSet attrs = Xml.asAttributeSet(parser);            
            removeItemFromXml(parser, attrs);
        } 
        catch (Exception e) {} 
        finally {
            if (parser != null) 
            	parser.close();
        }
    }
	
	private void removeItemFromXml(XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
        String tagName;
        boolean lookingForEndOfUnknownTag = false;
        String unknownTagName = null;

        ArrayList<Integer> list = new ArrayList<Integer>();
        ArrayList<Integer> list_contextual = new ArrayList<Integer>();
        
        // This loop will skip to the menu start tag
        do {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.getName();
                if (tagName.equals(XML_MENU)) {
                    eventType = parser.next();
                    break;
                }                
                throw new RuntimeException("Expecting menu, got " + tagName);
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
        
        boolean reachedEndOfMenu = false;
        while (!reachedEndOfMenu) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (lookingForEndOfUnknownTag) 
                        break;   
                    tagName = parser.getName();
                    if (tagName.equals(XML_ITEM)) {
                    	TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ActionBarItem);
                    	int itemId = a.getResourceId(R.styleable.ActionBarItem_id, 0);
                    	boolean contextual = a.hasValue(R.styleable.ActionBarItem_contextual) ? a.getBoolean(R.styleable.ActionBarItem_contextual, false) : false;
                    	
                    	if(contextual)
                    		list_contextual.add(itemId);
                    	else
                    		list.add(itemId);
                    	
                    	a.recycle();
                    }
                    else {
                        lookingForEndOfUnknownTag = true;
                        unknownTagName = tagName;
                    }
                    break;
                    
                case XmlPullParser.END_TAG:
                    tagName = parser.getName();
                    if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                        lookingForEndOfUnknownTag = false;
                        unknownTagName = null;
                    }                    
                    else if (tagName.equals(XML_MENU)) 
                        reachedEndOfMenu = true;
                    
                    break;
                    
                case XmlPullParser.END_DOCUMENT:
                    throw new RuntimeException("Unexpected end of document");
            }
            
            eventType = parser.next();
        }
        
        if(list.isEmpty() && list_contextual.isEmpty())
        	return;
        
        if(!list.isEmpty()){
        	int[] ids = new int[list.size()];
        	for(int i = 0; i < ids.length; i++)
        		ids[i] = list.get(i).intValue();
        	removeActionBarItems(ids);
        }
        
        if(!list_contextual.isEmpty()){
        	int[] ids = new int[list_contextual.size()];
        	for(int i = 0; i < ids.length; i++)
        		ids[i] = list_contextual.get(i).intValue();
        	removeContextualActionBarItems(ids);
        }
        
        requestLayout();
    }
		
	private void removeActionBarItems(int[] ids){
		for(int id : ids){
			boolean temp = ab_top.removeItem(id);
			if(!temp)
				temp = ab_bottom.removeItem(id);
		}
		
		if(uncollapseItems.isEmpty()){
			setTitleClickable(memo_titleClickable);
			titleItem.unhideText();
		}
	}
	
	private void removeContextualActionBarItems(int[] ids){
		for(int id : ids){
			boolean temp = ab_top_contextual.removeItem(id);
			if(!temp)
				temp = ab_bottom_contextual.removeItem(id);
		}
	}
		
	public void removeActionBarItems(IActionBarItem[] items){
		if(items == null)
			return;
		
		boolean removed = false;
		for(IActionBarItem item : items){
			switch (item.getCorner()) {
				case IActionBarItem.CORNER_TOP_LEFT:
				case IActionBarItem.CORNER_TOP_RIGHT:	
					if(item.isContextual())
						removed = ab_top_contextual.removeItem(item) ? true : removed;
					else
						removed = ab_top.removeItem(item) ? true : removed;
					break;
				case IActionBarItem.CORNER_BOTTOM_LEFT:
				case IActionBarItem.CORNER_BOTTOM_RIGHT:	
					if(item.isContextual())
						removed = ab_bottom_contextual.removeItem(item) ? true : removed;
					else
						removed = ab_bottom.removeItem(item) ? true : removed;
					break;
			}
		}
		
		if(removed)
			requestLayout();
	}
	
	private void actionButtonClicked(View v){
		if(v == overflowButton)
			overflowButton.showDropdownMenu();	
		else if(v == contextualOverflowButton)
			contextualOverflowButton.showDropdownMenu();
		else if(v == contextualDoneButton)
			hideContextualActionBar();
		else if(v == titleItem){
			synchronized (uncollapseItems) {
				if(!uncollapseItems.isEmpty()){
					ActionBarCollapseItem item = uncollapseItems.remove(uncollapseItems.size() - 1);					
					if(item.isOnTop() && (item.getCorner() == IActionBarItem.CORNER_BOTTOM_LEFT || item.getCorner() == IActionBarItem.CORNER_BOTTOM_RIGHT)){
						if(item.isContextual()){							
							ab_top_contextual.removeItem(item);
							ab_bottom_contextual.addItem(item.getMenoIndex(), item);
						}
						else{
							ab_top.removeItem(item);
							ab_bottom.addItem(item.getMenoIndex(), item);
						}
					}
					
					item.setCollapseMode(true);
					
					if(uncollapseItems.isEmpty()){
						setTitleClickable(memo_titleClickable);
						titleItem.unhideText();
					}
				}
				else
					dispatcher.actionBarItemClicked(v.getId(), (IActionBarItem)v);
				
				uncollapseItems.notify();
			}	
		}
		else
			dispatcher.actionBarItemClicked(v.getId(), (IActionBarItem)v);
	}
	
	private void collapseButtonClicked(View v){
		ActionBarCollapseButton button = (ActionBarCollapseButton)v;
		ActionBarCollapseItem item = button.getCollapseItem();
		
		if(item.isOnTop() && (item.getCorner() == IActionBarItem.CORNER_BOTTOM_LEFT || item.getCorner() == IActionBarItem.CORNER_BOTTOM_RIGHT)){
			if(item.isContextual()){
				item.setMemoIndex(ab_bottom_contextual.indexOfItem(item));
				ab_bottom_contextual.removeItem(item);
				ab_top_contextual.addItem(item);
			}
			else{
				item.setMemoIndex(ab_bottom.indexOfItem(item));
				ab_bottom.removeItem(item);
				ab_top.addItem(item);
			}
		}
		
		synchronized (uncollapseItems) {
			if(uncollapseItems.isEmpty())
				memo_titleClickable = titleItem_clickable;			
			uncollapseItems.add(item);
			uncollapseItems.notify();
		}			
		
		item.setCollapseMode(false);		
		setTitleClickable(true);
		titleItem.hideText();
	}
	
	private void actionButtonLongClicked(View v){
		if(v != overflowButton){
			int corner = ((IActionBarItem)v).getCorner();
			if(corner == IActionBarItem.CORNER_TOP_LEFT || corner == IActionBarItem.CORNER_TOP_RIGHT){
				Toast toast =Toast.makeText(getContext(), ((IActionBarItem)v).getTitle(), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, ab_top_height);
				toast.show();
			}
			else{
				Toast toast =Toast.makeText(getContext(), ((IActionBarItem)v).getTitle(), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, ab_bottom_height);
				toast.show();
			}
				
		}
	}
	
	private void overflowItemClicked(int index){
		ActionBarButton button = overflowButton.getItem(index);
		if(button != null)
			actionButtonClicked(button);
	}
	
	private void ContextualOverflowItemClicked(int index){
		ActionBarButton button = contextualOverflowButton.getItem(index);
		if(button != null)
			actionButtonClicked(button);
	}
		
}

class ListenerDispatcher {
    
	private final ArrayList<WeakReference<ActionBarLayout.ActionBarListener>> list_listeners = new ArrayList<WeakReference<ActionBarLayout.ActionBarListener>>();
	
    public synchronized void registerListener(ActionBarLayout.ActionBarListener listener){
    	boolean existed = false;
    	for(int i = list_listeners.size() - 1; i >= 0; i--){
    		WeakReference<ActionBarLayout.ActionBarListener> ref = list_listeners.get(i);
    		if(ref.get() == null)
    			list_listeners.remove(i);
    		else if(ref.get() == listener)
    			existed = true;    				
    	}
    	
    	if(!existed)
    		list_listeners.add(new WeakReference<ActionBarLayout.ActionBarListener>(listener));    	
    }
    
    public synchronized void unregisterListener(ActionBarLayout.ActionBarListener listener){
    	for(int i = list_listeners.size() - 1; i >= 0; i--){
    		WeakReference<ActionBarLayout.ActionBarListener> ref = list_listeners.get(i);
    		if(ref.get() == null || ref.get() == listener)
    			list_listeners.remove(i);
    	}          
    }
    
    public synchronized void actionBarItemClicked(int id, IActionBarItem item){
    	for(int i = list_listeners.size() - 1; i >= 0; i--){
    		WeakReference<ActionBarLayout.ActionBarListener> ref = list_listeners.get(i);
    		if(ref.get() == null)
    			list_listeners.remove(i);
    		else 
    			ref.get().actionBarItemClicked(id, item);
    	}
    	
    }
    
    public synchronized void contextualModeChanged(boolean mode){
    	for(int i = list_listeners.size() - 1; i >= 0; i--){
    		WeakReference<ActionBarLayout.ActionBarListener> ref = list_listeners.get(i);
    		if(ref.get() == null)
    			list_listeners.remove(i);
    		else 
    			ref.get().contextualModeChanged(mode);
    	}
    }
    
}

class SlideAnimation extends Animation {
	
	private View view;
	private int distance = 0;
	
	private byte type;
	public static final byte SLIDE_OUT = 1;
	public static final byte SLIDE_IN = 0;
	
	private byte margin;
	public static final byte MARGIN_BOTTOM = 0;
	public static final byte MARGIN_TOP = 1;
	public static final byte MARGIN_LEFT = 2;
	public static final byte MARGIN_RIGHT = 3;
	
	private ViewGroup.LayoutParams mLayoutParams;

	/**
	* Initializes expand collapse animation, has two types, collapse (1) and expand (0).
	 * @param view The view to animate
	 * @param duration
	 * @param type The type of animation: 0 will expand from gone and 0 size to visible and layout size defined in xml.
	 * 1 will collapse view and set to gone
	 */
	public SlideAnimation(View view, int duration, byte type, byte margin) {
		setDuration(duration);
		this.view = view;		
		if(margin == MARGIN_BOTTOM || margin == MARGIN_TOP)
			distance = view.getMeasuredHeight();
		else
			distance = view.getMeasuredWidth();
		mLayoutParams = view.getLayoutParams();
		this.type = type;
		this.margin = margin;
		if(type == SLIDE_IN){
			setMargin(-distance, margin);
			view.setVisibility(View.INVISIBLE);
			view.requestLayout();
		}		
	}
	
	private void setMargin(int value, byte margin){
		if(mLayoutParams instanceof LinearLayout.LayoutParams)
			switch(margin){
				case MARGIN_BOTTOM:
					((LinearLayout.LayoutParams)mLayoutParams).bottomMargin = value;
					break;
				case MARGIN_TOP:
					((LinearLayout.LayoutParams)mLayoutParams).topMargin = value;
					break;
				case MARGIN_LEFT:
					((LinearLayout.LayoutParams)mLayoutParams).leftMargin = value;
					break;
				case MARGIN_RIGHT:
					((LinearLayout.LayoutParams)mLayoutParams).rightMargin = value;
					break;	
			}
			
		else if(mLayoutParams instanceof RelativeLayout.LayoutParams)
			switch(margin){
				case MARGIN_BOTTOM:
					((RelativeLayout.LayoutParams)mLayoutParams).bottomMargin = value;
					break;
				case MARGIN_TOP:
					((RelativeLayout.LayoutParams)mLayoutParams).topMargin = value;
					break;
				case MARGIN_LEFT:
					((RelativeLayout.LayoutParams)mLayoutParams).leftMargin = value;
					break;
				case MARGIN_RIGHT:
					((RelativeLayout.LayoutParams)mLayoutParams).rightMargin = value;
					break;	
			}
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {		
		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {	
			if(type == SLIDE_IN) 
				setMargin(-distance + (int)(distance * interpolatedTime), margin);
			else
				setMargin((int)-(distance * interpolatedTime), margin);		
		} else {
			if(type == SLIDE_IN) 
				view.setVisibility(View.VISIBLE);						
			else						
				view.setVisibility(View.GONE);
			setMargin(0, margin);			
		}
		view.requestLayout();
	}
}
