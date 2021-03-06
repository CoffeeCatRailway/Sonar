package io.github.ocelot.sonar.common.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

/**
 * <p>Handles smooth scrolling automatically.</p>
 *
 * @author Ocelot
 * @since 2.2.0
 */
public final class ScrollHandler implements INBTSerializable<CompoundNBT>
{
    public static final float DEFAULT_SCROLL_SPEED = 5;
    public static final float DEFAULT_TRANSITION_SPEED = 0.5f;
    public static final float DEFAULT_MIN_SNAP = 0.1f;

    private final Runnable markDirty;
    private int height;
    private int visibleHeight;

    private float scroll;
    private float lastScroll;
    private float nextScroll;
    private float scrollSpeed;
    private float transitionSpeed;
    private float minSnap;

    public ScrollHandler(@Nullable Runnable markDirty, int height, int visibleHeight)
    {
        this.markDirty = markDirty;
        this.height = height;
        this.visibleHeight = visibleHeight;

        this.scroll = 0;
        this.scrollSpeed = DEFAULT_SCROLL_SPEED;
        this.transitionSpeed = DEFAULT_TRANSITION_SPEED;
        this.minSnap = DEFAULT_MIN_SNAP;
    }

    /**
     * Updates the smooth transition of scrolling.
     */
    public void update()
    {
        this.lastScroll = this.scroll;
        if (this.getMaxScroll() > 0)
        {
            float delta = this.nextScroll - this.scroll;
            if (Math.abs(delta) < this.minSnap)
            {
                this.scroll = this.nextScroll;
            }
            else
            {
                this.scroll += delta * this.transitionSpeed;
            }

            if (this.scroll < 0)
            {
                this.scroll = 0;
                this.nextScroll = 0;
                if (this.markDirty != null)
                    this.markDirty.run();
            }

            if (this.scroll >= this.getMaxScroll())
            {
                this.scroll = this.getMaxScroll();
                this.nextScroll = this.getMaxScroll();
                if (this.markDirty != null)
                    this.markDirty.run();
            }
        }
    }

    /**
     * Handles the mouse scrolling event.
     *
     * @param amount The amount the mouse was scrolled
     */
    public boolean mouseScrolled(double maxScroll, double amount)
    {
        if (this.getMaxScroll() > 0)
        {
            float scrollAmount = (float) Math.min(Math.abs(amount), maxScroll) * this.getScrollSpeed();
            float finalScroll = (amount < 0 ? -1 : 1) * scrollAmount;
            float scroll = MathHelper.clamp(this.getScroll() - finalScroll, 0, this.getMaxScroll());
            if (this.getScroll() != scroll)
            {
                this.scroll(finalScroll);
                return true;
            }
        }
        return false;
    }

    /**
     * Scrolls the specified amount over time.
     *
     * @param scrollAmount The amount to scroll
     */
    public ScrollHandler scroll(float scrollAmount)
    {
        this.nextScroll -= scrollAmount;
        if (this.markDirty != null)
            this.markDirty.run();
        return this;
    }

    /**
     * @return The height of the scrolling area
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @return The height visible at one time
     */
    public int getVisibleHeight()
    {
        return visibleHeight;
    }

    /**
     * @return The position of the scroll bar
     */
    public float getScroll()
    {
        return scroll;
    }

    /**
     * @return The maximum value the scroll can be
     */
    public float getMaxScroll()
    {
        return Math.max(0, this.height - this.visibleHeight);
    }

    /**
     * Calculates the position of the scroll bar based on where is was last tick and now.
     *
     * @param partialTicks The percentage from last tick to this tick
     * @return The position of the scroll bar interpolated over the specified value
     */
    @OnlyIn(Dist.CLIENT)
    public float getInterpolatedScroll(float partialTicks)
    {
        return this.lastScroll + (this.scroll - this.lastScroll) * partialTicks;
    }

    /**
     * @return The speed at which scrolling takes place
     */
    public float getScrollSpeed()
    {
        return scrollSpeed;
    }

    /**
     * @return The scrolling value last tick
     */
    public float getLastScroll()
    {
        return lastScroll;
    }

    /**
     * @return The scroll value being animated to
     */
    public float getNextScroll()
    {
        return nextScroll;
    }

    /**
     * Sets the height of the scrolling area.
     *
     * @param height The total height of the scroll area
     */
    public ScrollHandler setHeight(int height)
    {
        this.height = height;
        this.setScroll(this.scroll);
        return this;
    }

    /**
     * Sets the height visible at one time.
     *
     * @param visibleHeight The maximum height that can be displayed at one moment
     */
    public ScrollHandler setVisibleHeight(int visibleHeight)
    {
        this.visibleHeight = visibleHeight;
        this.setScroll(this.scroll);
        return this;
    }

    /**
     * Sets the position of the scroll bar.
     *
     * @param scroll The new scroll value
     */
    public ScrollHandler setScroll(float scroll)
    {
        this.scroll = MathHelper.clamp(scroll, 0, this.height - this.visibleHeight);
        this.nextScroll = this.scroll;
        this.lastScroll = this.scroll;
        if (this.markDirty != null)
            this.markDirty.run();
        return this;
    }

    /**
     * Sets the speed at which transitions happen.
     *
     * @param transitionSpeed The new speed of transitions
     */
    public ScrollHandler setTransitionSpeed(float transitionSpeed)
    {
        this.transitionSpeed = transitionSpeed;
        if (this.markDirty != null)
            this.markDirty.run();
        return this;
    }

    /**
     * Sets the maximum amount scroll velocity needs to be to snap down to zero.
     *
     * @param minSnap The new snapping value
     */
    public ScrollHandler setMinSnap(float minSnap)
    {
        this.minSnap = minSnap;
        if (this.markDirty != null)
            this.markDirty.run();
        return this;
    }

    /**
     * Sets the speed at which scrolling occurs.
     *
     * @param scrollSpeed The new scrolling speed
     */
    public ScrollHandler setScrollSpeed(float scrollSpeed)
    {
        this.scrollSpeed = Math.max(scrollSpeed, 0);
        if (this.markDirty != null)
            this.markDirty.run();
        return this;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("scroll", this.scroll);
        nbt.putFloat("nextScroll", this.nextScroll);
        nbt.putFloat("scrollSpeed", this.scrollSpeed);
        nbt.putFloat("transitionSpeed", this.transitionSpeed);
        nbt.putFloat("minSnap", this.minSnap);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        this.scroll = nbt.getFloat("scroll");
        this.nextScroll = nbt.getFloat("nextScroll");
        this.scrollSpeed = nbt.getFloat("scrollSpeed");
        this.transitionSpeed = nbt.getFloat("transitionSpeed");
        this.minSnap = nbt.getFloat("minSnap");
    }
}
