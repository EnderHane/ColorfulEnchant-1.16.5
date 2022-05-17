package com.github.enderhane.colorfulenchant.client.proxy;

import com.github.enderhane.colorfulenchant.ColorfulEnchant;
import com.github.enderhane.colorfulenchant.client.renderer.CEItemRenderer;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderType;
import com.github.enderhane.colorfulenchant.client.renderer.entity.layers.CEBipedArmorLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.DrownedModel;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityType;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class ClientProxy{

    private static boolean isRendererReplacementExecuted = false;

    /**
     * 渲染器修改入口方法
     */
    public static void init() {
        if (!isRendererReplacementExecuted){
            replaceItemRenderer();
            replaceArmorLayer();
            isRendererReplacementExecuted = true;
        }
    }

    /**
     * 物品渲染器修改方法
     * @return 无作用
     */
    private static boolean replaceItemRenderer(){
        Minecraft minecraft = Minecraft.getInstance();
        CEItemRenderer modItemRenderer = new CEItemRenderer(minecraft.getItemRenderer(), minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors());
        IngameGui gui = minecraft.gui;
        FirstPersonRenderer itemInHandRenderer = minecraft.getItemInHandRenderer();
        final Field itemRenderer_Minecraft = ObfuscationReflectionHelper.findField(Minecraft.class, "field_175621_X");//itemRenderer
        final Field itemRenderer_IngameGui = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73841_b");//itemRenderer
        final Field itemRenderer_FirstPersonRenderer = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_178112_h");//itemRenderer
        try {
            itemRenderer_Minecraft.setAccessible(true);
            itemRenderer_IngameGui.setAccessible(true);
            itemRenderer_FirstPersonRenderer.setAccessible(true);
            itemRenderer_Minecraft.set(minecraft, modItemRenderer);
            itemRenderer_IngameGui.set(gui, modItemRenderer);
            itemRenderer_FirstPersonRenderer.set(itemInHandRenderer, modItemRenderer);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        EntityRendererManager entityRendererManager = minecraft.getEntityRenderDispatcher();
        entityRendererManager.register(EntityType.EXPERIENCE_BOTTLE, new SpriteRenderer<>(entityRendererManager, modItemRenderer));
        entityRendererManager.register(EntityType.ITEM, new ItemRenderer(entityRendererManager, modItemRenderer));
        entityRendererManager.register(EntityType.ITEM_FRAME, new ItemFrameRenderer(entityRendererManager, modItemRenderer));
        entityRendererManager.register(EntityType.POTION, new SpriteRenderer<>(entityRendererManager, modItemRenderer));
        ((IReloadableResourceManager)(minecraft.getResourceManager())).registerReloadListener(modItemRenderer);
        return true;
    }

    /**
     * 装备渲染器修改方法
     * @return 无作用
     */
    private static boolean replaceArmorLayer(){
        Minecraft minecraft = Minecraft.getInstance();
        final Field layers_LivingRenderer = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
        try {
            layers_LivingRenderer.setAccessible(true);
            for (Map.Entry<String, PlayerRenderer> entry : minecraft.getEntityRenderDispatcher().playerRenderers.entrySet()) {
                PlayerRenderer playerRenderer = entry.getValue();
                List layerList = (List) layers_LivingRenderer.get(playerRenderer);
                layerList.replaceAll(e -> e instanceof BipedArmorLayer ? delegateArmorLayer((BipedArmorLayer) e, entry.getKey()) : e);
            }
            for (Map.Entry<EntityType<?>, EntityRenderer<?>> entry: minecraft.getEntityRenderDispatcher().renderers.entrySet()){
                EntityRenderer renderer = entry.getValue();
                if (renderer instanceof LivingRenderer){
                    List layerList = (List) layers_LivingRenderer.get(renderer);
                    layerList.replaceAll(e -> e instanceof BipedArmorLayer ? delegateArmorLayer((BipedArmorLayer) e, entry.getKey()) : e);
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 拆除原装备渲染器，重组一个新的渲染器
     * @param delegate 原渲染器
     * @return 返回新渲染器，如果出现异常则返回原渲染器
     */
    private static BipedArmorLayer delegateArmorLayer(BipedArmorLayer delegate, Object info){
        final Field renderer = ObfuscationReflectionHelper.findField(LayerRenderer.class, "field_215335_a");
        final Field innerModel = ObfuscationReflectionHelper.findField(BipedArmorLayer.class, "field_177189_c");
        final Field outerModel = ObfuscationReflectionHelper.findField(BipedArmorLayer.class, "field_177186_d");
        try {
            renderer.setAccessible(true);
            innerModel.setAccessible(true);
            outerModel.setAccessible(true);
            IEntityRenderer rd = (IEntityRenderer) renderer.get(delegate);
            BipedModel in = (BipedModel) innerModel.get(delegate);
            BipedModel out = (BipedModel) outerModel.get(delegate);
            return new CEBipedArmorLayer<>(rd, in, out);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            ColorfulEnchant.getLogger().error("ArmorLayer transformation failed: " + info.toString());
        }
        return delegate;
    }

}
