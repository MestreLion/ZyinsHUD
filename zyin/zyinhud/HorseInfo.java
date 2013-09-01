package zyin.zyinhud;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityHorse;
import zyin.zyinhud.util.FontCodes;

/**
 * Shows information about horses in the F3 menu.
 */
public class HorseInfo
{
	private static final DecimalFormat twoDecimalPlaces = new DecimalFormat("#.##");
	private static final DecimalFormat oneDecimalPlace = new DecimalFormat("#.#");
	private static Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Renders a horse's speed, hit points, and jump strength on the F3 menu when the player is riding it.
	 */
	public static void Render()
	{
    	//if the player is in the world
        //and not in a menu
        //and F3 is shown
        if (ZyinHUD.ShowHorseInfo &&
        		(mc.inGameHasFocus || mc.currentScreen == null || mc.currentScreen instanceof GuiChat)
                && mc.gameSettings.showDebugInfo)
        {
        	Entity riddenEntity = mc.thePlayer.ridingEntity;
        	if(riddenEntity instanceof EntityHorse)
        	{
        		EntityHorse horse = (EntityHorse) riddenEntity;
        		
        		double horseSpeed = GetEntityMaxSpeed(horse);
        		int horseHP = GetEntityMaxHP(horse);
        		int horseHearts = GetEntityMaxHearts(horse);
        		double horseJump = GetEntityMaxJump(horse);

        		String horseSpeedString = twoDecimalPlaces.format(horseSpeed);
        		String horseHPString = oneDecimalPlace.format(GetEntityMaxHP(horse));
        		String horseHeartsString = ""+horseHearts;
        		String horseJumpString = oneDecimalPlace.format(GetEntityMaxJump(horse));
        		
        		if(horseSpeed > 10)
        			horseSpeedString = FontCodes.GREEN + horseSpeedString + FontCodes.WHITE;
        		else if(horseSpeed < 8)
        			horseSpeedString = FontCodes.RED + horseSpeedString + FontCodes.WHITE;
        		
        		if(horseHP > 24)
        		{
        			horseHPString = FontCodes.GREEN + horseHPString + FontCodes.WHITE;
        			horseHeartsString = FontCodes.GREEN + horseHeartsString + FontCodes.WHITE;
        		}
        		else if(horseHP < 18)
        		{
        			horseHPString = FontCodes.RED + horseHPString + FontCodes.WHITE;
        			horseHeartsString = FontCodes.RED + horseHeartsString + FontCodes.WHITE;
        		}
        		
        		if(horseJump > 4)
        			horseJumpString = FontCodes.GREEN + horseJumpString + FontCodes.WHITE;
        		else if(horseJump < 2.5)
        			horseJumpString = FontCodes.RED + horseJumpString + FontCodes.WHITE;
        		
        		String horseSpeedMessage = "Horse Speed: " + horseSpeedString + " m/s";
        		String horseHPMessage = "Horse HP: " + horseHPString + " (" + horseHeartsString + " hearts)";
        		String horseJumpMessage = "Horse Jump: " + horseJumpString + " blocks";
        		
            	mc.fontRenderer.drawStringWithShadow(horseSpeedMessage, 1, 130, 0xffffff);
            	mc.fontRenderer.drawStringWithShadow(horseJumpMessage, 1, 140, 0xffffff);
            	mc.fontRenderer.drawStringWithShadow(horseHPMessage, 1, 150, 0xffffff);
        	}
        }
	}
	
	/**
	 * Gets the max height a horse can jump when the jump bar is fully charged.
	 * @param horse
	 * @return e.x. 1.5 for all donkeys, horses are ~2-5
	 */
	public static double GetEntityMaxJump(EntityHorse horse)
	{
		//testing data:
		//0.5000000 = 1.5 blocks (min) (all donkeys have 0.5)
		//0.55 = 2 blocks
		//0.58596 = 2 blocks
		//0.668953 = 2.5 blocks
		//0.7214789 = 3 blocks
		//0.833727 = 4 blocks
		//0.881760 = 4.5 blocks
		//...
		//??? = 5.1 blocks (max according to the Wiki)
		return (horse.func_110215_cj() - 0.5) * 4.0/0.5 + 1.5;
	}

	/**
	 * Gets an entity's max hit points
	 * @param entity
	 * @return e.x. Steve = 20 hit points
	 */
	public static int GetEntityMaxHP(EntityLivingBase entity)
	{
		return (int) entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111125_b();
	}
	
	/**
	 * Gets the max hearts an entity has
	 * @param entity
	 * @return e.x. Steve = 20 hit points
	 */
	public static int GetEntityMaxHearts(EntityLivingBase entity)
	{
		return (int) entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111125_b() / 2;
	}
	
	/**
	 * Gets an entity's max run speed in meters(blocks) per second
	 * @param entity
	 * @return e.x. Steve = 4.3 m/s. Horses ~7-12?
	 */
	public static double GetEntityMaxSpeed(EntityLivingBase entity)
	{
		//Steve has a movement speed of 0.1 and walks 4.3 blocks per second,
		//so multiply this result by 43 to convert to blocks per second
		
		//testing data:
		//0.174999	~7.69 m/s
		//0.19427	~8.333 m/s
		//0.1		~4.3-4.5 m/s
		return entity.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111125_b() * 43;
	}
	
	
}
