package com.espressif.iot.esptouch.esptouch_activity;

import java.util.List;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.xete.esptiny.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class EsptouchActivity extends Activity implements OnClickListener {

	private static final String TAG = "EsptouchActivity";

	private TextView mTextApSsid;

	private EditText mEditApPasswd;

	private Button mButtonSmartConfig;
	
	private CheckBox mCheckIsSsidHidden;

	private CheckBox mCheckBoxIsPasswdVissible;

	private EspWifiAdminSimple mWifiAdmin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartconfig);

		mWifiAdmin = new EspWifiAdminSimple(this);
		mTextApSsid = (TextView) findViewById(R.id.textNoSsid);
		mEditApPasswd = (EditText) findViewById(R.id.editApPasswd);
		mButtonSmartConfig = (Button) findViewById(R.id.buttonSmartConfig);
		mCheckIsSsidHidden = (CheckBox) findViewById(R.id.checkIsSsidHidden);
		mCheckBoxIsPasswdVissible = (CheckBox) findViewById(R.id.checkBoxIsPasswdVissible);
		mButtonSmartConfig.setOnClickListener(this);
		mCheckBoxIsPasswdVissible.setChecked(true);
		mCheckBoxIsPasswdVissible.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// display the connected ap's ssid
		String apSsid = mWifiAdmin.getWifiConnectedSsid();
		boolean isApSsidEmpty;
		if (apSsid != null) {
			mTextApSsid.setText(apSsid);
			isApSsidEmpty = false;
		} else {
			mTextApSsid.setText(getResources().getString(R.string.textNoSsid));
			isApSsidEmpty = true;
		}
		// check whether the wifi is connected
		mButtonSmartConfig.setEnabled(!isApSsidEmpty);
	}

	@Override
	public void onClick(View v) {

		if (v == mButtonSmartConfig) {
			String apSsid = mTextApSsid.getText().toString();
			String apPassword = mEditApPasswd.getText().toString();
			String apBssid = mWifiAdmin.getWifiConnectedBssid();
			Boolean isSsidHidden = mCheckIsSsidHidden.isChecked();
			String isSsidHiddenStr = "NO";
			if (isSsidHidden) 
			{
				isSsidHiddenStr = "YES";
			}
			if (__IEsptouchTask.DEBUG) {
				Log.d(TAG, "mButtonSmartConfig is clicked, mEdtApSsid = " + apSsid
						+ ", " + " mEdtApPassword = " + apPassword);
			}
			new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
					isSsidHiddenStr, "1");
		} else if (v == mCheckBoxIsPasswdVissible) {
			mEditApPasswd.setInputType(mEditApPasswd.getInputType() ^ InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
	}
	
	private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String text = result.getBssid() + " is connected to the wifi";
				Toast.makeText(EsptouchActivity.this, text,
						Toast.LENGTH_LONG).show();
			}

		});
	}

	private IEsptouchListener myListener = new IEsptouchListener() {

		@Override
		public void onEsptouchResultAdded(final IEsptouchResult result) {
			onEsptoucResultAddedPerform(result);
		}
	};
	
	private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

		private ProgressDialog mProgressDialog;

		private IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(EsptouchActivity.this);
			mProgressDialog
					.setMessage("Esptouch is configuring, please wait for a moment...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (mLock) {
						if (__IEsptouchTask.DEBUG) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if (mEsptouchTask != null) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					"Waiting...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}

		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			int taskResultCount = -1;
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				String taskResultCountStr = params[4];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
						isSsidHidden, EsptouchActivity.this);
				mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(1);
			return resultList;
		}

		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"Confirm");
			IEsptouchResult firstResult = result.get(0);
			// check whether the task is cancelled and no results received
			if (!firstResult.isCancelled()) {
				int count = 0;
				// max results to be displayed, if it is more than maxDisplayCount,
				// just show the count of redundant ones
				final int maxDisplayCount = 5;
				// the task received some results including cancelled while
				// executing before receiving enough results
				if (firstResult.isSuc()) {
					StringBuilder sb = new StringBuilder();
					for (IEsptouchResult resultInList : result) {
						sb.append("Esptouch success, bssid = "
								+ resultInList.getBssid()
								+ ",InetAddress = "
								+ resultInList.getInetAddress()
										.getHostAddress() + "\n");
						count++;
						if (count >= maxDisplayCount) {
							break;
						}
					}
					if (count < result.size()) {
						sb.append("\nthere's " + (result.size() - count)
								+ " more result(s) without showing\n");
					}
					mProgressDialog.setMessage(sb.toString());
				} else {
					mProgressDialog.setMessage("Esptouch fail");
				}
			}
		}
	}
}
