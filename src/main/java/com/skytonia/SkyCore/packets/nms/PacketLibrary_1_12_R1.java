package com.skytonia.SkyCore.packets.nms;

import com.skytonia.SkyCore.packets.PacketLibrary;
import com.skytonia.SkyCore.packets.Persistence;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 5/27/2017.
 */
public class PacketLibrary_1_12_R1 extends PacketLibrary
{
	
	private void sendPacket(PlayerConnection playerConnection, Packet packet)
	{
		playerConnection.sendPacket(packet);
	}
	
	/*
	 *  Title/ActionBar Sending
	 */
	
	public void sendActionBar(Player player, String message, int lifespan)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(new ChatComponentText(message), ChatMessageType.GAME_INFO);

		//Action bar lasts about 2-3 seconds. Send an update after 2 seconds to ensure it does not disappear
		startPersistingTask(player.getUniqueId(), Persistence.PersistingType.ACTION_BAR, 40L, lifespan / 2,
		                               () -> sendPacket(playerConnection, packetPlayOutChat));
	}
	
	public void sendTitle(Player player, String title, String subTitle, int persistTime, int fadeIn, int fadeOut)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
		
		//PacketPlayOutTitle timesPacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, persistTime, fadeOut);
		//playerConnection.sendPacket(timesPacket);
		//BUtil.log("Sent times packet as " + persistTime + ", " + fadeIn + ", " + fadeOut);
		
		//Title is required to send subtitle. Send both when possible.
		if(title == null)
		{
			title = "";
		}
		
		if(subTitle == null)
		{
			subTitle = "";
		}
		
		sendTitlePacket(playerConnection, PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subTitle, persistTime, fadeIn, fadeOut);
		sendTitlePacket(playerConnection, PacketPlayOutTitle.EnumTitleAction.TITLE, title, persistTime, fadeIn, fadeOut);
	}
	
	private void sendTitlePacket(PlayerConnection playerConnection, PacketPlayOutTitle.EnumTitleAction titleAction, String message,
	                             int persistTime, int fadeIn, int fadeOut)
	{
		//BUtil.log("Printing " + titleAction.name() + " with '" + "{\"text\": \"" + messaging + "\"}" + "'");
		playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
		                                                   IChatBaseComponent.ChatSerializer.a("{\"text\": \" \"}"),
		                                                   persistTime, fadeIn, fadeOut));
		playerConnection.sendPacket(new PacketPlayOutTitle(titleAction,
		                                                   IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}"),
		                                                   persistTime, fadeIn, fadeOut));
	}
	
}
