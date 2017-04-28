package com.skytonia.SkyCore.gui.nms;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Created by Chris Brown (OhBlihv) on 10/28/2016.
 */
public class GUICreationFactory_1_7_R4 implements GUICreationFactory
{
	
	@Override
	public Inventory createInventory(int guiSize, String guiTitle)
	{
		if(guiSize == 5)
		{
			return Bukkit.createInventory(null, InventoryType.HOPPER, guiTitle);
		}
		else
		{
			return new CraftInventoryCustom(null, guiSize, guiTitle);
		}
	}
}