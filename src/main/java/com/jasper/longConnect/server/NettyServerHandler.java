package com.jasper.longConnect.server;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    static AtomicInteger count = new AtomicInteger(1);
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO Auto-generated method stub
        String body = (String) msg;
        System.out.println("channelActived:" + body);
        super.channelActive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO Auto-generated method stub
        super.channelRead(ctx, msg);
        String body = (String) msg;
        System.out.println(count.getAndIncrement() + " | body:" + body + " | channel:" + ctx.channel().id());
        
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
        
        String msgStr = "Welcome to Netty " + count.get() + ".$_";
        System.out.println("res:" + msgStr + " | channel:" + ctx.channel().id());
        
        ctx.writeAndFlush(msgStr);
    }
    
}
