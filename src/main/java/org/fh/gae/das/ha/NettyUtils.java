package org.fh.gae.das.ha;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.fh.gae.das.ha.heartbeat.BeatMessage;

public class NettyUtils {

    public static FullHttpRequest buildRequest(BeatMessage msg) {
        String respJson = JSON.toJSONString(msg);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/",
                Unpooled.wrappedBuffer(respJson.getBytes())
        );

        request.headers().set(
                HttpHeaderNames.CONTENT_TYPE.toString(),
                "application/json;charset=utf8"
        );
        request.headers().set(
                HttpHeaderNames.CONTENT_LENGTH.toString(),
                request.content().readableBytes()
        );

        return request;
    }

    public static FullHttpResponse buildResponse(BeatMessage msg) {
        String respJson = JSON.toJSONString(msg);
        // byte[] buf = JSON.toJSONBytes(bidResponse, jsonSnakeConfig);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(respJson.getBytes())
        );

        response.headers().set(
                HttpHeaderNames.CONTENT_TYPE.toString(),
                "application/json;charset=utf8"
        );

        return response;
    }
}
