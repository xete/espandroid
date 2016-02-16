### esptouch
<br>
```
-> workflow
->> parameter passed in taken here
<< take parameter
<- return
 -> inner block
-> --> if else branch
```
<br>

#### demo_activity
<br>
* EsptouchDemoActivity
  * onCreate
	```
	-> setContentView
	-> findViewById
	-> setOnClickListener
	```
  * onResume
	```
	-> setText << EspWifiAdminSimple.getWifiConnectedSsid
	-> setEnabled
	```
  * onClick
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
	* onPreExecute
	```
	-> mProgressDialog
	-> setMessage << Esptouch configuring
	-> setCanceledOnTouchOutside << false
	-> setOnCancelListener -> mEsptouchTask.interrupt
	-> setButton POSTITIVE << Waiting -> setEnabled << false
	-> show
		Esptouch configuring
		--------------------
			 Waiting
	```
	* doInBackground
	```
	-> mIEsptouchTask
	->> String... params Array	
	-> synchronized mLock Object ->> apSsid, apBssid, apPassword, isSsidHidden, EsptouchDemoActivity.this
	-> EsptouchTask 
	-> setEsptouchListener IEsptouchListener
	-> executeForResults << parseInt(taskResultCountStr) 
	<- List IEsptouchResult
	```
	onPostEcecute
	```
	->> List IEsptouchResult << result
	-> getButton -> setEnabled << true -> setText << Confirm
	-> result.get << 0
	-> isCancelled -> isSuc -> collect result -> setMessage
	```
  * EsptouchAsyncTask2
	different on parameters
	* doInBackground
	```
	-> executeForResults << void
	```
	* onPostExecute
	```
		->> IEsptouchResult
	```
  * onEsptouchResultAddedPerform
	```
	runOnUiThread -> run -> toast: IEsptouchResult.getBssid (connect to wifi)
	```
<br>

* EspWifiAdminSimple
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
<br>

#### \*Esptouch\*
IEsptouch\* -- interfaces<br>
Esptouch\* -- implements
<br>
* IEsptouchResult<br>
* EsptouchResult<br>
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
* IEsptouchTask<br>
* EsptouchTask<br>
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
* IEsptouchListener<br>
  * onEsptouchResultAdded
<br>

#### task
<br>
* IEsptouchTaskParameter
* EsptouchTaskParameter
  * EsptouchTaskParameter<br>
    constructor for initializing private variables
* IEsptouchGenerator
* ICodeData
* __IEsptouchTask
* __EsptouchTask
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
   -->> mSocketClient.sendData
   << generator.getDCByte2, index, ONE_DATA_LEN, getTargetHostName, getTargetPort, getIntervalDataCodeMillisecond
   --> update index
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

#### udp

* UDPSocketClient

  * UDPSOcketClient
    ```
    -> mSocket DatagramSocket --> e.printStackTrace
    ```
  * finalize
    ```
    -> close -> super.finzlize
    ```
  * interrupt
    ```
    -> mIsStop << true 
    ```
  * close
    synchronized
    ```
    -> mIsClosed -> mSocket.close -> mIsClose << true 
    ```
  * sendData
	```
	-> mIsStop
	 -> check data and data packet length
	 -> mSocket.send
	 << localDatagramPacket DatagramPacket
	 << data packet  packet length  InetAddress.getByName << targetHostName  targetPort
	 ->> Thread.sleep << interval
	-> close
	```

* UDPSocketServer

  aquireLock not paired with releaseLock?

  * UDPSocketServer
  ```
  -> mReceivePacket DatagramPacket << buffer byte[]
  ->> mServerSocket DatagramSocket << port int
  ->> manager WifiManager Context.getSystemSever << Context.WIFI_SERVICE
  -> mLock WifiManager.MulticastLock manager.createMulticastLock
  --> IOException ->> e.printStackTrace
  ```
  * acquireLock, releaseLock
  ```
  -> check mLock and mLock.isHeld
  -> mLock.acquire, mLock.release
  ```
  * setSoTimeout
  ```
  ->> mSocketSocket.setSoTimeout << timeout
  ```
  * receiveOneByte
  ```
  -> acquireLock
  -> mServerSocket.receive << mReceivePacket
  <- mReceivePacket.getData
  --> <- Byte.MIN_VALUE
  ```
  * receiveSpecLenBytes
  ```
  -> acquireLock -> mServerSocket.receive << mReceivePacket
  <- Arrays.copyOf << mReceivePacket getData getLength
  ```
  * close, interrupt, finalize
  ```
  -> mIsClosed -> mServerSocket.close -> releaseLock -> mIsClosed << true,
  -> close,
  -> close -> super.finalize
  ```

