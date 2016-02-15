### *** esptouch ***
<br>
```
-> workflow
->> parameter passed in taken here
<< take parameter
<- return
 -> inner block
-> - > if else branch
```

#### demo_activity
<br>
* ##### EsptouchDemoActivity

  * onCreate<br>
	```
	-> setContentView
	-> findViewById
	-> setOnClickListener
	```
  * onResume<br>
	```
	-> setText << EspWifiAdminSimple.getWifiConnectedSsid
	-> setEnabled
	```
  * onClick<br>
	```
	-> check View confirm button
	-> EsptouchAsyncTask3.execute << apSsid, apPasswd, apBssid, isSsidHidden, taskResultCountStr
	```
  * IEsptouchListener
	```
	-> IEsptouchListener
	-> onEsptouchResultAdded
	->> onEsptouchResultAddedPerform
	<< IEsptouchResult
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
  * EspWifiAdminSimple
  ```
  ->> Context -> mContext
  ```
  * getWifiNetworkInfo
  ```
  -> Context.getSystemService << Context.CONNECT_SERVICE
  <- ConnectivityManager.getNetworkInfo << ConnectivityManager.TYPE_WIFI
  ```
  * isWifiConnected
  ```
  -> getWifiNetwofkInfo
  <- Networkinfo.isConnected
  ```
  * getConnectionInfo
  ```
  -> Context.getSystemService << Context.WIFI_SERVICE
  <- WifiManager.getConnectionInfo
  ```
  * getwifiConnectedSsid
  ```
  -> getConnectionInfo
  <- WifiInfo -> isConnected -> getSSID
  ```
  * getWifiConnectedBssid
  ```
  <- WifiInfo -> getBSSID
  ```

#### \*Esptouch\*
IEsptouch\* -- interfaces<br>
Esptouch\* -- implements
<br>
* ##### IEsptouchResult<br>
* ##### EsptouchResult<br>
  * EsptouchResult
  ```
  ->> mIsSuc, mBssid, mInetAddress InetAddress, mIsCancelled AtomicBoolean
  ```
  * isSuc, getBssid, isCancelled, getInetAddress
  ```
  <- mIsSuc, mBssid, mIsCancelled.get, mInetAddress.get
  ```
  * setIsCancelled
  ```
  ->> isCancelled
  -> mIsCancelled.set
  ```
* ##### IEsptouchTask<br>
* ##### EsptouchTask<br>
  * EsptouchTask
  ```
  -> _mParameter EsptouchTaskParameter
  ->> [EsptouchTaskParameter.setWaitUdpTotalMillisecond << timeoutMillisecond]
  -> _mEsptouchTask __EsptouchTask
  << apSsid, apBssid, apPasswd, context, _mParameter, isSsidHidden
  ```
  * interrupt, isCancelled
  ```
  -> _mEsptouchTask.interrupt,  _mEsptouchTask.isCancelled
  ```
  * executeForResult
  ```
  ->> _mEsptouchTask.executeForResult << [expectTaskResultCount]
  ```
  * setEsptouchListener
  ```
  ->> _mEsptouchTask.setEsptouchListener << esptouchListener IEsptouchListener
  ```
* ##### IEsptouchListener<br>
  * onEsptouchResultAdded

#### task
<br>
* ##### IEsptouchTaskParameter
* ##### EsptouchTaskParameter
  * EsptouchTaskParameter<br>
    constructor for initializing private variables
* ##### IEsptouchGenerator
* ##### ICodeData
* ##### __IEsptouchTask
* ##### __EsptouchTask
  * __EsptouchTask
  ```
  ->> apSsid, ApBssid, apPasswd, context Context, parameter IEsptouchTaskParameter, isSsidHidden
  -> mIsCancelled AtomicBoolean << false
  -> mSocketClient UDPSocketClient
  ->> mSocketServer
  << Parameter getPortListing getWaitUdpTotalMillisecond, context
  -> mEsptouchResult ArrayList IEsptouchResult
  -> mBssidTaskSucCountMap HashMap String Integer
  ```
  * __putEsptouchResult
  ```
  -> synchronized << mEsptouchResultList 
  ->> mBssidTaskSucCountMap.get << bssid -> increment count -> mBssidTaskSucCountMap.put << bssid, count
  <- count > mParameter.getThresholdSucBroadcastCount
  <- isExist 
  ->> EsptouchResult << isSuc, bssid, inetAddress
  -> mEsptouchResultList.add << esptouchResult
  -> mEsptouchListener.onEsptouchResultAdded << esptouchResult
  ```
  * __interrupt<br>
  synchronized
  ```
  -> check mIsInterrupt
  -> mSocketClient.interrupt
  -> mSocketServer.interrupt
  -> Thread.currentThread.interrupt
  ```
  * interrupt
  ```
  -> mIsCancelled.set << true
  -> __interrupt
  ```
  * __listenAsyn
  ```
  -> Thread
  -> run
   -> startTimestamp System.currentTimeMillis
   -> apSsidAndPassword ByteUtil.getBytesByString << mApSsid + mApPasswd
   -> length
   -> till mIsInterrupt or getExpectTaskResultCount
    ->> receiveBytes mSocket.receiveSpecLenBytes << expectDataLen
	-> compare receiveBytes expectOneByte 
	-> bssid ByteUtil.parseBssid
	<< receiveBytes getEsptouchResultOneLen getEsptouchResultMacLen
	-> inetAddress EspNetUtil.parseInetAddr
	<< receiveBytes getEsptouchResultOneLen + getEsptouchResultMacLen getEsptouchResultIpLen
	-> __putEsptouchResult << true bssid inetAddress
   -> mIsSuc compare mEsptouchResultList.size getExpectTaskResultCount
   -> __EsptouchTask.this.__interrupt
  -> start 
  ```
  * __execute
  ```
  -> startTime System.currentTimeMillis
  -> trick lastTime here
  -> check mIsInterrupt
   -> check timeout getTimeoutTotalCodeMillisecond (GC+DC)
    -> check mIsInterrupt and timeout getTimeoutGuideCodeMillisecond 2000milliseconds
	 ->> mSocketClient.sendData
	 << generator.getGCBytes2, getTargetHostName, getTargetPort, getIntervalGuideCodeMillisecond
	-> update lastTime
   - >> mSocketClient.sendData
   << generator.getDCByte2, index, ONE_DATA_LEN, getTargetHostName, getTargetPort, getIntervalDataCodeMillisecond
   - > update index
   -> update currentTime 
   <- timeout getWaitUdpSendingMillisecond 45000milliseconds
  <- mIsSuc
  ```
  * __checkTaskValid
  ```
  -> check task mIsExecuted
  ```
  * __getEsptouchResultList<br>
  ```
  -> synchronized << mEsptouchResultList
  -> isEmpty -> EsptouchResult << false -> setIsCancelled << mIsCancelled.get -> add << EsptouchResultFail EsptouchResult
  <- mEsptouchResultList
  ```
  * executeForResult
  ```
  -> __checkTaskValid
  ->> mParameter.setExpectTaskResultCount << expectTaskResultCount
  -> check Looper.myLooper not Looper.getMainLooper
  -> EspNetUtil.getLocalInetAddress
  -> generator EsptouchGenerator
  << mApSsid, mApBssid, mApPasswd, localInetAddress, mIsSsidHidden
  -> __listenAsyn << getEsptouchResultTotalLen
  -> __execute << generator
  -> loop getTotalRepeatTime -> isSuc <- __getEsptouchResultList
  -> timeout -> mIsInterrupt -> try Thread.sleep wait udp response
  <- __getEsptouchResultList
  ```
  * isCancelled
  ```
  <- mIsCancelled.get AtomicBoolean
  ```
  * setEsptouchListener
  ```
  ->> mEsptouchListener IEsptouchListener
  ```

