package jp.a840.push.subscriber.event;

abstract public class Event {
	protected final Object source;

	/**
     * �C�x���g���M���̃I�u�W�F�N�g��^���ăI�u�W�F�N�g�𐶐����܂�.
     * @param source
	 */
    public Event(Object source){
		this.source = source;
	}
	
    /**
     * �C�x���g���M���̃I�u�W�F�N�g��Ԃ��܂�.
     */
	public Object getSource(){
		return source;
	}

}
