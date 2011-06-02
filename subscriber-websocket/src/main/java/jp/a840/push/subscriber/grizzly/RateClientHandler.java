package jp.a840.push.subscriber.grizzly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import jp.a840.push.beans.RateBean;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketListener;


public class RateClientHandler implements WebSocketListener {

	public void onConnect(WebSocket websocket) {
		System.out.println("CONNECTED!");
	}

	public void onClose(WebSocket websocket) {
		System.out.println("CLOSE!");
	}

	public void onMessage(WebSocket websocket, byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try{
			ObjectInputStream ois = new ObjectInputStream(bais);
			RateBean dto = (RateBean)ois.readObject();
			System.out.println(dto.getBid());
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}			
	}

	public void onMessage(WebSocket websocket, String message) {		
	}

	public void onPing(byte[] bytes) {
	}
}
