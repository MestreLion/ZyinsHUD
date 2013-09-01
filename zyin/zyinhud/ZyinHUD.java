/*
 * IDEAS
 * =====
 * //Ctrl + click item to move it into the crafting area (cant figure out how to get right click hook)
 * //dont render other player's armor (or just helmet?) in multiplayer
 * horse tracker integrated into player locator, uses the horses name to render on the screen
 */

package zyin.zyinhud;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

import org.lwjgl.input.Keyboard;

import zyin.zyinhud.keyhandler.DistanceMeasurerKeyHandler;
import zyin.zyinhud.keyhandler.EatingAidKeyHandler;
import zyin.zyinhud.keyhandler.EnderPearlAidKeyHandler;
import zyin.zyinhud.keyhandler.HorseInfoKeyHandler;
import zyin.zyinhud.keyhandler.PlayerLocatorKeyHandler;
import zyin.zyinhud.keyhandler.PotionAidKeyHandler;
import zyin.zyinhud.keyhandler.SafeOverlayKeyHandler;
import zyin.zyinhud.keyhandler.WeaponSwapperKeyHandler;
import zyin.zyinhud.tickhandler.HUDTickHandler;
import zyin.zyinhud.tickhandler.RenderTickHandler;
import zyin.zyinhud.util.Localization;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ZyinHUD", name = "Zyin's HUD", version = "0.10.1")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ZyinHUD
{
    /**
     * Comma seperated values of languages to load by setting the default value in the config file.
     * Recreate the config file, or just the variable "SupportedLanguages" (located in the config file)
     * to see these values updated.
     */
    private static final String DefaultSupportedLanguages = "en_US"; //"en_US, zh_CN";
    
    public static final String CATEGORY_LANGUAGE = "language";
    public static final String CATEGORY_INFOLINE = "infoline";
    public static final String CATEGORY_COORDINATES = "coordinates";
    public static final String CATEGORY_COMPASS = "compass";
    public static final String CATEGORY_DISTANCEMEASURER = "distancemeasurer";
    public static final String CATEGORY_DURABILITYINFO = "durabilityinfo";
    public static final String CATEGORY_SAFEOVERLAY = "safeoverlay";
    public static final String CATEGORY_POTIONTIMERS = "potiontimers";
    public static final String CATEGORY_PLAYERLOCATOR = "playerlocator";
    public static final String CATEGORY_EATINGAID = "eatingaid";
    public static final String CATEGORY_WEAPONSWAP = "weaponswap";
    public static final String CATEGORY_FPS = "fps";
    public static final String CATEGORY_HORSEINFO = "horseinfo";
    public static final String CATEGORY_ENDERPEARLAID = "enderpearlaid";
    public static final String CATEGORY_CLOCK = "clock";
    public static final String CATEGORY_POTIONAID = "potionaid";

    //Configurable values - language
    public static String SupportedLanguages;
    
    //Configurable values - info line
    public static boolean ShowInfoLine;

    //Configurable values - coordinates
    public static boolean ShowCoordinates;
    public static boolean UseYCoordinateColors;

    //Configurable values - compass
    public static boolean ShowCompass;

    //Configurable values - distance measurer
    public static String DistanceMeasurerHotkey;

    //Configurable values - durability info
    public static boolean ShowDurabilityInfo;
    public static boolean ShowArmorDurability;
    public static boolean ShowItemDurability;
    public static int DurabilityUpdateFrequency;
    public static double DurabilityDisplayThresholdForArmor;
    public static double DurabilityDisplayThresholdForItem;
    public static int DurabilityLocationHorizontal;
    public static int DurabilityLocationVertical;

    //Configurable values - safe overlay
    public static String SafeOverlayHotkey;
    public static int SafeOverlayDrawDistance;
    public static double SafeOverlayTransparency;
    public static boolean SafeOverlayDisplayInNether;
    public static boolean SafeOverlaySeeThroughWalls;

    //Configurable values - potion timers
    public static boolean ShowPotionTimers;

    //Configurable values - player locator
    public static String PlayerLocatorHotkey;
    public static boolean ShowDistanceToPlayers;
    public static int PlayerLocatorMinViewDistance;

    //Configurable values - eating aid
    public static String EatingAidHotkey;
    public static boolean EnableEatingAid;
    public static boolean DontEatGoldenFood;
    public static boolean PrioritizeFoodInHotbar;
    public static int EatingAidMode = 0;	//0=basic, 1=intelligent

    //Configurable values - weapon swap
    public static String WeaponSwapHotkey;
    public static boolean EnableWeaponSwap;
    public static boolean ScanHotbarForWeaponsFromLeftToRight;

    //Configurable values - fps
    public static boolean ShowFPS;

    //Configurable values - horse info
    public static String HorseInfoHotkey;
    public static boolean ShowHorseStatsOnF3Menu;
    public static int HorseInfoMaxViewDistance;

    //Configurable values - ender pearl aid
    public static String EnderPearlAidHotkey;
    public static boolean EnableEnderPearlAid;

    //Configurable values - clock
    public static boolean ShowClock;

    //Configurable values - potion aid
    public static String PotionAidHotkey;
    public static boolean EnablePotionAid;
    
    
    //Key bindings
    private static KeyBinding[] key_K;
    private static KeyBinding[] key_L;
    private static KeyBinding[] key_P;
    private static KeyBinding[] key_G;
    private static KeyBinding[] key_F;
    private static KeyBinding[] key_O;
    private static KeyBinding[] key_C;
    private static KeyBinding[] key_V;
    
    
    //default hotkeys
    private static String DefaultDistanceMeasurerHotkey = "K";
    private static String DefaultSafeOverlayHotkey = "L";
    private static String DefaultPlayerLocatorHotkey = "P";
    private static String DefaultEatingAidHotkey = "G";
    private static String DefaultWeaponSwapHotkey = "F";
    private static String DefaultHorseInfoHotkey = "O";
    private static String DefaultEnderPearlAidHotkey = "C";
    private static String DefaultPotionAidHotkey = "V";
    
    
    //the current state of various mods (everything is turned off initially)
    public static int DistanceMeasurerMode = 0;	//0=off, 1=simple, 2=complex
    public static int SafeOverlayMode = 0;		//0=off, 1=on
    public static int PlayerLocatorMode = 0;	//0=off, 1=on
    public static int HorseInfoMode = 0;		//0=off, 1=on
    public static int ClockMode = 0;			//0=standard clock, 1=time till night/day
    
    
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
        
        Localization.loadLanguages("/lang/zyinhud/", GetSupportedLanguages());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        
        MinecraftForge.EVENT_BUS.register(RenderTickHandler.instance);	//needed for @ForgeSubscribe method subscriptions
        
        TickRegistry.registerTickHandler(new HUDTickHandler(), Side.CLIENT);
        
    	LoadKeyHandlers();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
    	//this event is not called on SMP worlds, which means we change the config file
    	//while in game
    	UpdateConfigFileWithModifiedValues();
    	UpdateConfigFileWithModifiedHotkeys();
        config.save();
    }

    
    private String[] GetSupportedLanguages()
    {
    	return SupportedLanguages.replace(" ","").split(",");
    }
    
    
    /**
     * If the user changes any values during gameplay, then update our config file.
     */
    private void UpdateConfigFileWithModifiedValues()
    {
    	Property p;
    	
        //Safe Overlay
        p = config.get(ZyinHUD.CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 20);
        p.comment = "How far away unsafe spots should be rendered around the player measured in blocks. This can be changed in game.";
        p.set(SafeOverlay.instance.getDrawDistance());
        
        p = config.get(ZyinHUD.CATEGORY_SAFEOVERLAY, "SafeOverlaySeeThroughWalls", 20);
        p.comment = "Enable/Disable showing unsafe areas through walls. Toggle in game with Ctrl + L.";
        p.set(SafeOverlay.instance.getSeeUnsafePositionsThroughWalls());
    }
    
    /**
     * If the user changes any hotkeys in the Options > Controls menu in game, then update our config file.
     */
    private void UpdateConfigFileWithModifiedHotkeys()
    {
    	Property p;
    	String hotkey;
    	
    	hotkey = Keyboard.getKeyName(key_K[0].keyCode);
        p = config.get(CATEGORY_DISTANCEMEASURER, "DistanceMeasurerHotkey", DefaultDistanceMeasurerHotkey);
        p.comment = "Default: " + DefaultDistanceMeasurerHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_L[0].keyCode);
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeoverlayHotkey", DefaultSafeOverlayHotkey);
        p.comment = "Default: " + DefaultSafeOverlayHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_P[0].keyCode);
        p = config.get(CATEGORY_PLAYERLOCATOR, "PlayerLocatorHotkey", DefaultPlayerLocatorHotkey);
        p.comment = "Default: " + DefaultPlayerLocatorHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_G[0].keyCode);
        p = config.get(CATEGORY_EATINGAID, "EatingAidHotkey", DefaultEatingAidHotkey);
        p.comment = "Default: " + DefaultEatingAidHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_F[0].keyCode);
        p = config.get(CATEGORY_WEAPONSWAP, "WeaponSwapHotkey", DefaultWeaponSwapHotkey);
        p.comment = "Default: " + DefaultWeaponSwapHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_O[0].keyCode);
        p = config.get(CATEGORY_HORSEINFO, "HorseInfoHotkey", DefaultHorseInfoHotkey);
        p.comment = "Default: " + DefaultHorseInfoHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_C[0].keyCode);
        p = config.get(CATEGORY_ENDERPEARLAID, "EnderPearlAidHotkey", DefaultEnderPearlAidHotkey);
        p.comment = "Default: " + DefaultEnderPearlAidHotkey;
        p.set(hotkey);
    	
    	hotkey = Keyboard.getKeyName(key_V[0].keyCode);
        p = config.get(CATEGORY_POTIONAID, "PotionAidHotkey", DefaultPotionAidHotkey);
        p.comment = "Default: " + DefaultPotionAidHotkey;
        p.set(hotkey);
	}
    

    private void LoadConfigSettings(File configFile)
    {
        //load configuration settings
    	//NOTE: doing config.save() multiple times will bug out and add additional quotes to
    	//categories with more than 1 word
        config = new Configuration(configFile);
        config.load();
        Property p;
        

        config.addCustomCategoryComment(CATEGORY_LANGUAGE, "Language support for other languages (you probably wont ever need to change this)");
        config.addCustomCategoryComment(CATEGORY_INFOLINE, "Info Line displays the status of other features in the top left corner of the screen.");
        config.addCustomCategoryComment(CATEGORY_COORDINATES, "Coordinates displays your coordinates. Nuff said.");
        config.addCustomCategoryComment(CATEGORY_COMPASS, "Compass displays a text compass.");
        config.addCustomCategoryComment(CATEGORY_DISTANCEMEASURER, "Distance Measurer can calculate distances between you and blocks that you aim at.");
        config.addCustomCategoryComment(CATEGORY_DURABILITYINFO, "Durability Info will display your breaking armor and equipment.");
        config.addCustomCategoryComment(CATEGORY_SAFEOVERLAY, "Safe Overlay shows you which blocks are dark enough to spawn mobs.");
        config.addCustomCategoryComment(CATEGORY_POTIONTIMERS, "Potion Timers shows the duration remaining on potions that you drink.");
        config.addCustomCategoryComment(CATEGORY_PLAYERLOCATOR, "Player Locator gives you a radar-like ability to easily see where other people are.");
        config.addCustomCategoryComment(CATEGORY_EATINGAID, "Eating Aid makes eating food quick and easy.");
        config.addCustomCategoryComment(CATEGORY_WEAPONSWAP, "Weapon Swap allows you to quickly select your sword and bow.");
        config.addCustomCategoryComment(CATEGORY_FPS, "FPS shows your frames per second without having to go into the F3 menu.");
        config.addCustomCategoryComment(CATEGORY_HORSEINFO, "Horse Info gives you information about horse stats, such as speed and jump height.");
        config.addCustomCategoryComment(CATEGORY_ENDERPEARLAID, "Ender Pearl Aid makes it easier to quickly throw ender pearls.");
        config.addCustomCategoryComment(CATEGORY_CLOCK, "Clock shows you time relevant to Minecraft time.");
        config.addCustomCategoryComment(CATEGORY_POTIONAID, "Potion Aid helps you quickly drink potions based on your circumstance.");
        
        
        //CATEGORY_LANGUAGE
        p = config.get(CATEGORY_LANGUAGE, "SupportedLanguages", DefaultSupportedLanguages);
        p.comment = "Languages must be added here in order to get loaded, in addition to adding a .properties file at /lang/zyinhud/. Values are comma seperated.";
        SupportedLanguages = p.getString();
        
        
        //CATEGORY_INFOLINE
        p = config.get(CATEGORY_INFOLINE, "ShowInfoLine", true);
        p.comment = "Enable/Disable the entire info line in the top left part of the screen. This includes coordinates, compass, mod status, etc.";
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
        

        //CATEGORY_DISTANCEMEASURER
        p = config.get(CATEGORY_DISTANCEMEASURER, "DistanceMeasurerHotkey", DefaultDistanceMeasurerHotkey);
        p.comment = "Default: " + DefaultDistanceMeasurerHotkey;
        DistanceMeasurerHotkey = p.getString();
        
        
        //CATEGORY_DURABILITYINFO
        p = config.get(CATEGORY_DURABILITYINFO, "ShowDurabilityInfo", true);
        p.comment = "Enable/Disable showing all durability info.";
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
        p.comment = "Update the HUD every XX render ticks (60 = 1 second at 60 fps)";
        DurabilityUpdateFrequency = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationHorizontal", 20);
        p.comment = "The horizontal position of the durability icons. 0 is left, 400 is far right.";
        DurabilityLocationHorizontal = p.getInt();
        
        p = config.get(CATEGORY_DURABILITYINFO, "DurabilityLocationVertical", 20);
        p.comment = "The vertical position of the durability icons. 0 is top, 200 is very bottom.";
        DurabilityLocationVertical = p.getInt();
        
        
        //CATEGORY_SAFEOVERLAY
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayHotkey", DefaultSafeOverlayHotkey);
        p.comment = "Default: "+DefaultSafeOverlayHotkey;
        SafeOverlayHotkey = p.getString();
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 20);
        p.comment = "How far away unsafe spots should be rendered around the player measured in blocks. This can be changed in game with - + "+DefaultSafeOverlayHotkey+" and + + "+DefaultSafeOverlayHotkey+".";
        SafeOverlayDrawDistance = p.getInt(20);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayTransparency", 0.3);
        p.comment = "The transparency of the unsafe marks. Must be between greater than 0.1 and less than or equal to 1.";
        SafeOverlayTransparency = p.getDouble(0.3);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDisplayInNether", false);
        p.comment = "Enable/Disable showing unsafe areas in the Nether.";
        SafeOverlayDisplayInNether = p.getBoolean(false);
        
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlaySeeThroughWalls", false);
        p.comment = "Enable/Disable showing unsafe areas through walls. Toggle in game with Ctrl + "+DefaultSafeOverlayHotkey+".";
        SafeOverlaySeeThroughWalls = p.getBoolean(false);
        
        
        //CATEGORY_POTIONTIMERS
        p = config.get(CATEGORY_POTIONTIMERS, "ShowPotionTimers", true);
        p.comment = "Enable/Disable showing the time remaining on potions.";
        ShowPotionTimers = p.getBoolean(true);
        
        
        //CATEGORY_PLAYERLOCATOR
        p = config.get(CATEGORY_PLAYERLOCATOR, "PlayerLocatorHotkey", DefaultPlayerLocatorHotkey);
        p.comment = "Default: "+DefaultPlayerLocatorHotkey;
        PlayerLocatorHotkey = p.getString();
        
        p = config.get(CATEGORY_PLAYERLOCATOR, "ShowDistanceToPlayers", false);
        p.comment = "Show how far away you are from the other players next to their name.";
        ShowDistanceToPlayers = p.getBoolean(false);
        
        p = config.get(CATEGORY_PLAYERLOCATOR, "PlayerLocatorMinViewDistance", 10);
        p.comment = "Stop showing player names when they are this close (distance measured in blocks).";
        PlayerLocatorMinViewDistance = p.getInt(10);
        
        
        //CATEGORY_EATINGAID
        p = config.get(CATEGORY_EATINGAID, "EatingAidHotkey", DefaultEatingAidHotkey);
        p.comment = "Default: "+DefaultEatingAidHotkey;
        EatingAidHotkey = p.getString();
        
        p = config.get(CATEGORY_EATINGAID, "EnableEatingAid", true);
        p.comment = "Enables pressing a hotkey (default="+DefaultEatingAidHotkey+") to eat food even if it is  in your inventory and not your hotbar.";
        EnableEatingAid = p.getBoolean(true);
        
        p = config.get(CATEGORY_EATINGAID, "DontEatGoldenFood", true);
        p.comment = "Enable/Disable using golden apples and golden carrots as food.";
        DontEatGoldenFood = p.getBoolean(true);
        
        p = config.get(CATEGORY_EATINGAID, "PrioritizeFoodInHotbar", false);
        p.comment = "Use food that is in your hotbar before looking for food in your main inventory.";
        PrioritizeFoodInHotbar = p.getBoolean(false);
        
        p = config.get(CATEGORY_EATINGAID, "EatingAidMode", 1);
        p.comment = "Set the eating aid mode:" + config.NEW_LINE +
					"0 = always eat food with the highest saturation value" + config.NEW_LINE +
					"1 = intelligently select food so that you don't overeat and waste anything";
        EatingAidMode = p.getInt(1);
        
        
        //CATEGORY_WEAPONSWAP
        p = config.get(CATEGORY_WEAPONSWAP, "WeaponSwapHotkey", DefaultWeaponSwapHotkey);
        p.comment = "Default: "+DefaultWeaponSwapHotkey;
        WeaponSwapHotkey = p.getString();
        
        p = config.get(CATEGORY_WEAPONSWAP, "EnableWeaponSwap", true);
        p.comment = "Enables pressing a hotkey (default="+DefaultWeaponSwapHotkey+") to swap between your sword and bow.";
        EnableWeaponSwap = p.getBoolean(true);
        
        p = config.get(CATEGORY_WEAPONSWAP, "ScanHotbarForWeaponsFromLeftToRight", true);
        p.comment = "Set to false to scan the hotbar for swords and bows from right to left. Only matters if you have multiple swords/bows in your hotbar.";
        ScanHotbarForWeaponsFromLeftToRight = p.getBoolean(true);
        
        
        //CATEGORY_FPS
        p = config.get(CATEGORY_FPS, "ShowFPS", false);
        p.comment = "Enable/Disable showing your FPS at the end of the Info Line.";
        ShowFPS = p.getBoolean(false);
        
        
        //CATEGORY_HORSEINFO
        p = config.get(CATEGORY_HORSEINFO, "HorseInfoHotkey", DefaultHorseInfoHotkey);
        p.comment = "Default: "+DefaultHorseInfoHotkey;
        HorseInfoHotkey = p.getString();
        
        p = config.get(CATEGORY_HORSEINFO, "ShowHorseStatsOnF3Menu", true);
        p.comment = "Enable/Disable showing the stats of the horse you're riding on the F3 screen.";
        ShowHorseStatsOnF3Menu = p.getBoolean(true);
        
        p = config.get(CATEGORY_HORSEINFO, "HorseInfoMaxViewDistance", 8);
        p.comment = "How far away horse stats will be rendered on the screen (distance measured in blocks).";
        HorseInfoMaxViewDistance = p.getInt(8);
        
        
        //CATEGORY_ENDERPEARLAID
        p = config.get(CATEGORY_ENDERPEARLAID, "EnderPearlAidHotkey", DefaultEnderPearlAidHotkey);
        p.comment = "Default: "+DefaultEnderPearlAidHotkey;
        EnderPearlAidHotkey = p.getString();
        
        p = config.get(CATEGORY_ENDERPEARLAID, "EnableEnderPearlAid", true);
        p.comment = "Enables pressing a hotkey (default="+DefaultEnderPearlAidHotkey+") to use an enderpearl even if it is  in your inventory and not your hotbar.";
        EnableEnderPearlAid = p.getBoolean(true);
        
        
        //CATEGORY_CLOCK
        p = config.get(CATEGORY_CLOCK, "ShowClock", true);
        p.comment = "Enable/Disable showing the clock.";
        ShowClock = p.getBoolean(true);
        
        p = config.get(CATEGORY_CLOCK, "ClockMode", 0);
        p.comment = "Set the clock mode:" + config.NEW_LINE +
        			"0 = standard Minecraft time in game" + config.NEW_LINE +
        			"1 = countdown timer till morning/night.";
        ClockMode = p.getInt(0);

        
        //CATEGORY_POTIONAID
        p = config.get(CATEGORY_POTIONAID, "PotionAidHotkey", DefaultPotionAidHotkey);
        p.comment = "Default: " + DefaultPotionAidHotkey;
        PotionAidHotkey = p.getString();
        
        p = config.get(CATEGORY_POTIONAID, "EnablePotionAid", true);
        p.comment = "Enables pressing a hotkey (default="+DefaultPotionAidHotkey+") to drink a potion even if it is  in your inventory and not your hotbar.";
        EnablePotionAid = p.getBoolean(true);
        
        

        config.save();
    }


    private void LoadKeyHandlers()
    {
        //Key Bind Handlers (for hotkeys) are defined here
        boolean[] repeatFalse = {false};
        boolean[] repeatTrue = {true};
        
        int hotkey;
        
        hotkey = GetKeyboardKeyFromString(DistanceMeasurerHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultDistanceMeasurerHotkey) : hotkey;
        key_K = new KeyBinding[] {new KeyBinding("Distance Measurer Toggle", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new DistanceMeasurerKeyHandler(key_K, repeatFalse));

        hotkey = GetKeyboardKeyFromString(SafeOverlayHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultSafeOverlayHotkey) : hotkey;
        key_L = new KeyBinding[] {new KeyBinding("Safe Overlay Toggle", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new SafeOverlayKeyHandler(key_L, repeatTrue));

        hotkey = GetKeyboardKeyFromString(PlayerLocatorHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultPlayerLocatorHotkey) : hotkey;
        key_P = new KeyBinding[] {new KeyBinding("Player Locator Toggle", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new PlayerLocatorKeyHandler(key_P, repeatFalse));

        hotkey = GetKeyboardKeyFromString(EatingAidHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultEatingAidHotkey) : hotkey;
        key_G = new KeyBinding[] {new KeyBinding("Eating Aid Hotkey", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new EatingAidKeyHandler(key_G, repeatFalse));

        hotkey = GetKeyboardKeyFromString(WeaponSwapHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultWeaponSwapHotkey) : hotkey;
        key_F = new KeyBinding[] {new KeyBinding("Weapon Swap Hotkey", 	hotkey)};
        KeyBindingRegistry.registerKeyBinding(new WeaponSwapperKeyHandler(key_F, repeatFalse));

        hotkey = GetKeyboardKeyFromString(HorseInfoHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultHorseInfoHotkey) : hotkey;
        key_O = new KeyBinding[] {new KeyBinding("Horse Info Hotkey", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new HorseInfoKeyHandler(key_O, repeatFalse));

        hotkey = GetKeyboardKeyFromString(EnderPearlAidHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultEnderPearlAidHotkey) : hotkey;
        key_C = new KeyBinding[] {new KeyBinding("Ender Pearl Aid Hotkey", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new EnderPearlAidKeyHandler(key_C, repeatFalse));

        hotkey = GetKeyboardKeyFromString(PotionAidHotkey);
        hotkey = (hotkey == 0) ? Keyboard.getKeyIndex(DefaultPotionAidHotkey) : hotkey;
        key_V = new KeyBinding[] {new KeyBinding("Potion Aid Hotkey", hotkey)};
        KeyBindingRegistry.registerKeyBinding(new PotionAidKeyHandler(key_V, repeatFalse));
    }
    
    /**
     * Converts the string representation of a key into an integer.
     * @param key example: "L", "G", "NUMPAD2"
     * @return an integer representation of this key
     */
    private static int GetKeyboardKeyFromString(String key)
    {
    	key = key.trim();
    	int keyIndex = Keyboard.getKeyIndex(key.toUpperCase());
    	if(keyIndex == 0)
    	{
    		System.out.println("=========================================================================");
    		System.out.println("[WARNING] ZyinHUD.cfg: \"" + key + "\" is not a valid hotkey! Setting to default.");
    		System.out.println("=========================================================================");
    	}
    	return keyIndex;
    }
}
