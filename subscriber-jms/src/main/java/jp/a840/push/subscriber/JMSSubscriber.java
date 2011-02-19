package jp.a840.push.subscriber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jp.a840.push.subscriber.AbstractSubscriber;
import jp.a840.push.subscriber.Message;
import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.exception.ConnectionException;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.exception.TimeoutException;
import jp.a840.push.subscriber.listener.CompositeMessageListener;
import jp.a840.push.subscriber.listener.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ���A���^�C���f�[�^��M�p�N���C�A���g�}�l�[�W���B<br>
 * JMS���g�p�����f�[�^�̎�M���R���g���[������B<br>
 * ���[�U�͂��̃N���X����ăT�[�o�Ƃ̃f�[�^�̂������s���B
 * 
 * @author t-hashimoto
 */
public class JMSSubscriber extends AbstractSubscriber {
	private Logger log = LoggerFactory.getLogger(JMSSubscriber.class);

	private static final String NAMING_FACTORY_INITIAL_KEY = "java.naming.factory.initial";

	private String namingFactoryInitial = "org.jnp.interfaces.NamingContextFactory";

	private static final String NAMING_FACTORY_URL_PKGS_KEY = "java.naming.factory.url.pkgs";

	private String namingFactoryUrlPkgs = "org.jboss.naming:org.jnp.interfaces";

	private static final String NAMING_PROVIDER_URL_KEY = "java.naming.provider.url";

	InitialContext ctx = null;

	/** HOST:1099�Ƃ� */
	private String namingProviderUrl = null;

	// JMS
	protected Connection connection = null;

	protected Session session = null;

	protected javax.jms.MessageListener jmsMessageListener;

	protected CompositeMessageListener messageListener = new CompositeMessageListener();

	protected List<Subscriber> subscriberList = new ArrayList<Subscriber>();
	
	protected List<MessageConsumer> messageConsumerList = new ArrayList<MessageConsumer>();
	
	protected Hashtable envContext = new Hashtable();

	public Object lock = new Object();

	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public JMSSubscriber() {
		super();
	}

	public JMSSubscriber(String jmsPropertyPath) throws InitializeException {
		super();
		try{
			ctx = this.getInitialContext(jmsPropertyPath);
		}catch(Exception e){
			throw new InitializeException(e);
		}
	}

	/**
	 * ���������B<br>
	 * �R�l�N�V�����A�Z�b�V�����A�g�s�b�N�T�u�X�N���C�o�A�L���[�Z���_�[�̍쐬�B
	 * 
	 */
	public void init() throws InitializeException {
		if(ctx != null){
			return;
		}
		try {
			// INITIALIZE JMS
			// JNDI �R���e�L�X�g�̍쐬
			if (namingFactoryInitial == null) {
				throw new InitializeException("NamingFactoryInitial������������Ă��܂���");
			}
			if (namingFactoryUrlPkgs == null) {
				throw new InitializeException("NamingFactoryUrlPkgs������������Ă��܂���");
			}
			if (namingProviderUrl == null) {
				throw new InitializeException("NamingProviderUrl������������Ă��܂���");
			}

			envContext.put(NAMING_FACTORY_INITIAL_KEY, namingFactoryInitial);
			envContext.put(NAMING_FACTORY_URL_PKGS_KEY, namingFactoryUrlPkgs);
			envContext.put(NAMING_PROVIDER_URL_KEY, namingProviderUrl);
			ctx = new InitialContext(envContext);
		} catch (InitializeException e) {
			throw e;
		} catch (Exception e) {
			throw new InitializeException(e);
		}
	}

    /* -------------------------------------------------------- *
     *                J N D I
     * -------------------------------------------------------- */
	/**
	 * JNDI�R���e�L�X�g���쐬����B
	 * 
	 * @param propertieFileName
	 * @return �쐬���ꂽJNDI�R���e�L�X�g
	 * @throws IOException
	 * @throws NamingException
	 */
	protected InitialContext getInitialContext() throws IOException, NamingException {
		return getInitialContext("jndi.properties");
	}
	
	/**
	 * JNDI�R���e�L�X�g���N���X�p�X��̃v���p�e�B�t�@�C������쐬����B
	 * 
	 * @param propertieFileName
	 * @return �쐬���ꂽJNDI�R���e�L�X�g
	 * @throws IOException
	 * @throws NamingException
	 */
	protected InitialContext getInitialContext(String fileName) throws IOException, NamingException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		Properties properties = new Properties();
		properties.load(is);
		return new InitialContext(properties);
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
	 * JMS�p�̃T�u�X�N���C�o�[��ǉ�����
	 * 
	 * @param topic
	 * @param messageSelector
	 */
	public void addSubscribe(String destination, String messageSelector) {
		if (connected == false) {
			subscriberList.add(new Subscriber(destination, messageSelector));
		} else {
			throw new IllegalStateException("�N���C�A���g�}�l�[�W���N�����Ƀ��N�G�X�g��ύX���邱�Ƃ͂ł��܂���B");
		}
	}

	/**
	 * RealtimeRequest�����X�g����폜����. start()�Ăяo���O�ɍs���Ă�������.
	 * start()�Ăяo�����stop()���Ă΂��܂ł����̕ύX���s���Ɨ�O���������܂�.
	 * 
	 * @param request
	 * @throws RequestException
	 *             start()�Ăяo���ォ��stop()���Ă΂��܂łɃ��X�g��ύX���悤�����ꍇ�ɔ������܂�.
	 */
	public void removeSubscribe(String destination, String messageSelector) {
		if (connected == false) {
			Subscriber removeTarget = null;
			for(Subscriber subscriber : subscriberList){
				if(subscriber.getDestination().equals(destination)
				|| subscriber.getMessageSelector().equals(messageSelector)){
					removeTarget = subscriber;
					break;
				}
			}
			subscriberList.remove(removeTarget);
		} else {
			throw new IllegalStateException("�N���C�A���g�}�l�[�W���N�����Ƀ��N�G�X�g��ύX���邱�Ƃ͂ł��܂���B");
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
		super.connect();

		// ���b�Z�[�W�̔z�����X�^�[�g
		connection.start();
	}

	protected void prepareConnect() throws Exception{
		// �R�l�N�V�������쐬
		// �g�s�b�N�R�l�N�V�����t�@�N�g���[�����b�N�A�b�v
		ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(JMS_FACTORY);
		connection = connectionFactory.createConnection();
		connection.setExceptionListener(new javax.jms.ExceptionListener() {			
			@Override
			public void onException(JMSException e) {
				fireException(e);
				quit();
			}
		});
		
		// �Z�b�V�������쐬
		session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);

		for(Subscriber subscriber : subscriberList){
			MessageConsumer consumer;
			// �T�u�X�N���C�o���쐬�i���������w��j
			Destination destination = (Destination) ctx.lookup(subscriber.getDestination());
			if (destination == null) {
				throw new JMSException("Can't find topic: " + subscriber.getDestination());
			}

			// �ʏ�̒l�i���z�M�����T�u�X�N���C�o(Feeder,Dataset�ōi�荞�݉�)
			consumer = session.createConsumer(destination, subscriber.getMessageSelector(), false);
			jmsMessageListener = new JMSMessageListener();
			consumer.setMessageListener(jmsMessageListener);
			messageConsumerList.add(consumer);
		}		
	}
	
	private class Subscriber {
		private String destination;
		private String messageSelector;
		
		public Subscriber(String destination, String messageSelector){
			this.destination = destination;
			this.messageSelector = messageSelector;
		}
		
		public String getDestination() {
			return destination;
		}
		public String getMessageSelector() {
			return messageSelector;
		}
	}

	protected void disconnect() {
		if (!connected) {
			return;
		}

		// �T�u�X�N���C�o���N���[�Y
		while(messageConsumerList.size() > 0){
			MessageConsumer consumer = (MessageConsumer)messageConsumerList.remove(0);
			if (consumer != null) {
				try {
					consumer.close();
				} catch (Exception e) {
					log.error("Can't close consumer.", e);
				}
			}
		}
		// �Z�b�V�������N���[�Y
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				log.error("Can't close session.", e);
			}
		}
		// �R�l�N�V�������N���[�Y
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Can't close connection.", e);
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
     *       J M S   M E S S A G E   L I S T E N E R
     * -------------------------------------------------------- */
    private class JMSMessageListener implements javax.jms.MessageListener {
        private Hashtable exceptionTable = new Hashtable();
        
        /**
         * �g�s�b�N�Ƀf�[�^�����������ۂɃf�[�^���󂯓n����郁�\�b�h
         * �I�����b�Z�[�W����M�������`�F�b�N���s���A���̃��\�b�h����
         * fireResponce�����s����B
         * 
         * @param msg
         * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
         */
        public void onMessage(javax.jms.Message msg) {
            try {
                if(quit){
                    return;
                }
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

	protected void fireMessage(javax.jms.Message msg) {
		MessageEvent me = createMessageEvent(msg);
		messageListener.onMessage(me);
	}
	
	protected MessageEvent createMessageEvent(javax.jms.Message m){
		return new MessageEvent(new JMSMessageWrapper(m));
	}

	private class JMSMessageWrapper implements Message {
		private final javax.jms.Message msg;
		private Object body;
		public JMSMessageWrapper(javax.jms.Message m){
			this.msg = m;
			if(m instanceof ObjectMessage){
				try{
					body = ((ObjectMessage)m).getObject();
				}catch(JMSException e){
					throw new RuntimeException(e);
				}	
			}else if(m instanceof BytesMessage
					){
				throw new RuntimeException("Not supported");
			}
		}
		@Override
		public Object getBody() {
			return body;
		}
		@Override
		public Object getProperty(String key) {
			try{
				return msg.getObjectProperty(key);
			}catch(JMSException e){
				throw new RuntimeException(e);
			}
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

	public String getNamingFactoryInitial() {
		return namingFactoryInitial;
	}

	public void setNamingFactoryInitial(String namingFactoryInitial) {
		this.namingFactoryInitial = namingFactoryInitial;
	}

	public String getNamingFactoryUrlPkgs() {
		return namingFactoryUrlPkgs;
	}

	public void setNamingFactoryUrlPkgs(String namingFactoryUrlPkgs) {
		this.namingFactoryUrlPkgs = namingFactoryUrlPkgs;
	}

	/**
	 * �ڑ���̃T�[�o(JBoss��JMS�p�̃|�[�g)�ւ̃A�h���X��Ԃ��܂�. �t�H�[�}�b�g��IP�A�h���X:PORT�ł�. ��:
	 * xxx.xxx.xxx.xxx:1099
	 * 
	 * @return
	 */
	public String getNamingProviderUrl() {
		return namingProviderUrl;
	}

	/**
	 * �ڑ���̃T�[�o(JBoss��JMS�p�̃|�[�g)�ւ̃A�h���X���w�肵�܂�. �t�H�[�}�b�g��IP�A�h���X:PORT�ł�. ��:
	 * xxx.xxx.xxx.xxx:1099
	 * 
	 * @param namingProviderUrl
	 *            IP�A�h���X:PORT
	 */
	public void setNamingProviderUrl(String namingProviderUrl) {
		this.namingProviderUrl = namingProviderUrl;
	}
}
