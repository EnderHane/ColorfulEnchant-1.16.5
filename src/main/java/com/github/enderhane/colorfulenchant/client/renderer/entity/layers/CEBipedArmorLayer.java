package com.github.enderhane.colorfulenchant.client.renderer.entity.layers;

import com.github.enderhane.colorfulenchant.client.renderer.CEBufferSourceHelper;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderType;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderTypeBuffer;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class CEBipedArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final A innerModel;
    private final A outerModel;

    public CEBipedArmorLayer(IEntityRenderer<T,M> entityRenderer, A inner, A outer) {
        super(entityRenderer, inner, outer);
        this.innerModel = inner;
        this.outerModel = outer;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer bufferSource, int lightmap, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        renderArmorPiece(matrixStack, bufferSource, entity, EquipmentSlotType.CHEST, lightmap, getArmorModel(EquipmentSlotType.CHEST));
        renderArmorPiece(matrixStack, bufferSource, entity, EquipmentSlotType.LEGS, lightmap, getArmorModel(EquipmentSlotType.LEGS));
        renderArmorPiece(matrixStack, bufferSource, entity, EquipmentSlotType.FEET, lightmap, getArmorModel(EquipmentSlotType.FEET));
        renderArmorPiece(matrixStack, bufferSource, entity, EquipmentSlotType.HEAD, lightmap, getArmorModel(EquipmentSlotType.HEAD));
    }

    private void renderArmorPiece(MatrixStack matrixStack, IRenderTypeBuffer bufferSource, T entity, EquipmentSlotType slot, int lightmap, A model) {
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (itemStack.getItem() instanceof ArmorItem) {
            ArmorItem item = (ArmorItem)itemStack.getItem();
            if (item.getSlot() == slot) {
                model = getArmorModelHook(entity, itemStack, slot, model);
                getParentModel().copyPropertiesTo(model);
                setPartVisibility(model, slot);
                boolean isInner = usesInnerModel(slot);
                boolean hasFoil = itemStack.hasFoil();
                if (item instanceof IDyeableArmorItem) {
                    int i = ((IDyeableArmorItem)item).getColor(itemStack);
                    float red = (float)(i >> 16 & 255) / 255.0F;
                    float green = (float)(i >> 8 & 255) / 255.0F;
                    float blue = (float)(i & 255) / 255.0F;
                    renderModel(matrixStack, bufferSource, lightmap, model, red, green, blue, getArmorResource(entity, itemStack, slot, null));
                    renderModel(matrixStack, bufferSource, lightmap, model, 1.0F, 1.0F, 1.0F, getArmorResource(entity, itemStack, slot, "overlay"));
                } else {
                    renderModel(matrixStack, bufferSource, lightmap, model, 1.0F, 1.0F, 1.0F, getArmorResource(entity, itemStack, slot, null));
                }
                if (hasFoil){
                    renderFoilModel(matrixStack, bufferSource, lightmap, model, 1.0f, 0.5f, 0.5f);
                }
            }
        }
    }

    /**
     * 重做的渲染方法，重新指派了 BufferSource
     * <p>此方法单独渲染装备</p>
     */
    private void renderModel(MatrixStack matrixStack, IRenderTypeBuffer bufferSource, int lightmap, A model, float red, float green, float blue, ResourceLocation armorResource) {
        CERenderTypeBuffer appointedBufferSource = CEBufferSourceHelper.bufferSource();
        RenderType armorRenderType = RenderType.armorCutoutNoCull(armorResource);
        IVertexBuilder armorBuilder = appointedBufferSource.getBuffer(armorRenderType);
        model.renderToBuffer(matrixStack, armorBuilder, lightmap, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
        appointedBufferSource.endBatch(armorRenderType);
    }

    /**
     * 此方法单独渲染光效
     */
    private void renderFoilModel(MatrixStack matrixStack, IRenderTypeBuffer bufferSource, int lightmap, A model, float red, float green, float blue){
        CERenderTypeBuffer appointedBufferSource = CEBufferSourceHelper.bufferSource();
        RenderType foilRenderType = CERenderType.coloredArmorEntityGlint();
        IVertexBuilder foilBuilder = appointedBufferSource.getBuffer(foilRenderType);
        model.renderToBuffer(matrixStack, foilBuilder, lightmap, OverlayTexture.NO_OVERLAY, red, blue, green, 1.0f);
        appointedBufferSource.endBatch(foilRenderType);
    }

    private A getArmorModel(EquipmentSlotType p_241736_1_) {
        return usesInnerModel(p_241736_1_) ? innerModel : outerModel;
    }

    private boolean usesInnerModel(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.LEGS;
    }

}
