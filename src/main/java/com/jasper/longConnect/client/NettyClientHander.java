package com.jasper.longConnect.client;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.jasper.longConnect.client.ro.BaseMsg;
import com.jasper.longConnect.client.ro.ChannelCtl;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

@Sharable
public class NettyClientHander extends ChannelInboundHandlerAdapter {

    static AtomicInteger count = new AtomicInteger(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
        System.out.println("channelActive | channel:" + ctx.channel().id()+ " | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelRegistered(ctx);
        System.out.println("channelRegistered | channel:" + ctx.channel().id()+ " | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        System.out.println("channelInactive | channel:" + ctx.channel().id()+ " | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("read:" + count.getAndIncrement() + " | msg:" + msg + " | channel:" + ctx.channel().id()+ " | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
    
//        BaseMsg baseMsg = (BaseMsg)msg;
        
//        if(baseMsg == null || baseMsg.getbBodyMsg() == null || baseMsg.getbHeadMsg() == null){
//            //..to do
//            return;
//        }
        
        Attribute<CompletableFuture<ChannelCtl>> futureAttribute = ctx.channel().attr(NettyPoolClient.FUTURE);
        CompletableFuture<ChannelCtl> future = futureAttribute.get();
        ChannelCtl ctl = future.get();
        
        //copmare baseMsg.getbHeadMsg().getSerial() and ctl.getSerial()
//        if(Arrays.equals(baseMsg.getbHeadMsg().getSerial(), ctl.getSerial())){
            ctl.setMsg(((String)msg).getBytes());
            ctl.start();
//        }
//        else{
//            // to do
//        }
        
//        Attribute<CompletableFuture<byte[]>> futureAttribute = ctx.channel().attr(NettyPoolClient.FUTURE);
//        CompletableFuture<byte[]> future = new CompletableFuture<>();
//        future.complete(((String)msg).getBytes());
//        
//        futureAttribute.set(future);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("exceptionCaught | channel:" + ctx.channel().id()+ " | tid:" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
//        Attribute<CompletableFuture<byte[]>> futureAttribute = ctx.channel().attr(NettyPoolClient.FUTURE);
//        CompletableFuture<byte[]> future = futureAttribute.getAndRemove();
        cause.printStackTrace();
        ctx.close();
//        future.completeExceptionally(cause);
    }
    
}