package zyin.zyinhud;

import zyin.zyinhud.util.FontCodes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;

/**
 * The Distance Measurer calculates the distance from the player to whatever the player's
 * crosshairs is looking at.
 * <p>
 * DistanceMeasurerMode = 0: "[FarthestDistance]"<br>
 * DistanceMeasurerMode = 1: "[x, z, y (AbsoluteDistance)]"
 */
public class DistanceMeasurer
{
    private static Minecraft mc = Minecraft.getMinecraft();

    /**
     * Calculates the distance of the block the player is pointing at
     * @return the distance to a block if Distance Measurer is enabled, otherwise "".
     */
    protected static String CalculateMessageForInfoLine()
    {
        if (ZyinHUD.DistanceMeasurerMode > 0)
        {
            MovingObjectPosition objectMouseOver = mc.thePlayer.rayTrace(300, 1);
            String distanceMeasurerString = "";

            if (objectMouseOver != null && objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
            {
            	double coordX = mc.thePlayer.posX;
                double coordY = mc.thePlayer.posY;
                double coordZ = mc.thePlayer.posZ;
            	
                //add 0.5 to center the coordinate into the middle of the block
                double blockX = objectMouseOver.blockX + 0.5;
                double blockY = objectMouseOver.blockY + 0.5;
                double blockZ = objectMouseOver.blockZ + 0.5;
                
                double deltaX;
                double deltaY;
                double deltaZ;

                if(coordX < blockX - 0.5)
                	deltaX = (blockX - 0.5) - coordX;
                else if(coordX > blockX + 0.5)
                	deltaX = coordX - (blockX + 0.5);
                else
                	deltaX = coordX - blockX;
                
                if(coordY < blockY - 0.5)
                	deltaY = (blockY - 0.5) - coordY;
                else if(coordY > blockY + 0.5)
                	deltaY = coordY - (blockY + 0.5);
                else
                	deltaY = coordY - blockY;
                
                if(coordZ < blockZ - 0.5)
                	deltaZ = (blockZ - 0.5) - coordZ;
                else if(coordZ > blockZ + 0.5)
                	deltaZ = coordZ - (blockZ + 0.5);
                else
                	deltaZ = coordZ - blockZ;
                

                if (ZyinHUD.DistanceMeasurerMode == 1)	//1=simple
                {
                	double farthestHorizontalDistance = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
                    double farthestDistance = Math.max(Math.abs(deltaY), farthestHorizontalDistance);
                    return FontCodes.ORANGE + "[" + String.format("%1$,.1f", farthestDistance) + "]" + InfoLine.SPACER;
                }
                else if (ZyinHUD.DistanceMeasurerMode == 2)	//2=complex
                {
                    double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                    String x = String.format("%1$,.1f", deltaX);
                    String y = String.format("%1$,.1f", deltaY);
                    String z = String.format("%1$,.1f", deltaZ);
                    return FontCodes.ORANGE + "[" + x + ", " + z + ", " + y + " (" + String.format("%1$,.1f", delta) + ")]" + InfoLine.SPACER;
                }
                else
                {
                	return FontCodes.ORANGE + "[???]" + InfoLine.SPACER;
                }
            }
            else
            {
            	return FontCodes.ORANGE + "[far]" + InfoLine.SPACER;
            }
        }

        return "";
    }
}
