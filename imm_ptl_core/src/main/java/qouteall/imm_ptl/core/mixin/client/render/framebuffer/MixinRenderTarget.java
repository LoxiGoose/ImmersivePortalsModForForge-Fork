package qouteall.imm_ptl.core.mixin.client.render.framebuffer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.ducks.IEFrameBuffer;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH24_STENCIL8;
import static org.lwjgl.opengl.GL30.GL_DEPTH32F_STENCIL8;
import static org.lwjgl.opengl.GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;

@Mixin(RenderTarget.class)
public abstract class MixinRenderTarget implements IEFrameBuffer {
    
    private boolean isStencilBufferEnabled;
    
    @Shadow
    public int width;
    @Shadow
    public int height;
    
    
    @Shadow
    public abstract void resize(int width, int height, boolean clearError);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(
        boolean useDepth,
        CallbackInfo ci
    ) {
        isStencilBufferEnabled = false;
    }

    @WrapOperation(
        method = "createBuffers",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
            remap = false
        )
    )
    private void modifyTexImage2D(int pTarget, int pLevel, int pInternalFormat, int pWidth, int pHeight, int pBorder, int pFormat, int pType, @Nullable IntBuffer pPixels, Operation<Void> original) {
        if (Objects.equals(pHeight, GL_DEPTH_COMPONENT)) {
            if (isStencilBufferEnabled) {
                pLevel = IPCGlobal.useSeparatedStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8;
                pBorder = ARBFramebufferObject.GL_DEPTH_STENCIL;
                pFormat = IPCGlobal.useSeparatedStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30.GL_UNSIGNED_INT_24_8;
                original.call(pTarget, pLevel, pInternalFormat, pWidth, pHeight, pBorder, pFormat, pType, pPixels);
                return;
            }
        }

        original.call(pTarget, pLevel, pInternalFormat, pWidth, pHeight, pBorder, pFormat, pType, pPixels);
    }

//    @Redirect(
//        method = "Lcom/mojang/blaze3d/pipeline/RenderTarget;createBuffers(IIZ)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
//            remap = false
//        )
//    )
//    private void redirectTexImage2d(
//        int target, int level, int internalFormat,
//        int width, int height,
//        int border, int format, int type,
//        IntBuffer pixels
//    ) {
//        if (internalFormat == GL_DEPTH_COMPONENT && isStencilBufferEnabled) {
//            GlStateManager._texImage2D(
//                target,
//                level,
//                IPCGlobal.useAnotherStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8,
//                width,
//                height,
//                border,
//                ARBFramebufferObject.GL_DEPTH_STENCIL,
//                IPCGlobal.useAnotherStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30.GL_UNSIGNED_INT_24_8,
//                pixels
//            );
//        }
//        else {
//            GlStateManager._texImage2D(
//                target, level, internalFormat, width, height,
//                border, format, type, pixels
//            );
//        }
//    }

    @WrapOperation(
        method = "createBuffers",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            remap = false
        )
    )
    private void modifyFrameBufferTexture2D(int pTarget, int pAttachment, int pTexTarget, int pWhatever, int pLevel, Operation<Void> original) {
        if (Objects.equals(pAttachment, GL30C.GL_DEPTH_ATTACHMENT)) {
            if (isStencilBufferEnabled) {
                original.call(pTarget, GL30C.GL_DEPTH_ATTACHMENT, pTexTarget, pWhatever, pLevel);
                return;
            }
        }

        original.call(pTarget, pAttachment, pTexTarget, pWhatever, pLevel);
    }

//    @Redirect(
//        method = "Lcom/mojang/blaze3d/pipeline/RenderTarget;createBuffers(IIZ)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
//            remap = false
//        )
//    )
//    private void redirectFrameBufferTexture2d(
//        int target, int attachment, int textureTarget, int texture, int level
//    ) {
//
//        if (attachment == GL30C.GL_DEPTH_ATTACHMENT && isStencilBufferEnabled) {
//            GlStateManager._glFramebufferTexture2D(
//                target, GL30.GL_DEPTH_STENCIL_ATTACHMENT, textureTarget, texture, level
//            );
//        }
//        else {
//            GlStateManager._glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
//        }
//    }
    
    @Inject(
        method = "Lcom/mojang/blaze3d/pipeline/RenderTarget;copyDepthFrom(Lcom/mojang/blaze3d/pipeline/RenderTarget;)V",
        at = @At("RETURN")
    )
    private void onCopiedDepthFrom(RenderTarget framebuffer, CallbackInfo ci) {
        CHelper.checkGlError();
    }
    
    @Override
    public boolean getIsStencilBufferEnabled() {
        return this.isStencilBufferEnabled;
    }
    
    @Override
    public void setIsStencilBufferEnabledAndReload(boolean cond) {
        if (isStencilBufferEnabled != cond) {
            isStencilBufferEnabled = cond;
            resize(width, height, Minecraft.ON_OSX);
        }
    }
}
