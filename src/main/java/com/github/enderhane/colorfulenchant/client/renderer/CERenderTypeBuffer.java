package com.github.enderhane.colorfulenchant.client.renderer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

import java.util.*;

public class CERenderTypeBuffer extends IRenderTypeBuffer.Impl {

    protected final BufferBuilder defaultBuilder;
    protected final SortedMap<RenderType, BufferBuilder> fixedBuffers;
    protected Optional<RenderType> lastState = Optional.empty();
    protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

    public CERenderTypeBuffer(BufferBuilder builder, SortedMap<RenderType, BufferBuilder> map){
        super(builder, map);
        defaultBuilder = builder;
        fixedBuffers = map;
    }

    @Override
    public IVertexBuilder getBuffer(RenderType renderType) {
        Optional<RenderType> stateIn = renderType.asOptional();
        BufferBuilder builder = getBuilderRaw(renderType);
        if (!Objects.equals(lastState, stateIn)) {
            if (lastState.isPresent()) {
                RenderType lastType = lastState.get();
                if (!fixedBuffers.containsKey(lastType)) {
                    endBatch(lastType);
                }
            }
            if (startedBuffers.add(builder)) {
                builder.begin(renderType.mode(), renderType.format());
            }
            lastState = stateIn;
        }
        return builder;
    }

    private BufferBuilder getBuilderRaw(RenderType renderType) {
        return fixedBuffers.getOrDefault(renderType, defaultBuilder);
    }

    @Override
    public void endBatch() {
        lastState.ifPresent((lastType) -> {
            if (fixedBuffers.containsKey(lastType)) {
                endBatch(lastType);
            }
        });
        for(RenderType rendertype : fixedBuffers.keySet()) {
            endBatch(rendertype);
        }
    }

    /**
     * 先遍历处理 fixedBuffers 再处理 lastState
     * <p> {@link com.github.enderhane.colorfulenchant.client.renderer.CEBufferSource#fixedBuffers}</p>
     */
    public void endBatchAfterAll(){
        for(RenderType rendertype : fixedBuffers.keySet()) {
            endBatch(rendertype);
        }
        lastState.ifPresent((renderType) -> {
            if (!fixedBuffers.containsKey(renderType)) {
                endBatch(renderType);
            }
        });
    }

    /**
     * lastState 将在 target 后处理
     * <p>如果 target 为 null，lastState 首先处理。</p>
     */
    public void endBatchAfter(RenderType target) {
        if (lastState.isPresent()) {
            RenderType lastType = lastState.get();
            if (target == null) {
                if(!fixedBuffers.containsKey(lastType)){
                    endBatch(lastType);
                }
                for(RenderType type : fixedBuffers.keySet()) {
                    endBatch(type);
                }
            } else if (fixedBuffers.containsKey(target) && !fixedBuffers.containsKey(lastType)) {
                for (RenderType type : fixedBuffers.keySet()) {
                    endBatch(type);
                    if (type == target){
                        endBatch(lastType);
                    }
                }
            }
        } else {
            for(RenderType rendertype : fixedBuffers.keySet()) {
                endBatch(rendertype);
            }
        }
    }

    @Override
    public void endBatch(RenderType renderType) {
        BufferBuilder builder = getBuilderRaw(renderType);
        boolean flag = Objects.equals(lastState, renderType.asOptional());
        if (flag || builder != defaultBuilder) {
            if (startedBuffers.remove(builder)) {
                renderType.end(builder, 0, 0, 0);
                if (flag) {
                    lastState = Optional.empty();
                }

            }
        }
    }

}
