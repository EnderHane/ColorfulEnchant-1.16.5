package com.github.enderhane.colorfulenchant.client.renderer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

import java.util.*;

public class CERenderTypeBuffer extends IRenderTypeBuffer.Impl {

    public CERenderTypeBuffer(BufferBuilder builder, SortedMap<RenderType, BufferBuilder> map){
        super(builder, map);
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

}
