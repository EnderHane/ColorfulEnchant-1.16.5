package com.github.enderhane.colorfulenchant.client.proxy;

import com.github.enderhane.colorfulenchant.client.renderer.CEItemRenderer;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public class ClientProxy{

    public static boolean isRendererReplacementExecuted = false;

    private static CEItemRenderer modItemRenderer;

    public static void init() {
        if (!isRendererReplacementExecuted){
            replaceRenderer();
            isRendererReplacementExecuted = true;
        }
    }

    public static boolean replaceRenderer(){
        Minecraft minecraft = Minecraft.getInstance();
        IngameGui gui = minecraft.gui;
        FirstPersonRenderer itemInHandRenderer = minecraft.getItemInHandRenderer();
        final Field itemRenderer_Minecraft = ObfuscationReflectionHelper.findField(Minecraft.class, "field_175621_X");//itemRenderer
        final Field itemRenderer_IngameGui = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73841_b");//itemRenderer
        final Field itemRenderer_FirstPersonRenderer = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_178112_h");//itemRenderer
        final Field textureManager = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71446_o");//textureManager
        final Field modelManager = ObfuscationReflectionHelper.findField(Minecraft.class, "field_175617_aL");//modelManager
        final Field itemColors = ObfuscationReflectionHelper.findField(Minecraft.class, "field_184128_aI");//itemColors
        try {
            itemRenderer_Minecraft.setAccessible(true);
            itemRenderer_IngameGui.setAccessible(true);
            itemRenderer_FirstPersonRenderer.setAccessible(true);
            ItemRenderer originalItemRenderer = (ItemRenderer) itemRenderer_Minecraft.get(minecraft);
            modItemRenderer = new CEItemRenderer(originalItemRenderer, (TextureManager)textureManager.get(minecraft), (ModelManager) modelManager.get(minecraft), (ItemColors) itemColors.get(minecraft));
            itemRenderer_Minecraft.set(minecraft, modItemRenderer);
            itemRenderer_IngameGui.set(gui, modItemRenderer);
            itemRenderer_FirstPersonRenderer.set(itemInHandRenderer, modItemRenderer);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        ((IReloadableResourceManager)(minecraft.getResourceManager())).registerReloadListener(modItemRenderer);
        //CERenderType.registerAll();
        return true;
    }

}