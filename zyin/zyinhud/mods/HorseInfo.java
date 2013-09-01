package zyin.zyinhud.mods;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityHorse;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import zyin.zyinhud.util.FontCodes;
import zyin.zyinhud.util.Localization;

/**
 * Shows information about horses in the F3 menu.
 */
public class HorseInfo
{
	/** Enables/Disables this Mod */
	public static boolean Enabled;

    /**
     * Toggles this Mod on or off
     * @return The state the Mod was changed to
     */
    public static boolean ToggleEnabled()
    {
    	Enabled = !Enabled;
    	return Enabled;
    }
    public static String Hotkey;
    public static final String HotkeyDescription = "ZyinHUD: Horse Info";
    
	/**
	 * 0=off<br>
	 * 1=on<br>
	 */
    public static int Mode = 0;
    
    /** The maximum number of modes that is supported */
    public static int NumberOfModes = 2;
    
    public static boolean ShowHorseStatsOnF3Menu;
    
    /** Sets the number of decimal places that will be rendered when displaying horse stats */
    public static int numberOfDecimalsDisplayed = 1;
    public static int minNumberOfDecimalsDisplayed = 0;
    public static int maxNumberOfDecimalsDisplayed = 20;
    
    
    private static Minecraft mc = Minecraft.getMinecraft();
    private static EntityClientPlayerMP me;

    //values above the perfect value are aqua
    //values between the perfect and good values are green
    //values between the good and bad values are white
    //values below the bad value are red
    private static double perfectHorseSpeedThreshold = 13;	//max: 14.1?
    private static double goodHorseSpeedThreshold = 11;
    private static double badHorseSpeedThreshold = 9.5;		//min: ~7?
    
    private static double perfectHorseJumpThreshold = 5;	//max: 5.5
    private static double goodHorseJumpThreshold = 4;
    private static double badHorseJumpThreshold = 2.5;		//min: 1.2
    
    private static int perfectHorseHPThreshold = 28;		//max: 30
    private static int goodHorseHPThreshold = 24;			
    private static int badHorseHPThreshold = 20;			//min: ~14?
    
    private static final int verticalSpaceBetweenLines = 10;	//space between the overlay lines (because it is more than one line)
    
    /** Horses that are farther away than this will not have their info shown */
    public static int viewDistanceCutoff = 8;		//how far away we will render the overlay
    public static int minViewDistance = 0;
    public static int maxViewDistance = 120;
    
    private static DecimalFormat decimalFormat = GetDecimalFormat();
    
    
    /**
     * Gets the amount of decimals that should be displayed with a DecimalFormat object.
     * @return
     */
    private static DecimalFormat GetDecimalFormat()
    {
    	if(numberOfDecimalsDisplayed < 1)
    		return new DecimalFormat("#");
    	
    	String format = "#.";
    	for(int i = 1; i <= numberOfDecimalsDisplayed; i++)
    		format += "#";
    	
    	return new DecimalFormat(format);
    }
    
    /**
     * Gets the number of deciamls used to display the horse stats.
     * @return
     */
    public static int GetNumberOfDecimalsDisplayed()
    {
    	return numberOfDecimalsDisplayed;
    }
    
    /**
     * Sets the number of deciamls used to display the horse stats.
     * @param numDecimals
     * @return
     */
    public static void SetNumberOfDecimalsDisplayed(int numDecimals)
    {
    	numberOfDecimalsDisplayed = numDecimals;
    	decimalFormat = GetDecimalFormat();
    }

    /**
     * Renders a horse's speed, hit points, and jump strength on the F3 menu when the player is riding it.
     */
    public static void RenderOntoDebugMenu()
    {
        //if the player is in the world
        //and not in a menu
        //and F3 is shown
        if (HorseInfo.Enabled && ShowHorseStatsOnF3Menu &&
                (mc.inGameHasFocus || mc.currentScreen == null || mc.currentScreen instanceof GuiChat)
                && mc.gameSettings.showDebugInfo)
        {
            Entity riddenEntity = mc.thePlayer.ridingEntity;

            if (riddenEntity instanceof EntityHorse)
            {
            	//Localization.get("horseinfo.debug"),
                EntityHorse horse = (EntityHorse) riddenEntity;
                String horseSpeedMessage = Localization.get("horseinfo.debug.speed") + " " + GetHorseSpeedText(horse) + " m/s";
                String horseJumpMessage = Localization.get("horseinfo.debug.jump") + " " + GetHorseJumpText(horse) + " blocks";
                String horseHPMessage = Localization.get("horseinfo.debug.hp") + " " + GetHorseHPText(horse);

                String coloring = GetHorseColoringText(horse);
                String marking = GetHorseMarkingText(horse);
                if(marking.isEmpty())	//no markings
                	marking = "None";
                
                String horseColor = Localization.get("horseinfo.debug.color") + " " + coloring;
                String horseMarking = Localization.get("horseinfo.debug.markings") + " " + marking;
                
                mc.fontRenderer.drawStringWithShadow(horseSpeedMessage, 1, 130, 0xffffff);
                mc.fontRenderer.drawStringWithShadow(horseJumpMessage, 1, 140, 0xffffff);
                mc.fontRenderer.drawStringWithShadow(horseHPMessage, 1, 150, 0xffffff);
                
                if(!coloring.isEmpty())	//not a donkey
                {
                    mc.fontRenderer.drawStringWithShadow(horseColor, 1, 170, 0xffffff);
                    mc.fontRenderer.drawStringWithShadow(horseMarking, 1, 180, 0xffffff);
                }
            }
        }
    }

