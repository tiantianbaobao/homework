package com.tiantianbaobao.week_02.httpserver.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author 86182
 */
public class HttpHandler extends ChannelInboundHandlerAdapter {

    private static final String TEST_URL = "http://127.0.0.1:8803";
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            String uri = fullRequest.uri();
            if (uri.contains("/test")) {
                handlerTest(fullRequest, ctx);
            }
    
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    private void handlerTest(FullHttpRequest fullRequest, ChannelHandlerContext ctx) throws IOException {
        CloseableHttpResponse response = null;
        FullHttpResponse nettyRes = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(TEST_URL);
            response = httpClient.execute(httpGet);
            nettyRes = new DefaultFullHttpResponse(HTTP_1_1, OK);
            nettyRes.headers().set("Content-Type", "application/json");
            nettyRes.headers().set("Content-Length", String.valueOf(response.getEntity().getContentLength()));
            if (response.getEntity().getContent().available() > 0) {
                nettyRes.content().writeBytes(response.getEntity().getContent(), (int) response.getEntity().getContentLength());
            }
        } catch (Exception e) {
            System.out.println("处理出错:"+e.getMessage());
            nettyRes = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(nettyRes).addListener(ChannelFutureListener.CLOSE);
                } else {
                    nettyRes.headers().set(CONNECTION.toString(), KEEP_ALIVE.toString());
                    ctx.write(nettyRes);
                }
            }
            if (httpClient !=null) {
                httpClient.close();
            }
            if (response != null ) {
                response.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
