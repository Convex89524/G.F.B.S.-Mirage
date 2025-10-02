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

package org.mirage.Client.ScriptSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.COM.COMUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ScriptFileChooserScreen extends Screen {
    private final String scriptId;

    // 定义必要的GUID
    private static final Guid.GUID CLSID_FileOpenDialog = new Guid.GUID("DC1C5A9C-E88A-4DDE-A7A1-45D21AE94E69");
    private static final Guid.GUID IID_IFileOpenDialog = new Guid.GUID("D57C7288-D4AD-4768-BE02-9D969532D960");
    private static final Guid.GUID IID_IShellItem = new Guid.GUID("43826D1E-E718-42EE-BC55-A1E261C37BFE");

    // 文件对话框选项常量
    private static final int FOS_FORCEFILESYSTEM = 0x40;
    private static final int FOS_PATHMUSTEXIST = 0x800;
    private static final int FOS_FILEMUSTEXIST = 0x1000;
    private static final int FOS_FORCESHOWHIDDEN = 0x10000000;

    // SIGDN 常量
    private static final int SIGDN_FILESYSPATH = 0x80058000;

    // CLSCTX 常量
    private static final int CLSCTX_INPROC_SERVER = 0x1;

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

        // 使用Win32 API打开文件选择器
        Util.ioPool().execute(() -> {
            try {
                // 检查是否是Windows系统
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    // 使用Win32 API打开原生文件选择器
                    Path selectedFile = showNativeFileDialog();

                    if (selectedFile != null) {
                        ClientHandler.uploadScript(scriptId, selectedFile);
                    }
                } else {
                    // 非Windows系统使用备选方案
                    fallbackFileSelection();
                }

                Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(null));
            } catch (Exception e) {
                org.mirage.Mirage_gfbs.LOGGER.error("打开文件选择器时出错", e);

                Minecraft.getInstance().tell(() -> {
                    Minecraft.getInstance().setScreen(null);
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.sendSystemMessage(
                                Component.literal("打开文件选择器时出错: " + e.getMessage())
                        );
                    }
                });
            }
        });
    }

    /**
     * 使用Win32 API显示原生文件选择对话框
     */
    private Path showNativeFileDialog() {
        // 初始化COM
        WinNT.HRESULT hr = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED);
        if (hr.intValue() != 0 && hr.intValue() != 1) { // S_FALSE = 1
            throw new RuntimeException("COM初始化失败: " + hr.intValue());
        }

        PointerByReference ppFileDialog = new PointerByReference();
        try {
            // 创建文件打开对话框
            hr = Ole32.INSTANCE.CoCreateInstance(
                    CLSID_FileOpenDialog,
                    null,
                    CLSCTX_INPROC_SERVER,
                    IID_IFileOpenDialog,
                    ppFileDialog
            );

            if (hr.intValue() != 0) {
                throw new RuntimeException("创建文件对话框失败: " + hr.intValue());
            }

            // 使用JNA平台库中预定义的IFileOpenDialog接口
            IFileOpenDialog fileDialog = (IFileOpenDialog) Native.load("shell32", IFileOpenDialog.class);

            // 设置对话框选项
            WinDef.DWORDByReference pdwOptions = new WinDef.DWORDByReference();
            fileDialog.GetOptions(pdwOptions);

            WinDef.DWORD newOptions = new WinDef.DWORD(
                    pdwOptions.getValue().intValue() |
                            FOS_FORCEFILESYSTEM |
                            FOS_PATHMUSTEXIST |
                            FOS_FILEMUSTEXIST |
                            FOS_FORCESHOWHIDDEN
            );

            fileDialog.SetOptions(newOptions);

            // 设置文件类型过滤器
            COMDLG_FILTERSPEC filterSpec = new COMDLG_FILTERSPEC();
            filterSpec.pszName = new WString("文本文件 (*.txt)");
            filterSpec.pszSpec = new WString("*.txt");

            fileDialog.SetFileTypes(1, filterSpec.getPointer());

            // 显示对话框
            hr = fileDialog.Show(null);
            if (hr.intValue() != 0) {
                // 用户取消或出错
                return null;
            }

            // 获取结果
            PointerByReference ppItem = new PointerByReference();
            hr = fileDialog.GetResult(ppItem);
            if (hr.intValue() != 0) {
                throw new RuntimeException("获取文件结果失败: " + hr.intValue());
            }

            // 使用JNA平台库中预定义的IShellItem接口
            IShellItem shellItem = (IShellItem) Native.load("shell32", IShellItem.class);

            // 获取文件路径
            PointerByReference ppszFilePath = new PointerByReference();
            hr = shellItem.GetDisplayName(SIGDN_FILESYSPATH, ppszFilePath);
            if (hr.intValue() != 0) {
                throw new RuntimeException("获取文件路径失败: " + hr.intValue());
            }

            // 读取文件路径字符串
            String filePath = null;
            Pointer pathPtr = ppszFilePath.getValue();
            if (pathPtr != null) {
                filePath = pathPtr.getWideString(0);
                // 释放COM分配的内存
                Ole32.INSTANCE.CoTaskMemFree(pathPtr);
            }

            if (filePath != null) {
                return new File(filePath).toPath();
            }
        } catch (Exception e) {
            throw new RuntimeException("文件对话框操作失败", e);
        } finally {
            // 释放COM对象
            if (ppFileDialog.getValue() != null) {
                IUnknown unknown = (IUnknown) Native.load("ole32", IUnknown.class);
                unknown.Release();
            }
            // 清理COM
            Ole32.INSTANCE.CoUninitialize();
        }

        return null;
    }

    /**
     * 非Windows系统的备选方案
     */
    private void fallbackFileSelection() {
        try {
            // 尝试使用AWT作为备选方案
            java.awt.FileDialog fileDialog = new java.awt.FileDialog((java.awt.Frame)null, "选择脚本文件", java.awt.FileDialog.LOAD);
            fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".txt"));
            fileDialog.setVisible(true);

            String selectedFile = fileDialog.getFile();
            String selectedDirectory = fileDialog.getDirectory();

            if (selectedFile != null) {
                File file = new File(selectedDirectory, selectedFile);
                ClientHandler.uploadScript(scriptId, file.toPath());
            }
        } catch (Exception e) {
            // 如果AWT也失败，使用PowerShell作为最后的手段
            usePowerShellFallback();
        }
    }

    /**
     * 使用PowerShell作为最后的备选方案
     */
    private void usePowerShellFallback() {
        try {
            // 构建PowerShell命令
            String command = "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                    "$dialog = New-Object System.Windows.Forms.OpenFileDialog; " +
                    "$dialog.Title = '选择脚本文件'; " +
                    "$dialog.Filter = '文本文件 (*.txt)|*.txt'; " +
                    "if ($dialog.ShowDialog() -eq 'OK') { Write-Output $dialog.FileName }\"";

            // 执行命令
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            if (output.length() > 0) {
                File file = new File(output.toString());
                ClientHandler.uploadScript(scriptId, file.toPath());
            }
        } catch (Exception e) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.literal("所有文件选择方法都失败了: " + e.getMessage())
                );
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    // 定义COM接口
    public interface IUnknown extends StdCallLibrary {
        WinNT.HRESULT QueryInterface(Guid.GUID.ByValue riid, PointerByReference ppvObject);
        int AddRef();
        int Release();
    }

    public interface IFileOpenDialog extends IUnknown {
        WinNT.HRESULT Show(WinDef.HWND hwndOwner);
        WinNT.HRESULT SetFileTypes(int cFileTypes, Pointer pFilterSpec);
        WinNT.HRESULT GetResult(PointerByReference ppsi);
        WinNT.HRESULT SetOptions(WinDef.DWORD fos);
        WinNT.HRESULT GetOptions(WinDef.DWORDByReference pfos);
    }

    public interface IShellItem extends IUnknown {
        WinNT.HRESULT GetDisplayName(int sigdnName, PointerByReference ppszName);
    }

    // COMDLG_FILTERSPEC 结构
    public static class COMDLG_FILTERSPEC extends Structure {
        public WString pszName;
        public WString pszSpec;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pszName", "pszSpec");
        }
    }
}