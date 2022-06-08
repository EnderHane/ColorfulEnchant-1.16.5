package com.github.enderhane.colorfulenchant.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.Util;

import java.util.SortedMap;

public class CEBufferSourceHelper {

    private static final RegionRenderCacheBuilder fixedBufferPack = new RegionRenderCacheBuilder();
    private static final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        map.put(Atlases.solidBlockSheet(), fixedBufferPack.builder(RenderType.solid()));
        map.put(Atlases.cutoutBlockSheet(), fixedBufferPack.builder(RenderType.cutout()));
        map.put(Atlases.bannerSheet(), fixedBufferPack.builder(RenderType.cutoutMipped()));
        map.put(Atlases.translucentCullBlockSheet(), fixedBufferPack.builder(RenderType.translucent()));
        put(map, Atlases.shieldSheet());
        put(map, Atlases.bedSheet());
        put(map, Atlases.shulkerBoxSheet());
        put(map, Atlases.signSheet());
        put(map, Atlases.chestSheet());
        put(map, RenderType.translucentNoCrumbling());
        put(map, RenderType.armorGlint());
        put(map, RenderType.armorEntityGlint());
        put(map, RenderType.glint());
        put(map, RenderType.glintDirect());
        put(map, RenderType.glintTranslucent());
        put(map, RenderType.entityGlint());
        put(map, RenderType.entityGlintDirect());
        put(map, RenderType.waterMask());
        ModelBakery.DESTROY_TYPES.forEach((destroyType) -> put(map, destroyType));
    });
    private static final CERenderTypeBuffer bufferSource = new CERenderTypeBuffer(new BufferBuilder(256), fixedBuffers);

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type) {
        map.put(type, new BufferBuilder(type.bufferSize()));
    }

    public static CERenderTypeBuffer bufferSource() {
        return bufferSource;
    }

}
