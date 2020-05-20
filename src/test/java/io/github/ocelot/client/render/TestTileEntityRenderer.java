package io.github.ocelot.client.render;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.client.framebuffer.AdvancedFbo;
import io.github.ocelot.tileentity.TestTileEntity;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TestTileEntityRenderer extends TileEntityRenderer<TestTileEntity>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation("shaders/post/notch.json");

    private static AdvancedFbo fbo;
    private static ShaderGroup rippleShader;
    private static boolean useShader;

    private static void createShader()
    {
        Minecraft minecraft = Minecraft.getInstance();
        try
        {
            rippleShader = new ShaderGroup(minecraft.getTextureManager(), minecraft.getResourceManager(), fbo.toVanillaFramebuffer(), SHADER_LOCATION);
            rippleShader.createBindFramebuffers(minecraft.getMainWindow().getFramebufferWidth(), minecraft.getMainWindow().getFramebufferHeight());
            useShader = true;
        }
        catch (IOException | JsonSyntaxException e)
        {
            LOGGER.warn("Failed to load shader: {}", SHADER_LOCATION, e);
            useShader = false;
        }
    }

    private static void updateFbo()
    {
        MainWindow window = Minecraft.getInstance().getMainWindow();
        if (fbo == null)
            fbo = createFbo(window.getFramebufferWidth(), window.getFramebufferHeight());

        if (rippleShader == null)
            createShader();

        if (fbo.getWidth() != window.getFramebufferWidth() || fbo.getHeight() != window.getFramebufferHeight())
        {
            fbo.close();
            fbo = createFbo(window.getFramebufferWidth(), window.getFramebufferHeight());
            rippleShader.createBindFramebuffers(window.getFramebufferWidth(), window.getFramebufferHeight());
        }
    }

    private static AdvancedFbo createFbo(int width, int height)
    {
        return new AdvancedFbo.Builder(width, height).build();
    }

    public TestTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(TestTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
    {
        if (useShader)
            fbo.bind(false);
        AbstractGui.fill(0, 0, 1, 1, 0xffff00ff);
        if (useShader)
            Minecraft.getInstance().getFramebuffer().bindFramebuffer(false);
        rippleShader.render(partialTicks);
        fbo.resolveToFrambuffer(Minecraft.getInstance().getFramebuffer());
    }
}
