/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.PluginPanel;
import static net.runelite.client.ui.PluginPanel.PANEL_HEIGHT;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPanel extends PluginPanel
{
	private static final Logger logger = LoggerFactory.getLogger(ConfigPanel.class);

	private final RuneLite runelite = RuneLite.getRunelite();
	private final ConfigManager configManager = runelite.getConfigManager();

	public ConfigPanel()
	{
		setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setSize(PANEL_WIDTH, PANEL_HEIGHT);
		setLayout(new BorderLayout());
		setVisible(true);
	}

	public void init()
	{
		add(createConfigPanel());
	}

	private Collection<ConfigDescriptor> getConfig()
	{
		List<ConfigDescriptor> list = new ArrayList<>();
		PluginManager pm = runelite.getPluginManager();
		for (Plugin plugin : pm.getPlugins())
		{
			Object config = plugin.getConfig();
			if (config != null)
			{
				ConfigDescriptor configDescriptor = configManager.getConfigDescriptor(config);
				list.add(configDescriptor);
			}
		}
		return list;
	}

	private JPanel createConfigPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1, 3, 3));

		Collection<ConfigDescriptor> config = getConfig();
		for (ConfigDescriptor cd : config)
		{
			for (ConfigItemDescriptor cid : cd.getItems())
			{
				panel.add(new JLabel(cid.getItem().name()));

				if (cid.getType() == boolean.class)
				{
					JCheckBox checkbox = new JCheckBox();
					checkbox.setSelected(Boolean.parseBoolean(configManager.getConfiguration(cd.getGroup().keyName(), cid.getItem().keyName())));
					checkbox.addActionListener(ae -> changeConfiguration(ae, checkbox, cd, cid));

					panel.add(checkbox);
				}
			}
		}
		return panel;
	}

	private void changeConfiguration(ActionEvent ae, JCheckBox checkbox, ConfigDescriptor cd, ConfigItemDescriptor cid)
	{
		configManager.setConfiguration(cd.getGroup().keyName(), cid.getItem().keyName(), "" + checkbox.isSelected());

		try
		{
			configManager.save();
		}
		catch (IOException ex)
		{
			logger.warn("Unable to save config", ex);
		}
	}

}
