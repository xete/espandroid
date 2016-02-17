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

import java.util.ArrayList;
import java.util.Arrays;

public class ControlActivity extends Activity
{
	
	private ListView listViewStatus;
	private TextView textViewControlInfo;
	private ArrayAdapter<String> listAdapter;
	private Space spaceGuiding;
	private int x, y;

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

    }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		x = (int) e.getX();
		y = (int) e.getY();
		String infoPosition = "";
		switch(e.getAction()) {
			case MotionEvent.ACTION_DOWN:	
				infoPosition += "x: "+String.valueOf(x)+", y: "+String.valueOf(y)+" DOWN";
				break;
			case MotionEvent.ACTION_UP:	
				infoPosition += "x: "+String.valueOf(x)+", y: "+String.valueOf(y)+" UP";
				break;
			case MotionEvent.ACTION_MOVE:	
				infoPosition += "x: "+String.valueOf(x)+", y: "+String.valueOf(y)+" MOVE";
				break;
		}
		textViewControlInfo.setText(infoPosition);
		return true;
	}

}
