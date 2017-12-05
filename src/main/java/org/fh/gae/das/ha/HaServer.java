package org.fh.gae.das.ha;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.ha.heartbeat.BeatHandler;
import org.fh.gae.das.ha.heartbeat.BeatTask;
import org.fh.gae.das.ha.heartbeat.JsonRequestDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HaServer {
    @Autowired
    private JsonRequestDecoder decoder;

    @Autowired
    private BeatHandler beatHandler;

    @Autowired
    private BeatTask beatTask;

    @Value("${das.ha.port}")
    private int port;

    private NioEventLoopGroup group = new NioEventLoopGroup(1);

    private NioEventLoopGroup scheduleGroup = new NioEventLoopGroup(1);

    public void start() {
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress("127.0.0.1", port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast("codec", new HttpServerCodec());
                            socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
                            socketChannel.pipeline().addLast("jsonDecoder", decoder);
                            socketChannel.pipeline().addLast("handler", beatHandler);
                        }
                    });

            ChannelFuture f = boot.bind().sync();

            log.info("start receiving heartbeat at {}:{}", "127.0.0.1", port);
            startBeat();

            // f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
            scheduleGroup.shutdownGracefully();
        }
    }

    private void startBeat() {
        Random random = new Random();
        scheduleGroup.scheduleAtFixedRate(beatTask, random.nextInt(1000), 5000, TimeUnit.MILLISECONDS);
    }

}
