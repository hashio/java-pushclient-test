package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ExceptionEvent;

public interface ExceptionListener {
	/**
	 * JMS�T�[�o�Ƃ̐ڑ����̗�O��JMS�T�[�o���Ŕ���������O���������ɌĂ΂�܂�.
	 * 
	 * @param e
	 */
	public void onException(ExceptionEvent e);
}
