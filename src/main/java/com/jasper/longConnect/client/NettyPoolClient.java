package com.jasper.longConnect.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.jasper.longConnect.client.ro.ChannelCtl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

public class NettyPoolClient {
    
    private static class Holder {
        private static NettyPoolClient singleton = new NettyPoolClient();
    }
    
    public static NettyPoolClient getSingleton(){
        return Holder.singleton;
    }
    
    final EventLoopGroup group = new NioEventLoopGroup();
    final Bootstrap strap = new Bootstrap();
    
    InetSocketAddress addr1 = new InetSocketAddress("127.0.0.1", 8088);
    InetSocketAddress addr2 = new InetSocketAddress("127.0.0.1", 8089);
    
    static InetSocketAddress[] isaArr = new InetSocketAddress[2];

    
//    public static final AttributeKey<CompletableFuture<byte[]>> FUTURE = AttributeKey.valueOf("fmfuture");
    public static final AttributeKey<CompletableFuture<ChannelCtl>> FUTURE = AttributeKey.valueOf("fmfuture");

    ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;

    public void build() throws Exception {
        strap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {

            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                // TODO Auto-generated method stub
                return new FixedChannelPool(strap.remoteAddress(key), new NettyChannelPoolHandler(), 3);
            }

        };
    }
    
    static AtomicInteger count = new AtomicInteger(1);

    public static void main(String[] args) throws Exception {
        NettyPoolClient client = new NettyPoolClient();
        client.build();
//        final String ECHO_REQ = "Hello Netty.$_";
        final String ECHO_REQ_PRE = "Hello Netty ";
        final String ECHO_REQ_POST = ".$_";
       
        System.out.println("client start");
        System.out.println("for outer:" + System.currentTimeMillis());
        
        isaArr[0] = client.addr1;
        isaArr[1] = client.addr2;
        
        int tNum = 10;
        ExecutorService es = Executors.newFixedThreadPool(tNum);
        
        for (int i = 0; i < tNum; i++) {
            es.execute(client.new T1());
        }
        
        es.shutdown();
        
    }
    
    public byte[] send(byte[] msg) throws Exception {
        
        System.out.println("thread:" + Thread.currentThread().getId());
        
//        SimpleChannelPool pool = client.poolMap.get(client.addr1);
        
        int rd = ThreadLocalRandom.current().nextInt(isaArr.length);
        System.out.println("ThreadLocalRandom:" + rd);

//        SimpleChannelPool pool = NettyPoolClient.getSingleton().poolMap.get(isaArr[rd]);
        SimpleChannelPool pool = NettyPoolClient.getSingleton().poolMap.get(isaArr[0]);
        
        Future<Channel> channelFuture = pool.acquire();
        System.out.println("pool.acquire() | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
        ChannelCtl chctl = new ChannelCtl();
        
        channelFuture.addListener((FutureListener<Channel>) f -> {
            if (f.isSuccess()) {
                final Channel ch = f.getNow();
                System.out.println("for inner:" + System.currentTimeMillis() + " | channel:" + ch.id() + " | msg:" + new String(msg));
                
                chctl.setChannel(ch);
                
                Attribute<CompletableFuture<ChannelCtl>> futureAttribute = ch.attr(NettyPoolClient.FUTURE);
                CompletableFuture<ChannelCtl> future = new CompletableFuture<>();
                futureAttribute.set(future);
                future.complete(chctl);
                
                ch.writeAndFlush(new String(msg, Charset.forName("UTF-8")));
                
//                //Release back to pool
//                pool.release(ch);
            }
        });
        
        byte[] res = null;
        
        try {
            
            chctl.stop(5000);
            
            Channel chl = chctl.getChannel();
            
            if(chl != null){

                res = chctl.getMsg();
                if(res != null && res.length != 0){
                    System.out.println("thread:" + Thread.currentThread().getId() + " front msg:" + new String(res));
                }

                // TO DO
                chl.close();

                //Release back to pool
                pool.release(chl);

            }
            else{
                System.out.println("ch is null | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return res;
        
    }
    
    class T1 extends Thread {
        
        Channel ch;
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            
            System.out.println("thread:" + Thread.currentThread().getId());
            
            final String ECHO_REQ_PRE = "Hello Netty ";
            final String ECHO_REQ_POST = ".$_";
            
//            SimpleChannelPool pool = client.poolMap.get(client.addr1);
            
            int rd = ThreadLocalRandom.current().nextInt(2);
            System.out.println("ThreadLocalRandom:" + rd);

            SimpleChannelPool pool = NettyPoolClient.getSingleton().poolMap.get(isaArr[rd]);
            
            Future<Channel> channelFuture = pool.acquire();
            System.out.println("pool.acquire() | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
            ChannelCtl chctl = new ChannelCtl();
            
            channelFuture.addListener((FutureListener<Channel>) f -> {
                if (f.isSuccess()) {
                    ch = f.getNow();
                    String msg = ECHO_REQ_PRE + count.getAndIncrement() + ECHO_REQ_POST;
                    System.out.println("for inner:" + System.currentTimeMillis() + " | channel:" + ch.id() + " | msg:" + msg);
                    
                    Attribute<CompletableFuture<ChannelCtl>> futureAttribute = ch.attr(NettyPoolClient.FUTURE);
                    CompletableFuture<ChannelCtl> future = new CompletableFuture<>();
                    futureAttribute.set(future);
                    future.complete(chctl);
                    
                    ch.writeAndFlush(msg);
//                    //Release back to pool
//                    pool.release(ch);
                }
            });
            
            String msg = null;
            
            try {
                
                chctl.stop(5000);
                
                //Release back to pool
                if(ch != null){
                    pool.release(ch);
                    
                    byte[] arr = chctl.getMsg();
                    if(arr != null || arr.length != 0){
                        msg = new String(arr);
                    }
                    
                    System.out.println("thread:" + Thread.currentThread().getId() + " front msg:" + msg);
                }
                else{
                    System.out.println("ch is null | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                }
                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
    }

    private void close() {
        // TODO Auto-generated method stub
        group.shutdownGracefully();
    }
    
    
    
}
