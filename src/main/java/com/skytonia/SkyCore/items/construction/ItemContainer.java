package com.skytonia.SkyCore.items.construction;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.SkyCore;
import com.skytonia.SkyCore.items.EnchantStatus;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.StaticNMS;
import com.skytonia.SkyCore.util.SupportedVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.INBTBase;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

import static org.bukkit.Material.*;

/**
 * Created by Chris Brown (OhBlihv) on 26/09/2016.
 */
@RequiredArgsConstructor
public class ItemContainer
{
	
	//Set all to a 'default' unusable value to indicate if it needs changing
	@Getter
	private final Material material;
	
	@Getter
	private final int   damage,
						amount;
	@Getter
	private final String displayName;
	
	@Getter
	private final List<String> lore;
	
	@Getter
	private final EnchantStatus enchantStatus;
	
	@Getter
	private final Map<Enchantment, Integer> enchantmentMap;
	
	@Getter
	private final String owner;
	
	@Getter
	private final String skullTexture;
	
	@Getter
	private final Color armorColor;

	@Getter
	private Map<String, INBTBase> nbtFlags = new HashMap<>();

	private Set<ItemFlag> itemFlags = EnumSet.noneOf(ItemFlag.class);

	public ItemContainer(Material material, int damage, int amount, String displayName, List<String> lore, EnchantStatus enchantStatus,
	                     Map<Enchantment, Integer> enchantmentMap, String owner, String skullTexture, Color armorColor,
	                     Map<String, INBTBase> nbtFlags, Set<ItemFlag> itemsFlags)
	{
		this.material = material;
		this.damage = damage;
		this.amount = amount;
		this.displayName = displayName;
		this.lore = lore;
		this.enchantStatus = enchantStatus;
		this.enchantmentMap = enchantmentMap;
		this.owner = owner;
		this.skullTexture = skullTexture;
		this.armorColor = armorColor;
		this.nbtFlags = nbtFlags;
		this.itemFlags = itemsFlags;
	}
	
	private Object getOverriddenValue(Map<ItemContainerVariable, Object> overriddenValues, ItemContainerVariable itemVariable, Object defaultValue)
	{
		Object returningValue;
		if(itemVariable.isNumber())
		{
			returningValue = overriddenValues.getOrDefault(itemVariable, -1);
			
			if(((Number) returningValue).intValue() == -1)
			{
				returningValue = defaultValue;
			}
		}
		else
		{
			returningValue = overriddenValues.get(itemVariable);
		}
		
		if(returningValue != null)
		{
			return returningValue;
		}
		
		return defaultValue;
	}
	
	public ItemStack toItemStack()
	{
		return toItemStack(null);
	}
	
	public ItemStack toItemStack(String playerName)
	{
		return toItemStack(playerName, new HashMap<>());
	}
	
