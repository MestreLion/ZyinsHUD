package zyin.zyinhud.gui.buttons;

import zyin.zyinhud.mods.HorseInfo;

public class GuiHorseInfoHotkeyButton extends GuiHotkeyButton
{
	public GuiHorseInfoHotkeyButton(int id, int x, int y, int width,int height, String hotkey)
	{
		super(id, x, y, width, height, hotkey);
	}

	@Override
	protected void SetHotkey(String hotkey)
	{
		HorseInfo.Hotkey = hotkey;
	}

	@Override
	protected String GetHotkeyDescription()
	{
		return HorseInfo.HotkeyDescription;
	}
}
