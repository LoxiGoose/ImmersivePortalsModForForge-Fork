package qouteall.imm_ptl.core.mixin.client.render.framebuffer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.ducks.IEFrameBuffer;

import javax.annotation.Nullable;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.GL_DEPTH24_STENCIL8;
import static org.lwjgl.opengl.GL30.GL_DEPTH32F_STENCIL8;
import static org.lwjgl.opengl.GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;

@Mixin(MainTarget.class)
public abstract class MixinMainTarget extends RenderTarget {
    
    public MixinMainTarget(boolean useDepth) {
        super(useDepth);
        throw new RuntimeException();
    }

    @WrapOperation(
        method = "allocateDepthAttachment",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
            remap = false
        )
    )
    private void modifyTexImage2D(int pTarget, int pLevel, int pInternalFormat, int pWidth, int pHeight, int pBorder, int pFormat, int pType, @Nullable IntBuffer pPixels, Operation<Void> original) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
        
        if (isStencilBufferEnabled) {
            pInternalFormat = IPCGlobal.useSeparatedStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8;
            pFormat = ARBFramebufferObject.GL_DEPTH_STENCIL;
            pType = IPCGlobal.useSeparatedStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30C.GL_UNSIGNED_INT_24_8;
            original.call(pTarget,pLevel,pInternalFormat,pWidth,pHeight,pBorder,pFormat,pType,pPixels);
            return;
        }

        original.call(pTarget,pLevel,pInternalFormat,pWidth,pHeight,pBorder,pFormat,pType,pPixels);
    }
    
    @Redirect(
        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;allocateDepthAttachment(Lcom/mojang/blaze3d/pipeline/MainTarget$Dimension;)Z",
        at = @At(
                value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
            remap = false
        )
    )
//    private void onTexImage2D(
//            int target, int level, int internalFormat,
//        int width, int height, int border, int format, int type, IntBuffer pixels
//    ) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
//
//        if (internalFormat == GL_DEPTH_COMPONENT && isStencilBufferEnabled) {
//            GlStateManager._texImage2D(
//                    target,
//                level,
//                IPCGlobal.useAnotherStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8,//
//                width,
//                height,
//                border,
//                ARBFramebufferObject.GL_DEPTH_STENCIL,
//                IPCGlobal.useAnotherStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30.GL_UNSIGNED_INT_24_8,//
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
        method = "createFrameBuffer",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            remap = false
        )
    )
    private void modifyFrameBufferTexture2d(int pTarget, int pAttachment, int pTexTarget, int pTexture, int pLevel, Operation<Void> original) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
        
        if (isStencilBufferEnabled) {
            if ((int) pAttachment == GL30.GL_DEPTH_ATTACHMENT) {
                original.call(pTarget, GL30.GL_DEPTH_STENCIL_ATTACHMENT, pTexTarget, pTexture, pLevel);
                return;
            }
        }

        original.call(pTarget, pAttachment, pTexTarget, pTexture, pLevel);
    }
    
//    @Redirect(
//        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;createFrameBuffer(II)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
//            remap = false
//        )
//    )
//    private void redirectFrameBufferTexture2d(
//        int target, int attachment, int textureTarget, int texture, int level
//    ) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
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
    
}
