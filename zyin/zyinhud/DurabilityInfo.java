package zyin.zyinhud;

import zyin.zyinhud.util.FontCodes;

import zyin.zyinhud.util.FontCodes;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.Icon;

import org.lwjgl.opengl.GL11;

/**
 * Durability Info checks to see if any equipment (items in the hotbar, and armor) is damaged
 * and then displays info about them onto the HUD.
 */
public class DurabilityInfo
{
    protected static final ResourceLocation RESOURCE_DURABILITY_ICONS_PNG = new ResourceLocation("textures/durability_icons.png");

    //U and V is the top left part of the image
    //X and Y is the width and height of the image
    protected static int armorDurabilityU = 0;
    protected static int armorDurabilityV = 0;
    protected static int armorDurabilityX = 1 * 16;
    protected static int armorDurabilityY = 2 * 16;

    //the height/width of the tools being rendered
    protected static int toolX = 1 * 16;
    protected static int toolY = 1 * 16;

    //where the armor icon is rendered
    public static int durabalityLocX = ZyinHUD.DurabilityLocationHorizontal;
    public static int durabalityLocY = ZyinHUD.DurabilityLocationVertical;

    //where the tool icons are rendered
    public static int equipmentLocX = durabalityLocX + armorDurabilityX;
    public static int equipmentLocY = durabalityLocY;

    private static Minecraft mc = Minecraft.getMinecraft();
    private static ArrayList<ItemStack> damagedItemsList = new ArrayList<ItemStack>(13);	//used to push items into the list of broken equipment to render
    private static final RenderItem itemRenderer = new RenderItem();
    private static final GuiIngame gig = new GuiIngame(mc);
    private static final TextureManager textureManager = mc.func_110434_K();

    private static int renderTickCount = 0;

    /**
     * Renders the main durability icon and any damaged tools onto the screen.
     */
    public static void RenderOntoHUD()
    {
        //if the player is in the world
        //and not in a menu
        //and F3 not shown
        if (ZyinHUD.ShowDurabilityInfo &&
                (mc.inGameHasFocus || mc.currentScreen == null || (mc.currentScreen instanceof GuiChat))
                && !mc.gameSettings.showDebugInfo)
        {
            //don't waste time recalculating things every tick
            if (renderTickCount % ZyinHUD.DurabilityUpdateFrequency == 0)	//default: 1 out of every 50 ticks
            {
                CalculateDurabilityIcons();
            }

            Boolean armorExists = false;

            for (ItemStack toolStack : damagedItemsList)
            {
                if (toolStack.getItem() instanceof ItemArmor)
                {
                    armorExists = true;
                }
            }

            int numTools = 0;

            for (ItemStack toolStack : damagedItemsList)
            {
                Item tool = toolStack.getItem();

                //if this tool is an armor
                if (tool instanceof ItemArmor)
                {
                    if (ZyinHUD.ShowArmorDurability)
                    {
                        GL11.glDisable(GL11.GL_LIGHTING);	//disable lighting so it renders at full brightness
                        //TODO: glBindTexture
                        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(DURABILITY_ICONS_PNG));	//ORIGINAL
                        //bind texture
                        textureManager.func_110577_a(RESOURCE_DURABILITY_ICONS_PNG);
                        GL11.glColor4f(255f, 255f, 255f, 255f);	//fixes transparency issue when a InfoLine Notification is displayed
                        gig.drawTexturedModalRect(durabalityLocX, durabalityLocY, armorDurabilityU, armorDurabilityV, armorDurabilityX, armorDurabilityY);
                    }
                }
                else //if this tool is an equipment/tool
                {
                    if (ZyinHUD.ShowItemDurability)
                    {
                        int verticalPadding = 0;
                        int verticalSpacer = equipmentLocY + (numTools * toolY) + verticalPadding;
                        int horizontalPosition = durabalityLocX;

                        if (armorExists && ZyinHUD.ShowArmorDurability)
                        {
                            horizontalPosition = equipmentLocX;    //if armor is being rendered then push this to the right
                        }

                        //render the item with enchant effect
                        itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, toolStack, horizontalPosition, verticalSpacer);
                        //render the item's durability bar
                        itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, toolStack, horizontalPosition, verticalSpacer);
                        String damage = "" + (toolStack.getMaxDamage() - toolStack.getItemDamage());
                        int damageX = (horizontalPosition) + toolX / 2;
                        int damageY = (verticalSpacer) + toolY - 9;
                        GL11.glDisable(GL11.GL_LIGHTING);	//this is needed because the itemRenderer.renderItem() method enables lighting
                        mc.fontRenderer.setUnicodeFlag(true);
                        mc.fontRenderer.drawStringWithShadow(damage, damageX, damageY, 0xffffff);
                        mc.fontRenderer.setUnicodeFlag(false);
                        //GL11.glEnable(GL11.GL_LIGHTING); 		not needed
                        numTools++;
                    }
                }
            }

            //TODO: resetBoundTexture
            //mc.renderEngine.resetBoundTexture();
            renderTickCount++;
        }
    }

    /**
     * Finds items in the players hot bar and equipped armor that is damaged and adds them to the damagedItemsList list.
     */
    protected static void CalculateDurabilityIcons()
    {
        //if the player is in the world
        //and not in a menu
        //and not typing
        if ((mc.inGameHasFocus || mc.currentScreen == null || (mc.currentScreen instanceof GuiChat))
                && !mc.gameSettings.keyBindPlayerList.pressed)
        {
            damagedItemsList.clear();
            CalculateDurabilityIconsForItems();
            CalculateDurabilityIconsForArmor();
        }
    }

    /**
     * Examines the players first 9 inventory slots (the players inventory) and sees if any tools are damaged.
     * It adds damaged tools to the damagedItemsList list.
     */
    private static void CalculateDurabilityIconsForItems()
    {
        ItemStack[] items = mc.thePlayer.inventory.mainInventory;

        for (int i = 0; i < 9; i++)
        {
            ItemStack itemStack = items[i];

            if (itemStack != null)
            {
                Item item = itemStack.getItem();

                if (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemHoe)
                {
                    int itemDamage = itemStack.getItemDamage();
                    int maxDamage = itemStack.getMaxDamage();

                    if (maxDamage != 0 &&
                            (double)itemDamage / maxDamage > ZyinHUD.DurabilityDisplayThresholdForItem)
                    {
                        damagedItemsList.add(itemStack);
                    }
                }
            }
        }
    }

    /**
     * Examines the players current armor and sees if any of them are damaged.
     * It adds damaged armors to the damagedItemsList list.
     */
    private static void CalculateDurabilityIconsForArmor()
    {
        ItemStack[] armorStacks = mc.thePlayer.inventory.armorInventory;

        for (ItemStack armorStack : armorStacks)
        {
            if (armorStack != null)
            {
                int itemDamage = armorStack.getItemDamage();
                int maxDamage = armorStack.getMaxDamage();

                if (maxDamage != 0 &&
                        (double)itemDamage / maxDamage > ZyinHUD.DurabilityDisplayThresholdForArmor)
                {
                    damagedItemsList.add(armorStack);
                }
            }
        }
    }
}
