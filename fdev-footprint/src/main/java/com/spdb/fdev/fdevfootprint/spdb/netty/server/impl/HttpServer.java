package com.spdb.fdev.fdevfootprint.spdb.netty.server.impl;

import com.spdb.fdev.fdevfootprint.spdb.netty.HttpChannelInitializer;
import com.spdb.fdev.fdevfootprint.spdb.netty.server.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer implements Server {

    protected Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private String name = "httpServer";
    private String host = "localhost";
    private int port = 8080;
    private int bossSize = 50;
    private int workerSize = 200;
    private String handler = "";

    private HttpChannelInitializer channelInitializer = new HttpChannelInitializer();

    @Override
    public void start() {
        new Thread(new Runnable() {
            public void run() {
                channelInitializer.setInboundHandler(handler);
                EventLoopGroup bossGroup = new NioEventLoopGroup(bossSize);
                EventLoopGroup workerGroup = new NioEventLoopGroup(workerSize);
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(channelInitializer).option(ChannelOption.SO_BACKLOG, 128)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                    ChannelFuture fulture = bootstrap.bind(port).sync();
                    logger.info(" ****** Http Server for " + name + " was started successfully ******");
                    fulture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    logger.error("@@@InterruptedException at Http Server@@@\n", e);
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }).start();
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void restart() {

    }

    @Override
    public void shutdown() {

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the bossSize
     */
    public int getBossSize() {
        return bossSize;
    }

    /**
     * @param bossSize the bossSize to set
     */
    public void setBossSize(int bossSize) {
        this.bossSize = bossSize;
    }

    /**
     * @return the workerSize
     */
    public int getWorkerSize() {
        return workerSize;
    }

    /**
     * @param workerSize the workerSize to set
     */
    public void setWorkerSize(int workerSize) {
        this.workerSize = workerSize;
    }

    /**
     * @return the handler
     */
    public String getHandler() {
        return handler;
    }

    /**
     * @param handler the handler to set
     */
    public void setHandler(String handler) {
        this.handler = handler;
    }

}
