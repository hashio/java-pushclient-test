package jp.a840.push.publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.grizzly.RateWebSocket;

import org.apache.commons.lang.math.RandomUtils;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


	public class DummyRateApplication extends WebSocketApplication {

	private Logger log = LoggerFactory.getLogger(DummyRateApplication.class);
	
	private ExecutorService executorService;
	
	private AtomicInteger updateInterval = new AtomicInteger(1000);
	
	private ConcurrentHashMap<String, RateGenerateIterator> its = new ConcurrentHashMap<String, RateGenerateIterator>();

	public DummyRateApplication() throws InitializeException {
		for(int i = 0; i < 10; i++){
			its.put(String.valueOf(i), new RateGenerateIterator(String.valueOf(i)));
		}
	}	
	

	@Override
    public void onClose(WebSocket websocket) {
		log.info("close");
		super.onClose(websocket);
	}

	public void onMessage(RateWebSocket websocket, String text)
			throws IOException {
		log.info("message");
		String[] params = text.split(":");
		if("UPDATE INTERVAL".equalsIgnoreCase(params[0])){
			updateInterval.set(Integer.valueOf(params[1]));
		}else if("ADD PAIR".equalsIgnoreCase(params[0])){
			String pair = params[1];
			RateGenerateIterator it = new RateGenerateIterator(pair);
			if(its.putIfAbsent(pair, it) == null){
				doExecuteService(it);	
			}
		}else if("REMOVE PAIR".equalsIgnoreCase(params[0])){
			String pair = params[1];
			RateGenerateIterator it = its.remove(pair);
			if(it != null){
				it.stop();
			}
		}
	}
	
	public void startSubscribe() throws Exception {
		executorService = Executors.newFixedThreadPool(30);
		
		for(final Iterator<RateBean> it : its.values()){
			doExecuteService(it);
		}
	}
	
	private void doExecuteService(final Iterator<RateBean> it){
		executorService.execute(new Runnable() {
			public void run() {
				try{
					while(it.hasNext()){
						byte[] bytes = serializeRateBean(it.next());
						for(final WebSocket rws : getWebSockets()){
							rws.send(bytes);
						}
						Thread.sleep(RandomUtils.nextInt(updateInterval.get()));
					}
				}catch(InterruptedException e){
					;
				}
			}
		});		
	}

	public byte[] serializeRateBean(RateBean rate){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(rate);
			return baos.toByteArray();
		}catch(IOException e){
			;
		}
		return null;
	}
	
	public void stopSubscribe(){
	}
	
	public void addSubscribe(String destination, String messageSelector){
	}

	@Override
	public boolean isApplicationRequest(HttpRequestPacket request) {
		return "/rate".equals(request.getRequestURI());
	}

	public class RateGenerateIterator implements Iterator<RateBean> {
		private RateBean currentDto;
		
		volatile private boolean hasNextFlg = true;
		
		public RateGenerateIterator(String currencyPair){
			RateBean dto = new RateBean();
			dto.setCurrencyPair(currencyPair);
			dto.setAsk(new BigDecimal("100.00"));
			dto.setBid(new BigDecimal("100.00"));
			currentDto = dto;
		}
		
		public boolean hasNext() {
			return hasNextFlg;
		}

		synchronized public RateBean next() {
			BigDecimal ask = generateRate(currentDto.getAsk());
			currentDto.setAsk(ask);
			
			BigDecimal bid = generateRate(currentDto.getBid());
			currentDto.setBid(bid);

			currentDto.setUpdateTime(new Date());
			return currentDto;
		}

		public void remove() {
		}
		
		public void stop(){
			hasNextFlg = false;
		}
		
		private BigDecimal generateRate(BigDecimal rate){
			int num = RandomUtils.nextInt(200) - 100;
			double amount = Math.round((num > 0? 1: -1) * num * num / 1000);
			int scale = (int)Math.pow(10, rate.scale());
			return rate.add(BigDecimal.valueOf(amount / scale));
		}
	}
}