    /**
     * Renders a horse's speed, hit points, and jump strength on the screen by an entity.
     * @param entity
     * @param x location on the HUD
     * @param y location on the HUD
     * @param isEntityBehindUs
     */
    public static void RenderEntityOverlay(Entity entity, int x, int y, boolean isEntityBehindUs)
    {
        if (!(entity instanceof EntityHorse))
        {
            return;    //we only care about horses
        }

        //if the player is in the world
        //and not looking at a menu
        //and F3 not pressed
        if (HorseInfo.Enabled && Mode == 1 &&
                (mc.inGameHasFocus || mc.currentScreen == null || mc.currentScreen instanceof GuiChat)
                && !mc.gameSettings.showDebugInfo)
        {
            if (isEntityBehindUs)
            {
                return;
            }

            me = mc.thePlayer;
            EntityHorse horse = (EntityHorse)entity;

            if (horse.riddenByEntity instanceof EntityClientPlayerMP)
            {
                return;    //don't render stats of the horse we are currently riding
            }

            //only show entities that are close by
            double distanceFromMe = me.getDistanceToEntity(horse);

            if (distanceFromMe > viewDistanceCutoff)
            {
                return;
            }

            String[] multilineOverlayMessage = GetMultilineOverlayMessage(horse);
            
            //calculate the width of the longest string in the multi-lined overlay message
            int overlayMessageWidth = 0;
            for (String overlayMessageLine : multilineOverlayMessage)
            {
                int thisMessageWidth = mc.fontRenderer.getStringWidth(overlayMessageLine);

                if (thisMessageWidth > overlayMessageWidth)
                {
                    overlayMessageWidth = thisMessageWidth;
                }
            }

            ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int width = res.getScaledWidth();
            int height = res.getScaledHeight();
            
            //center the text on the horse
            x -= overlayMessageWidth / 2;
            //move the text vertically based on how many lines are displayed
            y -= (multilineOverlayMessage.length - 3) * 10;

            //don't render text if it is off the screen
            if (x >= width || x <= 0 - overlayMessageWidth
                    || y <= 0 - multilineOverlayMessage.length * verticalSpaceBetweenLines || y >= height)
            {
                return;
            }

            //render the overlay message
            GL11.glDisable(GL11.GL_LIGHTING);
            int i = 0;

            for (String s : multilineOverlayMessage)
            {
                mc.fontRenderer.drawStringWithShadow(s, x, y + i * verticalSpaceBetweenLines, 0xFFFFFF);
                i++;
            }
        }
    }

    /**
     * Gets the status of the Horse Info
     * @return the string "horse" if the Horse Info is enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        if (Mode == 0)	//off
        {
            return FontCodes.WHITE + "";
        }
        else if (Mode == 1)	//on
        {
            return FontCodes.WHITE + "horse" + InfoLine.SPACER;
        }
        else
        {
            return FontCodes.WHITE + "???" + InfoLine.SPACER;
        }
    }

    private static String[] GetMultilineOverlayMessage(EntityHorse horse)
    {
        float horseGrowingAge = horse.func_110254_bY();	//horse age, 0.5 (baby) to 1 (adult)

        /*
        int field_110278_bp = horse.field_110278_bp;	//tail rotation
        int field_110279_bq = horse.field_110279_bq;
        float func_110258_o = horse.func_110258_o(1f);	//head rotation
        float bodyRotation = horse.func_110223_p(1f);	//standing up rotation (i.e. when jumping)
        float func_110201_q = horse.func_110201_q(1f);	//flickers for... idk

        int love = horse.inLove;	//countdown timer starting at ~600 when fed a breeding item
        //horse.breeding;	//countdown from 60 after breeding initiated

        mc.fontRenderer.drawStringWithShadow("tail rotation:"+field_110278_bp, 1, 40, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("field_110279_bq:"+field_110279_bq, 1, 50, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("age:"+horseAge, 1, 60, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("head rotation:"+func_110258_o, 1, 70, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("func_110223_p:"+bodyRotation, 1, 80, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("func_110201_q:"+func_110201_q, 1, 90, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("love:"+love, 1, 100, 0xFFFFFF);
        */

