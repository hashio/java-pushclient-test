package jp.a840.push.subscriber.exception;

/**
 * ���X�|���X����莞�Ԃ����Ă����Ȃ���������Exception
 * 
 */
public class TimeoutException extends RuntimeException {

	/**
	 * �f�t�H���g�R���X�g���N�^
	 * 
	 */
	public TimeoutException() {
		super();
	}

	/**
	 * �f�t�H���g�R���X�g���N�^
	 * 
	 * @param message
	 */
	public TimeoutException(String message) {
		super(message);
	}

	/**
	 * �f�t�H���g�R���X�g���N�^
	 * 
	 * @param cause
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * �f�t�H���g�R���X�g���N�^
	 * 
	 * @param message
	 * @param cause
	 */
	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
