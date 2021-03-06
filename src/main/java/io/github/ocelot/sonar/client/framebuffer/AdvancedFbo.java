package io.github.ocelot.sonar.client.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

/**
 * <p>A frame buffer that has more capabilities than the vanilla {@link Framebuffer}.</p>
 *
 * @author Ocelot
 * @since 2.4.0
 */
@OnlyIn(Dist.CLIENT)
public class AdvancedFbo implements NativeResource
{
    private int id;
    private int width;
    private int height;
    private final AdvancedFboAttachment[] colorAttachments;
    private final AdvancedFboAttachment depthAttachment;

    private AdvancedFbo(int width, int height, AdvancedFboAttachment[] colorAttachments, @Nullable AdvancedFboAttachment depthAttachment)
    {
        this.id = -1;
        this.width = width;
        this.height = height;
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
    }

    private void createRaw()
    {
        for (AdvancedFboAttachment attachment : this.colorAttachments)
            attachment.create();
        if (this.depthAttachment != null)
            this.depthAttachment.create();

        this.id = glGenFramebuffers();
        this.bind(false);

        for (int i = 0; i < this.colorAttachments.length; i++)
            this.colorAttachments[i].attach(GL_FRAMEBUFFER, i);
        if (this.depthAttachment != null)
            this.depthAttachment.attach(GL_FRAMEBUFFER, 0);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Advanced FBO status did not return GL_FRAMEBUFFER_COMPLETE. 0x" + Integer.toHexString(status));
        unbind();
    }