        if (horseGrowingAge < 1f)
        {
            String[] multilineOverlayMessage =
            {
                GetHorseAgeAsPercent(horse) + "%",
                "",
                GetHorseSpeedText(horse) + " " + Localization.get("horseinfo.overlay.speed"),
                GetHorseHPText(horse) + " " + Localization.get("horseinfo.overlay.hp"),
                GetHorseJumpText(horse) + " " + Localization.get("horseinfo.overlay.jump")
            };
            return multilineOverlayMessage;
        }
        else
        {
        	String[] multilineOverlayMessage =
            {
                GetHorseSpeedText(horse) + " " + Localization.get("horseinfo.overlay.speed"),
                GetHorseHPText(horse) + " " + Localization.get("horseinfo.overlay.hp"),
                GetHorseJumpText(horse) + " " + Localization.get("horseinfo.overlay.jump")
            };
            return multilineOverlayMessage;
        }
    }

    /**
     * Gets the horses age ranging from 0 to 100.
     * @param horse
     * @return
     */
    private static int GetHorseAgeAsPercent(EntityHorse horse)
    {
        float horseGrowingAge = horse.func_110254_bY();	//horse age ranges from 0.5 to 1
        return (int)((horseGrowingAge - 0.5f) * 2.0f * 100f);
    }

    /**
     * Gets a horses speed, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "13.5"<br>green "12.5"<br>white "11.3"<br>red "7.0"
     */
    private static String GetHorseSpeedText(EntityHorse horse)
    {
        double horseSpeed = GetEntityMaxSpeed(horse);
        String horseSpeedString = decimalFormat.format(horseSpeed);

        if (horseSpeed > perfectHorseSpeedThreshold)
            horseSpeedString = FontCodes.AQUA + horseSpeedString + FontCodes.WHITE;
        else if (horseSpeed > goodHorseSpeedThreshold)
            horseSpeedString = FontCodes.GREEN + horseSpeedString + FontCodes.WHITE;
        else if (horseSpeed < badHorseSpeedThreshold)
            horseSpeedString = FontCodes.RED + horseSpeedString + FontCodes.WHITE;

        return horseSpeedString;
    }

    /**
     * Gets a horses HP, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "28"<br>green "26"<br>white "22"<br>red "18"
     */
    private static String GetHorseHPText(EntityHorse horse)
    {
        int horseHP = GetEntityMaxHP(horse);
        String horseHPString = decimalFormat.format(GetEntityMaxHP(horse));

        if (horseHP > perfectHorseHPThreshold)
            horseHPString = FontCodes.AQUA + horseHPString + FontCodes.WHITE;
        else if (horseHP > goodHorseHPThreshold)
            horseHPString = FontCodes.GREEN + horseHPString + FontCodes.WHITE;
        else if (horseHP < badHorseHPThreshold)
            horseHPString = FontCodes.RED + horseHPString + FontCodes.WHITE;

        return horseHPString;
    }
    
    /**
     * Gets a horses hearts, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "15"<br>green "13"<br>white "11"<br>red "9"
     */
    private static String GetHorseHeartsText(EntityHorse horse)
    {
        int horseHP = GetEntityMaxHP(horse);
        int horseHearts = GetEntityMaxHearts(horse);
        String horseHeartsString = "" + horseHearts;

        if (horseHP > perfectHorseHPThreshold)
            horseHeartsString = FontCodes.AQUA + horseHeartsString + FontCodes.WHITE;
        else if (horseHP > goodHorseHPThreshold)
                horseHeartsString = FontCodes.GREEN + horseHeartsString + FontCodes.WHITE;
        else if (horseHP < badHorseHPThreshold)
            horseHeartsString = FontCodes.RED + horseHeartsString + FontCodes.WHITE;

        return horseHeartsString;
    }

    /**
     * Gets a horses jump height, colored based on how good it is.
     * @param horse
     * @return e.x.:<br>aqua "5.4"<br>green "4"<br>white "3"<br>red "1.5"
     */
    private static String GetHorseJumpText(EntityHorse horse)
    {
        double horseJump = GetHorseMaxJump(horse);
        String horseJumpString = decimalFormat.format(horseJump);

        if (horseJump > perfectHorseJumpThreshold)
            horseJumpString = FontCodes.AQUA + horseJumpString + FontCodes.WHITE;
        else if (horseJump > goodHorseJumpThreshold)
            horseJumpString = FontCodes.GREEN + horseJumpString + FontCodes.WHITE;
        else if (horseJump < badHorseJumpThreshold)
            horseJumpString = FontCodes.RED + horseJumpString + FontCodes.WHITE;

        return horseJumpString;
    }

    /**
     * Gets a horses primary coloring
     * @param horse
     * @return
     */
    private static String GetHorseColoringText(EntityHorse horse)
    {
        String texture = horse.func_110212_cp()[0];
        
        if(texture == null || texture.isEmpty())
        	return "";
        
        String[] textureArray = texture.split("/");			//"textures/entity/horse/horse_creamy.png"
        texture = textureArray[textureArray.length-1];		//"horse_creamy.png"
        texture = texture.substring(6, texture.length()-4);	//"creamy"
        texture = WordUtils.capitalize(texture);			//"Creamy"
        
        return texture;
    }

    /**
     * Gets a horses secondary coloring
     * @param horse
     * @return
     */
    private static String GetHorseMarkingText(EntityHorse horse)
    {
        String texture = horse.func_110212_cp()[1];
        
        if(texture == null || texture.isEmpty())
        	return "";
        
        String[] textureArray = texture.split("/");				//"textures/entity/horse/horse_markings_blackdots.png"
        texture = textureArray[textureArray.length-1];			//"horse_markings_blackdots.png"
        texture = texture.substring(15, texture.length()-4);	//"blackdots"
        texture = WordUtils.capitalize(texture);				//"Blackdots"
        
        return texture;
    }

    /**
     * Gets the max height a horse can jump when the jump bar is fully charged.
     * @param horse
     * @return e.x. 1.2?-5.5?
     */
    private static double GetHorseMaxJump(EntityHorse horse)
    {
        //testing data (tested using snow layers on blocks):
        //0.46865 = 1.5   blocks
        //0.47179 = 1.5   blocks
        //0.49099 = 1.5   blocks
        //0.49262 = 1.625 blocks
        //0.49494 = 1.625 blocks
        //0.50000 = 1.625 blocks
        //0.50505 = 1.625 blocks
    	//0.50937 = 1.75  blocks
    	//0.51283 = 1.75  blocks
    	//0.51358 = 1.75  blocks
    	//0.52574 = 1.75  blocks
    	//0.56227 = 2     blocks
    	//...
    	//0.90143 = 4.875 blocks
    	//0.90297 = 4.875 blocks
    	//0.90805 = 4.875 blocks
    	//0.91588 = 5     blocks
    	//0.91405 = 5     blocks
    	//0.93901 = 5.25  blocks
    	//0.94306 = 5.25  blocks
        //...
        //??? = 5.5 blocks (max according to the Wiki)
    	
    	//simulate gravity and air resistance to determine the jump height
    	double yVelocity = horse.func_110215_cj();	//horses's jump strength attribute
    	double jumpHeight = 0;
    	while (yVelocity > 0)
    	{
    		jumpHeight += yVelocity;
    		yVelocity -= 0.08;
    		yVelocity *= 0.98;
    	}
    	return jumpHeight;
    }

    /**
     * Gets an entity's max hit points
     * @param entity
     * @return e.x. Steve = 20 hit points
     */
    private static int GetEntityMaxHP(EntityLivingBase entity)
    {
        return (int) entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111125_b();
    }

    /**
     * Gets the max hearts an entity has
     * @param entity
     * @return e.x. Steve = 20 hit points
     */
    private static int GetEntityMaxHearts(EntityLivingBase entity)
    {
        return (int) Math.round(entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111125_b() / 2);
    }

    /**
     * Gets an entity's max run speed in meters(blocks) per second
     * @param entity
     * @return e.x. Steve = 4.3 m/s. Horses ~7-13
     */
    private	 static double GetEntityMaxSpeed(EntityLivingBase entity)
    {
        //Steve has a movement speed of 0.1 and walks 4.3 blocks per second,
        //so multiply this result by 43 to convert to blocks per second
        //testing data:
        //0.174999	~7.69 m/s
        //0.19427	~8.333 m/s
        //0.1		~4.3-4.5 m/s
        return entity.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111125_b() * 43;
    }
    

    
    /**
     * Increments the Clock mode
     * @return The new Clock mode
     */
    public static int ToggleMode()
    {
    	Mode++;
    	if(Mode >= NumberOfModes)
    		Mode = 0;
    	return Mode;
    }
}
