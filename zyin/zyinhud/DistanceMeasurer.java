package zyin.zyinhud;

import zyin.zyinhud.util.FontCodes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;

/**
 * The Distance Measurer calculates the distance from the player to whatever the player's
 * crosshairs is looking at.
 */
public class DistanceMeasurer
{
	private static Minecraft mc = Minecraft.getMinecraft();
	
	
	/**
     * Calculates the distance of the block the player is pointing at
     * @return if the Distance Measurer is enabled, the string "[FarthestDistance]" or
     * "[x, z, y (AbsoluteDistance)]" is returned, otherwise "".
     */
    protected static String CalculateMessageForInfoLine()
    {
        if (ZyinHUD.DistanceMeasurerMode > 0)
        {
            MovingObjectPosition objectMouseOver;
            objectMouseOver = mc.thePlayer.rayTrace(300, 1);
            String distanceMeasurerString = "";

            if (objectMouseOver != null && objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
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
                double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                
                String x = String.format("%1$,.1f", deltaX);
                String y = String.format("%1$,.1f", deltaY);
                String z = String.format("%1$,.1f", deltaZ);
                String distance = String.format("%1$,.1f", delta);
                
                double farthestHorizontalDistance = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
                double farthestDistance = Math.max(Math.abs(deltaY), farthestHorizontalDistance);
                String farthestDistanceDistanceStr = String.format("%1$,.1f", farthestDistance);

                if (ZyinHUD.DistanceMeasurerMode == 1)	//1=simple
                {
                    distanceMeasurerString = FontCodes.ORANGE + "[" + farthestDistanceDistanceStr + "]";
                }
                else if (ZyinHUD.DistanceMeasurerMode == 2)	//2=complex
                {
                    distanceMeasurerString = FontCodes.ORANGE + "[" + x + ", " + z + ", " + y + " (" + distance + ")]";
                }
                else
                {
                    distanceMeasurerString = FontCodes.ORANGE + "[???]";
                }
            }
            else
            {
                distanceMeasurerString = FontCodes.ORANGE + "[far]";
            }

            return distanceMeasurerString + InfoLine.SPACER;
        }

        return "";
    }
}
