package zyin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockHalfSlab;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.Property;

import org.lwjgl.opengl.GL11;

import zyin.util.FontCode;

/**
 * The Safe Overlay renders an overlay onto the game world showing which areas
 * mobs can spawn on.
 */
public class SafeOverlay
{
    /**
     * Time in MS between re-calculations. This value changes based on the drawDistance
     * from updateFrequencyMin to updateFrequencyMax.
     * <p>
     * Examples:
     * <br>drawDistance = 2, updateFrequency = 100ms
     * <br>drawDistance = 20, updateFrequency = 575ms
     * <br>drawDistance = 80, updateFrequency = 2000ms
     */
    private int updateFrequency;
    /**
     * The fastest the update frequency should be set to, in milliseconds.
     * It will be set to this value when the drawDistance = minDrawDistance.
     */
    private static final  int updateFrequencyMin = 100;
    /**
     * The slowest the update frequency should be set to, in milliseconds.
     * It will be set to this value when the drawDistance = maxDrawDistance.
     */
    private static final int updateFrequencyMax = 2000;

    /**
     * USE THE Getter/Setter METHODS FOR THIS!!
     * <p>
     * Calculate locations in a cube with this radius around the player.
     * <br>
     * Actual area calculated: (drawDistance*2)^3
     * <p>
     * drawDistance = 2 = 64 blocks (min)
     * <br>
     * drawDistance = 20 = 64,000 blocks (default)
     * <br>
     * drawDistance = 80 = 4,096,000 blocks (max)
     */
    private int drawDistance;
	public static final int minDrawDistance = 2;	//can't go lower than 2. setting to 1 dispays nothing
	public static final int defaultDrawDistance = 20;
    public static final int maxDrawDistance = 80;	//after 80 it really starts to lag

    /**
     * The transprancy of the "X" marks when rendered, between (0.1 and 1]
     */
    private float unsafeOverlayTransparency;

    /**
     * The last time the overlay cache was generated
     */
    private long lastGenerate;

    public boolean displayInNether;
    private boolean renderUnsafePositionsThroughWalls = false;
    
    private Position playerPosition;
    private Position cachePosition = new Position();
    private static List<Position> unsafePositionCache;

    private Minecraft mc;
    private RenderGlobal renderGlobal;
    private RenderBlocks renderBlocks;
    private EntityPlayer player;

	/**
	 * Use this instance of the Safe Overlay for method calls.
	 */
    public static SafeOverlay instance = new SafeOverlay();

    protected SafeOverlay()
    {
        mc = Minecraft.getMinecraft();;
        renderGlobal = mc.renderGlobal;
        renderBlocks = renderGlobal.globalRenderBlocks;
        player = mc.thePlayer;
        unsafePositionCache = new ArrayList<Position>();
        playerPosition = new Position();
        
        //grab configuration settings
        setDrawDistance(ZyinHUD.SafeOverlayDrawDistance);
        setSeeUnsafePositionsThroughWalls(ZyinHUD.SafeOverlaySeeThroughWalls);
        unsafeOverlayTransparency = (float)ZyinHUD.SafeOverlayTransparency;	//must be between (0.1, 1]
        unsafeOverlayTransparency = (unsafeOverlayTransparency <= 0.1f) ? 0.101f : unsafeOverlayTransparency;	//check lower bounds
        unsafeOverlayTransparency = (unsafeOverlayTransparency >= 1f) ? 1f : unsafeOverlayTransparency;	//check upper bounds
        displayInNether = ZyinHUD.SafeOverlayDisplayInNether;
    }

