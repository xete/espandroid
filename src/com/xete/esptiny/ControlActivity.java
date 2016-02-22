package com.xete.esptiny;

import com.espressif.iot.esptouch.esptouch_activity.EsptouchActivity;
import android.app.Activity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.StringBuilder;

public class ControlActivity extends Activity
{
	
	private ListView listViewStatus;
	private TextView textViewControlInfo;
	private ArrayAdapter<String> listAdapter;
	private Space spaceGuiding;
	private static XSocketServer mServer = null;

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
		if(mServer == null) {
			mServer = new XSocketServer(getApplicationContext(), 26666);
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
