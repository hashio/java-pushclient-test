package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ConnectionEvent;

public interface ConnectionListener {
	/**
	 * JMS�T�[�o�Ɛڑ��������ɌĂ΂�܂�.
	 * 
	 * @param e
	 */
	public void onConnected(ConnectionEvent e);

	/**
	 * JMS�T�[�o�Ƃ̐ڑ����ؒf(�T�[�o�A�N���C�A���g�ǂ��炩��ł�)�������ɌĂ΂�܂�.
	 * 
	 * @param e
	 */
	public void onDisonnected(ConnectionEvent e);
}
