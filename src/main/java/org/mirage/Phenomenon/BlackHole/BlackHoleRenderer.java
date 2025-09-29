package org.mirage.Phenomenon.BlackHole;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.mirage.Mirage_gfbs;

import java.lang.reflect.Method;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlackHoleRenderer {
    private static final ResourceLocation EVENT_HORIZON_TEXTURE = new ResourceLocation(Mirage_gfbs.MODID, "textures/entity/black_hole.png");

    // 最大支持的黑洞数量，与着色器中的数组大小保持一致
    private static final int MAX_BLACK_HOLES = 8;

    @SubscribeEvent
    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        List<BlackHole> blackHoles = BlackHoleManager.getBlackHoles();
        if (blackHoles.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getMainRenderTarget().bindWrite(false);

        applyLensingAndEventHorizonEffect(event, blackHoles);

        // 恢复状态
        minecraft.getMainRenderTarget().unbindWrite();
    }

    private static void applyLensingAndEventHorizonEffect(RenderLevelStageEvent event, List<BlackHole> blackHoles) {
        if (blackHoles.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();

        // 获取Post-processing着色器效果
        PostChain postChain = minecraft.gameRenderer.currentEffect();
        if (postChain == null) {
            Mirage_gfbs.LOGGER.warn("Post-processing chain is not available");
            return;
        }
        
        // 查找我们的lensing效果
        ShaderInstance shaderInstance = null;
        try {
            // 通过反射获取postChain中的效果
            java.lang.reflect.Field passesField = PostChain.class.getDeclaredField("passes");
            passesField.setAccessible(true);
            java.util.List<net.minecraft.client.renderer.EffectInstance> passes = 
                (java.util.List<net.minecraft.client.renderer.EffectInstance>) passesField.get(postChain);
            
            for (net.minecraft.client.renderer.EffectInstance pass : passes) {
                if (pass.getName().equals("lensing")) {
                    shaderInstance = pass.getEffect();
                    break;
                }
            }
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Failed to access post-processing effects: {}", e.getMessage());
        }
        
        if (shaderInstance == null) {
            Mirage_gfbs.LOGGER.error("Lensing shader not found in post-processing chain");
            return;
        }
        // 已经在上面检查过了，这里不再重复检查

        // 保存当前渲染状态
        minecraft.getMainRenderTarget().bindWrite(false);

        try {
            // 设置着色器参数
            setShaderUniforms(shaderInstance, minecraft, event, blackHoles);

            // 应用着色器
            shaderInstance.apply();

            // 渲染全屏四边形
            renderFullscreenQuad();

        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Failed to apply lensing effect: {}", e.getMessage());
        }
    }

    private static void setShaderUniforms(ShaderInstance shaderInstance, Minecraft minecraft,
                                          RenderLevelStageEvent event, List<BlackHole> blackHoles) {
        // 设置投影矩阵
        Uniform projMatUniform = shaderInstance.getUniform("ProjMat");
        if (projMatUniform != null) {
            projMatUniform.set(minecraft.gameRenderer.getProjectionMatrix(event.getPartialTick()));
        }

        // 设置视图矩阵
        Uniform viewMatUniform = shaderInstance.getUniform("ViewMat");
        if (viewMatUniform != null) {
            Matrix4f viewMat = event.getPoseStack().last().pose();
            viewMatUniform.set(viewMat);
        }

        // 设置逆投影矩阵
        Uniform invProjMatUniform = shaderInstance.getUniform("InvProjMat");
        if (invProjMatUniform != null) {
            Matrix4f projMat = minecraft.gameRenderer.getProjectionMatrix(event.getPartialTick());
            Matrix4f invProjMat = new Matrix4f(projMat);
            invProjMat.invert();
            invProjMatUniform.set(invProjMat);
        }

        // 设置逆视图矩阵
        Uniform invViewMatUniform = shaderInstance.getUniform("InvViewMat");
        if (invViewMatUniform != null) {
            Matrix4f viewMat = event.getPoseStack().last().pose();
            Matrix4f invViewMat = new Matrix4f(viewMat);
            invViewMat.invert();
            invViewMatUniform.set(invViewMat);
        }

        // 设置相机位置
        Uniform cameraPosUniform = shaderInstance.getUniform("CameraPos");
        if (cameraPosUniform != null) {
            Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            cameraPosUniform.set((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
        }

        // 设置屏幕尺寸
        Uniform screenSizeUniform = shaderInstance.getUniform("ScreenSize");
        if (screenSizeUniform != null) {
            screenSizeUniform.set(
                    (float) minecraft.getWindow().getWidth(),
                    (float) minecraft.getWindow().getHeight()
            );
        }

        // 设置颜色采样器
        Uniform diffuseSamplerUniform = shaderInstance.getUniform("DiffuseSampler");
        if (diffuseSamplerUniform != null) {
            // 绑定颜色纹理到纹理单元0
            GlStateManager._activeTexture(GL13.GL_TEXTURE0);
            GlStateManager._bindTexture(minecraft.getMainRenderTarget().getColorTextureId());
            diffuseSamplerUniform.set(0);
        }

        // 设置深度采样器
        Uniform depthSamplerUniform = shaderInstance.getUniform("DepthSampler");
        if (depthSamplerUniform != null) {
            // 绑定深度纹理到纹理单元1
            GlStateManager._activeTexture(GL13.GL_TEXTURE1);
            GlStateManager._bindTexture(minecraft.getMainRenderTarget().getDepthTextureId());
            depthSamplerUniform.set(1);

            // 恢复活动纹理单元到0
            GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        }

        // 设置事件视界纹理
        Uniform eventHorizonTextureUniform = shaderInstance.getUniform("EventHorizonTexture");
        if (eventHorizonTextureUniform != null) {
            // 绑定事件视界纹理到纹理单元2
            GlStateManager._activeTexture(GL13.GL_TEXTURE2);
            Minecraft.getInstance().getTextureManager().bindForSetup(EVENT_HORIZON_TEXTURE);
            eventHorizonTextureUniform.set(2);
            GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        }

        // 为每个黑洞设置参数
        int blackHoleCount = Math.min(blackHoles.size(), MAX_BLACK_HOLES);
        for (int i = 0; i < blackHoleCount; i++) {
            BlackHole blackHole = blackHoles.get(i);

            Uniform blackHolePosUniform = shaderInstance.getUniform("BlackHolePos[" + i + "]");
            if (blackHolePosUniform != null) {
                Vec3 pos = blackHole.getPosition();
                blackHolePosUniform.set((float) pos.x, (float) pos.y, (float) pos.z);
            }

            Uniform lensingFactorUniform = shaderInstance.getUniform("LensingFactor[" + i + "]");
            if (lensingFactorUniform != null) {
                lensingFactorUniform.set((float) blackHole.getLensingFactor());
            }

            Uniform eventHorizonUniform = shaderInstance.getUniform("EventHorizon[" + i + "]");
            if (eventHorizonUniform != null) {
                eventHorizonUniform.set((float) blackHole.getRenderRadius(event.getPartialTick()));
            }

            // 添加透明度参数
            Uniform alphaUniform = shaderInstance.getUniform("Alpha[" + i + "]");
            if (alphaUniform != null) {
                Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                double distance = camera.getPosition().distanceTo(blackHole.getPosition());
                float alpha = (float) Mth.clamp(1.0 - distance / 100.0, 0.1, 0.9);
                alphaUniform.set(alpha);
            }
        }

        // 设置黑洞数量
        Uniform blackHoleCountUniform = shaderInstance.getUniform("BlackHoleCount");
        if (blackHoleCountUniform != null) {
            blackHoleCountUniform.set(blackHoleCount);
        }
    }

    // 添加全屏四边形渲染方法
    private static void renderFullscreenQuad() {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.vertex(-1.0, -1.0, 0.0).uv(0.0F, 0.0F).endVertex();
        bufferBuilder.vertex(1.0, -1.0, 0.0).uv(1.0F, 0.0F).endVertex();
        bufferBuilder.vertex(1.0, 1.0, 0.0).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex(-1.0, 1.0, 0.0).uv(0.0F, 1.0F).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}