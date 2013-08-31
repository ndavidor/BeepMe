package il.ac.huji.beepme.ui;

import il.ac.huji.beepme.business.BeepMeApplication;
import il.ac.huji.beepme.db.Queue;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import il.ac.huji.beepme.business.R;

public class QrCodeFragment extends Fragment implements Queue.OnQueueStatusChangedListener{

	private ImageView iv_code;
	private Button bt_print;
	
	private Queue queue;
	private int nextNumber;
	private String nextUID;
	
	private Handler mHandler;
	
	public interface QrCodeListener{		
		public void printTicket(String name, int number, String uid);
	}
	
	private WeakReference<QrCodeListener> listener;
		
	public static QrCodeFragment newInstance(){
		QrCodeFragment fragment = new QrCodeFragment();
				
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_qrcode, container, false);
		
		iv_code = (ImageView)v.findViewById(R.id.qrcode_iv);
		
		bt_print = (Button)v.findViewById(R.id.qrcode_bt_print);
		bt_print.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				bt_printClicked();
			}
		});
		
		queue = BeepMeApplication.workingQueue;
		queue.addOnQueueStatusChangedListener(this);
				
		try {
			setImageCode();
		} catch (WriterException e) {}
		
		
		mHandler = new Handler();
		
		return v;
	}
	
	public void onResume(){
		super.onResume();		
	}
		
	public void onPause(){
		super.onPause();			
	}
	
	public void onDestroy(){
		super.onDestroy();
		queue.removeOnQueueStatusChangedListener(this);
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		if(activity instanceof QrCodeListener)
			listener = new WeakReference<QrCodeListener>((QrCodeListener)activity);
	}
	
	protected void showInfoDialog(String title, int iconID, String message, String okText){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
	    ft.add(dialog, InfoFragment.class.getName());
	    ft.commitAllowingStateLoss();
	}
	
	private void bt_printClicked(){
		if(listener != null && listener.get() != null)
			listener.get().printTicket(queue.getName(), nextNumber, nextUID);
	}
	
	private String generateUID(){
		return System.currentTimeMillis() + "";
	}
	
	private String generateCode(){
		nextNumber = queue.getTotal() + 1;
		nextUID = generateUID();
		return nextNumber + ":" + nextUID + ":" + queue.getBussinessID() + ":" + queue.getName();
	}
	
	private void setImageCode() throws WriterException{		
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix result = writer.encode(generateCode(), BarcodeFormat.QR_CODE, 256, 256);
		
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        
        int BLACK = 0xFF000000;
        int WHITE = 0xFFFFFFFF;
        
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) 
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        iv_code.setImageBitmap(bitmap);
	}

	@Override
	public void onQueueTotalChanged(int total) {
		if(total >= nextNumber){			
			mHandler.post(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(getActivity(), "Thank you! Please wait to your turn!", Toast.LENGTH_SHORT).show();
					
					getActivity().onBackPressed();
				}
			});
		}
	}

	@Override
	public void onQueueDeleted() {
		showInfoDialog("Sorry", android.R.drawable.ic_dialog_alert, "This queue is not available anymore!\nPlease select another one!", "OK");
		mHandler.post(new Runnable() {				
			@Override
			public void run() {
				getActivity().onBackPressed();
			}
		});
	}
}
