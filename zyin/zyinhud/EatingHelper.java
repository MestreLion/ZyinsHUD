package zyin.zyinhud;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneLogic;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;

/**
 * Eating Helper allows the player to eat food on their hotbar by calling its Eat() method.
 */
public class EatingHelper
{
    private Minecraft mc = Minecraft.getMinecraft();
    private Timer timer = new Timer();
    
    private Robot r = null;
    private boolean isCurrentlyEating;
    
    /**
     * Use this instance for all method calls.
     */
    public static EatingHelper instance = new EatingHelper();
    
	private EatingHelper()
	{
    	try
    	{
    		r = new Robot();
    	}
    	catch (AWTException e)
    	{
    		e.printStackTrace();
    	}
    	isCurrentlyEating = false;
	}
	
	/**
	 * Makes the player eat a food item on their hotbar.
	 */
    public void Eat()
    {
        //currentItemStack.onFoodEaten(mc.theWorld, mc.thePlayer);	//INSTANT EATING
    	
    	//make sure we're not about to click on a right-clickable thing
        MovingObjectPosition objectMouseOver = mc.thePlayer.rayTrace(5, 1);
        if (objectMouseOver != null && objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        {
        	int blockId = mc.theWorld.getBlockId(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);
        	Block block = Block.blocksList[blockId];
        	
        	//couldn't find a way to see if a block is 'right click-able' without running the onBlockActivation() method
        	//which we don't want to do
        	if(block instanceof BlockContainer	//chests, hoppers, dispenser, jukebox, beacon, etc.
    			|| block instanceof BlockButton
    			|| block instanceof BlockLever
    			|| block instanceof BlockRedstoneLogic
    			|| block instanceof BlockDoor
    			|| block instanceof BlockAnvil
    			|| block instanceof BlockBed
    			|| block instanceof BlockCake
    			|| block instanceof BlockFenceGate
    			|| block instanceof BlockTrapDoor
    			|| block instanceof BlockWorkbench)
        	{
        		return;
        	}
        }

    	
        if(isCurrentlyEating)
        {
        	r.mouseRelease(InputEvent.BUTTON3_MASK); //release right click
        	timer.cancel();
        	timer = new Timer();
        	isCurrentlyEating = false;
            return;
        }
        else
        {
        	if(!mc.thePlayer.getFoodStats().needFood())
        	{
        		//if we're not hungry then don't do anything
        		return;
        	}
        	
        	int foodItemSlot = GetFoodItemSlotFromHotbar();
        	if(foodItemSlot < 0)
        	{
        		InfoLine.DisplayNotification("No food in hotbar");
        		return;
        	}
        	
        	int previouslySelectedHotbarSlotIndex = mc.thePlayer.inventory.currentItem;
        	mc.thePlayer.inventory.currentItem = foodItemSlot;

            r.mousePress(InputEvent.BUTTON3_MASK); //perform a right click
            isCurrentlyEating = true;
            
        	ItemStack currentItemStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
        	ItemFood currentFood = (ItemFood)currentItemStack.getItem();
        	
            int eatingDurationInMilliseconds = 1000*currentFood.itemUseDuration / 20 + 100;	// +100 in case of lag
            
            //after this timer runs out we'll release right click to stop eating and select the previously selected item
            timer.schedule(new EatTimerTask(r, mc.thePlayer.inventory, previouslySelectedHotbarSlotIndex), eatingDurationInMilliseconds);
            
        }
    }
    
    
    /**
     * Gets the index of a food item that exists in the player's hotbar.
     * @return 0 through 8, inclusive. -1 if not found.
     */
    private int GetFoodItemSlotFromHotbar()
    {
    	ItemStack[] items = mc.thePlayer.inventory.mainInventory;
    	
    	if(ZyinHUD.ScanHotbarForFoodFromLeftToRight)
    	{
    		for (int i = 0; i < 9; i++)
            {
                ItemStack itemStack = items[i];
                if (itemStack != null)
                {
                    Item item = itemStack.getItem();
                    if (item instanceof ItemFood)
                    	return i;
                }
            }
    	}
    	else
    	{
    		for (int i = 8; i >= 0; i--)
            {
                ItemStack itemStack = items[i];
                if (itemStack != null)
                {
                    Item item = itemStack.getItem();
                    if (item instanceof ItemFood)
                    	return i;
                }
            }
    	}
    	
        return -1;
    }
	
    
    
    /**
     * Helper class whose purupose is to release right click and reselect the player's last selected item.
     */
    protected class EatTimerTask extends TimerTask
    {
    	private Robot r;
    	private InventoryPlayer inventory;
    	private int previouslySelectedItemSlot;
    	
    	EatTimerTask(Robot r, InventoryPlayer inventory, int previouslySelectedItemSlot)
    	{
    		this.r = r;
    		this.inventory = inventory;
    		this.previouslySelectedItemSlot = previouslySelectedItemSlot;
    	}
    	
    	@Override
        public void run()
        {
        	r.mouseRelease(InputEvent.BUTTON3_MASK); //release right click
        	inventory.currentItem = previouslySelectedItemSlot;
        	isCurrentlyEating = false;
        }
    	
    }
	
    
    
    
    /*@ForgeSubscribe
    public void EntityInteractEvent(net.minecraftforge.event.entity.player.EntityInteractEvent event)
    {
    	System.out.println("EntityInteractEvent");
    	Entity target = event.target;
    	
    	if(target instanceof EntityItemFrame)
    	{
    		InfoLine.DisplayNotification("EntityItemFrame");
    		return;
    	}
    	if(target instanceof EntityPig)
    	{

    		EntityPig pig = (EntityPig)event.target;
    		InfoLine.DisplayNotification("pig");
    		if(pig.getSaddled())
    		{
    			event.setResult(Result.DENY);
        		InfoLine.DisplayNotification("pig saddled");
    			return;
    		}
    	}
    }*/
    
    
	
}
