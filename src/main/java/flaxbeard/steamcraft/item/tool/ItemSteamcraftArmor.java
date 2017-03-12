package flaxbeard.steamcraft.item.tool;

import flaxbeard.steamcraft.api.util.UtilMisc;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemSteamcraftArmor extends ItemArmor {
    protected String name;
    private Object repairMaterial;

    public ItemSteamcraftArmor(ArmorMaterial armorMat, int renderIndex, int armorType, Object repair, String n) {
        super(armorMat, renderIndex, armorType);
        this.repairMaterial = repair;
        this.name = n;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (name != "Gilded") {
            if (slot == 2) {
                return "steamcraft:textures/models/armor/" + this.name.substring(0, 1).toLowerCase() + this.name.substring(1) + "_2.png";
            }
            return "steamcraft:textures/models/armor/" + this.name.substring(0, 1).toLowerCase() + this.name.substring(1) + "_1.png";
        } else {
            if (slot == 2) {
                return "minecraft:textures/models/armor/gold_layer_2.png";
            }
            return "minecraft:textures/models/armor/gold_layer_1.png";
        }
    }


    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        if (repairMaterial instanceof ItemStack) {
            return par2ItemStack.isItemEqual((ItemStack) repairMaterial) ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
        }
        if (repairMaterial instanceof String) {
            return UtilMisc.doesMatch(par2ItemStack, (String) repairMaterial) ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
        }
        return super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

}
