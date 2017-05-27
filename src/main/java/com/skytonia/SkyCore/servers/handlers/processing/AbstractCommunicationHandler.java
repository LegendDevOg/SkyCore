package com.skytonia.SkyCore.servers.handlers.processing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.skytonia.SkyCore.movement.events.PlayerChangeServerEvent;
import com.skytonia.SkyCore.movement.events.PlayerEnterServerEvent;
import com.skytonia.SkyCore.movement.events.PlayerServerChangeRequestEvent;
import com.skytonia.SkyCore.servers.MovementAction;
import com.skytonia.SkyCore.servers.MovementInfo;
import com.skytonia.SkyCore.servers.ServerInfo;
import com.skytonia.SkyCore.servers.ServerStatus;
import com.skytonia.SkyCore.servers.handlers.CommunicationHandler;
import com.skytonia.SkyCore.servers.handlers.exception.MessageException;
import com.skytonia.SkyCore.servers.listeners.ChannelSubscriber;
import com.skytonia.SkyCore.servers.listeners.ChannelSubscription;
import com.skytonia.SkyCore.servers.util.MessageUtil;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyRestart.SkyRestart;
import lombok.Getter;
import lombok.Setter;
import me.vaqxine.NetworkManager.NetworkManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chris Brown (OhBlihv) on 5/25/2017.
 */
public abstract class AbstractCommunicationHandler extends Thread implements CommunicationHandler
{
	
	/*
	 * Messaging
	 */
	
	protected final Multimap<String, ChannelSubscriber> subscriptionMap;
	
	private final CopyOnWriteArrayList<CommunicationMessage> pendingMessages = new CopyOnWriteArrayList<>();

	@Getter
	@Setter
	private boolean isRunning = true;
	
	//Holds message statistics for the past 5 seconds
	private final int[] MESSAGE_REPORTS = new int[100];
	
	/*
	 * Player Movement
	 */
	
	protected final String CHANNEL_MOVE_REQ  = "SC_MoveReq",
						   CHANNEL_MOVE_REPL = "SC_MoveRep";
	
	protected final MovementAction defaultAction = new MovementAction()
	{
		
		@Override
		public void onSend(Player player, String serverName)
		{
			transferPlayer(player, serverName);
		}
		
	};
	
	protected final Map<String, MovementInfo> movementMap = new HashMap<>();
	
	protected final Set<String> incomingPlayers = new HashSet<>();
	
	/*
	 * Server Management
	 */
	
	protected final String CHANNEL_INFO_REQ  = "SC_InfoReq",
						   CHANNEL_INFO_REPL = "SC_InfoRep";
	
	private final Random random = new Random();
	
	protected final Map<String, ServerInfo> serverMap = new HashMap<>();
	
	@Getter
	@Setter
	protected String currentServer = "NULL";
	
	public AbstractCommunicationHandler()
	{
		Multimap<String, ChannelSubscriber> tempSubscriptionMap;
		try
		{
			tempSubscriptionMap = MultimapBuilder.hashKeys().arrayListValues().build();
		}
		catch(Exception e)
		{
			BUtil.log("Server version does not support Guava 16.0. Creating Multimap directly...");
			tempSubscriptionMap = HashMultimap.create();
		}
		
		subscriptionMap = tempSubscriptionMap;
	}
	