	/**
     * Renders all unsafe areas around the player.
     * It will only recalculate the unsafe areas once every [updateFrequency] (250) milliseconds
     * @param partialTickTime
     */
    public void RenderAllUnsafePositions(float partialTickTime)
    {
        if (ZyinHUD.SafeOverlayMode == 0)	//0 = off, 1 = on
        {
            return;
        }

        player = mc.thePlayer;

        if (!displayInNether && player.dimension == -1)	//turn off in the nether, mobs can spawn no matter what
        {
            return;
        }

        long frameTime = System.currentTimeMillis();
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;
        playerPosition = new Position((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

        if (unsafePositionCache.size() == 0
                || !playerPosition.equals(cachePosition)
                || frameTime - lastGenerate > updateFrequency)
        {
            CalculateUnsafePositions();
        }

        GL11.glTranslated(-x, -y, -z);		//go from cartesian x,y,z coordinates to in-world x,y,z coordinates
        GL11.glDisable(GL11.GL_TEXTURE_2D);	//fixes color rendering bug (we aren't rendering textures)
        
        //allows for color transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (renderUnsafePositionsThroughWalls)
        {
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);    //allows this unsafe position to be rendered through other blocks
        }
        else
        {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        
        GL11.glBegin(GL11.GL_LINES);	//begin drawing lines defined by 2 vertices

        //render unsafe areas
        for (Position position : unsafePositionCache)
        {
            RenderUnsafeMarker(position);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);	//puts blending back to normal, fixes bad HD texture rendering
    }

    /**
     * Renders an unsafe marker ("X" icon) at the position with colors depending on the Positions light levels.
     * It also takes into account the block above this position and relocates the mark vertically if needed.
     * @param position A position defined by (x,y,z) coordinates
     */
    protected void RenderUnsafeMarker(Position position)
    {
        int blockId = position.GetBlockId(0, 0, 0);
        int blockAboveId = position.GetBlockId(0, 1, 0);
        
        Block block = Block.blocksList[blockId];
        Block blockAbove = Block.blocksList[blockAboveId];
        
        //block is null when attempting to render on an Air block
        //we don't like null references so treat Air like an ordinary Stone block
        block = (block == null) ? Block.stone : block;
        
        //get bounding box data for this block
        //don't bother for horizontal (X and Z) bounds because every hostile mob spawns on a 1.0 wide block
        //some blocks, like farmland, have a different vertical (Y) bound
        double boundingBoxMinX = 0;
        double boundingBoxMaxX = 1;
        double boundingBoxMaxY = block.getBlockBoundsMaxY();	//almost always 1, but farmland is 0.9375
        double boundingBoxMinZ = 0;
        double boundingBoxMaxZ = 1;
        float r, g, b, alpha;
        int lightLevelWithSky = position.GetLightLevelWithSky();
        int lightLevelWithoutSky = position.GetLightLevelWithoutSky();

        if (lightLevelWithSky > lightLevelWithoutSky && lightLevelWithSky > 7)
        {
            //yellow
            //decrease the brightness of the yellow "X" marks if the surrounding area is dark
            int blockLightLevel = Math.max(lightLevelWithSky, lightLevelWithoutSky);
            float colorBrightnessModifier = blockLightLevel / 15f;
            r = 1f * colorBrightnessModifier;
            g = 1f * colorBrightnessModifier;
            b = 0f;
            alpha = unsafeOverlayTransparency;
        }
        else
        {
            //red
            r = 0.5f;
            g = 0f;
            b = 0f;
            alpha = unsafeOverlayTransparency;
        }

        //Minecraft bug: the Y-bounds for half slabs change if the user is aimed at them, so set them manually
        if (block instanceof BlockHalfSlab)
        {
            //0 = normal half slab, 0 = double slab, 8 = upside down slab
            /*boolean isUpsideDown = (renderBlocks.blockAccess.getBlockMetadata(position.x, position.y, position.z) & 8) == 8;

            boolean isDoubleSlab = block.isOpaqueCube();

            if (isUpsideDown || isDoubleSlab)
            {
                boundingBoxMaxY = 1.0;
            }
            else //normal half slab
            	return;*/
            boundingBoxMaxY = 1.0;
        }

        /*
        //Minecraft bug: the X and Z bounds for stairs are wrong, so set them manually
        else if(block instanceof BlockStairs)
        {
            boundingBoxMinX = 0;
            boundingBoxMinZ = 0;
        }
        */

        if (blockAbove != null)	//if block above is not an Air block
        {
            if (blockAbove instanceof BlockSnow
                    || blockAbove instanceof BlockRailBase
                    || blockAbove instanceof BlockBasePressurePlate)
            {
                //is there a spawnable block on top of this one?
                //if so, then render the mark higher up to match its height
                boundingBoxMaxY += blockAbove.getBlockBoundsMaxY();
            }
        }

        double minX = position.x + boundingBoxMinX + 0.02;
        double maxX = position.x + boundingBoxMaxX - 0.02;
        double maxY = position.y + boundingBoxMaxY + 0.02;
        double minZ = position.z + boundingBoxMinZ + 0.02;
        double maxZ = position.z + boundingBoxMaxZ - 0.02;
        
        //render an "X" slightly above the block
        GL11.glColor4f(r, g, b, alpha);	//alpha must be > 0.1
        
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, minZ);
        
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);
    }

