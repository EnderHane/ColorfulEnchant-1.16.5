package com.github.enderhane.colorfulenchant.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

public class CEElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    public CEElytraLayer(IEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    /* TODO: elytra hacking */
}
