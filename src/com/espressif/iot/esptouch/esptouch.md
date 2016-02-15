### *** esptouch ***
<br>

#### demo_activity
<br>
* ##### EsptouchDemoActivity

  * onCreate<br>
	```
	setContentView -> findViewById -> setOnClickListener
	```
  * onResume<br>
	```
	EspWifiAdminSimple.getWifiConnectedSsid -> (confirm button)setEnabled
	```
  * onClick<br>
	```
	View == (confirm button)
	-> EsptouchAsyncTask3.execute << apSsid, apPasswd, paBssid, isSsidHidden, taskResultCountStr
	```
  * IEsptouchListener
	```
	IEsptouchListener
	->> IEsptouchResult
	-> onEsptouchResultAdded -> onEsptouchResultAddedPerform
	```
  * EsptouchAsyncTask3
	```
	onPreExecute
		-> mProgressDialog
		-> setMessage << Esptouch configuring
		-> setCanceledOnTouchOutside << false
		-> setOnCancelListener -> mEsptouchTask.interrupt
		-> setButton POSTITIVE << Waiting -> setEnabled << false
		-> show
			Esptouch configuring
			--------------------
				 Waiting
	doInBackground
		-> mIEsptouchTask
		->> String... params Array	
		-> synchronized mLock Object ->> apSsid, apBssid, apPassword, isSsidHidden, EsptouchDemoActivity.this
		-> EsptouchTask 
		-> setEsptouchListener IEsptouchListener
		-> executeForResults << parseInt(taskResultCountStr) 
		<- List IEsptouchResult
	onPostEcecute
		->> List IEsptouchResult << result
		-> getButton -> setEnabled << true -> setText << Confirm
		-> result.get << 0
		-> isCancelled -> isSuc -> collect result -> setMessage
	```
  * EsptouchAsyncTask2
	different on parameters
	```
	doInBackground
		-> executeForResults << void
	onPostExecute
		->> IEsptouchResult
	```
  * onEsptouchResultAddedPerform
	```
	runOnUiThread -> run -> toast: IEsptouchResult.getBssid (connect to wifi)
	```
* ##### EspWifiAdminSimple

