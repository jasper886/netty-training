package com.jasper.longConnect.client;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTest {
    
    static AtomicInteger count = new AtomicInteger(1);
    
    public static void main(String[] args) {
        
        try {
            
            ClientTest ct = new ClientTest();
            
            NettyPoolClient client = NettyPoolClient.getSingleton();
            client.build();
//            final String ECHO_REQ = "Hello Netty.$_";
            final String ECHO_REQ_PRE = "Hello Netty ";
            final String ECHO_REQ_POST = ".$_";
           
            System.out.println("client start");
            System.out.println("for outer:" + System.currentTimeMillis());
            
            client.isaArr[0] = client.addr1;
            client.isaArr[1] = client.addr2;
            
            int tNum = 20;
            ExecutorService es = Executors.newFixedThreadPool(tNum);
            
            for (int i = 0; i < tNum/2; i++) {
                es.execute(ct.new T2(client, count.getAndIncrement()));
            }
            
//            Thread.sleep(2000L);
//            
//            for (int i = 0; i < tNum/2; i++) {
//                es.execute(ct.new T2(client, count.getAndIncrement()));
//            }
            
            es.shutdown();
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    class T2 extends Thread {
        
        NettyPoolClient client;
        int num;
        
        public T2(NettyPoolClient client, int num) {
            // TODO Auto-generated constructor stub
            this.client = client;
            this.num = num;
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            
            try {
                
                final String ECHO_REQ_PRE = "Hello Netty ";
                final String ECHO_REQ_POST = ".$_";
                
                String msg = ECHO_REQ_PRE + num + ECHO_REQ_POST;
                
                byte[] res = client.send(msg.getBytes(Charset.forName("UTF-8")));
                
                if(res != null && res.length != 0){
                    System.out.println("res:" + new String(res, Charset.forName("UTF-8")));
                }
                else{
                    System.out.println("res is null");
                }
                
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            
        }
        
    }
    
}
