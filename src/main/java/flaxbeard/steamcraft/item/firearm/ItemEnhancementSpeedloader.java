package flaxbeard.steamcraft.item.firearm;

import flaxbeard.steamcraft.Steamcraft;
import flaxbeard.steamcraft.SteamcraftItems;
import flaxbeard.steamcraft.api.enhancement.IEnhancementFirearm;
import flaxbeard.steamcraft.entity.EntityMusketBall;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemEnhancementSpeedloader extends Item implements IEnhancementFirearm {
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return stack.getItem() == SteamcraftItems.musket || stack.getItem() == SteamcraftItems.blunderbuss;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return Steamcraft.upgrade;
    }

    @Override
    public String getID() {
        return "Speedloader";
    }

    @Override
    public String getIcon(Item item) {
        if (item == SteamcraftItems.musket) {
            return "steamcraft:weaponMusketSpeedloader";
        } else {
            return "steamcraft:weaponBlunderbussSpeedloader";
        }
    }

    @Override
    public String getName(Item item) {
        if (item == SteamcraftItems.musket) {
            return "item.steamcraft:musketSpeedloader";
        } else {
            return "item.steamcraft:blunderbussSpeedloader";
        }
    }

    @Override
    public float getAccuracyChange(Item weapon) {
        return 0;
    }

    @Override
    public float getKnockbackChange(Item weapon) {
        return 0;
    }

    @Override
    public float getDamageChange(Item weapon) {
        return 0;
    }

    @Override
    public int getReloadChange(Item weapon) {
        return -30;
    }

    @Override
    public int getClipSizeChange(Item weapon) {
        return 0;
    }

    @Override
    public EntityMusketBall changeBullet(EntityMusketBall bullet) {
        return bullet;
    }

}
