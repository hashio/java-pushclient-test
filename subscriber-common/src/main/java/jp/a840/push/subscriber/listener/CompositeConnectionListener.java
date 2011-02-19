package jp.a840.push.subscriber.listener;

import java.util.ArrayList;
import java.util.List;

import jp.a840.push.subscriber.event.ConnectionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * �����̃��X�i�[�փv���L�V���邽�߂̃N���X.
 * �ꕔ�̃��X�i�[���炾���t�B�[�h�����f�[�^���t�B���^���������͂�����p�����Ď������Ă݂Ă�������.
 * <br>
 */
public class CompositeConnectionListener implements ConnectionListener {
    private Logger log = LoggerFactory.getLogger(CompositeConnectionListener.class);
    
    protected ConnectionListener[] connectionListeners = new ConnectionListener[0];
    private List connectionListenerList = new ArrayList();

    public CompositeConnectionListener() {
        super();
    }

    public void onConnected(ConnectionEvent e) {
    	ConnectionListener[] listeners = connectionListeners;
        for(int i = 0; i < listeners.length; i++){
            if(log.isTraceEnabled()){
                log.trace("fire `onConnected' event to listener: " + listeners[i].toString());
            }
            listeners[i].onConnected(e);
        }
    }

    public void onDisonnected(ConnectionEvent e) {
    	ConnectionListener[] listeners = connectionListeners;
        for(int i = 0; i < listeners.length; i++){
            if(log.isTraceEnabled()){
                log.trace("fire `onDisconnected' event to listener: " + listeners[i].toString());
            }
            listeners[i].onDisonnected(e);
        }
    }

    /**
     * �V�K�ɒǉ����ꂽ���X�i�[�����ۂɎ�M�̍ۂɗ��p�ł���悤�ɂ��܂�.
     *
     */
    private void synchronizeConnectionListenerList(){
        ConnectionListener[] listeners = new ConnectionListener[connectionListenerList.size()];
        connectionListenerList.toArray(listeners);
        this.connectionListeners = listeners;
    }
    
    /**
     * ConnectionListener��ǉ����܂�.
     * @param listener ConnectionListener
     */
    public void addConnectionListener(ConnectionListener listener){
        if(listener == null){
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("add connection listener: " + listener.toString());
        }
        synchronized (connectionListenerList) {
            connectionListenerList.add(listener);
        }
        synchronizeConnectionListenerList();
    }

    /**
     * �w�肳�ꂽConnectionListner�����X�i�[���X�g����폜���܂�.
     * @param listener
     */
    public void removeConnectionListener(ConnectionListener listener){
        if(listener == null){
            return;
        }
        if(connectionListenerList.size() == 0){
            log.warn("Can't removed. listeners list is empty");
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("remove connection listener: " + listener.toString());
        }
        synchronized (connectionListenerList) {
            if(!connectionListenerList.remove(listener)){
                log.warn("Can't removed. listener not found in list");
            }
            if(connectionListenerList.size() == 0){
                log.warn("removed last message listener of realtime client manager");
            }
        }
        synchronizeConnectionListenerList();
    }

    /**
     * ���X�i�[�̃��X�g���擾���܂�.
     * @return
     */
    public List getConnectionListenerList() {
        return connectionListenerList;
    }
}
