package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.MessageEvent;

public interface MessageListener {
	/**
	 * ���b�Z�[�W���󂯂����ɌĂ΂�܂�.
	 * 
	 * @param e
	 */
	public void onMessage(MessageEvent e);
}
