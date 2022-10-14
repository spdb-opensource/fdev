package com.spdb.fdev.fdevfootprint.spdb.netty;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import com.spdb.fdev.fdevfootprint.spdb.netty.utils.ApplicationContextHelper;

import java.util.concurrent.TimeUnit;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private String inboundHandler = null;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
		ch.pipeline().addLast(new HttpRequestDecoder());
		// server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
		ch.pipeline().addLast(new HttpResponseEncoder());
		// maxContentLength-1MB
		ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
		// 处理keep-alive时长
		ch.pipeline().addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS));
		ch.pipeline().addLast((ChannelInboundHandler) ApplicationContextHelper.getBean(this.getInboundHandler()));
	}

	public String getInboundHandler() {
		return inboundHandler;
	}

	public void setInboundHandler(String inboundHandler) {
		this.inboundHandler = inboundHandler;
	}

}
