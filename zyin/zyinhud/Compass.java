package zyin.zyinhud;

import zyin.zyinhud.util.FontCodes;

import net.minecraft.client.Minecraft;

/**
 * The Compass determines what direction the player is facing.
 */
public class Compass
{
	private static Minecraft mc = Minecraft.getMinecraft();
	
	
	/**
     * Calculates the direction the player is facing
     * @return "[Direction]" compass formatted string if the Compass is enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        if (ZyinHUD.ShowCompass)
        {
            int yaw = (int)mc.thePlayer.rotationYaw;
            yaw += 22;	//+22 centers the compass (45degrees/2)
            yaw %= 360;
            if(yaw < 0)
            	yaw += 360;
            
            int facing = yaw / 45; //  360degrees divided by 45 == 8 zones
            String compassDirection = "";

            if (facing == 0)
                compassDirection = "S";
            else if (facing == 1)
                compassDirection = "SW";
            else if (facing == 2)
                compassDirection = "W";
            else if (facing == 3)
                compassDirection = "NW";
            else if (facing == 4)
                compassDirection = "N";
            else if (facing == 5)
                compassDirection = "NE";
            else if (facing == 6)
                compassDirection = "E";
            else// if(facing == 7)
                compassDirection = "SE";
             
            String compassString = FontCodes.GRAY + "[" + FontCodes.RED + compassDirection + FontCodes.GRAY + "]";
            return compassString + InfoLine.SPACER;
        }

        return "";
    }
    
}
