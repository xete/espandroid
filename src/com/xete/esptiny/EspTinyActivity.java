package com.xete.esptiny;

import com.espressif.iot.esptouch.esptouch_activity.EsptouchActivity;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

public class EspTinyActivity extends Activity implements OnClickListener
{
	
	private Button mButtonLogin;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		mButtonLogin = (Button) findViewById(R.id.buttonLogin);
		mButtonLogin.setOnClickListener(this);
    }

		
	@Override
	public void onClick(View v) {
		if(v == mButtonLogin) {
			Intent intent = new Intent();
			intent.setClass(EspTinyActivity.this, EsptouchActivity.class);
			EspTinyActivity.this.startActivity(intent);
			EspTinyActivity.this.finish();
		}
	}
}
