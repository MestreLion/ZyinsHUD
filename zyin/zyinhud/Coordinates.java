package zyin.zyinhud;

import zyin.zyinhud.util.FontCode;

import net.minecraft.client.Minecraft;

/**
 * The Coordinates calculates the player's position.
 */
public class Coordinates
{
	private static Minecraft mc = Minecraft.getMinecraft();


    /**
     * Calculates the players coordinates
     * @return "(x, z, y)" coordinates formatted string if the Coordinates are enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        if (ZyinHUD.ShowCoordinates)
        {
            int coordX = mc.thePlayer.getPlayerCoordinates().posX;
            int coordY = mc.thePlayer.getPlayerCoordinates().posY;
            int coordZ = mc.thePlayer.getPlayerCoordinates().posZ;
            String coordinatesString = FontCode.WHITE + "[" + coordX + ", " + coordZ + ", " + coordY + "]";
            return coordinatesString + InfoLine.SPACER;
        }

        return "";
    }
}
