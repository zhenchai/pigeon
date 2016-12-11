/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import static org.jboss.netty.channel.Channels.pipeline;

import com.dianping.pigeon.remoting.common.codec.CodecConfig;
import com.dianping.pigeon.remoting.common.codec.CodecConfigFactory;
import com.dianping.pigeon.remoting.netty.codec.CompressHandler;
import com.dianping.pigeon.remoting.netty.codec.Crc32Handler;
import com.dianping.pigeon.remoting.netty.codec.FrameDecoder;
import com.dianping.pigeon.remoting.netty.codec.FramePrepender;
import com.dianping.pigeon.remoting.netty.provider.codec.*;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;


public class NettyServerPipelineFactory implements ChannelPipelineFactory {

    private NettyServer server;

    private static CodecConfig codecConfig = CodecConfigFactory.createClientConfig();

    public NettyServerPipelineFactory(NettyServer server) {
        this.server = server;
    }

    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("framePrepender", new FramePrepender());
        pipeline.addLast("frameDecoder", new FrameDecoder());
        pipeline.addLast("crc32Handler", new Crc32Handler(codecConfig));
        pipeline.addLast("compressHandler", new CompressHandler(codecConfig));
        pipeline.addLast("providerDecoder", new ProviderDecoder());
        pipeline.addLast("providerEncoder", new ProviderEncoder());
        pipeline.addLast("serverHandler", new NettyServerHandler(server));
        return pipeline;
    }

}
