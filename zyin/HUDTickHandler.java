package zyin;

import java.util.ArrayList;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class HUDTickHandler implements ITickHandler {
	
	public static final String DURABILITY_ICONS_PNG = "/zyin/images/Durability_icons.png";

    //U and V is the top left part of the image
    //X and Y is the width and height of the image
    public int armorDurabilityU = 0;
    public int armorDurabilityV = 0;
    public int armorDurabilityX = 1*16;
    public int armorDurabilityY = 2*16;
    
    //the height/width of the tools being rendered
    public int toolX = 1*16;
    public int toolY = 1*16;
    
    //where the armor icon is rendered
    public int durabalityLocX = ZyinMod.DurabilityLocationHorizontal;
    public int durabalityLocY = ZyinMod.DurabilityLocationVertical;
    
    //where the tool icons are rendered
    public int equipmentLocX = durabalityLocX + armorDurabilityX;
    public int equipmentLocY = durabalityLocY;
    
    private String messageLineSpacer = " ";
    
    private static Minecraft mc = Minecraft.getMinecraft();
    private GuiIngame gig = new GuiIngame(mc);
    private int renderTickCount = 0;
    private ArrayList<ItemStack> damagedItemsList = new ArrayList<ItemStack>(13);	//used to push items into the list of broken equipment to render
    private static final RenderItem itemRenderer = new RenderItem();



	public HUDTickHandler()
	{
		
	}
	
	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		return "HUD Tick Handler";
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if(type.equals(EnumSet.of(TickType.RENDER)))
		{
			onRenderTick();
		}
		else if(type.equals(EnumSet.of(TickType.CLIENT)))
		{
			GuiScreen guiScreen = HUDTickHandler.mc.currentScreen;
			if(guiScreen == null)
			{
				onTickInGame();
			}
			else
			{
				onTickInGUI(guiScreen);
			}
		}
	}

	protected void onTickInGUI(GuiScreen guiScreen) 
	{
		
	}

	protected void onRenderTick() 
	{
		RenderOnScreenMessages();
		RenderDurabilityIcons();
		
		renderTickCount++;
	}
	
	protected void onTickInGame()
	{
		
	}
	
	
	
	
	
	
	/**
	 * Renders the on screen message consisting of everything that gets put into the top let message area
	 */
	public void RenderOnScreenMessages()
	{

		//if the player is in the world
		//and not looking at a menu
		//and F3 not pressed
		if (ZyinMod.ShowInfoLine
			&& mc.inGameHasFocus
			&& mc.currentScreen == null
			&& !mc.gameSettings.showDebugInfo)
		{

			String coordinates = CalculateCoordinates();
			String compass = CalculateCompass();
			String distance = CalculateDistanceMeasurer();
			String safe = CalculateIsSafeOverlayEnabled();
			
			String message = coordinates + compass + distance + safe;
	
			mc.fontRenderer.drawStringWithShadow(message, 1, 1, 0xffffff);
		}
	}
	
	/**
	 * Renders the main durability icon and any damaged tools onto the screen.
	 */
	protected void RenderDurabilityIcons()
	{
		//if the player is in the world
		//and not in a menu
		//and F3 not shown
		if ((mc.inGameHasFocus || mc.currentScreen == null || (mc.currentScreen instanceof GuiChat))
				&& !mc.gameSettings.showDebugInfo)
		{
			
			//don't waste time recalculating things every tick
			if(renderTickCount % ZyinMod.DurabilityUpdateFrequency == 0)	//default: 1 out of every 50 ticks
			{
				CalculateDurabilityIcons();
			}
			
			Boolean armorExists = false;

			for(ItemStack toolStack : damagedItemsList)
			{
				if(toolStack.getItem() instanceof ItemArmor)
					armorExists = true;
			}
			
			int numTools = 0;
			for(ItemStack toolStack : damagedItemsList)
			{
				Item tool = toolStack.getItem();
				
				//if this tool is an armor
				if(tool instanceof ItemArmor)
				{
					if(ZyinMod.ShowArmorDurability)
					{
		                GL11.glDisable(GL11.GL_LIGHTING);	//disable lighting so it renders at full brightness
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(DURABILITY_ICONS_PNG));
						gig.drawTexturedModalRect(durabalityLocX, durabalityLocY, armorDurabilityU, armorDurabilityV, armorDurabilityX, armorDurabilityY);
					}
				}
				else //if this tool is an equipment/tool
				{	
					if(ZyinMod.ShowItemDurability)
					{
						int verticalPadding = 0;
						int verticalSpacer = equipmentLocY + (numTools * toolY) + verticalPadding;
						
						int horizontalPosition = durabalityLocX;
						if(armorExists && ZyinMod.ShowArmorDurability)
							horizontalPosition = equipmentLocX;	//if armor is being rendered then push this to the right
						
						//render the item with enchant effect
						itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, toolStack, horizontalPosition, verticalSpacer);
						//render the item's durability bar
						itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, toolStack, horizontalPosition, verticalSpacer);
						
						String damage = "" + (toolStack.getMaxDamage() - toolStack.getItemDamage());
						int damageX = (horizontalPosition) + toolX/2;
						int damageY = (verticalSpacer) + toolY-9;
						
						GL11.glDisable(GL11.GL_LIGHTING);	//this is needed because the itemRenderer.renderItem() method enables lighting
						mc.fontRenderer.setUnicodeFlag(true);
						mc.fontRenderer.drawStringWithShadow(damage, damageX, damageY, 0xffffff);
						mc.fontRenderer.setUnicodeFlag(false);
						//GL11.glEnable(GL11.GL_LIGHTING); 		not needed
						
					    numTools++;
					}
					
				}
			}
			mc.renderEngine.resetBoundTexture();
		}
	}
	
	
	/**
	 * Finds items in the players hotbar and equipped armor that is damaged and adds them to the damagedItemsList list.
	 */
	protected void CalculateDurabilityIcons()
	{
		//if the player is in the world
		//and not in a menu
		//and not typing
	    if ((mc.inGameHasFocus || mc.currentScreen == null || (mc.currentScreen instanceof GuiChat))
				&& !mc.gameSettings.keyBindPlayerList.pressed)
		{
			damagedItemsList.clear();
			
	    	CalculateDurabilityIconsForItems();
	    	CalculateDurabilityIconsForArmor();
		}
	    
	}
	
	/**
	 * Examines the players first 9 inventory slots (the players inventory) and sees if any tools are damaged.
	 * It adds damaged tools to the damagedItemsList list.
	 */
	private void CalculateDurabilityIconsForItems()
	{
		ItemStack[] items = mc.thePlayer.inventory.mainInventory;
    	
    	for(int i = 0; i < 9; i++)
    	{
    		ItemStack itemStack = items[i];
    		if(itemStack != null)
    		{
    			Item item = itemStack.getItem();
    			
    			if(item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemHoe)
    			{
    				int itemDamage = itemStack.getItemDamage();
			    	int maxDamage = itemStack.getMaxDamage();
			    	
			    	if(maxDamage != 0 &&
		    			(double)itemDamage / maxDamage > ZyinMod.DurabilityDisplayThresholdForItem)
			    	{
			    		damagedItemsList.add(itemStack);
			    	}
    			}
    		}
    	}
	}
	
	/**
	 * Examines the players current armor and sees if any of them are damaged.
	 * It adds damaged armors to the damagedItemsList list.
	 */
	private void CalculateDurabilityIconsForArmor()
	{
		ItemStack[] armorStacks = mc.thePlayer.inventory.armorInventory;

	    for(ItemStack armorStack : armorStacks)
	    {
	    	if(armorStack != null)
	    	{
				int itemDamage = armorStack.getItemDamage();
		    	int maxDamage = armorStack.getMaxDamage();
		    	if(maxDamage != 0 &&
	    			(double)itemDamage / maxDamage > ZyinMod.DurabilityDisplayThresholdForArmor)
		    	{
		    		damagedItemsList.add(armorStack);
		    	}
	    	}
	    }
	}
	
	
	/**
	 * Calculates the players coordinates
	 * @return "(x, z, y)" coordinates formatted string if the Coordinates are enabled, otherwise "".
	 */
	protected String CalculateCoordinates()
	{
		if(ZyinMod.ShowCoordinates)
		{
			int coordX = mc.thePlayer.getPlayerCoordinates().posX;
			int coordY = mc.thePlayer.getPlayerCoordinates().posY;
			int coordZ = mc.thePlayer.getPlayerCoordinates().posZ;

			String coordinatesString = ColorCode.WHITE + "[" + coordX + ", " + coordZ + ", " + coordY + "]";
			return coordinatesString + messageLineSpacer;
		}
        return "";
	}
	

	/**
	 * Calculates the direction the player is facing
	 * @return "[Direction]" compass formatted string if the Compass is enabled, otherwise "".
	 */
	protected String CalculateCompass()
	{
		if(ZyinMod.ShowCompass)
		{
			
			int yaw = (int)mc.thePlayer.rotationYaw;

			if (yaw<0)		//due to the yaw running a -360 to positive 360
				yaw+=360;	//not sure why it's that way
			yaw+=22;		//centers coordinates you may want to drop this line
			yaw%=360;		//and this one if you want a strict interpretation of the zones
			int facing = yaw/45;   //  360degrees divided by 45 == 8 zones
			
			String compassDirection = "";

			if(facing == 0)
				compassDirection = "S";
			else if(facing == 1)
				compassDirection = "SW";
			else if(facing == 2)
				compassDirection = "W";
			else if(facing == 3)
				compassDirection = "NW";
			else if(facing == 4)
				compassDirection = "N";
			else if(facing == 5)
				compassDirection = "NE";
			else if(facing == 6)
				compassDirection = "E";
			else// if(facing == 7)
				compassDirection = "SE";
			
			String compassString = ColorCode.LIGHT_GREY + "[" + ColorCode.RED + compassDirection + ColorCode.LIGHT_GREY + "]";
			return compassString + messageLineSpacer;
		}
		return "";
	}
	/**
	 * Calculates the distance of the block the player is pointing at
	 * @return if the Distance Measurer is enabled, the string "[FarthestHorizontalDistance]" or 
	 * "[x, z, y (absolute)]" is returned, otherwise "".
	 */
	protected String CalculateDistanceMeasurer()
	{
		if(ZyinMod.DistanceMeasurerMode > 0)
		{
	   		 MovingObjectPosition objectMouseOver;
	
	   		 objectMouseOver = mc.thePlayer.rayTrace(300, 1);

			 String distanceMeasurerString = "";
	   		 if(objectMouseOver != null && objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
	   		 {
				double coordX = mc.thePlayer.posX - 0.5;
				double coordY = mc.thePlayer.posY - mc.thePlayer.height;
				double coordZ = mc.thePlayer.posZ - 0.5;
				
				double blockX = objectMouseOver.blockX;
				double blockY = objectMouseOver.blockY;
				double blockZ = objectMouseOver.blockZ;
				
				double deltaX = coordX - blockX;
				double deltaY = coordY - blockY;
				double deltaZ = coordZ - blockZ;
				double delta = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
				
				double farthestHorizontalDistance = Math.max(Math.abs(deltaX), Math.abs(deltaZ));

				String x = String.format("%1$,.1f", deltaX);
				String y = String.format("%1$,.1f", deltaY);
				String z = String.format("%1$,.1f", deltaZ);
				String distance = String.format("%1$,.1f", delta);

				String farthestHorizontalDistanceStr = String.format("%1$,.1f", farthestHorizontalDistance);
				
				if(ZyinMod.DistanceMeasurerMode == 1)
					distanceMeasurerString = ColorCode.CYAN + "[" + farthestHorizontalDistanceStr + "]";
				else if(ZyinMod.DistanceMeasurerMode == 2)
					distanceMeasurerString = ColorCode.CYAN + "[" + x + ", " + z + ", " + y + " (" + distance + ")]";
				else
					distanceMeasurerString = ColorCode.CYAN + "[???]";
	   		 }
	   		 else
	   			distanceMeasurerString = ColorCode.CYAN + "[far]";
	   		 
	   		return distanceMeasurerString + messageLineSpacer;
		}
		return "";
	}
	
	/**
	 * Gets the status of Safe Overlay
	 * @return the string "safe" if the Safe Overlay is enabled, otherwise "".
	 */
	public String CalculateIsSafeOverlayEnabled()
	{
		String safeOverlayString = "";
		if(ZyinMod.SafeOverlayMode == 0)	//off
			safeOverlayString = ColorCode.WHITE + "";
		else if(ZyinMod.SafeOverlayMode == 1)	//on
			safeOverlayString = ColorCode.WHITE + "safe";
		else
			safeOverlayString = ColorCode.WHITE + "???";
		
		return safeOverlayString + messageLineSpacer;
	}
}