package org.mirage.Client.Notification;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.mirage.Mirage_gfbs;
import org.mirage.Tools.NotifucationGUI.NotificationGUI;

@Mod.EventBusSubscriber(modid = Mirage_gfbs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            NotificationGUI.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        NotificationGUI.render(event.getGuiGraphics(), event.getPartialTick());
    }
}