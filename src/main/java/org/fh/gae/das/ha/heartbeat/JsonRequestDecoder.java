package org.fh.gae.das.ha.heartbeat;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;

@Component
@ChannelHandler.Sharable
@Slf4j
public class JsonRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        // 只允许POST请求
        boolean isPost = msg.method().name().equals("POST");
        if (false == isPost) {
            ctx.close();
            return;
        }

        // 取出body
        byte[] body = msg.content().copy().array();


        // 反序列化
        BeatMessage requestObj = JSON.parseObject(body, 0, body.length, Charset.forName("utf-8"), BeatMessage.class);
        out.add(requestObj);

        // log.info("heatbeat\t{}", requestObj);
    }
}
