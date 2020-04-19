package io.github.ocelot.common.valuecontainer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <p>A {@link NumberValueContainerEntry} that supports Minecraft {@link ResourceLocation}.</p>
 *
 * @author Ocelot
 * @since 2.1.0
 */
public class ResourceLocationValueContainerEntry implements ValueContainerEntry<ResourceLocation>, TextFieldEntry
{
    private final ITextComponent displayName;
    private final String name;
    private final ResourceLocation previousValue;
    private ResourceLocation value;
    private Predicate<String> validator;

    public ResourceLocationValueContainerEntry(ITextComponent displayName, String name, ResourceLocation value)
    {
        this.displayName = displayName;
        this.name = name;
        this.previousValue = value;
        this.value = value;
        this.validator = null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public InputType getInputType()
    {
        return InputType.TEXT_FIELD;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValue()
    {
        return (E) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getPreviousValue()
    {
        return (E) previousValue;
    }

    @Override
    public boolean isDirty()
    {
        return !this.value.equals(this.previousValue);
    }

    @Override
    public String getDisplay()
    {
        return this.value.toString();
    }

    @Override
    public void write(CompoundNBT nbt)
    {
        nbt.putString(this.getName(), this.value.toString());
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        this.value = nbt.contains(this.getName(), Constants.NBT.TAG_STRING) ? new ResourceLocation(nbt.getString(this.getName())) : this.previousValue;
    }

    @Override
    public void parse(Object data)
    {
        this.value = data instanceof ResourceLocation ? (ResourceLocation) data : new ResourceLocation(String.valueOf(data));
    }

    @Override
    public boolean isValid(Object data)
    {
        return data instanceof ResourceLocation || data instanceof String;
    }

    @Override
    public Optional<Predicate<String>> getValidator()
    {
        return Optional.ofNullable(this.validator);
    }

    /**
     * Sets the validator to the specified value.
     *
     * @param validator The new validator value or null for no validator
     */
    public void setValidator(@Nullable Predicate<String> validator)
    {
        this.validator = validator;
    }

    /**
     * Generates the default validator for the specified {@link ResourceLocationValueContainerEntry}.
     *
     * @param entry The entry to create the validator for
     * @return A new predicate that will be used for text area parsing
     */
    public static Predicate<String> createDefaultValidator(ResourceLocationValueContainerEntry entry)
    {
        return s ->
        {
            if (!ResourceLocation.isResouceNameValid(s))
                return false;
            try
            {
                entry.parse(s);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        };
    }
}