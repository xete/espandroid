package com.xete.esptiny;

import com.espressif.iot.esptouch.esptouch_activity.EsptouchActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Space;
import android.widget.Toast;
import android.view.View;
import android.view.Gravity;
import android.view.MotionEvent;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.StringBuilder;
import java.net.Socket;
import java.lang.Math;

public class ControlActivity extends Activity implements OnSeekBarChangeListener
{
	private final static String TAG = "ControlActivity";	
	private ListView listViewStatus;
	private TextView textViewControlInfo;
	private ArrayAdapter<String> listAdapter;
	private Space spaceGuiding;
	private Handler mHandler;
	private ArrayList<Socket> mSocket = null;
	private XSocketServer mServer = null;
	private SeekBar mSeekBarSpeed;
	private SeekBar mSeekBarHeading;
	private int[] mSpacePos;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		initUi();
		mSocket = new ArrayList<Socket>();
		if(mServer == null) {
			mServer = new XSocketServer(getApplicationContext(), 26666, 12000);
			mHandler = new Handler();
			if(mServer != null) {
				textViewControlInfo.setText("Server Up");
			}
			else {
				textViewControlInfo.setText("Server setup failed");
			}
			addListenerTo(mServer);
			mServer.execute();
		}
		if(mServer != null) {
			textViewControlInfo.setText(textViewControlInfo.getText().toString()+"\n"+mServer.getAddress()+":"+mServer.getPort());
		}
    }

	@Override
	public void finish() {
		/**
		 * override finish transition
		 * override should not be applied before finish(), just after
		 * as the official document says
		 * the two resid together only specify the trasitios of the
		 * ingoing and outgoing views,
		 * if you want to implemet on both the in and out transitions
		 * just specify the two transitions onCreate and finish 
		 * overridePendingTransition(int resid, int resid);
		 */
		super.finish();
		Log.v(TAG, "finish");
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if(mSpacePos == null) {
			mSpacePos = getViewPosition(spaceGuiding);
			Log.v(TAG, "mSpacePos: ("+mSpacePos[0]+","+mSpacePos[1]+","+mSpacePos[2]+","+mSpacePos[3]+")");
		}
		if( e.getY() > mSpacePos[1] ) {
			sendToClient(parseDirection(e.getX() - mSpacePos[0] - mSpacePos[2]/2, e.getY() - mSpacePos[1] - mSpacePos[3]/2));
			Log.v(TAG, "onTouchEvent: "+String.valueOf((int)e.getX())+", "+String.valueOf((int)e.getY()));	
		} else {
			Log.v(TAG, "outside space area");
		}
		return true;
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(seekBar == mSeekBarSpeed) {
			Log.v(TAG, "mSeekBarSpeed "+String.valueOf(progress));
			sendToClient("speed: "+String.valueOf(progress)+" ");	
		} else if(seekBar == mSeekBarHeading) {
			Log.v(TAG, "mSeekBarHeading "+String.valueOf(progress));
			sendToClient("heading: "+String.valueOf(progress)+" ");	
		}
		return ;	
	}
	public void onStartTrackingTouch(SeekBar seekBar) { }
	public void onStopTrackingTouch(SeekBar seekBar) { }
	
	public void initUi() {
        setContentView(R.layout.control);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		listViewStatus = (ListView)findViewById(R.id.listViewSensorStatus);	
		textViewControlInfo = (TextView)findViewById(R.id.textViewControlInfo);
		spaceGuiding = (Space)findViewById(R.id.spaceGuiding);
		mSeekBarSpeed = (SeekBar)findViewById(R.id.seekBarSpeed);
		mSeekBarHeading = (SeekBar)findViewById(R.id.seekBarHeading);
		ArrayList<String> statusList = new ArrayList<String>();
		statusList.addAll(Arrays.asList(new String[] {
			"action: ", "infrared: ", "distance: ", "direction: "
		}));
		listAdapter = new ArrayAdapter<String>(this, R.layout.stat_view, statusList);
		listViewStatus.setAdapter(listAdapter);	
		mSeekBarSpeed.setOnSeekBarChangeListener(this);
		mSeekBarHeading.setOnSeekBarChangeListener(this);
	}

	public int[] getViewPosition(View v) {
		/* MeasureSpec no effect */
		return new int[] {
		 v.getLeft(),
		 v.getTop(),
		 v.getWidth(),
		 v.getHeight()
		};
	}

	public String parseDirection(double x, double y) {
		final String direct[] = new String[] {
			"West", "South-West", "South", "South-East",
			"East", "North-East", "North", "North-West"
		};
		if( x == 0 ) {
			if( y >= 0 ) return "North";	
			else return "South";
		}
		double ang = Math.atan2(x, y);	
		double thr = Math.toDegrees(ang);
		return direct[(int)((thr-22.5+180)/45)];
	}

	public void sendToClient(String str) {
		if(mServer == null || mSocket == null) return;
		Socket c = null;
		if(mSocket.size() > 0) {
			c = mSocket.get(0);
		}
		if(c == null || c.isClosed()) return;
		try {
			String s = mServer.getAddress()+":"+mServer.getPort()+"->"+str;
			Log.v(TAG, s);
			c.getOutputStream().write(s.getBytes("UTF-8"));
		} catch(Exception e) {
			Log.v(TAG, "sendToClient failed with "+str);
			e.printStackTrace();
		}
	}

	public void addListenerTo(XSocketServer serv) {
		serv.addListener(new XSocketServer.IXSocketServer() {
			@Override
			public void onConnected(Socket socket) {
				/* final here for inner class access */
				final Socket s = socket;
				mSocket.add(socket);
				Log.v(TAG, "onConnected ");
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), s.getInetAddress()+":"+s.getPort()+" connected", Toast.LENGTH_SHORT).show();
					}
						
				});
			}
			@Override
			public void onRead(Socket socket, byte[] rb, int len) {
				Log.v(TAG, "onRead");
				/* update sensor status */
				final int leng = len;
				final String[] status = new String[] {"action: ", "infrared: ", "distance: ", "direction: "};
				final byte[] rbf = rb;
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						StringBuilder sb;
						ArrayList<String> s = new ArrayList<String>();
						int offset = 0;
						int row = 0;
						for(int i = 0; i < leng; i++) {
							if(rbf[i] == '\n') 	{
								sb = new StringBuilder();
								sb.append(status[row]);
								sb.append(new String(rbf, offset, i-offset));
								i++;
								offset = i;
								row++;
								s.add(sb.toString());
								if(row >= 4) break;
							}
						}	
						for(int i = row; i < 4; i++) {
							s.add(status[i]+"null");
						}
						listViewStatus.setAdapter(new ArrayAdapter<String>(ControlActivity.this.getApplicationContext(), R.layout.stat_view, s));
					}
				});

			}
			@Override
			public void onDisconnected(Socket socket) {
				Log.v(TAG, "onDisconnected");
				final Socket s = socket;
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						ArrayList<String> status = new ArrayList<String>();
						status.add("action: ");
						status.add("infrared: ");
						status.add("distance: ");
						status.add("direction: ");
						listViewStatus.setAdapter(new ArrayAdapter<String>(ControlActivity.this.getApplicationContext(), R.layout.stat_view, status));
						Toast.makeText(getApplicationContext(), s.getInetAddress()+":"+s.getPort()+" disconnected", Toast.LENGTH_SHORT).show();	
					}
						
				});
				mSocket.remove(socket);
			}
		});
	}
}
