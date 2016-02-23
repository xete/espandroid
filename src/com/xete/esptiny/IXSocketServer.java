package com.xete.esptiny;

import java.net.Socket;

public interface IXSocketServer {
	public void onConnected(Socket socket);
	public void onRead(Socket socket, byte[] rb, int len);
	public void onDisconnected(Socket socket);
}
