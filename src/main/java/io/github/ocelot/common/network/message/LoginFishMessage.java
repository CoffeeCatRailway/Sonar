package io.github.ocelot.common.network.message;

import io.github.ocelot.common.network.IFishMessageHandler;

import java.util.function.IntSupplier;

/**
 * <p>An implementation of {@link FishMessage} intended for login messages.</p>
 *
 * @param <T> The interface that should handle this message
 * @author Ocelot
 * @since 3.2.0
 */
public interface LoginFishMessage<T extends IFishMessageHandler> extends FishMessage<T>, IntSupplier
{
    /**
     * Sets the index for the login message. Should not usually be called.
     *
     * @param index The new login index
     */
    void setLoginIndex(int index);
}
