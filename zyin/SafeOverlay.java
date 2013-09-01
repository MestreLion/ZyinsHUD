package zyin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockHalfSlab;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumSkyBlock;

import org.lwjgl.opengl.GL11;

class SafeOverlay
{
    public static SafeOverlay instance = new SafeOverlay();
    
    //last time the overlay cache was generated
    private long lastGenerate;
    
    //point in time of the frame
    private long frameTime;
    
    //time in ms between re-calculations
    private int updateFrequency;
    
    //calculate locations in a cube with this radius around the player
    private int drawDistance; // actual area calculated: (drawDistance*2)^3
    
    //the transprancy of the "X" marks when rendered, between (0.1 and 1]
    private float unsafeOverlayTransparency;
    
    public boolean displayInNether;
    public boolean renderUnsafePositionsThroughWalls = false;

    private Position cachePosition = new Position();
    private static List<Position> unsafePositionCache = new ArrayList<Position>();
    
    private Minecraft mc;
    private RenderGlobal renderGlobal;
    private RenderBlocks renderBlocks;
    private EntityPlayer player;
    

    private SafeOverlay()
    {
    	mc = Minecraft.getMinecraft();;
		renderGlobal = mc.renderGlobal;
		renderBlocks = renderGlobal.globalRenderBlocks;
		player = mc.thePlayer;
		
    	//grab configuration settings
        drawDistance = ZyinMod.SafeOverlayDrawDistance;
        updateFrequency = ZyinMod.SafeOverlayUpdateFrequency;
        unsafeOverlayTransparency = (float)ZyinMod.SafeOverlayTransparency;	//must be between (0.1, 1]
        unsafeOverlayTransparency = (unsafeOverlayTransparency <= 0.1f) ? 0.101f : unsafeOverlayTransparency;	//check lower bounds
        unsafeOverlayTransparency = (unsafeOverlayTransparency >= 1f) ? 1f : unsafeOverlayTransparency;	//check upper bounds
        displayInNether = ZyinMod.SafeOverlayDisplayInNether;
    }

