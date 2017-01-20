package com.skytonia.SkyCore.cheapobjects.player;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

/**
 * Created by Chris Brown (OhBlihv) on 7/08/2016.
 */
public class CheapPlayer_1_10_R1 extends CheapPlayer
{
	
	//Provide quick access to networking
	@Getter
	private PlayerConnection playerConnection;
	
	private EntityPlayer entityPlayer;
	
	public CheapPlayer_1_10_R1(Player player)
	{
		super(player);
		
		entityPlayer = ((CraftPlayer) player).getHandle();
		
		playerConnection = entityPlayer.playerConnection;
	}
	
	public void queuePacket(Object packet)
	{
		if(!(packet instanceof Packet))
		{
			//Ignore this for now.
			//Possibly throw an exception to narrow down the illegal calls?
			throw new IllegalArgumentException("queuePacket() expects a Packet, and was given a " + (packet != null ? packet.getClass().getName() : "null"));
		}
		
		playerConnection.sendPacket((Packet<?>) packet);
	}
	
}
