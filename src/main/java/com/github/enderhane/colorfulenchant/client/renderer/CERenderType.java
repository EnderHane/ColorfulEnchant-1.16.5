package com.github.enderhane.colorfulenchant.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class CERenderType extends RenderType {

    //dummy
    private CERenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static final RenderType COLORED_GLINT_TRANSLUCENT = create(
            "colored_glint_translucent", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(GLINT_TEXTURING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));

    public static final RenderType COLORED_GLINT = create(
            "colored_glint", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(RenderState.EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(RenderState.GLINT_TEXTURING)
                    .createCompositeState(false));

    public static final RenderType COLORED_GLINT_DIRECT = create(
            "colored_glint_direct", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(GLINT_TEXTURING)
                    .createCompositeState(false));

    public static final RenderType COLORED_ARMOR_GLINT = create(
            "colored_armor_glint", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(GLINT_TEXTURING)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));

    public static final RenderType COLORED_ARMOR_ENTITY_GLINT = create(
            "colored_armor_entity_glint", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(ENTITY_GLINT_TEXTURING)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));

    public static final RenderType COLORED_ENTITY_GLINT = create(
            "colored_entity_glint", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setTexturingState(ENTITY_GLINT_TEXTURING)
                    .createCompositeState(false));

    public static final RenderType COLORED_ENTITY_GLINT_DIRECT = create(
            "colored_entity_glint_direct", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
            State.builder()
                    .setTextureState(new TextureState(CEItemRenderer.CUSTOM_GLINT_LOCATION, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(ENTITY_GLINT_TEXTURING)
                    .createCompositeState(false));

    public static RenderType coloredGlintTranslucent() {
        return COLORED_GLINT_TRANSLUCENT;
    }

    public static RenderType coloredGlint(){
        return COLORED_GLINT;
    }

    public static RenderType coloredGlintDirect() {
        return COLORED_GLINT_DIRECT;
    }

    public static RenderType coloredArmorGlint() {
        return COLORED_ARMOR_GLINT;
    }

    public static RenderType coloredArmorEntityGlint() {
        return COLORED_ARMOR_ENTITY_GLINT;
    }

    public static RenderType coloredEntityGlint(){
        return COLORED_ENTITY_GLINT;
    }

    public static RenderType coloredEntityGlintDirect() {
        return COLORED_ENTITY_GLINT_DIRECT;
    }

}
