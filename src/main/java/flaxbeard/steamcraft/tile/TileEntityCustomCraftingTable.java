package flaxbeard.steamcraft.tile;

import flaxbeard.steamcraft.api.IWrenchable;
import flaxbeard.steamcraft.api.block.IDisguisableBlock;
import flaxbeard.steamcraft.client.render.BlockSteamPipeRenderer;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityCustomCraftingTable extends TileEntity implements IWrenchable, IDisguisableBlock {
    public Block disguiseBlock = null;
    public int disguiseMeta = 0;
    private boolean lastWrench = false;

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound access = new NBTTagCompound();
        access.setInteger("disguiseBlock", Block.getIdFromBlock(disguiseBlock));
        access.setInteger("disguiseMeta", disguiseMeta);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.func_148857_g();
        this.disguiseBlock = Block.getBlockById(access.getInteger("disguiseBlock"));
        this.disguiseMeta = access.getInteger("disguiseMeta");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readFromNBT(par1NBTTagCompound);
        this.disguiseBlock = Block.getBlockById(par1NBTTagCompound.getInteger("disguiseBlock"));
        this.disguiseMeta = par1NBTTagCompound.getInteger("disguiseMeta");
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("disguiseBlock", Block.getIdFromBlock(disguiseBlock));
        par1NBTTagCompound.setInteger("disguiseMeta", disguiseMeta);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.worldObj.isRemote) {
            boolean hasWrench = BlockSteamPipeRenderer.updateWrenchStatus();
            if (hasWrench != lastWrench && !(this.disguiseBlock == null || this.disguiseBlock == Blocks.air)) {
                this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
            lastWrench = hasWrench;
        }
    }

    @Override
    public boolean onWrench(ItemStack stack, EntityPlayer player, World world,
                            int x, int y, int z, int side, float xO, float yO, float zO) {
        if (player.isSneaking()) {
            if (this.disguiseBlock != null) {
                if (!player.capabilities.isCreativeMode) {
                    EntityItem entityItem = new EntityItem(world, player.posX, player.posY, player.posZ, new ItemStack(disguiseBlock, 1, disguiseMeta));
                    world.spawnEntityInWorld(entityItem);
                }
                world.playSoundEffect((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), disguiseBlock.stepSound.getBreakSound(), (disguiseBlock.stepSound.getVolume() + 1.0F) / 2.0F, disguiseBlock.stepSound.getPitch() * 0.8F);
                disguiseBlock = null;
                this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return true;
            }
        }
        return false;
    }

    @Override
    public Block getDisguiseBlock() {
        return this.disguiseBlock;
    }

    @Override
    public void setDisguiseBlock(Block block) {
        this.disguiseBlock = block;
    }

    @Override
    public int getDisguiseMeta() {
        return this.disguiseMeta;
    }

    @Override
    public void setDisguiseMeta(int meta) {
        this.disguiseMeta = meta;
    }
}
