package com.jasper.longConnect.client.ro;

import io.netty.channel.Channel;

public class ChannelCtl {
    
    byte[] msg;
    
    Channel channel;
    
    byte[] serial;
    
    public byte[] getMsg() {
        return msg;
    }
    
    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
    
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public synchronized void start(){
        
        notify();
    }
    
//  public synchronized void startAll(){
//      
//      notifyAll();
//      
//  }
    
    public synchronized void stop(long timeout){
        
        try {
            wait(timeout);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