#### util

* EspNetUtil
  * getLocalInetAddress
  ```
  ->> context.getSystemService << Context.WIFI_SERVICE
  -> getConnectionInfo -> getIpAddress -> __formatString
  <- InetAddress.getByName
  ```
  * __formatString
  ```
  ->> byte[] __intToByteArray << int
  <- String << byte[] + .
  ```
  * __intToByteArray
  ```
  -> byte[] ->> and 0xFF << int
  <- byte[]
  ```
  * parseInetAddr
  ```
  ->> StringBuilder.append Integer.toString << loop byte[] with offset
  <- InetAddress.getByName << StringBuilder.toString
  ```
  * parseBssid2bytes
  ```
  ->> String[] String.split << :
  <- byte[] Integer.parseInt << String[] 
  ```
* ByteUtil
* CRC8


#### protocal

* EsptouchGenerator
  * EsptouchGenerator
  ```
  -> mGcBytes2 byte[][] ByteUtil.genSpecBytes << GuideCode.getU8s
  ->> mDcBytes2 byte[][] ByteUtil.genSpecBytes
  <<  DatumCode << apSsid, apBssid, apPassword, inetAddress, isSsidHiden getU8s
  ```
  * getGCBytes2, getDCBytes2
  ```
  <- mGcBytes2, mDcBytes2
  ```
* DatumCode
  * DatumCode
  ```
  ->> apSsid, apBssid, apPassword, ipAddress, isSsidHidden
  -> CRC8 check
  -> mDataCode[0] DataCode << _totalLen, 0 -> totalXor _totalLen
  -> mDataCode[1] DataCode << apPwdLen, 1 -> totalXor apPwdLen
  -> mDataCode[2] DataCode << apSsidCrc, 2 -> totalXor apSsidCrc
  -> mDataCode[3] DataCode << apBssidCrc, 3 -> totalXor apBssidCrc
  -> mDataCode[EXTRA_HEAD_LEN+i] DataCode
   << ipAddrChars[i], i+EXTRA_HEAD_LEN -> totalXor ipAddrChars[i]
  -> mDataCode[EXTRA_HEAD_LEN+ipLen] DataCode
   << apPwdChars[i], i+EXTRA_HEAD_LEN+ipLen -> totalXor apPwdChars[i]
  -> isSsidHiden -> mDataCode[i+EXTRA_HEAD_LEN+ipLen+apPwdLen] DataCode
   << apSsidChars[i], i+EXTRA_HEAD_LEN+ipLen+apPwdLen
  -> mDataCode[4] DataCode << totalXor, 4
  ```
  * getBytes
  * toString
  * getU8s
* DataCode
  * DataCode
  mSeqHeader, mDataHigh, mDataLow, mCrcHigh, mCrcLow
  ```
  ->> ByteUtil.splitUint8To2bytes << u8 -> mDataHigh, mDataLow
  -> CRC8
   -> update << ByteUtil.splitUint8toByte << u8 -> update << index
   -> mCrcHigh, mCrcLow
  -> mSeqHeader << index
  ```
  * getBytes
  ```
  -> dataBytes byte[]
  -> dataBytes[0] << 0x00
  -> dataBytes[1] ByteUtil.combine2bytesToOne << mCrcHigh, mDataHigh
  -> dataBytes[2] << 0x01
  -> databytes[3] mSeqHeader
  -> dataBytes[4] << 0x00
  -> dataBytes[5] ByteUtil.combine2bytesToOne << mCrcLow, mDataLow
  <- dataBytes
  ```
  * toString
