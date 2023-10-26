package qouteall.imm_ptl.core;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import qouteall.imm_ptl.core.collision.CollisionHelper;
import qouteall.imm_ptl.core.commands.ClientDebugCommand;
import qouteall.imm_ptl.core.compat.IPFlywheelCompat;
import qouteall.imm_ptl.core.compat.sodium_compatibility.SodiumInterface;
import qouteall.imm_ptl.core.miscellaneous.DubiousThings;
import qouteall.imm_ptl.core.miscellaneous.GcMonitor;
import qouteall.imm_ptl.core.network.IPNetworkingClient;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.platform_specific.O_O;
import qouteall.imm_ptl.core.portal.PortalRenderInfo;
import qouteall.imm_ptl.core.portal.animation.ClientPortalAnimationManagement;
import qouteall.imm_ptl.core.portal.animation.StableClientTimer;
import qouteall.imm_ptl.core.render.*;
import qouteall.imm_ptl.core.render.context_management.CloudContext;
import qouteall.imm_ptl.core.render.optimization.GLResourceCache;
import qouteall.imm_ptl.core.render.optimization.SharedBlockMeshBuffers;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;
import qouteall.q_misc_util.my_util.MyTaskList;

public class IPModMainClient {

    private static void showNvidiaVideoCardWarning() {
        IPGlobal.clientTaskList.addTask(MyTaskList.withDelayCondition(
                () -> Minecraft.getInstance().level == null,
                MyTaskList.oneShotTask(() -> {
                    if (IPMcHelper.isNvidiaVideocard()) {
                        if (!SodiumInterface.invoker.isSodiumPresent()) {
                            CHelper.printChat(
                                    Component.translatable("imm_ptl.nvidia_warning")
                                            .withStyle(ChatFormatting.RED)
                                            .append(McHelper.getLinkText("https://github.com/CaffeineMC/sodium-fabric/issues/1486"))
                            );
                        }
                    }
                })
        ));
    }

    private static void showQuiltWarning() {
        IPGlobal.clientTaskList.addTask(MyTaskList.withDelayCondition(
                () -> Minecraft.getInstance().level == null,
                MyTaskList.oneShotTask(() -> {
                    if (O_O.isQuilt()) {
                        if (IPConfig.getConfig().shouldDisplayWarning("quilt")) {
                            CHelper.printChat(
                                    Component.translatable("imm_ptl.quilt_warning")
                                            .append(IPMcHelper.getDisableWarningText("quilt"))
                            );
                        }
                    }
                })
        ));
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        IPNetworkingClient.init();

        ClientWorldLoader.init();

        ClientTeleportationManager.init();

        Minecraft.getInstance().execute(() -> {
            ShaderCodeTransformation.init();

            MyRenderHelper.init();

            IPCGlobal.rendererUsingStencil = new RendererUsingStencil();
            IPCGlobal.rendererUsingFrameBuffer = new RendererUsingFrameBuffer();

            IPCGlobal.renderer = IPCGlobal.rendererUsingStencil;
        });

        DubiousThings.init();

        CrossPortalEntityRenderer.init();

        GLResourceCache.init();

        CollisionHelper.initClient();

        PortalRenderInfo.init();

        CloudContext.init();

        SharedBlockMeshBuffers.init();

        GcMonitor.initClient();

        MinecraftForge.EVENT_BUS.register(ClientDebugCommand.class);
        MinecraftForge.EVENT_BUS.register(ClientWorldLoader.class);

        showNvidiaVideoCardWarning();

        showQuiltWarning();

        StableClientTimer.init();

        ClientPortalAnimationManagement.init();

        VisibleSectionDiscovery.init();

        MyBuiltChunkStorage.init();

        IPFlywheelCompat.init();

        GuiPortalRendering._init();
    }
    
}
