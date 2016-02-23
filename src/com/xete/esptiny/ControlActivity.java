package com.xete.esptiny;

import com.espressif.iot.esptouch.esptouch_activity.EsptouchActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SeekBar;
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

public class ControlActivity extends Activity
{
	private final static String TAG = "ControlActivity";	
	private ListView listViewStatus;
	private TextView textViewControlInfo;
	private ArrayAdapter<String> listAdapter;
	private Space spaceGuiding;
	private Handler mHandler;
	private ArrayList<Socket> mSocket = null;
	private XSocketServer mServer = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);
		listViewStatus = (ListView) findViewById(R.id.listViewSensorStatus);	
		textViewControlInfo = (TextView) findViewById(R.id.textViewControlInfo);
		spaceGuiding = (Space) findViewById(R.id.spaceGuiding);

		String[] status = new String[] {"action: ", "infrared: ", "distance: ", "direction: "};
		ArrayList<String> statusList = new ArrayList<String>();
		statusList.addAll(Arrays.asList(status));
		listAdapter = new ArrayAdapter<String>(this, R.layout.stat_view, statusList);
		listViewStatus.setAdapter(listAdapter);	
		mSocket = new ArrayList<Socket>();
		if(mServer == null) {
			mServer = new XSocketServer(getApplicationContext(), 26666);
			mHandler = new Handler();
			mServer.addListener(new XSocketServer.IXSocketServer() {
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
			mServer.execute();
		}
    }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		textViewControlInfo.setText(getMotionEventPosition(e));
		return true;
	}

	private String getMotionEventPosition(MotionEvent e) {
		String infoPosition = "";
		switch(e.getAction()) {
			case MotionEvent.ACTION_DOWN:	
				infoPosition += "x: "+String.valueOf((int)e.getX())+", y: "+String.valueOf((int)e.getY())+" DOWN";
				break;
			case MotionEvent.ACTION_UP:	
				infoPosition += "x: "+String.valueOf((int)e.getX())+", y: "+String.valueOf((int)e.getY())+" UP";
				break;
			case MotionEvent.ACTION_MOVE:	
				infoPosition += "x: "+String.valueOf((int)e.getX())+", y: "+String.valueOf((int)e.getY())+" MOVE";
				break;
		}
		return infoPosition;
	}

	public void showViewPosition(Context context, View... views) {
		Toast toast;
		StringBuilder sb = new StringBuilder();
		sb.append("positions:\n");
		for(View v : views) {
			sb.append(String.valueOf(v.getLeft()));
			sb.append(", ");
			sb.append(String.valueOf(v.getTop()));
			sb.append("\n");
		}
		toast = Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT);
		toast.show();
	}

}