	public void run()
	{
		short tick = -1;
		long executionStart,
			 executionEnd;
		
		while(isRunning)
		{
			/*
			 * Check movement expiries every 1/2 seconds
			 */
			if(tick % 10 == 0)
			{
				for(Iterator<Map.Entry<String, MovementInfo>> entryItr = movementMap.entrySet().iterator(); entryItr.hasNext();)
				{
					Map.Entry<String, MovementInfo> entry = entryItr.next();
					
					MovementInfo movementInfo = entry.getValue();
					if(movementInfo.hasTimedOut())
					{
						movementInfo.failPlayer();
						entryItr.remove();
					}
					else if(!entry.getValue().getPlayer().isOnline())
					{
						entryItr.remove();
					}
				}
			}
			
			/*
			 * Update server info/player counts every 1 second
			 */
			if(tick % 20 == 0)
			{
				ServerStatus serverStatus = ServerStatus.ONLINE;
				int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
				
				if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers())
				{
					serverStatus = ServerStatus.FULL;
				}
				else if(Bukkit.hasWhitelist())
				{
					serverStatus = ServerStatus.WHITELIST;
				}
				//Rebooting Plugins
				else
				{
					if(Bukkit.getPluginManager().getPlugin("SkyRestart").isEnabled())
					{
						if(SkyRestart.isRestarting())
						{
							serverStatus = ServerStatus.REBOOTING;
						}
					}
					else if(Bukkit.getPluginManager().getPlugin("NetworkManager").isEnabled())
					{
						if(NetworkManager.getPlugin().rebooting)
						{
							serverStatus = ServerStatus.REBOOTING;
						}
					}
				}
				
				addOutgoingMessage(null, CHANNEL_INFO_REPL, MessageUtil.mergeArguments(
					currentServer, serverStatus.name(), String.valueOf(onlinePlayerCount))
				);
			}
			
			if(pendingMessages.isEmpty())
			{
				sleep();
				continue;
			}
			
			executionStart = System.currentTimeMillis();
			
			Deque<CommunicationMessage> currentMessages = new ArrayDeque<>();
			currentMessages.addAll(pendingMessages);
			pendingMessages.clear();
			
			int sentMessages = currentMessages.size();
			
			for(CommunicationMessage message : currentMessages)
			{
				if(message.getDirection() == CommunicationDirection.INBOUND)
				{
					try
					{
						receiveMessage((InboundCommunicationMessage) message);
					}
					catch(Throwable e)
					{
						BUtil.log("Unable to receive message " + message.toString());
						e.printStackTrace();
						
						sentMessages--;
					}
				}
				else //if(message.getDirection() == CommunicationDirection.OUTBOUND)
				{
					try
					{
						sendMessage((OutboundCommunicationMessage) message);
					}
					catch(Throwable e)
					{
						BUtil.log("Unable to send message " + message.toString());
						e.printStackTrace();
						
						sentMessages--;
					}
				}
			}
			
			//Hold our message throughput
			if(++tick == MESSAGE_REPORTS.length)
			{
				tick = 0;
				
				MESSAGE_REPORTS[tick] = sentMessages;
			}
			
			executionEnd = System.currentTimeMillis();
			
			//Attempt to make up time if we're lagging behind to keep on a 50ms cycle
			long catchup = (executionEnd - executionStart);
			if(catchup > 49)
			{
				BUtil.log("Communications Thread behind 20tps target! Last execution took " + (50 - catchup) + "ms longer than expected.");
			}
			else
			{
				sleepFor(50L - Math.min(49L, catchup));
			}
		}
	}
	
	private void sleep()
	{
		sleepFor(50L);
	}
	
	private void sleepFor(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(InterruptedException e)
		{
			if(isRunning)
			{
				BUtil.log("An error occurred while running the AbstractCommunicationHandler thread:");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Retrieves the message count statistics for the past 5 seconds
	 *
	 * @return List of Integers containing the amount of messages sent/received during
	 *         operation containing one array index for every tick (50 milliseconds)
	 *         of the past 5 seconds
	 */
	public List<Integer> getMessageCounts()
	{
		synchronized(MESSAGE_REPORTS)
		{
			List<Integer> messageCounts = new ArrayList<>();
			for(int messageCount : MESSAGE_REPORTS)
			{
				messageCounts.add(messageCount);
			}
			
			return messageCounts;
		}
	}
	
	public abstract void sendMessage(OutboundCommunicationMessage message) throws MessageException;
	
	@Override
	public void receiveMessage(InboundCommunicationMessage message) throws MessageException
	{
		switch(message.getChannel())
		{
			case CHANNEL_MOVE_REQ:
			{
				String serverName = message.getMessageArgs()[0],
					   playerName = message.getMessageArgs()[1];
				
				//No response is success.
				String response = "";
				
				if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers())
				{
					response = "FULL";
				}
				else if(Bukkit.hasWhitelist())
				{
					OfflinePlayer offlinePlayer = null;
					try
					{
						offlinePlayer = Bukkit.getOfflinePlayer(playerName);
						if(offlinePlayer == null)
						{
							throw new IllegalArgumentException();
						}
					}
					catch(Exception e)
					{
						//
					}
					
					if(offlinePlayer == null || !Bukkit.getWhitelistedPlayers().contains(offlinePlayer))
					{
						response = ServerStatus.WHITELIST.name();
					}
				}
				
				PlayerServerChangeRequestEvent requestEvent = new PlayerServerChangeRequestEvent(playerName, response, !response.isEmpty());
				Bukkit.getPluginManager().callEvent(requestEvent);
				if(requestEvent.isCancelled())
				{
					response = requestEvent.getCancelReason();
				}
				else
				{
					response = ""; //Reset the response
				}
				
				sendMessage(new OutboundCommunicationMessage(
					                                            serverName, CHANNEL_MOVE_REPL, MessageUtil.mergeArguments(serverName, playerName, response)
				));
				
				if(!requestEvent.isCancelled())
				{
					//Ensure the player can join once their request has been accepted
					incomingPlayers.add(playerName);
					
					Bukkit.getPluginManager().callEvent(new PlayerEnterServerEvent(playerName));
				}
				
				break;
			}
			case CHANNEL_MOVE_REPL:
			{
				String targetServer = message.getMessageArgs()[0],
					   playerName   = message.getMessageArgs()[1],
					   response     = "";
				
				OfflinePlayer offlinePlayer = null;
				try
				{
					offlinePlayer = Bukkit.getOfflinePlayer(playerName);
					if(offlinePlayer == null || !offlinePlayer.isOnline())
					{
						offlinePlayer = null;
					}
				}
				catch(Exception e)
				{
					//
				}
				
				if(offlinePlayer == null)
				{
					BUtil.log("Player " + playerName + " was not online and could not be moved to " + targetServer);
					return;
				}
				
				if(message.getMessageArgs().length >= 3)
				{
					response = message.getMessageArgs()[2];
				}
				
				if(response.isEmpty())
				{
					BUtil.log("Received successful reply for " + playerName + "'s transfer to " + targetServer);
					setPlayerMovementStatusSuccess(playerName);
					
					transferPlayer(offlinePlayer.getPlayer(), targetServer);
				}
				else
				{
					BUtil.log("Received unsuccessful reply for " + playerName + "'s transfer to " + targetServer);
					setPlayerMovementStatusFailure(playerName);
				}
				
				break;
			}
			case CHANNEL_INFO_REPL:
			{
				String serverName        = message.getMessageArgs()[0],
					   statusString      = message.getMessageArgs()[1],
					   playerCountString = message.getMessageArgs()[2];
					   
				ServerInfo serverInfo = serverMap.get(serverName);
				if(serverInfo == null)
				{
					serverInfo = new ServerInfo();
					serverMap.put(serverName, serverInfo);
				}
				
				ServerStatus serverStatus = ServerStatus.ONLINE;
				try
				{
					serverStatus = ServerStatus.valueOf(statusString);
				}
				catch(IllegalArgumentException e)
				{
					BUtil.log("Unable to parse server status string '" + statusString + "'. Defaulting to ONLINE");
				}
				
				int playerCount = 0;
				try
				{
					playerCount = Integer.parseInt(playerCountString);
				}
				catch(NumberFormatException e)
				{
					BUtil.log("Unable to parse server player count string '" + playerCountString + "'. Defaulting to 0.");
				}
				
				serverInfo.setServerStatus(serverStatus);
				serverInfo.setPlayerCount(playerCount);
				
				BUtil.log("Updated " + serverName + " as " + serverStatus + " with " + playerCount + " players.");
				
				break;
			}
		}
	}
	
	/* ----------------------------------------------------------------------
	 *                              Messaging
	 * ----------------------------------------------------------------------*/
	
	public void addIncomingMessage(String sendingServer, String channel, String message)
	{
		addMessage(new InboundCommunicationMessage(sendingServer, channel, message));
	}
	
	public void addOutgoingMessage(String targetServer, String channel, String message)
	{
		addMessage(new OutboundCommunicationMessage(targetServer, channel, message));
	}
	
	public void addMessage(CommunicationMessage message)
	{
		this.pendingMessages.add(message);
	}
	
	public void registerSubscription(ChannelSubscription subscriber, String... channels)
	{
		registerSubscription(subscriber, true, channels);
	}
	
	public abstract void registerSubscription(ChannelSubscription subscriber, boolean prefixWithServerName, String... channels);
	
	/* ----------------------------------------------------------------------
	 *                      Communications Handler
	 * ----------------------------------------------------------------------*/
	
	public void setPlayerMovementStatusSuccess(String playerName)
	{
		MovementInfo movementInfo = movementMap.get(playerName);
		if(movementInfo == null)
		{
			return;
		}
		
		movementInfo.sendPlayer();
	}
	
	public void setPlayerMovementStatusFailure(String playerName)
	{
		MovementInfo movementInfo = movementMap.get(playerName);
		if(movementInfo == null)
		{
			return;
		}
		
		movementInfo.failPlayer();
	}
	
	@Override
	public void requestPlayerTransfer(Player player, String serverName)
	{
		requestPlayerTransfer(player, serverName, null);
	}
	
	@Override
	public void requestPlayerTransfer(Player player, String serverName, MovementAction movementAction)
	{
		PlayerChangeServerEvent event = new PlayerChangeServerEvent(player, serverName);
		Bukkit.getPluginManager().callEvent(event);
		
		if(event.isCancelled())
		{
			if(event.getCancelReason() != null && !event.getCancelReason().isEmpty())
			{
				player.sendMessage(event.getCancelReason());
			}
			
			return;
		}
		
		if(movementAction == null)
		{
			movementAction = defaultAction;
		}
		
		movementMap.put(player.getName(), new MovementInfo(player, serverName, movementAction));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event)
	{
		if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED && event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.KICK_BANNED)
		{
			//Attempt to get our player on!
			if(incomingPlayers.contains(event.getName()))
			{
				event.allow();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		//Once the player has arrived, clear them from incoming players.
		if(incomingPlayers.contains(event.getPlayer().getName()))
		{
			event.allow();
			incomingPlayers.remove(event.getPlayer().getName());
		}
	}
	
	/*
	 * Server Management
	 */
	
	@Override
	public String getOnlineHub()
	{
		List<String> availableHubs = getAvailableServersMatching("hub", "lobby");
		
		return availableHubs.get(random.nextInt(availableHubs.size()));
	}
	
	@Override
	public List<String> getServersMatching(String... searchPhrases)
	{
		for(int i = 0;i < searchPhrases.length;i++)
		{
			searchPhrases[i] = searchPhrases[i].toLowerCase();
		}
		
		List<String> matchedServers = new ArrayList<>();
		for(String serverName : serverMap.keySet())
		{
			String lowerServerName = serverName.toLowerCase();
			for(String searchPhrase : searchPhrases)
			{
				if(lowerServerName.contains(searchPhrase))
				{
					matchedServers.add(serverName);
				}
			}
		}
		
		return matchedServers;
	}
	
	@Override
	public List<String> getAvailableServersMatching(String... searchPhrases)
	{
		for(int i = 0;i < searchPhrases.length;i++)
		{
			searchPhrases[i] = searchPhrases[i].toLowerCase();
		}
		
		List<String> matchedServers = new ArrayList<>();
		for(Map.Entry<String, ServerInfo> entry : serverMap.entrySet())
		{
			String lowerServerName = entry.getKey().toLowerCase();
			for(String searchPhrase : searchPhrases)
			{
				if(entry.getValue().getServerStatus() == ServerStatus.ONLINE && lowerServerName.contains(searchPhrase))
				{
					matchedServers.add(entry.getKey());
				}
			}
		}
		
		return matchedServers;
	}
	
	@Override
	public ServerInfo getServer(String serverName)
	{
		ServerInfo serverInfo = serverMap.get(serverName);
		if(serverInfo == null)
		{
			serverInfo = new ServerInfo();
		}
		
		return serverInfo;
	}
	
	@Override
	public int getPlayerCount(String serverName)
	{
		return getServer(serverName).getPlayerCount();
	}
	
}
