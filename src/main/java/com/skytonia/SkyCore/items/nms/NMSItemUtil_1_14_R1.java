package com.skytonia.SkyCore.items.nms;

import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagInt;
import net.minecraft.server.v1_14_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chris Brown (OhBlihv) on 5/27/2017.
 */
public class NMSItemUtil_1_14_R1 extends NMSItemUtil_Post_1_13 implements NMSItemUtil
{
	
	@Override
	public ItemStack setSpawnedEntity(ItemStack itemStack, int damage)
	{
		net.minecraft.server.v1_14_R1.ItemStack stack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tagCompound = stack.getTag();
		if(tagCompound == null)
		{
			tagCompound = new NBTTagCompound();
		}
		
		NBTTagCompound id = new NBTTagCompound();
		{
			EntityType entityType = EntityType.fromId(damage);
			if(entityType == null)
			{
				throw new IllegalArgumentException("Entity does not exist with ID(" + damage + "). Ensure this entity exists within 1.14");
			}

			id.setString("id", entityType.getName());
		}
		tagCompound.set("EntityTag", id);
		stack.setTag(tagCompound);
		return CraftItemStack.asBukkitCopy(stack);
	}
	
	@Override
	public Object addEnchantmentEffect(Object enchTag)
	{
		NBTTagList enchTagList = (NBTTagList) enchTag;
		while(!enchTagList.isEmpty())
		{
			enchTagList.remove(0);
		}
		((NBTTagList) enchTag).add(new NBTTagInt(-1));
		
		return enchTag;
	}
	
}
