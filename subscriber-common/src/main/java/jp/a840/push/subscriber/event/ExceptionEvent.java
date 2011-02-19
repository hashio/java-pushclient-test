package jp.a840.push.subscriber.event;

public class ExceptionEvent extends Event{
	protected final Exception exception;

    /**
     * �C�x���g���M���̃I�u�W�F�N�g�Ɣ���������O��^���ăI�u�W�F�N�g�𐶐����܂�.
     * @param source
     * @param exception
     */
	public ExceptionEvent(Object source, Exception exception){
		super(source);
		this.exception = exception;
	}

    /**
     * ����������O��Ԃ��܂�.
     * @return
     */
	public Exception getException() {
		return exception;
	}

}