	public ItemStack toItemStack(String playerName, Map<ItemContainerVariable, Object> overriddenValues)
	{
		if(material == AIR && !overriddenValues.containsKey(ItemContainerVariable.MATERIAL))
		{
			return null;
		}
		
		//Load any overridden values
		Material        material = (Material) getOverriddenValue(overriddenValues, ItemContainerVariable.MATERIAL, this.material);
		int             amount = (int) getOverriddenValue(overriddenValues, ItemContainerVariable.AMOUNT, this.amount),
						damage = (int) getOverriddenValue(overriddenValues, ItemContainerVariable.DAMAGE, this.damage);
		String          displayName = (String) getOverriddenValue(overriddenValues, ItemContainerVariable.DISPLAYNAME, this.displayName);
		List<String>    lore = (List<String>) getOverriddenValue(overriddenValues, ItemContainerVariable.LORE,
		                                                         this.lore == null ? null : new ArrayList<>(this.lore));
		EnchantStatus   enchantStatus = (EnchantStatus) getOverriddenValue(overriddenValues, ItemContainerVariable.ENCHANTED, this.enchantStatus);
		Map<Enchantment, Integer> enchantmentMap =
						(Map<Enchantment, Integer>) getOverriddenValue(overriddenValues, ItemContainerVariable.ENCHANTMENTS, this.enchantmentMap);
		String          owner = (String) getOverriddenValue(overriddenValues, ItemContainerVariable.OWNER, this.owner),
						skullTexture = (String) getOverriddenValue(overriddenValues, ItemContainerVariable.SKULL_TEXTURE, this.skullTexture);
		
		ItemStack itemStack = new ItemStack(material, amount, (short) damage);
		
		//Handle NBT Tags early on before we customise it too much
		if((StaticNMS.getNMSItemUtil().isMonsterEggMaterial(material) || material == StaticNMS.getNMSItemUtil().getSpawnerMaterial()) &&
			damage > 0)
		{
			itemStack = StaticNMS.getNMSItemUtil().setSpawnedEntity(itemStack, damage);
		}
		
		ItemMeta itemMeta = itemStack.getItemMeta();
		
		if(displayName != null)
		{
			itemMeta.setDisplayName(displayName);
		}
		if((StaticNMS.getNMSItemUtil().isSkullMaterial(material)) && damage == 3)
		{
			if(skullTexture == null)
			{
				if(playerName != null || owner != null)
				{
					String skullOwner;
					//Allow the input player to override the owner for this skull
					if(owner == null || owner.equals("PLAYER"))
					{
						skullOwner = playerName;
					}
					else
					{
						skullOwner = owner;
					}
					
					if(skullOwner != null && !skullOwner.isEmpty())
					{
						((SkullMeta) itemMeta).setOwner(skullOwner);
						if(itemMeta.hasDisplayName())
						{
							itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{player}", skullOwner));
						}
					}
				}
			}
			else
			{
				GameProfile skinProfile = new GameProfile(UUID.randomUUID(), null);
				
				
				skinProfile.getProperties().put("textures", new Property("textures",
				                                                         skullTexture,
				                                                         "signed"));
				
				try
				{
					Field profileField = itemMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(itemMeta, skinProfile);
				}
				catch(IllegalAccessException | NoSuchFieldException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if(lore != null)
		{
			if(playerName != null && !playerName.isEmpty())
			{
				int lineNum = 0;
				for(String line : lore)
				{
					if(line.contains("{player}"))
					{
						line = line.replace("{player}", playerName);
					}
					
					lore.set(lineNum, line);
					
					lineNum++;
				}
			}
			
			itemMeta.setLore(lore);
		}
		
		if(armorColor != null && material.name().contains("LEATHER_"))
		{
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
			leatherMeta.setColor(armorColor);
			
			leatherMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		
		if(SkyCore.isSkytonia() && SkyCore.getCurrentVersion().isAtLeast(SupportedVersion.ONE_NINE) &&
			    material == DIAMOND_PICKAXE)
		{
			itemMeta.spigot().setUnbreakable(true);
			itemMeta.addItemFlags(
				ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_ATTRIBUTES,
				ItemFlag.HIDE_UNBREAKABLE,
				ItemFlag.HIDE_DESTROYS,
				ItemFlag.HIDE_PLACED_ON,
				ItemFlag.HIDE_POTION_EFFECTS);
		}

		if(itemFlags != null && !itemFlags.isEmpty())
		{
			for(ItemFlag itemFlag : itemFlags)
			{
				itemMeta.addItemFlags(itemFlag);
			}
		}

		if(nbtFlags != null && !nbtFlags.isEmpty() && SkyCore.getCurrentVersion().isExact(SupportedVersion.ONE_EIGHT))
		{
			for(Map.Entry<String, INBTBase> entry : nbtFlags.entrySet())
			{
				StaticNMS.getNMSItemUtil().addNBTFlag(itemMeta, entry.getKey(), entry.getValue());
			}
		}
		
		itemStack.setItemMeta(itemMeta);
		
		if(enchantmentMap != null)
		{
			itemStack.addUnsafeEnchantments(enchantmentMap);
		}
		
		if(enchantStatus != null)
		{
			return enchantStatus.alterEnchantmentStatus(itemStack);
		}
		else
		{
			return itemStack;
		}
	}
	
	public int getMaxStackSize()
	{
		return material.getMaxStackSize();
	}
	
	public int getMaxDurability()
	{
		return material.getMaxDurability();
	}
	
	public ItemStack replaceItemStack(ItemStack original, String playerName)
	{
		ItemMeta meta = original.getItemMeta();
		//Cannot clone, since it loses attributes for some reason.
		if(material != null)
		{
			original.setType(material);
		}
		if((StaticNMS.getNMSItemUtil().isSkullMaterial(material)) && playerName != null)
		{
			((SkullMeta) meta).setOwner(playerName);
		}
		if(damage != -1)
		{
			original.setDurability((short) damage);
		}
		if(amount != -1)
		{
			original.setAmount(amount);
		}
		if(displayName != null)
		{
			meta.setDisplayName(displayName);
		}
		if(playerName != null && meta.hasDisplayName())
		{
			meta.setDisplayName(meta.getDisplayName().replace("{player}", playerName));
		}
		if(lore != null)
		{
			meta.setLore(lore);
		}
		
		original.setItemMeta(meta);
		
		original = enchantStatus.alterEnchantmentStatus(original);
		
		if(enchantmentMap != null && !enchantmentMap.isEmpty())
		{
			for(Enchantment enchantment : original.getEnchantments().keySet())
			{
				original.removeEnchantment(enchantment);
			}
			original.addEnchantments(enchantmentMap);
		}
		
		return original;
	}
	
	public boolean equals(ItemStack itemStack)
	{
		return equals(itemStack, null);
	}
	
	public boolean equals(ItemStack itemStack, Set<ItemContainerVariable> ignoredChecks)
	{
		if(ignoredChecks == null)
		{
			ignoredChecks = EnumSet.noneOf(ItemContainerVariable.class);
		}
		
		if(itemStack == null ||
			   (!ignoredChecks.contains(ItemContainerVariable.MATERIAL) && itemStack.getType() != material) ||
			   (!ignoredChecks.contains(ItemContainerVariable.DAMAGE) && itemStack.getDurability() != damage))
		{
			return false;
		}
		
		ItemMeta itemMeta = itemStack.getItemMeta();
		
		//Expecting a displayname
		if(!ignoredChecks.contains(ItemContainerVariable.DISPLAYNAME) && displayName != null && !displayName.isEmpty())
		{
			if(!itemMeta.hasDisplayName() || !itemMeta.getDisplayName().equals(displayName))
			{
				return false;
			}
		}
		
		//Expecting Lore
		if(!ignoredChecks.contains(ItemContainerVariable.LORE) && lore != null && !lore.isEmpty())
		{
			if(!itemMeta.hasLore() || itemMeta.getLore().isEmpty() || !itemMeta.getLore().equals(lore))
			{
				return false;
			}
		}
		
		if(itemMeta instanceof SkullMeta)
		{
			if(owner != null && owner.equals("player"))
			{
				return true;
			}
			
			if(skullTexture != null)
			{
				//Find our texture
				GameProfile skullProfile = null;
				try
				{
					Class skullClass = Class.forName("org.bukkit.craftbukkit." + BUtil.getNMSVersion() + ".inventory.CraftMetaSkull");
					
					Field profileField = skullClass.getDeclaredField("profile");
					profileField.setAccessible(true);
					
					skullProfile = (GameProfile) profileField.get(itemMeta);
				}
				catch(ClassNotFoundException | NoSuchFieldException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
				
				try
				{
					if(skullProfile == null || !skullProfile.getProperties().containsKey("textures") ||
						   !skullProfile.getProperties().get("textures").iterator().next().getValue().equals(skullTexture))
					{
						return false;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				return ((SkullMeta) itemMeta).getOwner().equals(owner);
			}
		}
		
		return true;
	}
	
	@Override
	public String toString()
	{
		return material + ":" + damage + " (" + amount + ") \"" + displayName + "\"";
	}
	
	public void saveItem(ConfigurationSection configurationSection)
	{
		//Clear this section
		if(!configurationSection.getKeys(false).isEmpty())
		{
			String[] pathSplit = configurationSection.getCurrentPath().split("[.]");
			
			ConfigurationSection parentSection = configurationSection.getParent();
			parentSection.set(pathSplit[pathSplit.length - 1], null);
			configurationSection = parentSection.createSection(pathSplit[pathSplit.length - 1]);
		}
		
		configurationSection.set("material", material.name());
		configurationSection.set("damage", damage);
		configurationSection.set("amount", amount);
		
		if(displayName != null)
		{
			configurationSection.set("displayname", displayName);
		}
		
		if(lore != null)
		{
			configurationSection.set("lore", lore);
		}
		
		if((enchantmentMap == null || enchantmentMap.isEmpty()) && enchantStatus != null)
		{
			configurationSection.set("enchanted", enchantStatus.name());
		}
		else if(enchantmentMap != null && !enchantmentMap.isEmpty())
		{
			List<String> enchantStringList = new ArrayList<>();
			for(Map.Entry<Enchantment, Integer> entry : enchantmentMap.entrySet())
			{
				enchantStringList.add(entry.getKey().getName() + ":" + entry.getValue());
			}
			
			configurationSection.set("enchanted", enchantStringList);
		}
		
		if(owner != null)
		{
			configurationSection.set("owner", owner);
		}
		
		if(skullTexture != null)
		{
			configurationSection.set("texture", skullTexture);
		}
		
		if(armorColor != null)
		{
			configurationSection.set("color", armorColor.asRGB());
		}
		
	}
	
}
