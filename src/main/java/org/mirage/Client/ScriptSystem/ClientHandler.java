package org.mirage.Client.ScriptSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.mirage.Mirage_gfbs;
import org.mirage.Phenomenon.network.ScriptSystem.NetworkHandler;
import org.mirage.Phenomenon.network.ScriptSystem.UploadScriptPacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = Mirage_gfbs.MODID, value = Dist.CLIENT)
public class ClientHandler {

    @OnlyIn(Dist.CLIENT)
    public static void openFileChooser(String scriptId) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ScriptFileChooserScreen(scriptId));
    }

    public static void uploadScript(String scriptId, Path filePath) {
        try {
            byte[] fileData = Files.readAllBytes(filePath);
            NetworkHandler.sendToServer(new UploadScriptPacket(scriptId, fileData));
        } catch (IOException e) {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("读取文件失败: " + e.getMessage())
            );
        }
    }
}