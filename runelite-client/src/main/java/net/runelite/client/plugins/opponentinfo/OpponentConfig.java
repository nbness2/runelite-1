package net.runelite.client.plugins.opponentinfo;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(
	keyName = "oppinfo",
	name = "Opponent Info",
	description = "Configuration for the opponent info plugin"
)
public interface OpponentConfig
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enabled",
		description = "Configures whether or not opponent info is displayed"
	)
	default boolean enabled()
	{
		return true;
	}
}
