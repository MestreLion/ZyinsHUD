package zyin.zyinhud;

import net.minecraft.item.ItemEnderPearl;
import zyin.zyinhud.util.InventoryUtil;
import zyin.zyinhud.util.Localization;

/**
 * EnderPearl Aid allows the player to easily use an ender pearl on their hotbar by calling its UseEnderPearl() method.
 */
public class EnderPearlAid
{
    /**
     * Makes the player throw an ender pearl if there is one on their hotbar.
     */
    public static void UseEnderPearl()
    {
        if (EatingAid.instance.isEating())
        {
            EatingAid.instance.StopEating();    //it's not good if we have an ender pearl selected and hold right click down...
        }
        
        boolean usedEnderPearlSuccessfully = InventoryUtil.UseItem(ItemEnderPearl.class);

        if (!usedEnderPearlSuccessfully)
        {
            InfoLine.DisplayNotification(Localization.get("enderpearlaid.noenderpearls"));
        }
    }
}
