package io.github.ocelot.sonar.client.framebuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import static org.lwjgl.opengl.GL30.*;

/**
 * <p>An attachment for an {@link AdvancedFbo} that represents a depth render buffer.</p>
 *
 * @author Ocelot
 * @since 2.4.0
 */
@OnlyIn(Dist.CLIENT)
public class AdvancedFboAttachmentDepthRenderBuffer implements AdvancedFboAttachment
{
    private int id;
    private final int width;
    private final int height;
    private final int samples;

    public AdvancedFboAttachmentDepthRenderBuffer(int width, int height, int samples)
    {
        this.id = -1;
        this.width = width;
        this.height = height;
        Validate.inclusiveBetween(1, glGetInteger(GL_MAX_SAMPLES), samples);
        this.samples = samples;
    }

    private void createRaw()
    {
        this.bind();
        if (this.samples == 1)
        {
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, this.width, this.height);
        }
        else
        {
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, this.samples, GL_DEPTH_COMPONENT24, this.width, this.height);
        }
        this.unbind();
    }

    private int getId()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (this.id == -1)
        {
            this.id = glGenRenderbuffers();
        }

        return this.id;
    }

    @Override
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

    @Override
    public void attach(int target, int attachment)
    {
        Validate.isTrue(attachment == 0, "Only one depth buffer attachment is supported.");

        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glFramebufferRenderbuffer(target, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.getId()));
        }
        else
        {
            glFramebufferRenderbuffer(target, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.getId());
        }
    }

    @Override
    public void bind()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glBindRenderbuffer(GL_RENDERBUFFER, this.getId()));
        }
        else
        {
            glBindRenderbuffer(GL_RENDERBUFFER, this.getId());
        }
    }

    @Override
    public void unbind()
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> glBindRenderbuffer(GL_RENDERBUFFER, 0));
        }
        else
        {
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        }
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public int getSamples()
    {
        return samples;
    }

    @Override
    public boolean canSample()
    {
        return false;
    }

    @Override
    public AdvancedFboAttachment createCopy()
    {
        return new AdvancedFboAttachmentDepthRenderBuffer(this.width, this.height, this.samples);
    }

    @Override
    public void free()
    {
        if (this.id == -1)
            return;

        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                glDeleteRenderbuffers(this.id);
                this.id = -1;
            });
        }
        else
        {
            glDeleteRenderbuffers(this.id);
            this.id = -1;
        }
    }
}