    /**
     * Calculates which areas around the player are unsafe and adds these Positions
     * to the unsafePositionCache. The cache is used when the unsafe positions are
     * rendered (a.k.a. every frame). The cache is used to save CPU cycles from not
     * having to recalculate the unsafe locations every frame.
     * @param playerPosition
     */
    protected void CalculateUnsafePositions()
    {
        unsafePositionCache.clear();
        Position pos = new Position();
        boolean previous = false;

        for (int x = -drawDistance; x < drawDistance; x++)
        for (int z = -drawDistance; z < drawDistance; z++)
        for (int y = -drawDistance; y < drawDistance; y++)
        {
            pos.x = playerPosition.x + x;
            pos.y = playerPosition.y + y;
            pos.z = playerPosition.z + z;
            /*
            boolean first = pos.CanMobsSpawnOnBlock(0, 0, 0);
            boolean second = pos.CanMobsSpawnOnBlock(0, 1, 0);

            if (first && !second)
                unsafePositionCache.add(new Position(pos));

            else if (!first && previous)
                unsafePositionCache.add(new Position(pos, 0, -1, 0));

            previous = second;
            */

            if (pos.CanMobsSpawnOnBlock(0, 0, 0) && pos.CanMobsSpawnInBlock(0, 1, 0)
                    && pos.GetLightLevelWithoutSky() < 8)
            {
                unsafePositionCache.add(new Position(pos));
            }
        }

        cachePosition = playerPosition;
        lastGenerate = System.currentTimeMillis();
    }
    
    

    /**
     * Gets the status of the Safe Overlay
     * @return the string "safe" if the Safe Overlay is enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        String safeOverlayString = "";

        if (ZyinHUD.SafeOverlayMode == 0)	//off
        {
            safeOverlayString = FontCode.WHITE + "";
        }
        else if (ZyinHUD.SafeOverlayMode == 1)	//on
        {
            safeOverlayString = FontCode.WHITE + "safe";
        }
        else
        {
            safeOverlayString = FontCode.WHITE + "???";
        }

        return safeOverlayString + InfoLine.SPACER;
    }
    

    /**
     * Gets the current draw distance.
     * @return the draw distance radius
     */
    public int getDrawDistance()
    {
		return drawDistance;
	}
    
    /**
     * Sets the current draw distance.
     * @param newDrawDistance the new draw distance
     * @return the updated draw distance
     */
	public int setDrawDistance(int newDrawDistance)
	{
		if(newDrawDistance > maxDrawDistance)
			newDrawDistance = maxDrawDistance;
		else if(newDrawDistance < minDrawDistance)
			newDrawDistance = minDrawDistance;
		
		drawDistance = newDrawDistance;
		
		double percent = (double)newDrawDistance / maxDrawDistance;
		updateFrequency = (int) ((double)(updateFrequencyMax-updateFrequencyMin) * percent  + updateFrequencyMin);
		
		CalculateUnsafePositions();
		

        //save the new draw distance
        Property p = ZyinHUD.config.get(ZyinHUD.CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 20);
        p.set(drawDistance);
        ZyinHUD.config.save();
		
		return drawDistance;
	}

	/**
	 * Increases the current draw distance by 3.
	 * @return the updated draw distance
	 */
	public int increaseDrawDistance()
	{
		return setDrawDistance(drawDistance + 3);
	}
	/**
	 * Decreases the current draw distance by 3.
	 * @return the updated draw distance
	 */
	public int decreaseDrawDistance()
	{
		return setDrawDistance(drawDistance - 3);
	}
	/**
	 * Increases the current draw distance.
	 * @param amount how much to increase the draw distance by
	 * @return the updated draw distance
	 */
	public int increaseDrawDistance(int amount)
	{
		return setDrawDistance(drawDistance + amount);
	}
	/**
	 * Decreases the current draw distance.
	 * @param amount how much to increase the draw distance by
	 * @return the updated draw distance
	 */
	public int decreaseDrawDistance(int amount)
	{
		return setDrawDistance(drawDistance - amount);
	}
	
	/**
	 * Checks if see through walls mode is enabled.
	 * @return
	 */
    public boolean canSeeUnsafePositionsThroughWalls()
    {
    	return renderUnsafePositionsThroughWalls;
	}
    /**
     * Sets the see through wall mode
     * @param safeOverlaySeeThroughWalls true or false
     * @return the updated see through wall mode
     */
    public boolean setSeeUnsafePositionsThroughWalls(Boolean safeOverlaySeeThroughWalls)
    {
    	renderUnsafePositionsThroughWalls = safeOverlaySeeThroughWalls;

        //save this new setting
        Property p = ZyinHUD.config.get(ZyinHUD.CATEGORY_SAFEOVERLAY, "SafeOverlaySeeThroughWalls", 20);
        p.set(renderUnsafePositionsThroughWalls);
        ZyinHUD.config.save();
        
		return renderUnsafePositionsThroughWalls;
	}
    /**
     * Toggles the current see through wall mode
     * @return the udpated see through wall mode
     */
    public boolean toggleSeeUnsafePositionsThroughWalls()
    {
    	return setSeeUnsafePositionsThroughWalls(!renderUnsafePositionsThroughWalls);
	}
    

