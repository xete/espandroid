package com.xete.esptiny;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;
import android.util.Log;

public class XSocketServer extends AsyncTask<Void, Void, Void> {
	private final static String TAG = "XSocketServer";
	private final Context mContext;
	private String mAddress;
	private int mPort = 10866;
	private int mTimeout = 0;
	private static ServerSocket mServer = null;
	private Handler mHandler;
	private ArrayList<Socket> mClients = null;
	private StringBuilder mStatus;
	private boolean mIsSuccess = true;
	private final static Integer SIZE_RB = 2048;
	private final static Integer SIZE_SB = 2048;
	private IXSocketServer mListener = null;
	public interface IXSocketServer {
		public void onConnected(Socket socket);	
		public void onRead(Socket socket, byte[] rb, int len);
		public void onDisconnected(Socket socket);
	}
	
	public XSocketServer(Context context) {
		this(context, 0, 0);
	}
	public XSocketServer(Context context, int port) {
		this(context, port, 0);
	}
	public XSocketServer(Context context, int port, int timeout) {
		mContext = context;
		if(port != 0) mPort = port;
		mTimeout = timeout;
		mAddress = acquireAddress();
	}

	public void addListener(IXSocketServer listener) {
		mListener = listener;	
	}

	@Override
	protected void onPreExecute() {
		String promopt = "Server already set up";
		Log.v(TAG, "onPreExecute");
		try {
			if(mServer == null) {
				mServer = new ServerSocket(mPort);	
				promopt = "Server: "+mAddress+":"+String.valueOf(mPort);
				mHandler = new Handler();
			}
		/* try accept socket */
		} catch(UnknownHostException e) {
			mIsSuccess = false;
			promopt = "UnknowHost";
			e.printStackTrace();
		} catch(IOException e) {
			mIsSuccess = false;
			promopt = "IOException";
			e.printStackTrace();
		} finally {
			if(mIsSuccess == false) {
				if(mClients != null) {
					for(int i = 0; i < mClients.size(); i++) {
						closeSocket(mClients.get(i));	
					}	
				}
				closeServer(null);	
			}
		}
		Log.v(TAG, promopt);
		if(!mIsSuccess) {
			Toast toast = Toast.makeText(mContext.getApplicationContext(), promopt, Toast.LENGTH_SHORT);	
			toast.show();
		}
	}

	@Override
	protected Void doInBackground(Void... args) {
		Log.v(TAG, "doInBackground");
		if(mListener == null) {
			Log.v(TAG, "no listener");
			return null;
		}
		if(!mIsSuccess) {
			Log.v(TAG, "server not setup, nothing to do");
			return null;
		}
		Socket client = null;
		mClients = new ArrayList<Socket>();
		StringBuilder sb;
		while(true) {
			try {
				Log.v(TAG, "waiting for client to connect");
				client = mServer.accept();
				sb = new StringBuilder();
				sb.append("accepted client: ");
				sb.append(client.getInetAddress());
				sb.append(":");
				sb.append(client.getPort());
				Log.v(TAG, sb.toString());
				mListener.onConnected(client);
			} catch(IOException e) {
				e.printStackTrace();
				this.cancel(true);
				break;
			}
			/* Runnable blocks if is directly invoke */
			(new Thread(new Receiver(client))).start();
			if(client == null) {
				Log.v(TAG, "ridiculous client null");
			} else {
				/**
				 * crash here for not new ArrayList() and invoke add to null
				 * throw NullPointerException, leading to RuntimeException
				 */
				mClients.add(client);
			}
		}
		return null;
	}

	private String acquireAddress() {
		XWifiManager wifiManager = new XWifiManager(mContext.getApplicationContext());
		return wifiManager.getIpAddr();
	}

	public String getPort() {
		return String.valueOf(mPort);
	}
	public String getAddress() {
		return mAddress;
	}

	private void closeSocket(Socket s) {
		if(s == null) return;
		if(s.isClosed()) return;
		try {
			s.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void closeServer(ServerSocket server) {
		if(server == null) {
			try {
				if(mServer != null) mServer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if(server.isClosed()) return;
		try {
			server.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public String getStat() {
		return mStatus.toString();
	}

	private class Receiver extends Socket implements Runnable {
		private final static String TAG = "Receiver";
		private Socket client = null;
		private InputStream stream = null;
		private byte[] read_buffer;
		private int len;
		private String id;
		private final static int timeout = 5000;
		private boolean isDown = false;

		Receiver(Socket client) {
			this.client = client;
			this.read_buffer = new byte[SIZE_RB];
			try {
				this.client.setSoTimeout(timeout);
				this.stream = client.getInputStream();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			if(stream == null) return;
			id = client.getInetAddress()+":"+client.getPort();
			Log.v(TAG, id+" waiting for data");
			while(true) {
				try {
					/* it seems the check of connection is wiredly single side */
					len = stream.read(read_buffer, 0, SIZE_RB);	
					if( len != -1 ) {
						Log.v(TAG, id+" -> "+(new String(read_buffer, 0, len)));
						mListener.onRead(client, read_buffer, len);
					}
				} catch(IOException e) {
					Log.v(TAG, id+" has been closed");
					isDown = true;
				} catch(Exception e) {
					Log.v(TAG, "exception close "+id);
					isDown = true;
					try {
						if(!client.isClosed()) client.close();
					} catch(IOException err) {
						Log.v(TAG, "Unknown error closing client "+id);
						err.printStackTrace();	
					}
					e.printStackTrace();
				} finally {
					if(isDown) { 
						Log.v(TAG, id+" is down");
						mClients.remove(client);	
						mListener.onDisconnected(client);
						break;
					}
				}
			}
		}

		public String read() {
			return (new String(read_buffer, 0, len));	
		}

		public String getId() {
			return id;	
		}
	}
	
}
