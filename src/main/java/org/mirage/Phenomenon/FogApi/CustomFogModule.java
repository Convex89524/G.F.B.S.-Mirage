/**
 * G.F.B.S. Mirage (mirage_gfbs) - A Minecraft Mod
 * Copyright (C) 2025-2029 Convex89524

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mirage.Phenomenon.FogApi;

import net.minecraft.client.Minecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.mirage.Phenomenon.network.Network.ClientEventHandler;

public class CustomFogModule {

    private float fogRed = 0.5f;
    private float fogGreen = 0.5f;
    private float fogBlue = 0.5f;
    private float fogStart = 0.0f;
    private float fogEnd = 1.0f;
    private boolean active = false;

    public CustomFogModule() {
        ClientEventHandler.registerEvent("fog_settings", this::handleFogSettingsUpdate);
    }

    /**
     * 处理从服务端接收的雾效果设置更新
     */
    private void handleFogSettingsUpdate(CompoundTag data) {
        this.setFogColor(
                data.getFloat("red"),
                data.getFloat("green"),
                data.getFloat("blue")
        );
        this.setFogStart(data.getFloat("start"));
        this.setFogEnd(data.getFloat("end"));

        if (data.getBoolean("active")) {
            this.register();
        } else {
            this.unregister();
        }
    }

    /**
     * 注册雾效果事件监听器
     */
    public void register() {
        if (!active) {
            MinecraftForge.EVENT_BUS.register(this);
            active = true;
        }
    }

    /**
     * 注销雾效果事件监听器
     */
    public void unregister() {
        if (active) {
            MinecraftForge.EVENT_BUS.unregister(this);
            active = false;
        }
    }

    public void setFogColor(float r, float g, float b) {
        fogRed = clamp(r);
        fogGreen = clamp(g);
        fogBlue = clamp(b);
    }

    public void setFogStart(float start) {
        fogStart = Math.max(0, start);
    }

    public void setFogEnd(float end) {
        fogEnd = Math.max(fogStart + 0.1f, end);
    }

    private float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    @SubscribeEvent
    public void onFogColors(ViewportEvent.ComputeFogColor event) {
        event.setRed(fogRed);
        event.setGreen(fogGreen);
        event.setBlue(fogBlue);
    }

    @SubscribeEvent
    public void onFogRender(ViewportEvent.RenderFog event) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) return;

        FogType fogType = Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera();
        if (fogType == FogType.NONE) {
            event.setNearPlaneDistance(fogStart);
            event.setFarPlaneDistance(fogEnd);
            event.setCanceled(true);
        }
    }

    public boolean isActive() {
        return active;
    }

    public float getFogRed() {
        return fogRed;
    }

    public float getFogGreen() {
        return fogGreen;
    }

    public float getFogBlue() {
        return fogBlue;
    }

    public float getFogStart() {
        return fogStart;
    }

    public float getFogEnd() {
        return fogEnd;
    }
}