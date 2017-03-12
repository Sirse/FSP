package flaxbeard.steamcraft.tile;

import flaxbeard.steamcraft.Config;
import flaxbeard.steamcraft.api.ISteamTransporter;
import flaxbeard.steamcraft.api.tile.SteamTransporterTileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySteamTank extends SteamTransporterTileEntity implements ISteamTransporter {

    public TileEntitySteamTank() {
        super(80000, ForgeDirection.VALID_DIRECTIONS);
    }
}
