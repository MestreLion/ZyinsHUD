package zyin.zyinhud;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 * Eating Helper allows the player to eat food on their hotbar by calling its Eat() method.
 */
public class EatingHelper
{
    private Minecraft mc = Minecraft.getMinecraft();
    private Timer timer = new Timer();
    
    private Robot r = null;
    private boolean isCurrentlyEating;
    private int previouslySelectedHotbarSlotIndex;
    
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
        	int foodItemSlot = GetFoodItemSlotFromHotbar();
        	if(foodItemSlot < 0)
        	{
        		InfoLine.DisplayNotification("No food in hotbar");
        		return;
        	}
        	
        	previouslySelectedHotbarSlotIndex = mc.thePlayer.inventory.currentItem;
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
	
	
}
