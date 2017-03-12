package flaxbeard.steamcraft.tile;

import flaxbeard.steamcraft.Config;
import flaxbeard.steamcraft.SteamcraftBlocks;
import flaxbeard.steamcraft.api.ISteamTransporter;
import flaxbeard.steamcraft.api.IWrenchDisplay;
import flaxbeard.steamcraft.api.IWrenchable;
import flaxbeard.steamcraft.api.steamnet.SteamNetwork;
import flaxbeard.steamcraft.api.tile.SteamTransporterTileEntity;
import flaxbeard.steamcraft.misc.ItemStackUtility;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TileEntitySmasher extends SteamTransporterTileEntity implements ISteamTransporter, IWrenchable, IWrenchDisplay {

	public static final SmashablesRegistry REGISTRY = new SmashablesRegistry(); 
	
    public int spinup = 0;
    public float extendedLength = 0.0F;
    public Block smooshingBlock;
    public int smooshingMeta;
    public int extendedTicks = 0;
    public ArrayList<ItemStack> smooshedStack;
    private boolean hasBlockUpdate = false;
    private boolean isActive = false;
    private boolean isBreaking = false;
    private boolean shouldStop = false;
    private boolean isInitialized = false;
    private boolean running = false;
    private boolean smashNextRound = false;
    private boolean noSmashDrops = false;
    private boolean hasBeenSet = false;

    public TileEntitySmasher() {
        super(ForgeDirection.VALID_DIRECTIONS);
    }

    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        this.extendedLength = access.getFloat("extendedLength");
        this.extendedTicks = access.getInteger("extendedTicks");
        this.spinup = access.getInteger("spinup");

        this.noSmashDrops = access.getBoolean("noSmashDrops");
        this.hasBeenSet = access.getBoolean("hasBeenSet");


        this.smooshingBlock = Block.getBlockById(access.getInteger("block"));
        this.smooshingMeta = access.getInteger("smooshingMeta");
        NBTTagList nbttaglist = (NBTTagList) access.getTag("Items");
        this.smooshedStack = new ArrayList<ItemStack>();

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            this.smooshedStack.add(ItemStack.loadItemStackFromNBT(nbttagcompound1));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        access.setBoolean("noSmashDrops", noSmashDrops);
        access.setBoolean("hasBeenSet", hasBeenSet);
        access.setInteger("spinup", spinup);
        access.setFloat("extendedLength", extendedLength);
        access.setInteger("extendedTicks", extendedTicks);
        access.setInteger("block", Block.getIdFromBlock(smooshingBlock));
        access.setInteger("smooshingMeta", smooshingMeta);
        NBTTagList nbttaglist = new NBTTagList();

        if (this.smooshedStack != null) {
            for (int i = 0; i < this.smooshedStack.size(); ++i) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                this.smooshedStack.get(i).writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        access.setTag("Items", nbttaglist);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound access = super.getDescriptionTag();
        access.setInteger("spinup", spinup);
        access.setFloat("extendedLength", extendedLength);
        access.setInteger("extendedTicks", extendedTicks);
        access.setInteger("block", Block.getIdFromBlock(smooshingBlock));
        access.setInteger("smooshingMeta", smooshingMeta);
        access.setBoolean("running", this.running);
        access.setBoolean("noSmashDrops", noSmashDrops);
        access.setBoolean("hasBeenSet", hasBeenSet);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, access);
    }


