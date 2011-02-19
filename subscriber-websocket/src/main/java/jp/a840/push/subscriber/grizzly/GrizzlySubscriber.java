package jp.a840.push.subscriber.grizzly;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jp.a840.push.beans.BestRateBean;
import jp.a840.push.subscriber.AbstractSubscriber;
import jp.a840.push.subscriber.Message;
import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.exception.ConnectionException;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.exception.TimeoutException;
import jp.a840.push.subscriber.listener.CompositeMessageListener;
import jp.a840.push.subscriber.listener.MessageListener;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransportFactory;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketClientHandler;
import org.glassfish.grizzly.websockets.WebSocketConnectorHandler;
import org.glassfish.grizzly.websockets.frame.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ���A���^�C���f�[�^��M�p�N���C�A���g�}�l�[�W���B<br>
 * JMS���g�p�����f�[�^�̎�M���R���g���[������B<br>
 * ���[�U�͂��̃N���X����ăT�[�o�Ƃ̃f�[�^�̂������s���B
 * 
 * @author t-hashimoto
 */
public class GrizzlySubscriber extends AbstractSubscriber {
	private Logger log = LoggerFactory.getLogger(GrizzlySubscriber.class);

	// Grizzly
	private final TCPNIOTransport transport;
	
	private WebSocket websocket = null;
	
	private String location;
	
	private long connectionTimeout = 60;
	
	protected CompositeMessageListener messageListener = new CompositeMessageListener();

	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public GrizzlySubscriber() {
		super();
		transport = TransportFactory.getInstance().createTCPTransport();
	}

	public GrizzlySubscriber(String propertyPath) throws FileNotFoundException,IOException {
		this();
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(propertyPath);
		props.load(fis);

		location = props.getProperty("location");
	}

	/**
	 * ���������B<br>
	 * �R�l�N�V�����A�Z�b�V�����A�g�s�b�N�T�u�X�N���C�o�A�L���[�Z���_�[�̍쐬�B
	 * 
	 */
	public void init() throws InitializeException {
	}

	/**
	 * �N���C�A���g�}�l�[�W�����N�����A�T�[�o����f�[�^�̎�M���J�n���܂�.
	 * addSubscribe(),addSubscribeList(),setSubscribeList()�̂����ꂩ��RealtimeRequest��o�^���Ă����K�v������܂�.
	 * 
	 * @throws InitializeException
	 *             RealtimeRequest��1���o�^����Ă��Ȃ����������ɔ������܂�.
	 * @throws ConnectionException
	 *             JBoss,���T�[�o�Ƃ̐ڑ����ł��Ȃ������ɔ������܂�.
	 * @throws TimeoutException
	 *             ���T�[�o�Ƃ̐ڑ����^�C���A�E�g�������ɔ������܂�.
	 */
	public void start() throws InitializeException {
		boolean failFlag = true;
		try {
			super.start();
			init();
			connect();
			failFlag = false;
		} catch (InitializeException e) {
			throw e;
		} catch (ConnectionException e) {
			throw e;
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			if (failFlag) {
				quit();
			}
		}
	}

	/**
	 * ���X�i��o�^���f�[�^�̎�M���J�n����B
	 * 
	 * @throws Exception
	 */
	@Override
	protected void connect() throws Exception {
		if (!started || connected) {
			return;
		}

		prepareConnect();
		// ���b�Z�[�W�̔z�����X�^�[�g
		transport.start();
		WebSocketConnectorHandler connectorHandler = new WebSocketConnectorHandler(transport);
		Future<WebSocket> connectFuture = connectorHandler.connect(
				new URI(location), new GrizzlySubscriberClientHandler());

		websocket = (WebSocket) connectFuture.get(connectionTimeout, TimeUnit.SECONDS);
		super.connect();
	}

	protected void prepareConnect() throws Exception{
	}
	
	protected void disconnect() {

		// WebSocket���N���[�Y
		if (websocket != null && websocket.isConnected()) {
			try {
				websocket.close();
			} catch (Exception e) {
				log.error("Can't close websocket.", e);
			}
		}
		
		if(!transport.isStopped()){
			try{
				transport.stop();
			}catch(Exception e){
				log.error("Can't stop transport.", e);
			}
		}
		super.disconnect();
	}

	/**
	 * �o�^���ꂽ���X�i����������M���I������B
	 */
	public void stop() {
		super.stop();
	}

    /* -------------------------------------------------------- *
     *       WebSocket Client Handler 
     * -------------------------------------------------------- */
	public class GrizzlySubscriberClientHandler extends WebSocketClientHandler<WebSocket> {

		@Override
		public void onConnect(WebSocket websocket) throws IOException {
			System.out.println("CONNECTED!");
		}

		@Override
		public void onClose(WebSocket websocket) throws IOException {
			System.out.println("CLOSE!");
		}

		@Override
		public void onMessage(WebSocket websocket, Frame frame)
				throws IOException {
            try {
                if(quit){
                    return;
                }
                Buffer buffer = frame.getAsBinary();
                byte[] bytes = new byte[buffer.limit() - buffer.position()];
                buffer.get(bytes);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object msg = ois.readObject();
                healthCheckTouch();
                if(connected){
                	fireMessage(msg);
                }
            } catch (Exception e) {
            	fireException(e);
            	quit();
            }
		}
	}
	
	protected void fireMessage(Object msg) {
		MessageEvent me = createMessageEvent(msg);
		messageListener.onMessage(me);
	}
	
	protected MessageEvent createMessageEvent(Object m){
		return new MessageEvent(new ObjectMessageWrapper(m));
	}

	private class ObjectMessageWrapper implements Message {
		private Object msg;
		private Object body;
		public ObjectMessageWrapper(Object m){
			this.msg = m;
			this.body = m;
		}
		@Override
		public Object getBody() {
			return body;
		}
		@Override
		public Object getProperty(String key) {
			throw new RuntimeException("Not implemented");
		}
	}
	
	/**
	 * MessageListener��ǉ����܂�. ���̕ύX�͂����ɓK�p����܂�.
	 * 
	 * @param listener
	 *            ���X�g�֒ǉ�����RealtimeMessageListener
	 */
	public void addMessageListener(MessageListener listener) {
		messageListener.addMessageListener(listener);
	}

	/**
	 * MessageListener���폜���܂�. ���̕ύX�͂����ɓK�p����܂�.
	 * 
	 * @param listener
	 *            ���X�g����폜����RealtimeMessageListener
	 */
	public void removeMessageListener(MessageListener listener) {
		messageListener.removeMessageListener(listener);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	public static void main(String[] args) throws Exception {
		GrizzlySubscriber sub = new GrizzlySubscriber();
		sub.setLocation("ws://localhost:8088/rate");
		sub.start();
		sub.addMessageListener(new MessageListener() {			
			@Override
			public void onMessage(MessageEvent e) {
				Message msg = e.getMessage();
				BestRateBean dto = (BestRateBean)msg.getBody();
				System.out.println(dto.getBid());
			}
		});
		while(true){
			Thread.sleep(1000);
		}
	}

}
