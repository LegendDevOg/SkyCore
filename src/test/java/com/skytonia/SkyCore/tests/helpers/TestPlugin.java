package com.skytonia.SkyCore.tests.helpers;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class TestPlugin implements Plugin
{

	private final String pluginName;

	public TestPlugin(String pluginName)
	{
		this.pluginName = pluginName;
	}

	@Override
	public File getDataFolder()
	{
		File dataFolder = new File("plugins/" + pluginName);

		if(!dataFolder.exists())
		{
			dataFolder.mkdirs();
		}

		return dataFolder;
	}

	@Override
	public PluginDescriptionFile getDescription()
	{
		return null;
	}

	@Override
	public FileConfiguration getConfig()
	{
		return null;
	}

	@Override
	public InputStream getResource(String s)
	{
		return null;
	}

	@Override
	public void saveConfig()
	{

	}

	@Override
	public void saveDefaultConfig()
	{

	}

	@Override
	public void saveResource(String s, boolean b)
	{

	}

	@Override
	public void reloadConfig()
	{

	}

	@Override
	public PluginLoader getPluginLoader()
	{
		return null;
	}

	@Override
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public void onDisable()
	{

	}

	@Override
	public void onLoad()
	{

	}

	@Override
	public void onEnable()
	{

	}

	@Override
	public boolean isNaggable()
	{
		return false;
	}

	@Override
	public void setNaggable(boolean b)
	{

	}

	@Override
	public EbeanServer getDatabase()
	{
		return null;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String s, String s1)
	{
		return null;
	}

	@Override
	public Logger getLogger()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return pluginName;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
	{
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings)
	{
		return null;
	}
}
