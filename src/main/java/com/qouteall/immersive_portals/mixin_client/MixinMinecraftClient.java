package com.qouteall.immersive_portals.mixin_client;

import com.qouteall.immersive_portals.CGlobal;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.ducks.IEMinecraftClient;
import com.qouteall.immersive_portals.network.ClientNetworkingTaskList;
import com.qouteall.immersive_portals.render.CrossPortalEntityRenderer;
import com.qouteall.immersive_portals.render.FPSMonitor;
import com.qouteall.immersive_portals.render.context_management.PortalRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IEMinecraftClient {
    @Final
    @Shadow
    @Mutable
    private Framebuffer framebuffer;
    
    @Shadow
    public Screen currentScreen;
    
    @Mutable
    @Shadow
    @Final
    public WorldRenderer worldRenderer;
    
    @Shadow
    private static int currentFps;
    
    @Shadow
    public abstract Profiler getProfiler();
    
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;tick(Ljava/util/function/BooleanSupplier;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onAfterClientTick(CallbackInfo ci) {
        ModMain.postClientTickSignal.emit();
        
        CGlobal.clientTeleportationManager.manageTeleportation(0);
    }
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/snooper/Snooper;update()V"
        )
    )
    private void onSnooperUpdate(boolean tick, CallbackInfo ci) {
        FPSMonitor.updateEverySecond(currentFps);
    }
    
    @Inject(
        method = "Lnet/minecraft/client/MinecraftClient;setWorld(Lnet/minecraft/client/world/ClientWorld;)V",
        at = @At("HEAD")
    )
    private void onSetWorld(ClientWorld clientWorld_1, CallbackInfo ci) {
        ClientNetworkingTaskList.flush();
        CGlobal.clientWorldLoader.cleanUp();
        CGlobal.clientTeleportationManager.disableTeleportFor(40);
        CrossPortalEntityRenderer.cleanUp();
    }
    
    //avoid messing up rendering states in fabulous
    @Inject(method = "isFabulousGraphicsOrBetter", at = @At("HEAD"), cancellable = true)
    private static void onIsFabulousGraphicsOrBetter(CallbackInfoReturnable<Boolean> cir) {
        if (PortalRendering.isRendering()) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;runTasks()V",
            shift = At.Shift.AFTER
        )
    )
    private void onRunTasks(boolean tick, CallbackInfo ci) {
        getProfiler().push("portal_networking");
        ClientNetworkingTaskList.processClientNetworkingTasks();
        getProfiler().pop();
    }
    
    @Override
    public void setFrameBuffer(Framebuffer buffer) {
        framebuffer = buffer;
    }
    
    @Override
    public Screen getCurrentScreen() {
        return currentScreen;
    }
    
    @Override
    public void setWorldRenderer(WorldRenderer r) {
        worldRenderer = r;
    }
}
