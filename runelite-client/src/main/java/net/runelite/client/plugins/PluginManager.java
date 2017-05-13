/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.boosts.Boosts;
import net.runelite.client.plugins.bosstimer.BossTimers;
import net.runelite.client.plugins.clanchat.ClanChat;
import net.runelite.client.plugins.config.ConfigPlugin;
import net.runelite.client.plugins.devtools.DevTools;
import net.runelite.client.plugins.fpsinfo.FPS;
import net.runelite.client.plugins.hiscore.Hiscore;
import net.runelite.client.plugins.idlenotifier.IdleNotifier;
import net.runelite.client.plugins.mousehighlight.MouseHighlight;
import net.runelite.client.plugins.opponentinfo.OpponentInfo;
import net.runelite.client.plugins.pestcontrol.PestControl;
import net.runelite.client.plugins.runecraft.Runecraft;
import net.runelite.client.plugins.xtea.Xtea;
import net.runelite.client.plugins.zulrah.Zulrah;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager
{
	private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

	private final RuneLite runelite;
	private ServiceManager manager;

	public PluginManager(RuneLite runelite)
	{
		this.runelite = runelite;
	}

	public void loadAll()
	{
		List<Plugin> plugins = new ArrayList<>();
		plugins.add(new Boosts());
		plugins.add(new OpponentInfo());
		plugins.add(new FPS());
		plugins.add(new Hiscore());
		plugins.add(new BossTimers());
		plugins.add(new Xtea());
		plugins.add(new IdleNotifier());
		plugins.add(new Runecraft());
		plugins.add(new MouseHighlight());
		plugins.add(new PestControl());
		plugins.add(new ClanChat());
		plugins.add(new Zulrah());
		plugins.add(new ConfigPlugin());

		if (RuneLite.getOptions().has("developer-mode"))
		{
			logger.info("Loading developer plugins");
			plugins.add(new DevTools());
		}

		// Add plugin listeners
		for (Plugin plugin : plugins)
		{
			Service.Listener listener = new Service.Listener()
			{
				@Override
				public void running()
				{
					logger.debug("Plugin {} is now running", plugin);
					runelite.getEventBus().register(plugin);
				}

				@Override
				public void stopping(Service.State from)
				{
					logger.debug("Plugin {} is stopping", plugin);
					runelite.getEventBus().unregister(plugin);
				}

				@Override
				public void failed(Service.State from, Throwable failure)
				{
					logger.warn("Plugin {} has failed", plugin, failure);

					if (from == Service.State.RUNNING)
					{
						runelite.getEventBus().unregister(plugin);
					}
				}
			};

			plugin.addListener(listener, MoreExecutors.directExecutor());
		}

		manager = new ServiceManager(plugins);

		logger.debug("Starting plugins...");
		manager.startAsync();
	}

	public Collection<Plugin> getPlugins()
	{
		return manager.servicesByState().get(Service.State.RUNNING)
			.stream()
			.map(s -> (Plugin) s)
			.collect(Collectors.toList());
	}
}
