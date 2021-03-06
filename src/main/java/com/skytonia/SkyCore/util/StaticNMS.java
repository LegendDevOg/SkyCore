package com.skytonia.SkyCore.util;

import com.skytonia.SkyCore.cosmetics.util.IParticlePacketFactory;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_10_R1;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_11_R1;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_12_R1;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_13_R1;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_13_R2;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_14_R1;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_7_R4;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_8_R3;
import com.skytonia.SkyCore.cosmetics.util.ParticlePacketFactory_1_9_R2;
import com.skytonia.SkyCore.items.nms.NMSItemUtil;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_11_R1;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_12_R1;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_13_R1;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_13_R2;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_14_R1;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_7_R4;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_8_R3;
import com.skytonia.SkyCore.items.nms.NMSItemUtil_1_9_R2;
import com.skytonia.SkyCore.packets.PacketLibrary;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_11_R1;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_12_R1;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_13_R1;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_13_R2;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_14_R1;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_7_R4;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_8_R3;
import com.skytonia.SkyCore.packets.nms.PacketLibrary_1_9_R2;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

/**
 * Created by Chris Brown (OhBlihv) on 9/08/2016.
 */
public class StaticNMS
{
	
	private static boolean isForge = false;
	@Getter
	private static String serverName;
	static
	{
		try
		{
			serverName = Bukkit.getServer().getName();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String packageServerName = "null";
			try //Forge is always the slowest D:
			{
				Class craftServerClass = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + ".CraftServer");
				Field serverNameField = craftServerClass.getDeclaredField("serverName");
				serverNameField.setAccessible(true);

				packageServerName = (String) serverNameField.get(Bukkit.getServer());

				//Very primitive Forge check, only really tested with Thermos
				if(packageServerName.equals("Cauldron"))
				{
					isForge = true;
				}
			}
			catch(Exception e2)
			{
				//Handled below if particleFactoryInstance is not set.
				e.printStackTrace();
			}

			serverName = packageServerName;
		}

