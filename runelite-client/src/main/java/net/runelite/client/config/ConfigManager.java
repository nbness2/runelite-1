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
package net.runelite.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager
{
	private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

	private final File propertiesFile;
	private final ConfigInvocationHandler handler = new ConfigInvocationHandler(this);
	private final Properties properties = new Properties();

	public ConfigManager(File propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}

	public void load() throws IOException
	{
		try (FileInputStream in = new FileInputStream(propertiesFile))
		{
			properties.load(in);
		}
	}

	public void save() throws IOException
	{
		try (FileOutputStream out = new FileOutputStream(propertiesFile))
		{
			properties.store(out, "Runelite configuration");
		}
	}

	public <T> T getConfig(Class<T> clazz)
	{
		if (!Modifier.isPublic(clazz.getModifiers()))
		{
			throw new RuntimeException("Non-public configuration classes can't have default methods invoked");
		}

		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]
		{
			clazz
		}, handler);
	}

	public String getConfiguration(String groupName, String key)
	{
		return properties.getProperty(groupName + "." + key);
	}

	public void setConfiguration(String groupName, String key, String value)
	{
		logger.debug("Setting configuration value for {}.{} to {}", groupName, key, value);

		properties.setProperty(groupName + "." + key, value);
	}

	public void unsetConfiguration(String groupName, String key)
	{
		logger.debug("Unsetting configuration value for {}.{}", groupName, key);

		properties.remove(groupName + "." + key);
	}

	public ConfigDescriptor getConfigDescriptor(Object configurationProxy)
	{
		Class<?> inter = configurationProxy.getClass().getInterfaces()[0];
		ConfigGroup group = inter.getAnnotation(ConfigGroup.class);

		if (group == null)
		{
			throw new IllegalArgumentException("Not a config group");
		}

		List<ConfigItemDescriptor> items = Arrays.stream(inter.getMethods())
			.filter(m -> m.getParameterCount() == 0)
			.map(m -> new ConfigItemDescriptor(
				m.getDeclaredAnnotation(ConfigItem.class),
				m.getReturnType()
			))
			.collect(Collectors.toList());
		return new ConfigDescriptor(group, items);
	}
}
