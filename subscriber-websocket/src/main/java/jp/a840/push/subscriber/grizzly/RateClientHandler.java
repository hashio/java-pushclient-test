package jp.a840.push.subscriber.grizzly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import jp.a840.push.beans.RateBean;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.websockets.ClientWebSocketMeta;
import org.glassfish.grizzly.websockets.WebSocketClientHandler;
import org.glassfish.grizzly.websockets.frame.Frame;


public class RateClientHandler extends WebSocketClientHandler<RateWebSocket> {

	public void onConnect(RateWebSocket websocket) throws IOException {
		System.out.println("CONNECTED!");
	}

	public void onClose(RateWebSocket websocket) throws IOException {
		System.out.println("CLOSE!");
	}

	public void onMessage(RateWebSocket websocket, Frame frame)
			throws IOException {
			Buffer buffer = frame.getAsBinary();
			byte[] bytes = new byte[buffer.limit() - buffer.position()];
			buffer.get(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		try{
			RateBean dto = (RateBean)ois.readObject();
			System.out.println(dto.getBid());
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}			
	}

	protected RateWebSocket createWebSocket(Connection connection,
			ClientWebSocketMeta meta) {
		return new RateWebSocket(connection, meta, this);
	}
}
