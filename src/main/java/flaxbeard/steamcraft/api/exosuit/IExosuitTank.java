package flaxbeard.steamcraft.api.exosuit;

import net.minecraft.item.ItemStack;

/**
 * The interface which allows items to be considered steam tanks for use in the exosuit.
 */
public interface IExosuitTank {
    /**
     * Whether this tank allows the exosuit to be filled or not.
     * @param stack The armor containing the tank.
     * @return Whether this tank in this armor can charge in a charging device.
     */
    boolean canFill(ItemStack stack);

    /**
     * Gets the maximum storage capacity for the tank.
     * @param stack The armor containing the tank.
     * @return The maximum storage that the tank (self) provides.
     */
    int getStorage(ItemStack stack);
}
