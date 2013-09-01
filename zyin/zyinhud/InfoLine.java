package zyin.zyinhud;

import java.util.List;

import zyin.zyinhud.util.FontCodes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

/**
 * The Info Line consists of everything that gets displayed in the top-left portion
 * of the screen. It's job is to gather information about other classes and render
 * their message into the Info Line.
 */
public class InfoLine
{
    private static Minecraft mc = Minecraft.getMinecraft();

    /**
     * The padding string that is inserted between different elements of the Info Line
     */
    public static final String SPACER = " ";

    private static final int notificationDuration = 1200;	//measured in milliseconds
    private static long notificationTimer = 0;				//timer that goes from notificationDuration to 0
    private static long notificationStartTime;
    private static String notificationMessage = "";

    /**
     * Renders the on screen message consisting of everything that gets put into the top let message area,
     * including coordinates and the state of things that can be activated
     */
    public static void RenderOntoHUD()
    {
        //if the player is in the world
        //and not looking at a menu
        //and F3 not pressed
        if (ZyinHUD.ShowInfoLine &&
                (mc.inGameHasFocus || mc.currentScreen == null || (mc.currentScreen instanceof GuiChat))
                && !mc.gameSettings.showDebugInfo)
        {
            String clock = Clock.CalculateMessageForInfoLine();
            String coordinates = Coordinates.CalculateMessageForInfoLine();
            String compass = Compass.CalculateMessageForInfoLine();
            String distance = DistanceMeasurer.CalculateMessageForInfoLine();
            String safe = SafeOverlay.CalculateMessageForInfoLine();
            String locator = PlayerLocator.CalculateMessageForInfoLine();
            String horse = HorseInfo.CalculateMessageForInfoLine();
            String fps = Fps.CalculateMessageForInfoLine();
            
            String message = clock + coordinates + compass + distance + safe + locator + horse + fps;
            mc.fontRenderer.drawStringWithShadow(message, 1, 1, 0xffffff);
        }

        if (notificationTimer > 0)
        {
            RenderNotification(notificationMessage);
        }
    }

    /**
     * Displays a short notification to the user.
     * @param message the message to be displayed
     */
    public static void DisplayNotification(String message)
    {
        notificationMessage = message;
        notificationTimer = notificationDuration;
        notificationStartTime = System.currentTimeMillis();
    }

    /**
     * Renders a short message on the screen.
     * @param message the message to be displayed
     */
    private static void RenderNotification(String message)
    {
        if ((mc.inGameHasFocus || mc.currentScreen == null))
        {
            ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int width = res.getScaledWidth();		//~427
            int height = res.getScaledHeight();	//~240
            int overlayMessageWidth = mc.fontRenderer.getStringWidth(notificationMessage);
            int x = width / 2 - overlayMessageWidth / 2;
            int y = height - 65;
            double alphaLevel;	//ranges from [0..1]

            if ((double)notificationTimer * 2 / notificationDuration > 1)
            {
                alphaLevel = 1;    //for the first half of the notifications rendering we want it 100% opaque.
            }
            else
            {
                alphaLevel = (double)notificationTimer * 2 / notificationDuration;    //for the second half, we want it to fade out.
            }

            int alpha = (int)(0x33 + 0xCC * alphaLevel);
            alpha = alpha << 24;	//turns it into the format: 0x##000000
            int rgb = 0xFFFFFF;
            int color = rgb + alpha;	//alpha:r:g:b
            mc.fontRenderer.drawStringWithShadow(notificationMessage, x, y, color);
        }

        notificationTimer = notificationStartTime - System.currentTimeMillis() + notificationDuration;	//counts down from 1000 to 0
    }
}
