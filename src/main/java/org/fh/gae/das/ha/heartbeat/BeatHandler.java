package org.fh.gae.das.ha.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.ha.CoordinationService;
import org.fh.gae.das.ha.NettyUtils;
import org.fh.gae.das.mysql.binlog.BinlogPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 处理master的心跳包
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class BeatHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private CoordinationService coordinationService;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        BeatMessage msg = (BeatMessage) o;
        if (BeatMessage.MessageType.REPORT == BeatMessage.MessageType.of(msg.getType())) {
            log.info("heartbeat received, {}:{}", msg.getBinlog(), msg.getPosition());
            BeatTimeHolder.lastBeat = new Date().getTime();

            // 已经是slave状态
            // 发送ACK
            if (CoordinationService.Status.SLAVE == coordinationService.status()) {
                if (!StringUtils.isEmpty(msg.getBinlog())) {
                    BeatTimeHolder.position = new BinlogPosition(msg.getBinlog(), msg.getPosition());
                }

                ctx.writeAndFlush(NettyUtils.buildResponse(BeatMessage.Builder.buildAck()));
                ctx.close();

                return;
            }

            // 已经是master状态
            if (CoordinationService.Status.MASTER == coordinationService.status()) {
                ctx.writeAndFlush(NettyUtils.buildResponse(BeatMessage.Builder.buildMaster()));
                ctx.close();
                return;
            }

            // 无状态
            // 尝试变成slave
            boolean result = coordinationService.trySlave();
            if (result) {
                log.info("changed status to SLAVE");

                if (!StringUtils.isEmpty(msg.getBinlog())) {
                    BeatTimeHolder.position = new BinlogPosition(msg.getBinlog(), msg.getPosition());
                }

                ctx.writeAndFlush(NettyUtils.buildResponse(BeatMessage.Builder.buildAck()));
                ctx.close();

                return;

            }

            ctx.writeAndFlush(NettyUtils.buildResponse(BeatMessage.Builder.buildUnknown()));
            ctx.close();
            return;
        }

        ctx.writeAndFlush(NettyUtils.buildResponse(BeatMessage.Builder.buildUnknown()));
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println(evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;

            switch (e.state()) {
                case READER_IDLE:
                    // takeOver(ctx);
                    break;

                case WRITER_IDLE:
                    // sendBeat(ctx);
                    break;
            }
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
    }

}
