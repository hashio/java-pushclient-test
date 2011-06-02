package jp.a840.push.subscriber.grizzly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import jp.a840.push.beans.RateBean;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.memory.ByteBufferManager;
import org.glassfish.grizzly.websockets.BaseWebSocket;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RateWebSocket extends BaseWebSocket {

	private Logger log = LoggerFactory.getLogger(RateWebSocket.class);
	
	private ByteBufferManager byteBufferManager = new ByteBufferManager();
	
	public RateWebSocket(Connection connection, WebSocketListener... listeners) {
		super(connection, listeners);
	}

	public void sendRate(RateBean rate){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(rate);

			send(baos.toByteArray());
		}catch(IOException e){
			log.error("Caught exception.", e);
		}
	}
}
