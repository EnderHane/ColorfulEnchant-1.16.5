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
        Optional<RenderType> optional = renderType.asOptional();
        BufferBuilder bufferbuilder = getBuilderRaw(renderType);
        if (!Objects.equals(lastState, optional)) {
            if (lastState.isPresent()) {
                RenderType rendertype = lastState.get();
                if (!fixedBuffers.containsKey(rendertype)) {
                    endBatch(rendertype);
                }
            }
            if (startedBuffers.add(bufferbuilder)) {
                bufferbuilder.begin(renderType.mode(), renderType.format());
            }
            lastState = optional;
        }
        return bufferbuilder;
    }

    private BufferBuilder getBuilderRaw(RenderType renderType) {
        return fixedBuffers.getOrDefault(renderType, defaultBuilder);
    }

    @Override
    public void endBatch() {
        lastState.ifPresent((lastType) -> {
            if (getBuilderRaw(lastType) == defaultBuilder) {
                endBatch(lastType);
            }
        });
        for(RenderType rendertype : fixedBuffers.keySet()) {
            endBatch(rendertype);
        }
    }

    /**
     * 先遍历处理 fixedBuffers 再处理 lastState，以满足某些特殊需求
     * <p>例如，本mod中分开渲染附魔光效，要想光效正常渲染，须保证自定义 Glint 系列的 RenderType 在最后被处理
     * （原版也是这么做的 {@link net.minecraft.client.renderer.RenderTypeBuffers#fixedBuffers}，但是写死了）</p>
     */
    public void endBatchAfterAll(){
        for(RenderType rendertype : fixedBuffers.keySet()) {
            endBatch(rendertype);
        }
        lastState.ifPresent((renderType) -> {
            IVertexBuilder vertexBuilder = getBuilderRaw(renderType);
            if (vertexBuilder == defaultBuilder) {
                endBatch(renderType);
            }
        });
    }


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
        BufferBuilder bufferbuilder = getBuilderRaw(renderType);
        boolean flag = Objects.equals(lastState, renderType.asOptional());
        if (flag || bufferbuilder != defaultBuilder) {
            if (startedBuffers.remove(bufferbuilder)) {
                renderType.end(bufferbuilder, 0, 0, 0);
                if (flag) {
                    lastState = Optional.empty();
                }

            }
        }
    }

}
