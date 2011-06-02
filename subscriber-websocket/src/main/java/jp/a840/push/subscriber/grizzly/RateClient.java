package jp.a840.push.subscriber.grizzly;

import java.net.URI;

import org.glassfish.grizzly.websockets.ClientWebSocket;
import org.glassfish.grizzly.websockets.WebSocket;

public class RateClient {

	public static void main(String[] args) throws Exception {
		WebSocket websocket = null;

		try {
			final RateClientHandler clientHandler = new RateClientHandler();
			ClientWebSocket clientWebSocket = new ClientWebSocket(new URI("ws://localhost:8088/rate"), clientHandler);
			websocket = clientWebSocket.connect(60 * 1000);
			while(websocket.isConnected()){
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
