package org.mirage.Client.ScriptSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics; // 添加导入
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.io.File;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ScriptFileChooserScreen extends Screen {
    private final String scriptId;

    public ScriptFileChooserScreen(String scriptId) {
        super(Component.literal("选择脚本文件"));
        this.scriptId = scriptId;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("取消"), button -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 100, this.height / 2, 200, 20).build());

        Util.ioPool().execute(() -> {
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("选择脚本文件");
                    fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));

                    int result = fileChooser.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        ClientHandler.uploadScript(scriptId, selectedFile.toPath());
                    }

                    Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(null));
                });
            } catch (Exception e) {
                Minecraft.getInstance().tell(() -> {
                    Minecraft.getInstance().setScreen(null);
                    Minecraft.getInstance().player.sendSystemMessage(
                            Component.literal("打开文件选择器时出错: " + e.getMessage())
                    );
                });
            }
        });
    }

    // 修改渲染方法
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}