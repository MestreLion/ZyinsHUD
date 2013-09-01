package zyin.zyinhud;

import zyin.zyinhud.util.FontCode;
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
     * @return if the Distance Measurer is enabled, the string "[FarthestHorizontalDistance]" or
     * "[x, z, y (absolute)]" is returned, otherwise "".
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
                double farthestHorizontalDistance = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
                String x = String.format("%1$,.1f", deltaX);
                String y = String.format("%1$,.1f", deltaY);
                String z = String.format("%1$,.1f", deltaZ);
                String distance = String.format("%1$,.1f", delta);
                String farthestHorizontalDistanceStr = String.format("%1$,.1f", farthestHorizontalDistance);

                if (ZyinHUD.DistanceMeasurerMode == 1)
                {
                    distanceMeasurerString = FontCode.AQUA + "[" + farthestHorizontalDistanceStr + "]";
                }
                else if (ZyinHUD.DistanceMeasurerMode == 2)
                {
                    distanceMeasurerString = FontCode.AQUA + "[" + x + ", " + z + ", " + y + " (" + distance + ")]";
                }
                else
                {
                    distanceMeasurerString = FontCode.AQUA + "[???]";
                }
            }
            else
            {
                distanceMeasurerString = FontCode.AQUA + "[far]";
            }

            return distanceMeasurerString + InfoLine.SPACER;
        }

        return "";
    }
}