//	private int encodeParticles(){
//		int smash=0 , smoke1=0, smoke2=0, smoke3=0;
//		if (!worldObj.isRemote){
//			if (extendedTicks==3) smash = 1;
//			if (extendedTicks >= 8 && extendedTicks <= 16 && (extendedTicks % 4) == 0) smoke1 = 2;
//			//int smoke1 = (extendedTicks == 3) ? (1 << 1) : 0;
//			//int smoke2 = (extendedTicks >= 8) && (extendedTicks <= 16) && ((extendedTicks % 4) == 0) ? (1  << 2): 0;
//			//int smoke3 =  (extendedTicks == 6) ? (1 << 3) : 0;
//			
//		}
//		
//		
//		return smash + smoke1 + smoke2 + smoke3;
//	}

    private void decodeAndCreateParticles(int flags) {
        if (worldObj.isRemote) {
//			boolean smash = (flags & 1) == 1; // 0b0001
//			boolean smoke1 = ((flags & 2) >> 1 ) == 1; // 0b0010
//			boolean smoke2 = ((flags & 4) >> 2) == 1; // 0b0100
//			boolean smoke3 = ((flags & 8) >> 3) == 1; // 0b1000
            int[] tgt = getTarget(1);
            int x = tgt[0], y = yCoord, z = tgt[1];
//
//			if (smash){
//				//Steamcraft.log.debug((float)(Math.random()-0.5F)/3.0F);
//				//worldObj.spawnParticle("smoke", x+0.5D, y+0.5D, z+0.5D, (float)(Math.random()-0.5F)/3.0F, (float)(Math.random()-0.5F)/3.0F, (float)(Math.random()-0.5F)/3.0F);
//				//worldObj.spawnParticle("smoke", x+0.5D, y+0.5D, z+0.5D, (float)(Math.random()-0.5F)/3.0F, (float)(Math.random()-0.5F)/6.0F, (float)(Math.random()-0.5F)/3.0F);
//				//worldObj.spawnParticle("smoke", x+0.5D, y+0.5D, z+0.5D, (float)(Math.random()-0.5F)/32.0F, (float)(Math.random()-0.5F)/12.0F, (float)(Math.random()-0.5F)/3.0F);
//			}
//			if (smoke1){
            if (extendedTicks > 15) {
                float xV = 0F, zV = 0F;
                double xO = 0D, zO = 0D;
                switch (getBlockMetadata()) {
                    case 2:
                        zV = 0.05F;
                        zO = 0.1D;
                        break;
                    case 3:
                        zV = -0.05F;
                        zO = -0.1D;
                        break;
                    case 4:
                        xV = 0.05F;
                        xO = 0.1D;
                        break;
                    case 5:
                        xV = -0.05F;
                        xO = -0.1D;
                        break;
                    default:
                        break;
                }
                worldObj.spawnParticle("smoke", xCoord + 0.5D + xO, y + 1.1D, zCoord + 0.5D + zO, xV, 0.05F, zV);
                //	//Steamcraft.log.debug("STEAM!");


                //}
            }
        }

    }


    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.func_148857_g();
        this.extendedLength = access.getFloat("extendedLength");
        this.extendedTicks = access.getInteger("extendedTicks");
        this.spinup = access.getInteger("spinup");
        this.smooshingBlock = Block.getBlockById(access.getInteger("block"));
        this.smooshingMeta = access.getInteger("smooshingMeta");
        this.running = access.getBoolean("running");
        this.noSmashDrops = access.getBoolean("noSmashDrops");
        this.hasBeenSet = access.getBoolean("hasBeenSet");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

    }

    @Override
    public void updateEntity() {
        if (!isInitialized) {
            ForgeDirection myDir = myDir();
            this.addSideToGaugeBlacklist(myDir);
            ForgeDirection[] directions = new ForgeDirection[5];
            int i = 0;
            for (ForgeDirection direction : ForgeDirection.values()) {
                if (direction != myDir && direction != ForgeDirection.UP) {
                    directions[i] = direction;
                    i++;
                }
            }
            this.setDistributionDirections(directions);
            this.isInitialized = true;
            if (!worldObj.isRemote) {
                //Steamcraft.log.debug(worldObj.getChunkProvider().chunkExists(10, -63));
            }

        }
        super.updateEntity();
        if (!worldObj.isRemote) {
            int[] target = getTarget(1);

            int x = target[0], y = yCoord, z = target[1];
            if (this.spinup == 1) {
                this.worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "steamcraft:hiss", Block.soundTypeAnvil.getVolume(), 0.9F);
            }
            if (extendedTicks > 15) {
                this.worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "steamcraft:leaking", 2.0F, 0.9F);
            }
            if (extendedTicks == 5) {

                this.worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "random.break", 0.5F, (float) (0.75F + (Math.random() * 0.1F)));
            }
            if (extendedTicks > 0 && extendedTicks < 6) {
                if (smooshingBlock != null && smooshingBlock.stepSound != null) {
                    this.worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, smooshingBlock.stepSound.getBreakSound(), 0.5F, (float) (0.75F + (Math.random() * 0.1F)));
                }
            }

            //Remote == client, might as well not run on server

            //Flag does nothing

            //handle state changes
            if (this.shouldStop) {
                ////Steamcraft.log.debug("shouldStop");
                this.spinup = 0;
                this.extendedLength = 0;
                this.extendedTicks = 0;
                this.isActive = false;
                this.shouldStop = false;
                this.isBreaking = false;
                this.running = false;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return;
            }
            if (!this.smashNextRound && this.hasSomethingToSmash() && this.hasPartner() && this.getSteamShare() > 1000 && !isActive) {

                ////Steamcraft.log.debug("Smash next round!");
                this.smashNextRound = true;
                return;
                ////Steamcraft.log.debug("I should never get here");


            }
            boolean smashThisRound = false;
            ////Steamcraft.log.debug("Status: isActive: "+isActive+"; isBreaking: "+isBreaking+"; shouldStop: "+shouldStop);
            if (this.smashNextRound) {
                ////Steamcraft.log.debug("Smash this round!");
                smashThisRound = true;
                this.smashNextRound = false;
            }
            if (smashThisRound) {

                ////Steamcraft.log.debug("Smashing!");
                if (this.hasSomethingToSmash() && !this.isActive) {
                    this.decrSteam(1000);
                    this.running = true;
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                    this.isActive = true;
                    this.isBreaking = true;
                }
            }

            //handle processing
            if (this.isActive) {
                // if we haven't spun up yet, do it.
                if (this.isBreaking) {
                    if (!this.hasSomethingToSmash() && this.spinup < 40 && this.getBlockMetadata() % 2 == 0) {
                        this.shouldStop = true;
                        int[] tc = getTarget(2);
                        TileEntitySmasher partner = (TileEntitySmasher) worldObj.getTileEntity(tc[0], yCoord, tc[1]);
                        partner.shouldStop = true;
                        return;
                    }
                    if (this.spinup < 41) {
                        ////Steamcraft.log.debug("Spinning up!" + spinup);
                        // spinup complete. SMAASH!
                        if (this.spinup == 40) {
                            ////Steamcraft.log.debug("SMAAAAASH");

                            if (!worldObj.isAirBlock(x, y, z) && worldObj.getBlock(x, y, z) != Blocks.bedrock && worldObj.getTileEntity(x, y, z) == null && worldObj.getBlock(x, y, z).getBlockHardness(worldObj, x, y, z) < 50F) {
                                this.spinup++;
                                if (this.getBlockMetadata() % 2 == 0) {
                                    try {
                                        this.smooshingBlock = worldObj.getBlock(x, y, z);
                                        this.smooshingMeta = worldObj.getBlockMetadata(x, y, z);

                                        this.smooshedStack = smooshingBlock.getDrops(worldObj, x, y, z, smooshingMeta, 0);
                                    } catch (Exception e) {
                                        //Steamcraft.log.debug("================== WOULD HAVE CRASHED ==================");
                                        //Steamcraft.log.debug("This smasher's meta: "+this.getBlockMetadata());
                                        e.printStackTrace();
                                    }
                                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

                                    worldObj.setBlock(x, y, z, SteamcraftBlocks.dummy);
                                }
                            } else {
                                ////Steamcraft.log.debug("No block.");
                                if (this.hasPartner()) {
                                    ////Steamcraft.log.debug("I have a partner");
                                    int[] pc = getTarget(2);
                                    TileEntitySmasher partner = (TileEntitySmasher) worldObj.getTileEntity(pc[0], yCoord, pc[1]);
                                    //	//Steamcraft.log.debug("partner.spinup: "+partner.spinup);
                                    if (partner.spinup < 41) {
                                        ////Steamcraft.log.debug("No block and partner hasn't updated. I should stop.");
                                        this.shouldStop = true;
                                    }
                                    if (partner.spinup >= 41) {
                                        ////Steamcraft.log.debug("Partner has updated.");
                                        if (partner.shouldStop) {
                                            //	//Steamcraft.log.debug("Partner is stopping. I should stop too.");
                                            this.shouldStop = true;
                                        }
                                    }
                                    if (shouldStop) {
                                        this.spinup++;
                                        return;
                                    }


                                }


                            }
                        }
                        this.spinup++;

                        // if we've spun up, extend
                    } else if (this.extendedLength < 0.5F && !this.shouldStop) {
                        ////Steamcraft.log.debug("Extending: "+this.extendedLength);
                        this.extendedLength += 0.1F;
                        if (this.extendedTicks == 3) {

                            if (this.getBlockMetadata() % 2 == 0 && !worldObj.isRemote) spawnItems(x, y, z);

                        }
                        this.extendedTicks++;

                        // we're done extending. Time to go inactive and start retracting
                    } else {
                        this.isBreaking = false;
                        this.spinup = 0;

                    }
                } else {
                    // Get back in line!
                    if (this.extendedLength > 0.0F) {
                        this.extendedLength -= 0.025F;
                        this.extendedTicks++;

                        ////Steamcraft.log.debug("Retracting: "+this.extendedLength);
                        if (this.extendedLength < 0F) {
                            this.extendedLength = 0F;
                        }
                    } else {
                        ////Steamcraft.log.debug("Done!");
                        this.isActive = false;
                        this.running = false;
                        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                        this.extendedTicks = 0;
                        if (worldObj.getBlock(x, y, z) == SteamcraftBlocks.dummy) {
                            worldObj.setBlockToAir(x, y, z);
                        }

                    }
                }
                //Mark for sync

            } else if (worldObj.getBlock(x, y, z) == SteamcraftBlocks.dummy && getBlockMetadata() % 2 == 0) {
                worldObj.setBlockToAir(x, y, z);
            }
            //this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        } else {
            //if (this.extendedTicks >  0) //Steamcraft.log.debug(this.extendedTicks);
            if (this.running) {
                decodeAndCreateParticles(1);
                if (this.spinup < 40) {
                    this.spinup++;
                } else if (this.spinup == 40) {
                    int[] tc = getTarget(1);
                    int x = tc[0], y = tc[1], z = tc[2];
                    this.spinup++;
                    if (!worldObj.isAirBlock(x, y, z) && worldObj.getTileEntity(x, y, z) == null && worldObj.getBlock(x, y, z).getBlockHardness(worldObj, x, y, z) < 50F) {
                        if (this.getBlockMetadata() % 2 == 0) {
                            try {
                                this.smooshingBlock = worldObj.getBlock(x, y, z);
                                this.smooshingMeta = worldObj.getBlockMetadata(x, y, z);
                            } catch (Exception e) {

                            }
                        }
                    }

                } else if (this.extendedTicks < 25) {
                    this.extendedTicks++;
                }
            } else {
                this.spinup = 0;
                this.extendedTicks = 0;
            }

        }


    }

    private void spawnItems(int x, int y, int z) {
        if (smooshedStack != null) {
            for (ItemStack stack : smooshedStack) {
            	ItemStack output = REGISTRY.getOutput(stack);
                
                if(output != null && !this.noSmashDrops) {
                	if (worldObj.rand.nextInt(Config.chance) == 0) {
                    	output.stackSize *= 2; //doubling
                    }
                	
                	EntityItem entityItem = new EntityItem(this.worldObj, x + 0.5F, y + 0.1F, z + 0.5F, output);
                    this.worldObj.spawnEntityInWorld(entityItem);
                    this.smooshedStack = null;
                } else { //Drops itself
                	output = stack;
                    EntityItem entityItem = new EntityItem(this.worldObj, x + 0.5F, y + 0.1F, z + 0.5F, output);
                    this.worldObj.spawnEntityInWorld(entityItem);
                }
            }
        }
    }

    private boolean hasSomethingToSmash() {
        ////Steamcraft.log.debug("Can I smash anything?");
        int[] target = getTarget(1);
        int x = target[0], y = yCoord, z = target[1];
        if (!worldObj.isAirBlock(x, y, z) && worldObj.getBlock(x, y, z) != Blocks.bedrock && worldObj.getTileEntity(x, y, z) == null && worldObj.getBlock(x, y, z).getBlockHardness(worldObj, x, y, z) < 50F) {
            ////Steamcraft.log.debug("Maybe?");
            return true;
        } else {
            ////Steamcraft.log.debug("No =( " + x + ", " + y + ", " + z);
            return false;
        }

    }

    public boolean hasPartner() {
        //	//Steamcraft.log.debug("Do I have a partner?");
        int[] target = getTarget(2);
        int x = target[0], y = yCoord, z = target[1], opposite = target[2];

        if (worldObj.getBlock(x, y, z) == SteamcraftBlocks.smasher && ((TileEntitySmasher) worldObj.getTileEntity(x, y, z)).getSteamShare() > 100 && worldObj.getBlockMetadata(x, y, z) == opposite) {
            //	//Steamcraft.log.debug("I have a partner!");
            TileEntitySmasher partner = ((TileEntitySmasher) worldObj.getTileEntity(x, y, z));
            if (partner.noSmashDrops != this.noSmashDrops) {
                if (this.hasBeenSet && !partner.hasBeenSet) {
                    partner.noSmashDrops = this.noSmashDrops;
                } else if (!this.hasBeenSet && partner.hasBeenSet) {
                    this.noSmashDrops = partner.noSmashDrops;
                } else {
                    this.noSmashDrops = partner.noSmashDrops;
                }
                hasBeenSet = true;
                partner.hasBeenSet = true;
            }
            return true;
        }

        return false;
    }

    // returns x, z, opposite
    private int[] getTarget(int distance) {
        int x = xCoord, z = zCoord, opposite = 0, meta = getBlockMetadata();
        opposite = meta % 2 == 0 ? meta + 1 : meta - 1;
        switch (meta) {
            case 2:
                z -= distance;
                opposite = 3;
                break;
            case 3:
                z += distance;
                opposite = 2;
                break;
            case 4:
                x -= distance;
                opposite = 5;
                break;
            case 5:
                x += distance;
                opposite = 4;
                break;
            default:
                break;
        }

        return new int[]{x, z, opposite};
    }

    public void blockUpdate() {
        this.hasBlockUpdate = true;
    }

    public boolean hasUpdate() {
        return hasBlockUpdate;
    }

    public ForgeDirection myDir() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        switch (meta) {
            case 2:
                return ForgeDirection.NORTH;
            case 3:
                return ForgeDirection.SOUTH;
            case 4:
                return ForgeDirection.WEST;
            case 5:
                return ForgeDirection.EAST;
        }
        return ForgeDirection.NORTH;
    }

    @Override
    public boolean onWrench(ItemStack stack, EntityPlayer player, World world,
                            int x, int y, int z, int side, float xO, float yO, float zO) {
        if (player.isSneaking()) {
            this.hasBeenSet = true;
            this.noSmashDrops = !noSmashDrops;
            int[] target = getTarget(2);
            int x2 = target[0], y2 = yCoord, z2 = target[1], opposite = target[2];
            if (worldObj.getBlock(x2, y2, z2) == SteamcraftBlocks.smasher && worldObj.getBlockMetadata(x2, y2, z2) == opposite) {
                ((TileEntitySmasher) worldObj.getTileEntity(x2, y2, z2)).noSmashDrops = noSmashDrops;
                ((TileEntitySmasher) worldObj.getTileEntity(x2, y2, z2)).hasBeenSet = true;
                this.worldObj.markBlockForUpdate(x2, y2, z2);

            }
            this.worldObj.markBlockForUpdate(x, y, z);
            return true;
        } else {
            int steam = this.getSteamShare();
            this.getNetwork().split(this, true);
            ForgeDirection[] directions = new ForgeDirection[5];
            int i = 0;
            for (ForgeDirection direction : ForgeDirection.values()) {
                if (direction != myDir() && direction != ForgeDirection.UP) {
                    directions[i] = direction;
                    i++;
                }
            }
            this.setDistributionDirections(directions);
            SteamNetwork.newOrJoin(this);
            this.getNetwork().addSteam(steam);
            return true;
        }
    }


    @Override
    public void displayWrench(Post event) {
        GL11.glPushMatrix();
        int color = Minecraft.getMinecraft().thePlayer.isSneaking() ? 0xC6C6C6 : 0x777777;
        int x = event.resolution.getScaledWidth() / 2 - 8;
        int y = event.resolution.getScaledHeight() / 2 - 8;
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("steamcraft.smasher." + this.noSmashDrops), x + 15, y + 13, color);
        GL11.glPopMatrix();
    }

    public static class SmashablesRegistry {
    	public final Map<String, ItemStack> oreDicts = new HashMap<>();
    	public final Map<ItemStack, ItemStack> registry = new HashMap<>();

    	public ItemStack getOutput(ItemStack input) {
    		if (input == null) {
    			return null;
    		}
    		ItemStack output = null;
    		int[] ids = OreDictionary.getOreIDs(input);
    		
    		if (ids != null && ids.length > 0) {
    			for (int id : ids) {
    				output = oreDicts.get(OreDictionary.getOreName(id));
    				if (output != null) {
    					break;
    				}
    			}
    		}

    		if (output == null) {
    			for (Entry<ItemStack, ItemStack> entry : registry.entrySet()) {
    				if (ItemStack.areItemStacksEqual(entry.getKey(), input)) {
    					output = entry.getValue();
    					if (output != null) {
    						break;
    					}
    				}
    			}
    		}
    		
    		return ItemStack.copyItemStack(output);
    	}
    	
    	public List<ItemStack> getInputs(ItemStack output) {
    		if(output == null) {
    			return null;
    		}
    		
    		List<ItemStack> inputs = new ArrayList<ItemStack>();
    		
    		for (Entry<ItemStack, ItemStack> entry : registry.entrySet()) {
    			if(ItemStackUtility.areItemStacksMostlyEqual(entry.getValue(), output)){
    				inputs.add(entry.getKey());
    			}
    		}
    		
    		for (Entry<String, ItemStack> entry : oreDicts.entrySet()) {
    			if(ItemStackUtility.areItemStacksMostlyEqual(entry.getValue(), output)){
    				inputs.addAll(OreDictionary.getOres(entry.getKey()));
    			}
    		}
    		
    		if(inputs.isEmpty()){
    			return null;
    		}
    		
    		return inputs;
    	}

    	public void registerSmashable(String input, ItemStack output) {
    		oreDicts.put(input, output);
    	}

    	public void registerSmashable(Block input, ItemStack output) {
    		registerSmashable(new ItemStack(input), output);
    	}

    	public void registerSmashable(Item input, ItemStack output) {
    		registerSmashable(new ItemStack(input), output);
    	}

    	public void registerSmashable(ItemStack input, ItemStack output) {
    		registry.put(input, output);
    	}

        public void removeSmashable(String input, ItemStack output) {
            oreDicts.remove(input);
        }

        public void removeSmashable(ItemStack input, ItemStack output) {
            registry.remove(input);
        }
    }
}
