package io.github.ocelot.common.valuecontainer;

import net.minecraft.dispenser.IPosition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <p>Specifies a {@link ValueContainerEntry} as being for a {@link Vec3d} type</p>
 *
 * @author Ocelot
 * @since 2.1.0
 */
@SuppressWarnings("unused")
public class VectorValueContainerEntry extends AbstractVectorValueContainerEntry<Vec3d>
{
    public VectorValueContainerEntry(ITextComponent displayName, String name, Vec3d value)
    {
        this(displayName, name, value, null, null);
    }

    public VectorValueContainerEntry(ITextComponent displayName, String name, Vec3d value, Vec3d minValue, Vec3d maxValue)
    {
        super(displayName, name, value, minValue, maxValue, true);
    }

    @Override
    protected Vec3d create(Number x, Number y, Number z)
    {
        return new Vec3d(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    @Override
    protected Number getX(Vec3d value)
    {
        return value.getX();
    }

    @Override
    protected Number getY(Vec3d value)
    {
        return value.getY();
    }

    @Override
    protected Number getZ(Vec3d value)
    {
        return value.getZ();
    }

    @Override
    public void write(CompoundNBT nbt)
    {
        CompoundNBT valueNbt = new CompoundNBT();
        valueNbt.putDouble("x", this.value.getX());
        valueNbt.putDouble("y", this.value.getY());
        valueNbt.putDouble("z", this.value.getZ());
        nbt.put(this.getName(), valueNbt);
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        if (nbt.contains(this.getName(), Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT valueNbt = nbt.getCompound(this.getName());
            this.value = new Vec3d(valueNbt.getDouble("x"), valueNbt.getDouble("y"), valueNbt.getDouble("z"));
        }
        else
        {
            this.value = new Vec3d(0, 0, 0);
        }
    }

    @Override
    public void parse(Object data)
    {
        if (data instanceof Vec3d)
        {
            this.value = this.clamp((Vec3d) data, this.getMinValue(), this.getMaxValue());
            return;
        }
        if (data instanceof IPosition)
        {
            IPosition position = (IPosition) data;
            this.value = this.clamp(new Vec3d(position.getX(), position.getY(), position.getZ()), this.getMinValue(), this.getMaxValue());
            return;
        }
        String[] tokens = String.valueOf(data).split(",");
        if (tokens.length != 3)
            return;
        double x = StringUtils.isEmpty(tokens[0]) ? 0 : NumberUtils.createNumber(tokens[0]).doubleValue();
        double y = StringUtils.isEmpty(tokens[1]) ? 0 : NumberUtils.createNumber(tokens[1]).doubleValue();
        double z = StringUtils.isEmpty(tokens[2]) ? 0 : NumberUtils.createNumber(tokens[2]).doubleValue();
        this.value = this.clamp(new Vec3d(x, y, z), this.getMinValue(), this.getMaxValue());
    }

    @Override
    public boolean isValid(Object data)
    {
        return data instanceof IPosition || data instanceof String;
    }
}
