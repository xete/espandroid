package com.xete.esptiny;

import com.espressif.iot.esptouch.esptouch_activity.EsptouchActivity;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

public class EspTinyActivity extends Activity implements OnClickListener
{
	
	private Button mButtonLogin;
	private Button mButtonLogout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		mButtonLogin = (Button) findViewById(R.id.buttonLogin);
		mButtonLogout = (Button) findViewById(R.id.buttonLogout);
		mButtonLogin.setOnClickListener(this);
		mButtonLogout.setOnClickListener(this);

    }

		
	@Override
	public void onClick(View v) {
		if(v != null) {
			Intent intent = new Intent();
			if(v == mButtonLogin) {
				intent.setClass(EspTinyActivity.this, EsptouchActivity.class);
			}else if(v == mButtonLogout) {
				intent.setClass(EspTinyActivity.this, ControlActivity.class);	
			}
			EspTinyActivity.this.startActivity(intent);
			EspTinyActivity.this.finish();
		}
	}
}