    /**
     * Helper class to storing information about a location in the world.
     * <p>
     * It uses (x,y,z) coordinates to determine things like mob spawning, and helper methods
     * to find blocks nearby.
     */
    class Position
    {
        public int x;
        public int y;
        public int z;

        public Position() {}

        public Position(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Position(Position o)
        {
            this(o.x, o.y, o.z);
        }

        public Position(Position o, int dx, int dy, int dz)
        {
            this(o.x + dx, o.y + dy, o.z + dz);
        }

        /**
         * Gets the ID of a block relative to this block.
         * @param dx x location relative to this block
         * @param dy y location relative to this block
         * @param dz z location relative to this block
         * @return
         */
        public int GetBlockId(int dx, int dy, int dz)
        {
            return mc.theWorld.getBlockId(x + dx, y + dy, z + dz);
        }

        /**
         * Checks if mobs can spawn ON the block at a location.
         * @param dx x location relative to this block
         * @param dy y location relative to this block
         * @param dz z location relative to this block
         * @return true if mobs can spawn ON this block
         */
        public boolean CanMobsSpawnOnBlock(int dx, int dy, int dz)
        {
            int blockId = GetBlockId(dx, dy, dz);
            Block block = Block.blocksList[blockId];

            if (blockId > 0 && block.isOpaqueCube())
            {
                return true;
            }

            if (mc.theWorld.doesBlockHaveSolidTopSurface(x + dx, y + dy, z + dz))
            {
                return true;
            }

            // exception to the isOpaqueCube and doesBlockHaveSolidTopSurface rules
            if (block instanceof BlockFarmland)
            {
                return true;
            }

            return false;
        }

        /**
         * Checks if mobs can spawn IN the block at a location.
         * @param dx x location relative to this block
         * @param dy y location relative to this block
         * @param dz z location relative to this block
         * @return true if mobs can spawn ON this block
         */
        public boolean CanMobsSpawnInBlock(int dx, int dy, int dz)
        {
            int blockId = GetBlockId(dx, dy, dz);
            Block block = Block.blocksList[blockId];

            if (block == null)	//air block
            {
                return true;
            }

            if (block.isOpaqueCube())	//majority of blocks: dirt, stone, etc.
            {
                return false;
            }

            //list of transparent blocks mobs can not spawn inside of
            //  (I wonder if the list shorter for blocks that mobs CAN spawn in?
            //  [lever, button, redstone  torches, reeds, rail, plants, crops, etc.])
            return !(block instanceof BlockDoor
                     || block instanceof BlockHalfSlab
                     || block instanceof BlockStairs
                     || block instanceof BlockFluid
                     || block instanceof BlockChest
                     || block instanceof BlockGlass
                     || block instanceof BlockIce
                     || block instanceof BlockFence
                     || block instanceof BlockFenceGate
                     || block instanceof BlockLeaves
                     || block instanceof BlockWall
                     || block instanceof BlockPane
                     || block instanceof BlockWeb
                     || block instanceof BlockCactus
                     || block instanceof BlockAnvil
                     || block instanceof BlockBed
                     || block instanceof BlockFarmland
                     || block instanceof BlockHopper
                     || block instanceof BlockPistonBase
                     || block instanceof BlockPistonExtension
                     || block instanceof BlockPistonMoving
                     || block instanceof BlockCake);
        }

        /**
         * Gets the light level of the spot above this block. Does not take into account sunlight.
         * @return 0-15
         */
        public int GetLightLevelWithoutSky()
        {
            return mc.theWorld.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z);
        }

        /**
         * Gets the light level of the spot above this block. Take into account sunlight.
         * @return 0-15
         */
        public int GetLightLevelWithSky()
        {
            return mc.theWorld.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Position that = (Position) o;

            if (x != that.x)
            {
                return false;
            }

            if (y != that.y)
            {
                return false;
            }

            if (z != that.z)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = (x ^ (x >>> 16));
            result = 31 * result + (y ^ (y >>> 16));
            result = 31 * result + (z ^ (z >>> 16));
            return result;
        }
    }
}