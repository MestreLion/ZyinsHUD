package zyin.zyinhud;

import zyin.zyinhud.util.FontCode;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;

/**
 * The Player Locator checks for nearby players and displays their name on screen wherever they are.
 */
public class PlayerLocator
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static EntityClientPlayerMP me;
    
    private static final double pi = Math.PI;

    private static final String Sprinting = FontCode.ITALICS;
    private static final float SneakingHeightScaler = 0.7f;
    private static final float RidingHeightScaler = 1.3f;
    
    /**
     * Renders only the first nearest X entities names
     */
    public static int maxOverlayMessagesRendered = 40;
    public static int maxViewDistance = 100;
    private static final int maxViewDistanceSq = (int) Math.pow(maxViewDistance, 2);	//optimization to save sqrt calculations later
    
	public static void Render()
	{
        //if the player is in the world
        //and not looking at a menu
        //and F3 not pressed
        if (ZyinHUD.PlayerLocatorMode == 1 &&
        		(mc.inGameHasFocus || mc.currentScreen == null)
                && !mc.gameSettings.showDebugInfo)
        {
             me = mc.thePlayer;
             
             //if(world == null) return;
             
             int i = 0;
             
             //iterate over all the loaded Entity objects and find just the players
             for(Object object : mc.theWorld.loadedEntityList)
             {
            	 if(i > maxOverlayMessagesRendered)
            		 break;
            	 
            	 if((object instanceof EntityClientPlayerMP) ||
            			 !(object instanceof EntityPlayer))
            	 //if(!((Entity)object instanceof EntityCow))	//for single player testing/debugging!
            		 continue;	//we only care about other players
            	 
            	 EntityPlayer otherPlayer = (EntityPlayer)object;
            	 //EntityCow otherPlayer = (EntityCow)object;	//for single player testing/debugging!
            	 
            	 //only show entities that are close by
                 //double distanceFromMe = me.getDistanceToEntity(otherPlayer);		//expensive sqrt operation
                 double distanceFromMeSq = me.getDistanceSqToEntity(otherPlayer);	//not an expensive operation
            	 if(distanceFromMeSq > maxViewDistanceSq
        			 || distanceFromMeSq == 0) //don't do ourself!
            		 return;
            	 
            	 
                 String otherPlayerName = otherPlayer.getEntityName();
            	 String overlayMessage = otherPlayerName;
                 int overlayMessageWidth = mc.fontRenderer.getStringWidth(overlayMessage);
                 
            	 
                 
            	 /*
            	  * start calculating the angles needed to render the overlay message onto the screen
            	  * in (x,y) coordinates
            	  */
            	 double pitch = ((me.rotationPitch + 90) * Math.PI) / 180;
            	 double yaw  = ((me.rotationYaw + 90)  * Math.PI) / 180;
            	 
            	 //calculate the vector located at the player with respect to the camera's orientation
            	 double ax = Math.sin(pitch) * Math.cos(yaw);
            	 double ay = Math.cos(pitch);
            	 double az = Math.sin(pitch) * Math.sin(yaw);
            	 
            	 //Vector 3D: a - normalized vector created by the direction the player's camera is facing
            	 //Vector 3D: b - vector from the player's position to the entity's position
            	 Vec3 a = Vec3.createVectorHelper(ax, ay, az);
            	 Vec3 b = Vec3.createVectorHelper(otherPlayer.posX - me.posX, otherPlayer.posY - me.posY, otherPlayer.posZ - me.posZ);
            	 

            	 ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                 int width = res.getScaledWidth();		//~427
                 int height = res.getScaledHeight();	//~240
            	 

                 //Vec3 aNorm = a.normalize();	//a is already normalized
                 Vec3 bNorm = b.normalize();

            	 //Vector 2D: ha - only the horizontal (x, 0, z) components of vector a. it is already normalized.
            	 //Vector 2D: hb - only the horizontal (x, 0, z) components of vector b. it is normalized.
            	 //Vec2 ha = new Vec2(a.xCoord, a.zCoord);
            	 //Vec2 hb = new Vec2(bNorm.xCoord, bNorm.zCoord);
            	 
            	 
            	 //compute the horizontal angle the between the player's corsshair and the location of the entity
            	 double horizontalAngleA = Math.atan2(a.zCoord, a.xCoord);		//-pi to pi
            	 double horizontalAngleB = Math.atan2(b.zCoord, b.xCoord);		//-pi to pi
            	 double horizontalAngle = horizontalAngleA - horizontalAngleB;	//-2pi to 2pi
            	 //mc.fontRenderer.drawStringWithShadow("horizontalAngleA: " + horizontalAngleA, 1, 50, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("horizontalAngleB: " + horizontalAngleB, 1, 60, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("horizontalAngle: " + horizontalAngle, 1, 70, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("sin(horizontalAngle): " + Math.sin(horizontalAngle), 1, 80, 0xffffff);
            	 
            	//normalize the horizontal angle so that 0 -> pointing at target, pi -> pointint away, 2pi -> pointing at target
            	 if(horizontalAngle < 0)
            		 horizontalAngle += 2*pi;	
            	 
            	//compute the vertical angle the between the player's corsshair and the location of the entity
                 double verticalAngleA = Math.asin(a.yCoord);
                 double verticalAngleB = Math.asin(bNorm.yCoord);
            	 double verticalAngle = verticalAngleA - verticalAngleB;	//-pi/2 to pi/2
            	 //mc.fontRenderer.drawStringWithShadow("verticalAngleA:"+verticalAngleA, 1, 100, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("verticalAngleB:"+verticalAngleB, 1, 110, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("verticalAngle: " + verticalAngle, 1, 120, 0xffffff);
            	 //mc.fontRenderer.drawStringWithShadow("sin(verticalAngle): " + Math.sin(verticalAngle), 1, 130, 0xffffff);
            	 
            	 
            	 //the player's FOV can range from 70 to 110 degrees (70*pi/180 to 110*pi/180)
            	 double fov = mc.gameSettings.fovSetting;	//float:0 to 1, (representing 70 to 110 degrees)
            	 fov = (70 + fov*40) * pi/180;
            	 fov *= me.getFOVMultiplier();	//FOV multiplier when sprinting/flying
            	 
            	 //compute the x and y coordinates where the overlayMessage should be rendered on screen
            	 int x = (int) (Math.sin(horizontalAngle)*-2*width/pi/fov) + width/2;	//not sure exactly why this works, but it does (almost exactly)
            	 int y = (int) (Math.sin(verticalAngle)*height/fov) + height/2;
            	 
    			 //if we are facing away from target, snap the message to the right or left side of the screen
            	 //otherwise it gets displayed in the middle of the screen when looking away from it
            	 if(horizontalAngle > pi/2 && horizontalAngle <= pi)
            		 x = 0;
            	 else if(horizontalAngle < 3*pi/2 && horizontalAngle > pi)
            		 x = width - overlayMessageWidth;
            	 
            	 //check if the text is attempting to render outside of the screen, and if so, fix it to snap to the edge of the screen.
            	 x = (x > width-overlayMessageWidth) ? width-overlayMessageWidth : x;
            	 x = (x < 0) ? 0 : x;
            	 y = (y > height-10) ? height-10: y;
            	 y = (y < 10) ? 10 : y;	//use 10 instead of 0 so that we don't write text onto the top-left InfoLine message area
            	 
            	 //finally, render the overlayMessage!
        		 int alpha = (int) (0x33 + 0xCC*((maxViewDistanceSq-distanceFromMeSq)/maxViewDistanceSq));
        		 alpha = alpha << 24;	//turns it into the format: 0x##000000
        		 int rgb = 0xFFFFFF;
        		 int color = rgb + alpha;	//alpha:r:g:b

                 if(otherPlayer.isSprinting())
                	 overlayMessage = Sprinting + overlayMessage;
        		 if(otherPlayer.isSneaking())
        		 {
        			 //render the text slightly flattened
            		 GL11.glPushMatrix();
                     GL11.glScalef(1f, SneakingHeightScaler, 1f);	//glScalef for text rendering: (width, height, ???)
                	 mc.fontRenderer.drawStringWithShadow(overlayMessage, x, (int)(y/SneakingHeightScaler), color);
            		 GL11.glPopMatrix();
        		 }
        		 else if(otherPlayer.isRiding())
        		 {
        			 //render the text slightly taller
            		 GL11.glPushMatrix();
                     GL11.glScalef(1f, RidingHeightScaler, 1f);	//glScalef for text rendering: (width, height, ???)
                	 mc.fontRenderer.drawStringWithShadow(overlayMessage, x, (int)(y/RidingHeightScaler), color);
            		 GL11.glPopMatrix();
        		 }
        		 else
        		 {
        			 //render the overlayMessage normally
        			 mc.fontRenderer.drawStringWithShadow(overlayMessage, x, y, color);
        		 }
        		 i++;
             }
        }
	}

	
	
	/*public static double AngleBetweenTwoVectors(Vec3 a, Vec3 b)
	{
	    double crossX = a.yCoord * b.zCoord - a.zCoord * b.yCoord;
	    double crossY = a.zCoord * b.xCoord - a.xCoord * b.zCoord;
	    double crossZ = a.xCoord * b.yCoord - a.yCoord * b.xCoord;
	    double cross = Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
	    double dot = a.xCoord * b.xCoord + a.yCoord * b.yCoord + a.zCoord + b.zCoord;
	    
	    return Math.atan2(cross, dot);
	}
	public static double SignedAngleBetweenTwoVectors(Vec3 a, Vec3 b)
	{
		  
		// Get the angle in degrees between 0 and 180
		double angle = AngleBetweenTwoVectors(b, a);
		  
		// the vector perpendicular to referenceForward (90 degrees clockwise)
		// (used to determine if angle is positive or negative)
		Vec3 referenceRight = (Vec3.createVectorHelper(0, 1, 0)).crossProduct(a);
		  
		// Determine if the degree value should be negative.  Here, a positive value
		// from the dot product means that our vector is the right of the reference vector   
		// whereas a negative value means we're on the left.
		double sign = (b.dotProduct(referenceRight) > 0.0) ? 1.0: -1.0;
		  
		return sign * angle;
	}*/
	
	

    /**
     * Gets the status of the Player Locator
     * @return the string "players" if the Player Locator is enabled, otherwise "".
     */
    public static String CalculateMessageForInfoLine()
    {
        String safeOverlayString = "";

        if (ZyinHUD.PlayerLocatorMode == 0)	//off
        {
            safeOverlayString = FontCode.WHITE + "";
        }
        else if (ZyinHUD.PlayerLocatorMode == 1)	//on
        {
            safeOverlayString = FontCode.WHITE + "players";
        }
        else
        {
            safeOverlayString = FontCode.WHITE + "???";
        }

        return safeOverlayString + InfoLine.SPACER;
    }
}


