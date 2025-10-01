package org.mirage.Phenomenon.network.ScriptSystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent;
import org.mirage.Mirage_gfbs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

public class UploadScriptPacket {
    private final String scriptId;
    private final byte[] fileData;

    public UploadScriptPacket(String scriptId, byte[] fileData) {
        this.scriptId = scriptId;
        this.fileData = fileData;
    }

    public UploadScriptPacket(FriendlyByteBuf buf) {
        this.scriptId = buf.readUtf();
        this.fileData = buf.readByteArray();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(scriptId);
        buf.writeByteArray(fileData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MinecraftServer server = ctx.get().getSender().getServer();
            server.execute(() -> {
                try {
                    Path scriptPath = Mirage_gfbs.SCRIPTS_DIR.resolve(scriptId + ".txt");
                    Files.write(scriptPath, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    ctx.get().getSender().sendSystemMessage(
                            Component.literal("脚本上传成功: " + scriptId)
                    );
                } catch (Exception e) {
                    ctx.get().getSender().sendSystemMessage(
                            Component.literal("脚本保存失败: " + e.getMessage())
                    );
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}