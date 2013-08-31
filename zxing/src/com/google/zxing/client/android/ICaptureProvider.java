package com.google.zxing.client.android;

import android.app.Activity;
import android.os.Handler;

import com.google.zxing.client.android.camera.CameraManager;

public interface ICaptureProvider {
	
	public static final int ROTATE_NONE = 0;
	public static final int ROTATE_90 = 1;
	public static final int ROTATE_180 = 2;
	public static final int ROTATE_270 = 4;

	public ViewfinderView getViewfinderView();

	public Handler getHandler();

	public CameraManager getCameraManager();
	
	public Activity getActivity();
	
	public void restartPreviewAfterDelay(long delayMS);
	
	public int getRotateHint();
}