		BUtil.log("Server Name Registered as '" + serverName + "'");
	}
	
	private static IParticlePacketFactory particleFactoryInstance = null;
	public static IParticlePacketFactory getParticleFactoryInstance() throws IllegalArgumentException
	{
		if(particleFactoryInstance == null)
		{
			switch(BUtil.getNMSVersion())
			{
				//case "v1_7_R1": particleFactoryInstance = new ParticlePacketFactory_1_7_R1(); break;
				//case "v1_7_R2": particleFactoryInstance = new ParticlePacketFactory_1_7_R2(); break;
				//case "v1_7_R3": particleFactoryInstance = new ParticlePacketFactory_1_7_R3(); break;
				case "v1_7_R4": particleFactoryInstance = new ParticlePacketFactory_1_7_R4(); break;
				//case "v1_8_R1": particleFactoryInstance = new ParticlePacketFactory_1_8_R1(); break;
				//case "v1_8_R2": particleFactoryInstance = new ParticlePacketFactory_1_8_R2(); break;
				case "v1_8_R3": particleFactoryInstance = new ParticlePacketFactory_1_8_R3(); break;
				//case "v1_9_R1": particleFactoryInstance = new ParticlePacketFactory_1_9_R1(); break;
				case "v1_9_R2": particleFactoryInstance = new ParticlePacketFactory_1_9_R2(); break;
				case "v1_10_R1": particleFactoryInstance = new ParticlePacketFactory_1_10_R1(); break;
				case "v1_11_R1": particleFactoryInstance = new ParticlePacketFactory_1_11_R1(); break;
				case "v1_12_R1": particleFactoryInstance = new ParticlePacketFactory_1_12_R1(); break;
				case "v1_13_R1": particleFactoryInstance = new ParticlePacketFactory_1_13_R1(); break;
				case "v1_13_R2": particleFactoryInstance = new ParticlePacketFactory_1_13_R2(); break;
				case "v1_14_R1": particleFactoryInstance = new ParticlePacketFactory_1_14_R1(); break;
				default: //Check if we're running forge
				{
					/*if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						particleFactoryInstance = new ParticlePacketFactory_Cauldron_1_7_R4();
						break;
					}*/
					
					if(particleFactoryInstance == null)
					{
						throw new IllegalArgumentException("(PARTICLES) This server version is not supported '" + serverName + "' (" + BUtil.getNMSVersion() +  ")");
					}
				}
			}
		}
		
		return particleFactoryInstance;
	}
	
	private static PacketLibrary packetLibrary = null;
	public static PacketLibrary getPacketLibrary() throws IllegalArgumentException
	{
		if(packetLibrary == null)
		{
			switch(BUtil.getNMSVersion())
			{
				//case "v1_7_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R1(); break;
				//case "v1_7_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R2(); break;
				//case "v1_7_R3": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R3(); break;
				case "v1_7_R4": packetLibrary = new PacketLibrary_1_7_R4(); break;
				//case "v1_8_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R1(); break;
				//case "v1_8_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R2(); break;
				case "v1_8_R3": packetLibrary = new PacketLibrary_1_8_R3(); break;
				//case "v1_9_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_9_R1(); break;
				case "v1_9_R2": packetLibrary = new PacketLibrary_1_9_R2(); break;
				//case "v1_10_R1": guiCreationFactory = new GUICreationFactory_1_10_R1(); break;
				case "v1_11_R1": packetLibrary = new PacketLibrary_1_11_R1(); break;
				case "v1_12_R1": packetLibrary = new PacketLibrary_1_12_R1(); break;
				case "v1_13_R1": packetLibrary = new PacketLibrary_1_13_R1(); break;
				case "v1_13_R2": packetLibrary = new PacketLibrary_1_13_R2(); break;
				case "v1_14_R1": packetLibrary = new PacketLibrary_1_14_R1(); break;
				default: //Check if we're running forge
				{
					/*if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R4();
						break;
					}*/
					
					if(packetLibrary == null)
					{
						throw new IllegalArgumentException("(PACKETS) This server version is not supported '" + serverName + "' (" + BUtil.getNMSVersion() +  ")");
					}
				}
			}
		}
		
		return packetLibrary;
	}
	
	private static NMSItemUtil nmsItemUtil = null;
	public static NMSItemUtil getNMSItemUtil() throws IllegalArgumentException
	{
		if(nmsItemUtil == null)
		{
			switch(BUtil.getNMSVersion())
			{
				//case "v1_7_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R1(); break;
				//case "v1_7_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R2(); break;
				//case "v1_7_R3": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R3(); break;
				case "v1_7_R4": nmsItemUtil = new NMSItemUtil_1_7_R4(); break;
				//case "v1_8_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R1(); break;
				//case "v1_8_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R2(); break;
				case "v1_8_R3": nmsItemUtil = new NMSItemUtil_1_8_R3(); break;
				//case "v1_9_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_9_R1(); break;
				case "v1_9_R2": nmsItemUtil = new NMSItemUtil_1_9_R2(); break;
				//case "v1_10_R1": nmsItemUtil = new GUICreationFactory_1_10_R1(); break;
				case "v1_11_R1": nmsItemUtil = new NMSItemUtil_1_11_R1(); break;
				case "v1_12_R1": nmsItemUtil = new NMSItemUtil_1_12_R1(); break;
				case "v1_13_R1": nmsItemUtil = new NMSItemUtil_1_13_R1(); break;
				case "v1_13_R2": nmsItemUtil = new NMSItemUtil_1_13_R2(); break;
				case "v1_14_R1": nmsItemUtil = new NMSItemUtil_1_14_R1(); break;
				default: //Check if we're running forge
				{
					/*if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R4();
						break;
					}*/
					
					if(nmsItemUtil == null)
					{
						throw new IllegalArgumentException("(NMSITEMS) This server version is not supported '" + serverName + "' (" + BUtil.getNMSVersion() +  ")");
					}
				}
			}
		}
		
		return nmsItemUtil;
	}
	
}