    /**
     * Renders all unsafe areas around the player.
     * It will only recalculate the unsafe areas once every [updateFrequency] (250) milliseconds
     * @param partialTickTime
     */
    public void RenderUnsafePositions(float partialTickTime)
    {
        if(ZyinMod.SafeOverlayMode == 0)	//0 = off, 1 = on
        	return;

		player = mc.thePlayer;
        if(displayInNether && player.dimension == -1)	//turn off in the nether, mobs can spawn no matter what
        	return;
        
        frameTime = System.currentTimeMillis();

        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;

        Position playerPosition = new Position((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

        
        if (unsafePositionCache.size() == 0
    		|| !playerPosition.equals(cachePosition)
            || frameTime - lastGenerate > updateFrequency)
        {
            CalculateUnsafePositions(playerPosition);
        }

        GL11.glTranslated(-x, -y, -z);		//go from cartesian x,y,z coordinates to in-world x,y,z coordinates
        GL11.glDisable(GL11.GL_TEXTURE_2D);	//fixes color rendering bug (we aren't rendering textures)
        GL11.glEnable(GL11.GL_BLEND);		//allows for color transparency
        
        if(renderUnsafePositionsThroughWalls)
        	GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);	//allows this unsafe position to be rendered through other blocks
        else
        	GL11.glEnable(GL11.GL_DEPTH_TEST);
        
        GL11.glBegin(GL11.GL_LINES);	//begin drawing lines defined by 2 vertices

        //render unsafe areas
        for (Position position : unsafePositionCache)
        {
            int lightLevel = position.GetLightLevelWithoutSky();
            if (lightLevel > 7)	//mobs spawn at light level 7
                continue;		//don't render areas that are safe
            
            int lightLevelWithSky = position.GetLightLevelWithSky();
            
            RenderUnsafeMarker(position, lightLevel, lightLevelWithSky);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
    
    /**
     * Renders an unsafe marker ("X" icon) at the position with (r,g,b) colors.
     * It takes into account the block above this position and relocates it accordinely.
     * It also takes into account if the block above dis-allows spawning on at position
     * (such as half slabs, stairs, fences, etc.)
     * @param position
     */
    public void RenderUnsafeMarker(Position position, int lightLevel, int lightLevelWithSky)
    {
        Tessellator tessellator = Tessellator.instance;
        
    	int blockId = position.GetBlockId(0, 0, 0);
    	int blockAboveId = position.GetBlockId(0, 1, 0);
    	
        Block block = Block.blocksList[blockId];
        Block blockAbove = Block.blocksList[blockAboveId];
        
        block = (block == null) ? Block.stone :  block;

        //get bounding box data for this block
    	double boundingBoxMinX = block.getBlockBoundsMinX();
    	double boundingBoxMaxX = block.getBlockBoundsMaxX();
    	double boundingBoxMaxY = block.getBlockBoundsMaxY();
    	double boundingBoxMinZ = block.getBlockBoundsMinZ();
    	double boundingBoxMaxZ = block.getBlockBoundsMaxZ();

        float r, g, b, alpha;
        
        if(lightLevelWithSky > lightLevel && lightLevelWithSky > 7)
        {
        	//yellow
        	r = 1f;
        	g = 1f;
        	b = 0f;
        	alpha = unsafeOverlayTransparency;
        }
        else
        {
        	//red
        	r = 1f;
        	g = 0f;
        	b = 0f;
        	alpha = unsafeOverlayTransparency;
        }

    	//Minecraft bug: the Y-bounds for half slabs are wrong, so set them manually
    	if(block instanceof BlockHalfSlab)
    	{
    		//0 = normal half slab, 0 = double slab, 8 = upside down slab
    	    boolean isUpsideDown = (renderBlocks.blockAccess.getBlockMetadata(position.x, position.y, position.z) & 8) == 8;
    	    
    	    boolean isDoubleSlab = block.isOpaqueCube();
    	    
    	    if (isUpsideDown || isDoubleSlab)
    	    {
                boundingBoxMaxY = 1.0;
    	    }
    	    else //normal half slab
    	    	return;
    	}
    	
    	if(blockAbove != null)	//if block above is not an Air block
    	{
        	if (blockAbove instanceof BlockSnow
        			|| blockAbove instanceof BlockRailBase
        			|| blockAbove instanceof BlockBasePressurePlate)	
        	{
        		//is there a spawnable block on top of this one?
        		//if so, then render the mark higher up
            	boundingBoxMaxY += blockAbove.getBlockBoundsMaxY();
        	}
        	else if(!CanSpawnInBlock(blockAbove))
        	{
        		return;
        	}
    	}

        double minX = position.x + boundingBoxMinX + 0.02;
        double maxX = position.x + boundingBoxMaxX - 0.02;
        
        double maxY = position.y + boundingBoxMaxY + 0.02;//0.014;
        
        double minZ = position.z + boundingBoxMinZ + 0.02;
        double maxZ = position.z + boundingBoxMaxZ - 0.02;
        
        
        //render the an X above the block
        GL11.glColor4f(r, g, b, alpha);	//opacity must be > 0.1
        
        GL11.glVertex3d(maxX,maxY,maxZ); 
		GL11.glVertex3d(minX,maxY,minZ);
		
        GL11.glVertex3d(maxX,maxY,minZ); 
		GL11.glVertex3d(minX,maxY,maxZ);
    }
    
    /**
     * Checks if mobs can spawn INSIDE OF the block
     * @param block
     * @return
     */
    public boolean CanSpawnInBlock(Block block)
    {
    	//mobs can not spawn INSIDE OF these blocks
    	return !(block instanceof BlockDoor
    			|| block instanceof BlockFluid
    			|| block instanceof BlockChest
    			|| block instanceof BlockGlass
    			|| block instanceof BlockFence
    			|| block instanceof BlockFenceGate
    			|| block instanceof BlockLeaves
    			|| block instanceof BlockWall
    			|| block instanceof BlockPane
    			|| block instanceof BlockWeb
    			|| block instanceof BlockCactus
    			|| block instanceof BlockAnvil
    			|| block instanceof BlockBed);
    }
    
    /**
     * Calculates which areas around the player are unsafe and adds these Positions
     * to the unsafePositionCache. The cache is used when the unsafe positions are
     * rendered (a.k.a. every frame). The cache is used to save CPU cycles from not
     * having to recalculate the unsafe locations every frame.
     * @param playerPosition
     */
    private void CalculateUnsafePositions(Position playerPosition)
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
            
            boolean first = pos.CanMobsSpawnOn(0, 0, 0);
            boolean second = pos.CanMobsSpawnOn(0, 1, 0);
            
            if (first && !second)
                unsafePositionCache.add(new Position(pos));
            
            else if (!first && previous)
                unsafePositionCache.add(new Position(pos, 0, -1, 0));
            
            previous = second;
        }
        
        cachePosition = playerPosition;
        lastGenerate = System.currentTimeMillis();
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
            this.x = o.x;
            this.y = o.y;
            this.z = o.z;
        }
        
        public Position(Position o, int dx, int dy, int dz)
        {
            this.x = o.x + dx;
            this.y = o.y + dy;
            this.z = o.z + dz;
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
        public boolean CanMobsSpawnOn(int dx, int dy, int dz)
        {
            int blockId = GetBlockId(dx, dy, dz);
            Block block = Block.blocksList[blockId];

            if (blockId > 0 && block.isOpaqueCube())
                return true;
            
            if (mc.theWorld.doesBlockHaveSolidTopSurface(x + dx, y + dy, z + dz))
                return true;
            
            // exception to the isOpaqueCube and doesBlockHaveSolidTopSurface rules
            if (block instanceof BlockFarmland)
                return true;
            
            return false;
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Position that = (Position) o;

            if (x != that.x) return false;
            if (y != that.y) return false;
            if (z != that.z) return false;

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