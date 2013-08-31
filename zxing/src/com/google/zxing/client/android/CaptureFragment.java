package com.google.zxing.client.android;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.R;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.history.HistoryManager;
import com.google.zxing.client.android.result.ResultButtonListener;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.client.android.result.supplement.SupplementalInfoRetriever;

public class CaptureFragment extends Fragment implements ICaptureProvider, SurfaceHolder.Callback{
	
	private static final String TAG = CaptureFragment.class.getSimpleName();

	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 0L;
	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	private static final String PACKAGE_NAME = "com.google.zxing.client.android";
	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
	      EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
	                 ResultMetadataType.SUGGESTED_PRICE,
	                 ResultMetadataType.ERROR_CORRECTION_LEVEL,
	                 ResultMetadataType.POSSIBLE_COUNTRY);

	private CameraManager cameraManager;
	private CaptureFragmentHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private View resultView;
	private SurfaceView surfaceView;
	private Result lastResult;
	private boolean hasSurface;
	private boolean copyToClipboard;
	private IntentSource source;
	private String sourceUrl;
	private ScanFromWebPageManager scanFromWebPageManager;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType,?> decodeHints;
	private String characterSet;
	private HistoryManager historyManager;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	
	private Intent intent;
	
	private boolean showRawCode = true;
	
	public interface CaptureListener{
		public void codeCaptured(Intent data);
	}
	
	public void setIntent(Intent intent){
		this.intent = intent;
	}
	
	public Intent getIntent(){
		if(intent == null)
			intent = new Intent();
		
		return intent;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
	    return handler;
	}

	public CameraManager getCameraManager() {
	    return cameraManager;
	}
	
	public int getRotateHint(){
		if(getActivity() != null){
			Display display = ((WindowManager)getActivity().getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
			if(display.getRotation() == Surface.ROTATION_0)
				return ROTATE_90;
		    else if(display.getRotation() == Surface.ROTATION_90)
		    	return ROTATE_NONE;
		    else if(display.getRotation() == Surface.ROTATION_180)
		    	return ROTATE_270;
		    else if(display.getRotation() == Surface.ROTATION_270)
		    	return ROTATE_180;
		}
		
		return ROTATE_NONE;
	}
	
	/**
	 * @param return Result deliver result back to activity or not
	 * @param displayDuration If set to 0, then wait to user click on view before continue scan 
	 * @return
	 */
	public static final CaptureFragment newInstance(boolean returnResult, int width, int height, boolean saveHistory, String promptMessage, long displayDuration){
		CaptureFragment fragment = new CaptureFragment();
		Intent intent = new Intent();
		if(returnResult)
			intent.setAction(Intents.Scan.ACTION);
		
		if(width > 0 && height > 0){
			intent.putExtra(Intents.Scan.WIDTH, width);
			intent.putExtra(Intents.Scan.HEIGHT, height);
		}
		
		intent.putExtra(Intents.Scan.SAVE_HISTORY, saveHistory);
		
		if(promptMessage != null)
			intent.putExtra(Intents.Scan.PROMPT_MESSAGE, promptMessage);
		
		intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, displayDuration);
		
		fragment.setIntent(intent);		
		return fragment;
	}
	
	public static final CaptureFragment newInstance(boolean returnResult, int width, int height, boolean saveHistory, String promptMessage){
		CaptureFragment fragment = new CaptureFragment();
		Intent intent = new Intent();
		if(returnResult)
			intent.setAction(Intents.Scan.ACTION);
		
		if(width > 0 && height > 0){
			intent.putExtra(Intents.Scan.WIDTH, width);
			intent.putExtra(Intents.Scan.HEIGHT, height);
		}
		
		intent.putExtra(Intents.Scan.SAVE_HISTORY, saveHistory);
		
		if(promptMessage != null)
			intent.putExtra(Intents.Scan.PROMPT_MESSAGE, promptMessage);
		
		fragment.setIntent(intent);		
		return fragment;
	}
	
	public static final CaptureFragment newInstance(boolean returnResult, boolean saveHistory, String promptMessage){
		CaptureFragment fragment = new CaptureFragment();
		Intent intent = new Intent();
		if(returnResult)
			intent.setAction(Intents.Scan.ACTION);
				
		intent.putExtra(Intents.Scan.SAVE_HISTORY, saveHistory);
		
		if(promptMessage != null)
			intent.putExtra(Intents.Scan.PROMPT_MESSAGE, promptMessage);
		
		fragment.setIntent(intent);
		return fragment;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		    
		View v = inflater.inflate(R.layout.capture, container, false);
		viewfinderView = (ViewfinderView)v.findViewById(R.id.viewfinder_view);
		resultView = v.findViewById(R.id.result_view);
		statusView = (TextView)v.findViewById(R.id.status_view);
		surfaceView = (SurfaceView)v.findViewById(R.id.preview_view);
		 
		viewfinderView.setClickable(true);
		viewfinderView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				viewfinderViewClicked();
			}
		});
		
		hasSurface = false;
	    historyManager = new HistoryManager(getActivity());
	    historyManager.trimHistory();
	    inactivityTimer = new InactivityTimer(getActivity());
	    beepManager = new BeepManager(getActivity());
	    ambientLightManager = new AmbientLightManager(getActivity());
	    
	    PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);	    
	    
	    if(savedInstanceState != null){
	    	Intent intent = new Intent();
			if(savedInstanceState.getBoolean(Intents.Scan.ACTION, false))
				intent.setAction(Intents.Scan.ACTION);
			
			int width = savedInstanceState.getInt(Intents.Scan.WIDTH);
			int height = savedInstanceState.getInt(Intents.Scan.HEIGHT);
			if(width > 0 && height > 0){
				intent.putExtra(Intents.Scan.WIDTH, width);
				intent.putExtra(Intents.Scan.HEIGHT, height);
			}
			
			intent.putExtra(Intents.Scan.SAVE_HISTORY, savedInstanceState.getBoolean(Intents.Scan.SAVE_HISTORY, false));
			
			String promptMessage = savedInstanceState.getString(Intents.Scan.PROMPT_MESSAGE);
			if(promptMessage != null)
				intent.putExtra(Intents.Scan.PROMPT_MESSAGE, promptMessage);
			
			intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, savedInstanceState.getLong(Intents.Scan.RESULT_DISPLAY_DURATION_MS, DEFAULT_INTENT_RESULT_DURATION_MS));
			
	    	setIntent(intent);
	    }
	    return v;
	}		
	
	public void setPromptMessage(String promptMessage){
		getIntent().putExtra(Intents.Scan.PROMPT_MESSAGE, promptMessage);
		if(statusView != null && !waitingUserClick()){
			if(promptMessage == null)
				statusView.setText(R.string.msg_default_status);
			else 
				statusView.setText(promptMessage); 	    	
		}			
	}
	
	public String getPromptMessage(){
		if(intent != null)
			return intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
		else
			return getString(R.string.msg_default_status);
	}
	
	public boolean isSavedHistory(){
		if(intent != null)
			return intent.getBooleanExtra(Intents.Scan.SAVE_HISTORY, false);
		
		return false;
	}
	
	public int getPrefWidth(){
		if(intent != null)
			return intent.getIntExtra(Intents.Scan.WIDTH, 0);
		
		return 0;
	}
	
	public int getPrefHeight(){
		if(intent != null)
			return intent.getIntExtra(Intents.Scan.HEIGHT, 0);
		
		return 0;
	}
	
	public boolean isReturnResult(){
		if(intent != null)
			return intent.getAction() == Intents.Scan.ACTION;
		
		return false;
	}
	
	public long getResultDurationMS(){
		if(intent != null)
			return intent.getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, DEFAULT_INTENT_RESULT_DURATION_MS);
		
		return DEFAULT_INTENT_RESULT_DURATION_MS;
	}

	public void setTorch(boolean open){
		cameraManager.setTorch(open);
	}
	
	private boolean waitingUserClick(){
		if(lastResult != null){
			switch (source) {
				case NATIVE_APP_INTENT:
				case PRODUCT_SEARCH_LINK:	    			
					if(getResultDurationMS() <= 0)
						return true;
				case ZXING_LINK:
					if (scanFromWebPageManager == null || !scanFromWebPageManager.isScanFromWebPage()){
						if(getResultDurationMS() <= 0)
							return true;
					}
					break;
			}
		}
		
		return false;
	}
	
	private void viewfinderViewClicked(){
		if(waitingUserClick())
			restartPreviewAfterDelay(0L);		
	}

	@Override
	public void onResume() {
		super.onResume();

	    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
	    // want to open the camera driver and measure the screen size if we're going to show the help on
	    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
	    // off screen.
	    cameraManager = new CameraManager(getActivity().getApplication());
	    
	    viewfinderView.setCameraManager(cameraManager);

	    handler = null;
	    lastResult = null;

	    resetStatusView();
	    
	    SurfaceHolder surfaceHolder = surfaceView.getHolder();
	    if (hasSurface) {
	    	// The activity was paused but not stopped, so the surface still exists. Therefore
	    	// surfaceCreated() won't be called, so init the camera here.
	    	initCamera(surfaceView);
	    } else {
	    	// Install the callback and wait for surfaceCreated() to init the camera.
	    	surfaceHolder.addCallback(this);
	    	surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    beepManager.updatePrefs();
	    ambientLightManager.start(cameraManager);

	    inactivityTimer.onResume();

	    Intent intent = getIntent();

	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    copyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true)
	        && (intent == null || intent.getBooleanExtra(Intents.Scan.SAVE_HISTORY, true));

	    source = IntentSource.NONE;
	    decodeFormats = null;
	    characterSet = null;

	    if (intent != null) {	    	
	    	String action = intent.getAction();
	    	String dataString = intent.getDataString();

	        if (Intents.Scan.ACTION.equals(action)) {
	        	// Scan the formats the intent requested, and return the result to the calling activity.
	        	source = IntentSource.NATIVE_APP_INTENT;
	        	decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
	        	decodeHints = DecodeHintManager.parseDecodeHints(intent);

	        	if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
	        		int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
	        		int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
	        		if (width > 0 && height > 0) 
	        			cameraManager.setManualFramingRect(width, height);	        	
	        	}
	        
	        	statusView.setText(getPromptMessage());  
	        } 
	        else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) &&  dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {
	        	// Scan only products and send the result to mobile Product Search.
	        	source = IntentSource.PRODUCT_SEARCH_LINK;
	        	sourceUrl = dataString;
	        	decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
	        } 
	        else if (isZXingURL(dataString)) {
	        	// Scan formats requested in query string (all formats if none specified).
	        	// If a return URL is specified, send the results there. Otherwise, handle it ourselves.
	        	source = IntentSource.ZXING_LINK;
	        	sourceUrl = dataString;
	        	Uri inputUri = Uri.parse(dataString);
	        	scanFromWebPageManager = new ScanFromWebPageManager(inputUri);
	        	decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
	        	// Allow a sub-set of the hints to be specified by the caller.
	        	decodeHints = DecodeHintManager.parseDecodeHints(inputUri);
	        }
	        
	        characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
	    }
	}
	  
	private static boolean isZXingURL(String dataString) {
		if (dataString == null) 
			return false;	    
	    for (String url : ZXING_URLS) 
	    	if (dataString.startsWith(url)) 
	    		return true;
	    
	    return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		
	    if (handler != null) {
	      handler.quitSynchronously();
	      handler = null;
	    }
	    
	    inactivityTimer.onPause();
	    ambientLightManager.stop();
	    cameraManager.closeDriver();
	    
	    if (!hasSurface) {
	      SurfaceHolder surfaceHolder = surfaceView.getHolder();
	      surfaceHolder.removeCallback(this);
	    }
	    
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	    inactivityTimer.shutdown();	    
	}

	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putBoolean(Intents.Scan.ACTION, isReturnResult());
		outState.putInt(Intents.Scan.WIDTH, getPrefWidth());
		outState.putInt(Intents.Scan.HEIGHT, getPrefHeight());
		outState.putBoolean(Intents.Scan.SAVE_HISTORY, isSavedHistory());
		outState.putString(Intents.Scan.PROMPT_MESSAGE, getPromptMessage());
		outState.putLong(Intents.Scan.RESULT_DISPLAY_DURATION_MS, getResultDurationMS());
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null)
			savedResultToShow = result;
		else {
			if (result != null) 
				savedResultToShow = result;
			  
			if(savedResultToShow != null){
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			  
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) 
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
	    
		if (!hasSurface) {
			hasSurface = true;
			initCamera(surfaceView);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show the results.
	 *
	 * @param rawResult The contents of the barcode.
	 * @param scaleFactor amount by which thumbnail was scaled
	 * @param barcode   A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), rawResult);

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			historyManager.addHistoryItem(rawResult, resultHandler);
			// Then not from history, so beep/vibrate and we have an image to draw on
			beepManager.playBeepSoundAndVibrate();
			drawResultPoints(barcode, scaleFactor, rawResult);
		}

		switch (source) {
	    	case NATIVE_APP_INTENT:
	      	case PRODUCT_SEARCH_LINK:
	      		handleDecodeExternally(rawResult, resultHandler, barcode);
	      		break;
	      	case ZXING_LINK:
	      		if (scanFromWebPageManager == null || !scanFromWebPageManager.isScanFromWebPage())
	      			handleDecodeInternally(rawResult, resultHandler, barcode);
	      		else
	      			handleDecodeExternally(rawResult, resultHandler, barcode);	        
	        break;
	      	case NONE:
	      		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	      		if (fromLiveScan && prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE, false)) {
	      			String message = getResources().getString(R.string.msg_bulk_mode_scanned) + " (" + rawResult.getText() + ')';
	      			Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	      			// Wait a moment or else it will scan the same barcode continuously about 3 times
	      			restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
	      		} else 
	      			handleDecodeInternally(rawResult, resultHandler, barcode);	      		
	        break;
	    }
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
	 *
	 * @param barcode   A bitmap of the captured image.
	 * @param scaleFactor amount by which thumbnail was scaled
	 * @param rawResult The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
	    ResultPoint[] points = rawResult.getResultPoints();
	    if (points != null && points.length > 0) {
	    	Canvas canvas = new Canvas(barcode);
	    	Paint paint = new Paint();
	    	paint.setColor(getResources().getColor(R.color.result_points));
	    	if (points.length == 2) {
	    		paint.setStrokeWidth(4.0f);
	    		drawLine(canvas, paint, points[0], points[1], scaleFactor);
	    	} else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
	    		// Hacky special case -- draw two lines, for the barcode and metadata
	    		drawLine(canvas, paint, points[0], points[1], scaleFactor);
	    		drawLine(canvas, paint, points[2], points[3], scaleFactor);
	    	} else {
	    		paint.setStrokeWidth(10.0f);
	    		for (ResultPoint point : points) 
	    			canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);	    		
	    	}
	    }
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
	    if (a != null && b != null) 
	    	canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);	    
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
	    statusView.setVisibility(View.GONE);
	    viewfinderView.setVisibility(View.GONE);
	    resultView.setVisibility(View.VISIBLE);

	    ImageView barcodeImageView = (ImageView) getView().findViewById(R.id.barcode_image_view);
	    if (barcode == null) 
	    	barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.launcher_icon));
	    else 
	    	barcodeImageView.setImageBitmap(barcode);	    

	    TextView formatTextView = (TextView) getView().findViewById(R.id.format_text_view);
	    formatTextView.setText(rawResult.getBarcodeFormat().toString());

	    TextView typeTextView = (TextView) getView().findViewById(R.id.type_text_view);
	    typeTextView.setText(resultHandler.getType().toString());

	    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	    String formattedTime = formatter.format(new Date(rawResult.getTimestamp()));
	    TextView timeTextView = (TextView) getView().findViewById(R.id.time_text_view);
	    timeTextView.setText(formattedTime);

	    TextView metaTextView = (TextView) getView().findViewById(R.id.meta_text_view);
	    View metaTextViewLabel = getView().findViewById(R.id.meta_text_view_label);
	    metaTextView.setVisibility(View.GONE);
	    metaTextViewLabel.setVisibility(View.GONE);
	    Map<ResultMetadataType, Object> metadata = rawResult.getResultMetadata();
	    if (metadata != null) {
	    	StringBuilder metadataText = new StringBuilder(20);
	    	for (Map.Entry<ResultMetadataType,Object> entry : metadata.entrySet()) {
	    		if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) 
	    			metadataText.append(entry.getValue()).append('\n');	    		
	    	}
	    	if (metadataText.length() > 0) {
	    		metadataText.setLength(metadataText.length() - 1);
	    		metaTextView.setText(metadataText);
	    		metaTextView.setVisibility(View.VISIBLE);
	    		metaTextViewLabel.setVisibility(View.VISIBLE);
	    	}
	    }

	    TextView contentsTextView = (TextView) getView().findViewById(R.id.contents_text_view);
	    CharSequence displayContents = resultHandler.getDisplayContents();
	    contentsTextView.setText(displayContents);
	    // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
	    int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
	    contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

	    TextView supplementTextView = (TextView) getView().findViewById(R.id.contents_supplement_text_view);
	    supplementTextView.setText("");
	    supplementTextView.setOnClickListener(null);
	    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PreferencesActivity.KEY_SUPPLEMENTAL, true)) 
	    	SupplementalInfoRetriever.maybeInvokeRetrieval(supplementTextView, resultHandler.getResult(), historyManager, getActivity());
	    
	    int buttonCount = resultHandler.getButtonCount();
	    ViewGroup buttonView = (ViewGroup) getView().findViewById(R.id.result_button_view);
	    buttonView.requestFocus();
	    for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
	    	TextView button = (TextView) buttonView.getChildAt(x);
	    	if (x < buttonCount) {
	    		button.setVisibility(View.VISIBLE);
	    		button.setText(resultHandler.getButtonText(x));
	    		button.setOnClickListener(new ResultButtonListener(this, resultHandler, x));
	    	} else 
	    		button.setVisibility(View.GONE);	    	
	    }

	    if (copyToClipboard && !resultHandler.areContentsSecure()) {
	    	ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
	    	if (displayContents != null) 
	    		try {
	    			clipboard.setText(displayContents);
	    		} catch (NullPointerException npe) {
	    			// Some kind of bug inside the clipboard implementation, not due to null input
	    			Log.w(TAG, "Clipboard bug", npe);
	    		}	    	
	    }
	}

	// Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
	private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {

	    if (barcode != null){	    	
	    	viewfinderView.drawResultBitmap(barcode);	    
	    }

	    long resultDurationMS = getResultDurationMS();  

	    String rawResultString = String.valueOf(rawResult);
    	if (rawResultString.length() > 32) 
    		rawResultString = rawResultString.substring(0, 32) + " ...";	   
    	
	    if (resultDurationMS > 0) 	    	 	
	    	statusView.setText(getString(resultHandler.getDisplayTitle()));
	    else
	    	statusView.setText(getString(resultHandler.getDisplayTitle()) + "\n" + getString(R.string.continue_scan));

	    if (copyToClipboard && !resultHandler.areContentsSecure()) {
	    	ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
	    	CharSequence text = resultHandler.getDisplayContents();
	    	if (text != null) 
	    		try {
	    			clipboard.setText(text);
	    		} catch (NullPointerException npe) {
	    			// Some kind of bug inside the clipboard implementation, not due to null input
	    			Log.w(TAG, "Clipboard bug", npe);
	    		}	      
	    }

	    if (source == IntentSource.NATIVE_APP_INTENT) {	      
	    	// Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
	    	// the deprecated intent is retired.
	    	Intent intent = new Intent(getIntent().getAction());
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	    	intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
	    	intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
	    	byte[] rawBytes = rawResult.getRawBytes();
	    	if (rawBytes != null && rawBytes.length > 0) 
	    		intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
	    	
	    	Map<ResultMetadataType,?> metadata = rawResult.getResultMetadata();
	    	if (metadata != null) {
	    		if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) 
	    			intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION, metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
	        
	    		Integer orientation = (Integer) metadata.get(ResultMetadataType.ORIENTATION);
	    		if (orientation != null) 
	    			intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
	    		
	    		String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
	    		if (ecLevel != null) 
	    			intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
	    		
	    		Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
	    		if (byteSegments != null) {
	    			int i = 0;
	    			for (byte[] byteSegment : byteSegments) {
	    				intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
	    				i++;
	    			}
	    		}
	      }
	    	
	     if(getActivity() != null && getActivity() instanceof CaptureFragment.CaptureListener)
	    	 ((CaptureFragment.CaptureListener)getActivity()).codeCaptured(intent);
	     
	     if(resultDurationMS > 0)
	    	 sendReplyMessage(R.id.return_scan_result, intent, resultDurationMS);
	      
	    } else if (source == IntentSource.PRODUCT_SEARCH_LINK) {	      
	    	// Reformulate the URL which triggered us into a query, so that the request goes to the same
	    	// TLD as the scan URL.
	    	int end = sourceUrl.lastIndexOf("/scan");
	    	String replyURL = sourceUrl.substring(0, end) + "?q=" + resultHandler.getDisplayContents() + "&source=zxing";      
	    	sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);	      
	    } else if (source == IntentSource.ZXING_LINK) {
	    	if (scanFromWebPageManager != null && scanFromWebPageManager.isScanFromWebPage()) {
	    		String replyURL = scanFromWebPageManager.buildReplyURL(rawResult, resultHandler);
	    		sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);
	    	}	      
	    }
	}
	  
	private void sendReplyMessage(int id, Object arg, long delayMS) {
	    Message message = Message.obtain(handler, id, arg);
	    if (delayMS > 0L) 
	      handler.sendMessageDelayed(message, delayMS);
	    else 
	      handler.sendMessage(message);	    
	}

	private void initCamera(SurfaceView view) {
	    if (view == null || view.getHolder() == null) 
	    	throw new IllegalStateException("No SurfaceHolder provided");	    
	    
	    if (cameraManager.isOpen()) {
	    	Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
	    	return;
	    }
	    
	    try {
	    	cameraManager.openDriver(getActivity(), view);
	    	// Creating the handler starts the preview, which can also throw a RuntimeException.
	    	if (handler == null) 
	    		handler = new CaptureFragmentHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
	    	
	    	decodeOrStoreSavedBitmap(null, null);
	    } catch (IOException ioe) {
	    	ioe.printStackTrace();
	    	Log.w(TAG, ioe);
	    	displayFrameworkBugMessageAndExit();
	    } catch (RuntimeException e) {
	    	// Barcode Scanner has seen crashes in the wild of this variety:
	    	// java.?lang.?RuntimeException: Fail to connect to camera service
	    	Log.w(TAG, "Unexpected error initializing camera", e);
	    	displayFrameworkBugMessageAndExit();
	    }
	}

	private void displayFrameworkBugMessageAndExit() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(getString(R.string.app_name));
	    builder.setMessage(getString(R.string.msg_camera_framework_bug));
	    builder.setPositiveButton(R.string.button_ok, new FinishListener(getActivity()));
	    builder.setOnCancelListener(new FinishListener(getActivity()));
	    builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
	    if (handler != null) 
	      handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
	    
	    resetStatusView();
	}

	private void resetStatusView() {
	    resultView.setVisibility(View.GONE);
    	statusView.setText(getPromptMessage());
	    statusView.setVisibility(View.VISIBLE);
	    viewfinderView.setVisibility(View.VISIBLE);
	    lastResult = null;
	}

	public void drawViewfinder() {
	    viewfinderView.drawViewfinder();
	}
}
