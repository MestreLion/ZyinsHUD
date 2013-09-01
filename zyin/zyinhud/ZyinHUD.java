/*
 * IDEAS
 * =====
 * //Ctrl + click item to move it into the crafting area (cant figure out how to get right click hook)
 * //FPS display (no public variable in Minecraft for this)
 */

package zyin.zyinhud;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import org.lwjgl.input.Keyboard;

import zyin.zyinhud.keyhandler.DistanceMeasurerKeyHandler;
import zyin.zyinhud.keyhandler.PlayerLocatorKeyHandler;
import zyin.zyinhud.keyhandler.SafeOverlayKeyHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ZyinHUD", name = "Zyin's HUD", version = "0.5.3")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ZyinHUD
{
    public static final String CATEGORY_INFOLINE = "info line";
    public static final String CATEGORY_COORDINATES = "coordinates";
    public static final String CATEGORY_COMPASS = "compass";
    public static final String CATEGORY_DURABILITYINFO = "durability info";
    public static final String CATEGORY_SAFEOVERLAY = "safe overlay";
    public static final String CATEGORY_POTIONTIMERS = "potion timers";
    public static final String CATEGORY_PLAYERLOCATOR = "player locator";

    //Configurable values - info line
    public static Boolean ShowInfoLine;

    //Configurable values - coordinates
    public static Boolean ShowCoordinates;
    public static Boolean UseYCoordinateColors;
    
    //Configurable values - compass
    public static Boolean ShowCompass;

    //Configurable values - durability info
    public static Boolean ShowDurabilityInfo;
    public static Boolean ShowArmorDurability;
    public static Boolean ShowItemDurability;
    public static int DurabilityUpdateFrequency;
    public static double DurabilityDisplayThresholdForArmor;
    public static double DurabilityDisplayThresholdForItem;
    public static int DurabilityLocationHorizontal;
    public static int DurabilityLocationVertical;

    //Configurable values - safe overlay
    public static int SafeOverlayDrawDistance;
    public static double SafeOverlayTransparency;
    public static Boolean SafeOverlayDisplayInNether;
    public static Boolean SafeOverlaySeeThroughWalls;
    
    //Configurable values - potion timers
    public static boolean ShowPotionTimers;

    //Configurable values - player locator
    public static boolean ShowDistanceToPlayers;
    
    
    public static int PlayerLocatorMode = 0;	//0=off, 1=on
    public static int DistanceMeasurerMode = 0;	//0=off, 1=simple, 2=complex
    public static int SafeOverlayMode = 0;		//0=off, 1=on
    

    Minecraft mc = Minecraft.getMinecraft();
    public static Configuration config = null;

    @Instance("ZyinHUD")
    public static ZyinHUD instance;

    @SidedProxy(clientSide = "zyin.zyinhud.ClientProxy", serverSide = "zyin.zyinhud.CommonProxy")
    public static CommonProxy proxy;

    public ZyinHUD()
    {
    	
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LoadConfigSettings(event.getSuggestedConfigurationFile());
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        
        //needed for @ForgeSubscribe method subscriptions
        MinecraftForge.EVENT_BUS.register(SafeOverlay.instance);
        
        
        
        LoadTickHandlers();
        LoadKeyHandlers();
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	
    }

    private void LoadTickHandlers()
    {
        //Tick Handlers (for drawing on the HUD) are defined here
        TickRegistry.registerTickHandler(new HUDTickHandler(), Side.CLIENT);
    }

    private void LoadKeyHandlers()
    {
        //Key Bind Handlers (for hotkeys) are defined here
        boolean[] repeat = {false};
        
        KeyBinding[] key_K = {new KeyBinding("Distance Measurer Toggle", 	Keyboard.KEY_K)};
        KeyBindingRegistry.registerKeyBinding(new DistanceMeasurerKeyHandler(key_K, repeat));
        
        KeyBinding[] key_L = {new KeyBinding("Safe Overlay Toggle", 		Keyboard.KEY_L)};
        KeyBindingRegistry.registerKeyBinding(new SafeOverlayKeyHandler(key_L, repeat));
        
        KeyBinding[] key_P = {new KeyBinding("Player Locator Toggle", 		Keyboard.KEY_P)};
        KeyBindingRegistry.registerKeyBinding(new PlayerLocatorKeyHandler(key_P, repeat));
        
    }

    private void LoadConfigSettings(File configFile)
    {
        //load configuration settings
        Configuration config = new Configuration(configFile);
        config.load();
        Property p;
        
        
        //CATEGORY_INFOLINE
        p = config.get(CATEGORY_INFOLINE, "ShowInfoLine", true);
        p.comment = "Enable/Disable the entire info line in the top left part of the screen.";
        ShowInfoLine = p.getBoolean(true);
        
        
        //CATEGORY_COORDINATES
        p = config.get(CATEGORY_COORDINATES, "ShowCoordinates", true);
        p.comment = "Enable/Disable showing your coordinates.";
        ShowCoordinates = p.getBoolean(true);
        p = config.get(CATEGORY_COORDINATES, "UseYCoordinateColors", true);
        p.comment = "Color code the Y (height) coordinate based on what ores can spawn at that level.";
        UseYCoordinateColors = p.getBoolean(true);
        
        
        //CATEGORY_COMPASS
        p = config.get(CATEGORY_COMPASS, "ShowCompass", true);
        p.comment = "Enable/Disable showing the compass.";
        ShowCompass = p.getBoolean(true);
        
        
        //CATEGORY_DURABILITYINFO
        p = config.get(CATEGORY_DURABILITYINFO, "ShowDurabilityInfo", true);
        p.comment = "Enable/Disable durability info.";
        ShowDurabilityInfo = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "ShowArmorDurability", true);
        p.comment = "Enable/Disable showing breaking armor.";
        ShowArmorDurability = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "ShowItemDurability", true);
        p.comment = "Enable/Disable showing breaking items.";
        ShowItemDurability = p.getBoolean(true);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityDisplayThresholdForArmor", 0.9);
        p.comment = "Display when armor gets damaged more than this fraction of its durability.";
        DurabilityDisplayThresholdForArmor = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityDisplayThresholdForItem", 0.9);
        p.comment = "Display when an item gets damaged more than this fraction of its durability.";
        DurabilityDisplayThresholdForItem = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityUpdateFrequency", 50);
        p.comment = "Update the HUD every XX game ticks (~100 = 1 second)";
        DurabilityUpdateFrequency = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationHorizontal", 20);
        p.comment = "The horizontal position of the durability icons. 0 is left, 400 is far right.";
        DurabilityLocationHorizontal = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationVertical", 20);
        p.comment = "The vertical position of the durability icons. 0 is top, 200 is very bottom.";
        DurabilityLocationVertical = p.getInt();
        
        
        //CATEGORY_SAFEOVERLAY
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 20);
        p.comment = "How far away unsafe spots should be rendered around the player measured in blocks.";
        SafeOverlayDrawDistance = p.getInt(20);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayTransparency", 0.3);
        p.comment = "The transparency of the unsafe marks. Must be between greater than 0.1 and less than or equal to 1.";
        SafeOverlayTransparency = p.getDouble(0.3);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDisplayInNether", false);
        p.comment = "Enable/Disable showing unsafe areas in the Nether.";
        SafeOverlayDisplayInNether = p.getBoolean(false);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlaySeeThroughWalls", false);
        p.comment = "Enable/Disable showing unsafe areas through walls. Toggle ingame with Ctrl + L.";
        SafeOverlaySeeThroughWalls = p.getBoolean(false);
        
        //CATEGORY_POTIONTIMERS
        p = config.get(CATEGORY_POTIONTIMERS, "ShowPotionTimers", true);
        p.comment = "Enable/Disable showing the time remaining on potions.";
        ShowPotionTimers = p.getBoolean(true);
        
        
        //CATEGORY_PLAYERLOCATOR
        p = config.get(CATEGORY_PLAYERLOCATOR, "ShowDistanceToPlayers", false);
        p.comment = "Show how far away you are from the other players next to their name.";
        ShowDistanceToPlayers = p.getBoolean(false);
        
        
        
        config.save();
        this.config = config;	//so other classes can reference this config file
    }
    
    
}