    /**
     * Creates the framebuffer and all attachments.
     */
    public void create()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(this::createRaw);
        }
        else
        {
            this.createRaw();
        }
    }

    /**
     * Sets the size of this framebuffer to the specified values.
     *
     * @param width  The new width of the framebuffer
     * @param height The new height of the framebuffer
     */
    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Clears the buffers in this framebuffer.
     */
    public void clear()
    {
        int mask = 0;
        if (this.hasColorAttachment(0))
            mask |= GL_COLOR_BUFFER_BIT;
        if (this.hasDepthAttachment())
            mask |= GL_DEPTH_BUFFER_BIT;
        GlStateManager.clear(mask, Minecraft.IS_RUNNING_ON_MAC);
    }

    /**
     * Binds this framebuffer for read and draw requests.
     *
     * @param setViewport Whether or not to set the viewport to fit the bounds of this framebuffer
     */
    public void bind(boolean setViewport)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                glBindFramebuffer(GL_FRAMEBUFFER, this.id);
                if (setViewport)
                    RenderSystem.viewport(0, 0, this.width, this.height);
            });
        }
        else
        {
            glBindFramebuffer(GL_FRAMEBUFFER, this.id);
            if (setViewport)
                RenderSystem.viewport(0, 0, this.width, this.height);
        }
    }

    /**
     * Unbinds the framebuffer used for read and draw requests.
     */
    public static void unbind()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glBindFramebuffer(GL_FRAMEBUFFER, 0));
        }
        else
        {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    /**
     * Binds this framebuffer for read requests.
     *
     * @param setViewport Whether or not to set the viewport to fit the bounds of this framebuffer
     */
    public void bindRead(boolean setViewport)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                glBindFramebuffer(GL_READ_FRAMEBUFFER, this.id);
                if (setViewport)
                    RenderSystem.viewport(0, 0, this.width, this.height);
            });
        }
        else
        {
            glBindFramebuffer(GL_READ_FRAMEBUFFER, this.id);
            if (setViewport)
                RenderSystem.viewport(0, 0, this.width, this.height);
        }
    }

    /**
     * Unbinds the framebuffer used for read requests.
     */
    public static void unbindRead()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glBindFramebuffer(GL_READ_FRAMEBUFFER, 0));
        }
        else
        {
            glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        }
    }

    /**
     * Binds this framebuffer for draw requests.
     *
     * @param setViewport Whether or not to set the viewport to fit the bounds of this framebuffer
     */
    public void bindDraw(boolean setViewport)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.id);
                if (setViewport)
                    RenderSystem.viewport(0, 0, this.width, this.height);
            });
        }
        else
        {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.id);
            if (setViewport)
                RenderSystem.viewport(0, 0, this.width, this.height);
        }
    }

    /**
     * Unbinds the framebuffer used for draw requests.
     */
    public static void unbindDraw()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0));
        }
        else
        {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        }
    }

    /**
     * Resolves this framebuffer to the framebuffer with the specified id as the target.
     *
     * @param id        The id of the framebuffer to copy into
     * @param width     The width of the framebuffer being copied into
     * @param height    The height of the framebuffer being copied into
     * @param mask      The buffers to copy into the provided framebuffer
     * @param filtering The filter to use if this framebuffer and the provided framebuffer are different sizes
     */
    public void resolveToFbo(int id, int width, int height, int mask, int filtering)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this.bindRead(false);
                glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
                glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, width, height, mask, filtering);
                unbindDraw();
                unbindRead();
            });
        }
        else
        {
            this.bindRead(false);
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
            glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, width, height, mask, filtering);
            unbindDraw();
            unbindRead();
        }
    }

    /**
     * Resolves this framebuffer to the provided advanced framebuffer as the target.
     *
     * @param target The target framebuffer to copy data into
     */
    public void resolveToAdvancedFbo(AdvancedFbo target)
    {
        this.resolveToFbo(target.getId(), target.getWidth(), target.getHeight(), GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    }

    /**
     * Resolves this framebuffer to the provided advanced framebuffer as the target.
     *
     * @param target    The target framebuffer to copy data into
     * @param mask      The buffers to copy into the provided framebuffer
     * @param filtering The filter to use if this framebuffer and the provided framebuffer are different sizes
     */
    public void resolveToAdvancedFbo(AdvancedFbo target, int mask, int filtering)
    {
        this.resolveToFbo(target.getId(), target.getWidth(), target.getHeight(), mask, filtering);
    }

    /**
     * Resolves this framebuffer to the provided minecraft framebuffer as the target.
     *
     * @param target The target framebuffer to copy data into
     */
    public void resolveToFrambuffer(Framebuffer target)
    {
        this.resolveToFbo(target.framebufferObject, target.framebufferWidth, target.framebufferHeight, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    }

    /**
     * Resolves this framebuffer to the provided minecraft framebuffer as the target.
     *
     * @param target    The target framebuffer to copy data into
     * @param mask      The buffers to copy into the provided framebuffer
     * @param filtering The filter to use if this framebuffer and the provided framebuffer are different sizes
     */
    public void resolveToFrambuffer(Framebuffer target, int mask, int filtering)
    {
        this.resolveToFbo(target.framebufferObject, target.framebufferWidth, target.framebufferHeight, mask, filtering);
    }

    /**
     * Resolves this framebuffer to the the window framebuffer as the target.
     */
    public void resolveToScreen()
    {
        this.resolveToScreen(GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    /**
     * Resolves this framebuffer to the the window framebuffer as the target.
     *
     * @param mask      The buffers to copy into the provided framebuffer
     * @param filtering The filter to use if this framebuffer and the provided framebuffer are different sizes
     */
    public void resolveToScreen(int mask, int filtering)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                MainWindow window = Minecraft.getInstance().getMainWindow();
                this.bindRead(false);
                unbindDraw();
                glDrawBuffer(GL_BACK);
                glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, window.getFramebufferWidth(), window.getFramebufferHeight(), mask, filtering);
                unbindRead();
            });
        }
        else
        {
            MainWindow window = Minecraft.getInstance().getMainWindow();
            this.bindRead(false);
            unbindDraw();
            glDrawBuffer(GL_BACK);
            glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, window.getFramebufferWidth(), window.getFramebufferHeight(), mask, filtering);
            unbindRead();
        }
    }

    @Override
    public void free()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                if (this.id != -1)
                {
                    glDeleteFramebuffers(this.id);
                    this.id = -1;
                }
                for (AdvancedFboAttachment attachment : this.colorAttachments)
                    attachment.free();
                if (this.depthAttachment != null)
                    this.depthAttachment.free();
            });
        }
        else
        {
            if (this.id != -1)
            {
                glDeleteFramebuffers(this.id);
                this.id = -1;
            }
            for (AdvancedFboAttachment attachment : this.colorAttachments)
                attachment.free();
            if (this.depthAttachment != null)
                this.depthAttachment.free();
        }
    }

    /**
     * @return The id of this framebuffer or -1 if it has been deleted
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return The width of this framebuffer
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @return The height of this framebuffer
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @return The amount of color attachments in this framebuffer
     */
    public int getColorAttachments()
    {
        return colorAttachments.length;
    }

    /**
     * Checks to see if the provided attachment has been added to this framebuffer.
     *
     * @param attachment The attachment to check
     * @return Whether or not there is a valid attachment in the specified slot
     */
    public boolean hasColorAttachment(int attachment)
    {
        return attachment >= 0 && attachment < this.colorAttachments.length;
    }

    /**
     * @return Whether or not there is a depth attachment added to this framebuffer
     */
    public boolean hasDepthAttachment()
    {
        return this.depthAttachment != null;
    }

    /**
     * Checks the attachments for the specified slot. If the amount of attachments is unknown, use {@link #hasColorAttachment(int)} to verify before calling this.
     *
     * @param attachment The attachment to get
     * @return The attachment in the specified attachment slot
     * @throws IllegalArgumentException If there is no attachment in the specified attachment slot
     */
    public AdvancedFboAttachment getColorAttachment(int attachment)
    {
        Validate.isTrue(this.hasColorAttachment(attachment), "Color attachment " + attachment + " does not exist.");
        return this.colorAttachments[attachment];
    }

    /**
     * Checks to see if the provided attachment has been added to this framebuffer and is a texture attachment.
     *
     * @param attachment The attachment to check
     * @return Whether or not there is a valid attachment in the specified slot
     */
    public boolean isColorTextureAttachment(int attachment)
    {
        return this.hasColorAttachment(attachment) && this.getColorAttachment(attachment) instanceof AdvancedFboTextureAttachment;
    }

    /**
     * Checks the attachments for the specified slot. If the attachment is not known to be an {@link AdvancedFboTextureAttachment}, use {@link #isColorTextureAttachment(int)} before calling this.
     *
     * @param attachment The attachment to get
     * @return The texture attachment in the specified attachment slot
     * @throws IllegalArgumentException If there is no attachment in the specified attachment slot or it is not an {@link AdvancedFboTextureAttachment}
     */
    public AdvancedFboTextureAttachment getColorTextureAttachment(int attachment)
    {
        AdvancedFboAttachment advancedFboAttachment = this.getColorAttachment(attachment);
        Validate.isTrue(this.isColorTextureAttachment(attachment), "Color attachment " + attachment + " must be a texture attachment to modify texture information.");
        return (AdvancedFboTextureAttachment) advancedFboAttachment;
    }

    /**
     * @return The depth attachment of this framebuffer
     * @throws IllegalArgumentException If there is no depth attachment in this framebuffer
     */
    public AdvancedFboAttachment getDepthAttachment()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        Validate.isTrue(this.hasDepthAttachment(), "Depth attachment does not exist.");
        return Objects.requireNonNull(this.depthAttachment);
    }

    /**
     * @return Whether or not a depth attachment has been added to this framebuffer
     */
    public boolean isDepthTextureAttachment()
    {
        return this.hasDepthAttachment() && this.getDepthAttachment() instanceof AdvancedFboTextureAttachment;
    }

    /**
     * Checks this framebuffer for a depth buffer texture attachment. If the attachment is not known to be a {@link AdvancedFboTextureAttachment}, use {@link #isDepthTextureAttachment()} before calling this.
     *
     * @return The texture attachment in the specified attachment slot
     * @throws IllegalArgumentException If there is no depth attachment in this framebuffer or it is not an {@link AdvancedFboTextureAttachment}
     */
    public AdvancedFboTextureAttachment getDepthTextureAttachment()
    {
        AdvancedFboAttachment advancedFboAttachment = this.getDepthAttachment();
        Validate.isTrue(this.isDepthTextureAttachment(), "Depth attachment must be a texture attachment to modify texture information.");
        return (AdvancedFboTextureAttachment) advancedFboAttachment;
    }

    /**
     * @return A {@link Framebuffer} that uses this advanced fbo as the target
     */
    public Wrapper getVanillaWrapper()
    {
        return new Wrapper(this);
    }

    /**
     * <p>A builder used to attach buffers to an {@link AdvancedFbo}.</p>
     *
     * @author Ocelot
     * @see AdvancedFbo
     * @since 2.4.0
     */
    public static class Builder
    {
        private final int width;
        private final int height;
        private final List<AdvancedFboAttachment> colorAttachments;
        private AdvancedFboAttachment depthAttachment;

        public Builder(int width, int height)
        {
            this.width = width;
            this.height = height;
            this.colorAttachments = new ArrayList<>();
            this.depthAttachment = null;
        }

        public Builder(AdvancedFbo parent)
        {
            this(parent.getWidth(), parent.getHeight());
            this.addAttachments(parent);
        }

        public Builder(Framebuffer parent)
        {
            this(parent.framebufferWidth, parent.framebufferHeight);
            this.addAttachments(parent);
        }

        private void validateColorSize()
        {
            Validate.inclusiveBetween(0, glGetInteger(GL_MAX_COLOR_ATTACHMENTS), this.colorAttachments.size());
        }

        /**
         * Adds copies of the buffers inside the specified fbo.
         *
         * @param parent The parent to add the attachments for
         */
        public Builder addAttachments(AdvancedFbo parent)
        {
            for (int i = 0; i < parent.getColorAttachments(); i++)
                this.colorAttachments.add(parent.getColorAttachment(i).createCopy());
            this.validateColorSize();
            if (parent.hasDepthAttachment())
            {
                Validate.isTrue(this.depthAttachment == null, "Only one depth attachment can be applied to an FBO.");
                this.depthAttachment = parent.getDepthAttachment().createCopy();
            }
            return this;
        }

        /**
         * Adds copies of the buffers inside the specified fbo.
         *
         * @param parent The parent to add the attachments for
         */
        public Builder addAttachments(Framebuffer parent)
        {
            this.addColorTextureBuffer(parent.framebufferTextureWidth, parent.framebufferTextureHeight, 0);
            if (parent.useDepth)
            {
                Validate.isTrue(this.depthAttachment == null, "Only one depth attachment can be applied to an FBO.");
                this.setDepthRenderBuffer(parent.framebufferTextureWidth, parent.framebufferTextureHeight, 1);
            }
            return this;
        }

        /**
         * Adds a color texture buffer with the size of the framebuffer and 1 mipmap level.
         */
        public Builder addColorTextureBuffer()
        {
            this.addColorTextureBuffer(this.width, this.height, 0);
            return this;
        }

        /**
         * Adds a color texture buffer with the size of the framebuffer and the specified mipmap levels.
         *
         * @param mipmapLevels The levels of mipmapping to allocate
         */
        public Builder addColorTextureBuffer(int mipmapLevels)
        {
            this.addColorTextureBuffer(this.width, this.height, mipmapLevels);
            return this;
        }

        /**
         * Adds a color texture buffer with the specified size and the specified mipmap levels.
         *
         * @param width        The width of the texture buffer
         * @param height       The height of the texture buffer
         * @param mipmapLevels The levels of mipmapping to allocate
         */
        public Builder addColorTextureBuffer(int width, int height, int mipmapLevels)
        {
            this.colorAttachments.add(new AdvancedFboAttachmentColorTexture2D(width, height, mipmapLevels));
            this.validateColorSize();
            return this;
        }

        /**
         * <p>Adds a color render buffer with the size of the framebuffer and 1 sample.</p>
         * <p><b><i>NOTE: COLOR RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         */
        public Builder addColorRenderBuffer()
        {
            this.addColorRenderBuffer(this.width, this.height, 1);
            return this;
        }

        /**
         * <p>Adds a color render buffer with the size of the framebuffer and the specified samples.</p>
         * <p><b><i>NOTE: COLOR RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         *
         * @param samples The amount of samples to use with this buffer
         */
        public Builder addColorRenderBuffer(int samples)
        {
            this.addColorRenderBuffer(this.width, this.height, samples);
            return this;
        }

        /**
         * <p>Adds a color render buffer with the specified size and the specified samples.</p>
         * <p><b><i>NOTE: COLOR RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         *
         * @param width   The width of the render buffer
         * @param height  The height of the render buffer
         * @param samples The amount of samples to use with this buffer
         */
        public Builder addColorRenderBuffer(int width, int height, int samples)
        {
            this.colorAttachments.add(new AdvancedFboAttachmentColorRenderBuffer(width, height, samples));
            this.validateColorSize();
            return this;
        }

        /**
         * Sets the depth texture buffer to the size of the framebuffer and 1 mipmap level.
         */
        public Builder setDepthTextureBuffer()
        {
            this.setDepthTextureBuffer(this.width, this.height, 0);
            return this;
        }

        /**
         * Sets the depth texture buffer to the size of the framebuffer and the specified mipmap levels.
         *
         * @param mipmapLevels The levels of mipmapping to allocate
         */
        public Builder setDepthTextureBuffer(int mipmapLevels)
        {
            this.setDepthTextureBuffer(this.width, this.height, mipmapLevels);
            return this;
        }

        /**
         * Sets the depth texture buffer to the size of the framebuffer and the specified mipmap levels.
         *
         * @param width        The width of the texture buffer
         * @param height       The height of the texture buffer
         * @param mipmapLevels The levels of mipmapping to allocate
         */
        public Builder setDepthTextureBuffer(int width, int height, int mipmapLevels)
        {
            Validate.isTrue(this.depthAttachment == null, "Only one depth attachment can be applied to an FBO.");
            this.depthAttachment = new AdvancedFboAttachmentDepthTexture2D(width, height, mipmapLevels);
            return this;
        }

        /**
         * <p>Sets the depth texture buffer to the size of the framebuffer and 1 sample.</p>
         * <p><b><i>NOTE: DEPTH RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         */
        public Builder setDepthRenderBuffer()
        {
            this.setDepthRenderBuffer(this.width, this.height, 1);
            return this;
        }

        /**
         * <p>Sets the depth texture buffer to the size of the framebuffer and the specified samples.</p>
         * <p><b><i>NOTE: DEPTH RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         *
         * @param samples The amount of samples to use with this buffer
         */
        public Builder setDepthRenderBuffer(int samples)
        {
            this.setDepthRenderBuffer(this.width, this.height, samples);
            return this;
        }

        /**
         * <p>Sets the depth texture buffer to the specified size and the specified samples.</p>
         * <p><b><i>NOTE: DEPTH RENDER BUFFERS CAN ONLY BE COPIED TO OTHER FRAMEBUFFERS</i></b></p>
         *
         * @param width   The width of the render buffer
         * @param height  The height of the render buffer
         * @param samples The amount of samples to use with this buffer
         */
        public Builder setDepthRenderBuffer(int width, int height, int samples)
        {
            Validate.isTrue(this.depthAttachment == null, "Only one depth attachment can be applied to an FBO.");
            this.depthAttachment = new AdvancedFboAttachmentDepthRenderBuffer(width, height, samples);
            return this;
        }

        /**
         * @return A new {@link AdvancedFbo} with the specified builder properties.
         */
        public AdvancedFbo build()
        {
            if (this.colorAttachments.isEmpty())
                throw new IllegalArgumentException("Framebuffer needs at least one color attachment to be complete.");
            int samples = -1;
            for (AdvancedFboAttachment attachment : this.colorAttachments)
            {
                if (samples == -1)
                {
                    samples = attachment.getSamples();
                    continue;
                }
                if (attachment.getSamples() != samples)
                    throw new IllegalArgumentException("Framebuffer attachments need to have the same number of samples to be complete.");
            }
            if (this.depthAttachment != null && this.depthAttachment.getSamples() != samples)
                throw new IllegalArgumentException("Framebuffer attachments need to have the same number of samples to be complete.");
            return new AdvancedFbo(this.width, this.height, this.colorAttachments.toArray(new AdvancedFboAttachment[0]), this.depthAttachment);
        }
    }

    /**
     * <p>A vanilla {@link Framebuffer} wrapper of the {@link AdvancedFbo}.</p>
     *
     * @author Ocelot
     * @see AdvancedFbo
     * @since 3.0.0
     */
    public static class Wrapper extends Framebuffer
    {
        private final AdvancedFbo fbo;

        private Wrapper(AdvancedFbo fbo)
        {
            super(fbo.width, fbo.height, fbo.hasDepthAttachment(), Minecraft.IS_RUNNING_ON_MAC);
            this.fbo = fbo;
            this.createBuffers(this.fbo.getWidth(), this.fbo.getHeight(), Minecraft.IS_RUNNING_ON_MAC);
        }

        @Override
        public void resize(int width, int height, boolean onMac)
        {
            if (!RenderSystem.isOnRenderThread())
            {
                RenderSystem.recordRenderCall(() -> this.createBuffers(width, height, onMac));
            }
            else
            {
                this.createBuffers(width, height, onMac);
            }
        }

        @Override
        public void deleteFramebuffer()
        {
            this.fbo.close();
        }

        @Override
        public void createBuffers(int width, int height, boolean onMac)
        {
            this.framebufferWidth = width;
            this.framebufferHeight = height;
            if (this.fbo == null) // Assumed to be init phase so no action taken
                return;
            this.fbo.setSize(width, height);
            AdvancedFboAttachment attachment = this.fbo.hasColorAttachment(0) ? this.fbo.getColorAttachment(0) : null;
            this.framebufferTextureWidth = attachment == null ? this.framebufferWidth : attachment.getWidth();
            this.framebufferTextureHeight = attachment == null ? this.framebufferHeight : attachment.getHeight();
        }

        @Override
        public void setFramebufferFilter(int framebufferFilter)
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            this.framebufferFilter = framebufferFilter;
            for (int i = 0; i < this.fbo.getColorAttachments(); i++)
            {
                this.fbo.getColorAttachment(i).bind();
                GlStateManager.texParameter(3553, 10241, framebufferFilter);
                GlStateManager.texParameter(3553, 10240, framebufferFilter);
                GlStateManager.texParameter(3553, 10242, 10496);
                GlStateManager.texParameter(3553, 10243, 10496);
                this.fbo.getColorAttachment(i).unbind();
            }
        }

        @Override
        public void bindFramebufferTexture()
        {
            if (this.fbo.hasColorAttachment(0))
                this.fbo.getColorAttachment(0).bind();
        }

        @Override
        public void unbindFramebufferTexture()
        {
            if (this.fbo.hasColorAttachment(0))
                this.fbo.getColorAttachment(0).unbind();
        }

        @Override
        public void bindFramebuffer(boolean setViewport)
        {
            this.fbo.bind(setViewport);
        }

        /**
         * @return The backing advanced fbo
         */
        public AdvancedFbo getFbo()
        {
            return fbo;
        }
    }
}
