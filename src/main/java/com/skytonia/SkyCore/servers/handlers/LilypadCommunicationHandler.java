package com.skytonia.SkyCore.servers.handlers;

import com.skytonia.SkyCore.servers.MovementAction;
import com.skytonia.SkyCore.servers.handlers.exception.MessageException;
import com.skytonia.SkyCore.servers.handlers.processing.AbstractCommunicationHandler;
import com.skytonia.SkyCore.servers.handlers.processing.OutboundCommunicationMessage;
import com.skytonia.SkyCore.servers.listeners.ChannelSubscriber;
import com.skytonia.SkyCore.servers.listeners.ChannelSubscription;
import com.skytonia.SkyCore.servers.listeners.LilypadChannelSubscriber;
import com.skytonia.SkyCore.servers.util.MessageUtil;
import com.skytonia.SkyCore.util.BUtil;
import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.event.EventListener;
import lilypad.client.connect.api.event.MessageEvent;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chris Brown (OhBlihv) on 5/24/2017.
 */
public class LilypadCommunicationHandler extends AbstractCommunicationHandler implements CommunicationHandler
{
	
	private final Connect lilypad;
	
	public LilypadCommunicationHandler()
	{
		super();
		
		lilypad = Bukkit.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
		
		currentServer = lilypad.getSettings().getUsername();
	}

	@Override
	public void registerChannels()
	{
		lilypad.registerEvents(this);
	}

	@Override
	public int getPlayerCount(String serverName)
	{
		return 0;
	}
	
	@Override
	public void requestPlayerTransfer(Player player, String serverName, MovementAction movementAction)
	{
		super.requestPlayerTransfer(player, serverName, movementAction);
		
		try
		{
			lilypad.request(new MessageRequest(serverName, CHANNEL_MOVE_REQ,
				MessageUtil.mergeArguments(currentServer, player.getName(), player.getUniqueId().toString())));
		}
		catch(RequestException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void transferPlayer(Player player, String serverName)
	{
		super.transferPlayer(player, serverName);

		try
		{
			//Alert the other server of an incoming player
			sendMessage(new OutboundCommunicationMessage(
				serverName, CHANNEL_MOVE_FORCE, MessageUtil.mergeArguments(player.getName(), player.getUniqueId().toString())
			));

			lilypad.request(new RedirectRequest(serverName, player.getName()));
		}
		catch(RequestException | MessageException e)
		{
			e.printStackTrace();
		}
	}

	public void sendMessage(OutboundCommunicationMessage message) throws MessageException
	{
		if(!lilypad.isConnected() || lilypad.isClosed())
		{
			throw new IllegalArgumentException("Lilypad Inaccessible. (Offline?)");
		}
		
		try
		{
			lilypad.request(new MessageRequest(message.getServer(), message.getChannel(), message.getMessage()));
		}
		catch(RequestException | UnsupportedEncodingException e)
		{
			throw new MessageException(message, e);
		}
	}
	
	/**
	 *
	 * @param subscriber
	 * @param prefixWithServerName Ignored since Lilypad handles channels separately
	 * @param channelList
	 */
	@Override
	public void registerSubscription(ChannelSubscription subscriber, boolean prefixWithServerName, String... channels)
	{
		List<String> channelList = new ArrayList<>();
		Collections.addAll(channelList, channels);
		
		ChannelSubscriber channelSubscriber = new LilypadChannelSubscriber(lilypad, channelList, subscriber);
		
		for(String channel : channelList)
		{
			subscriptionMap.put(channel, channelSubscriber);
		}
	}
	
	@EventListener
	public void onMessage(MessageEvent event)
	{
		try
		{
			addIncomingMessage(event.getSender(), event.getChannel(), event.getMessageAsString());
		}
		catch(Exception e)
		{
			BUtil.log("Unable to receive incoming messaging from '" + event.getSender() + "' on channel " + event.getChannel() + " => '" +
				          Arrays.toString(event.getMessage()) + "'");
			e.printStackTrace();
		}
	}
	
}
