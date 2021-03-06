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
package net.runelite.api;

public class Experience
{
	/**
	 * Maximum level under 200m xp
	 */
	private static final int MAX_VIRT_LEVEL = 126;

	private static final int[] XP_FOR_LEVEL = new int[MAX_VIRT_LEVEL];

	static
	{
		int xp = 0;

		for (int level = 1; level <= MAX_VIRT_LEVEL; ++level)
		{
			int difference = (int) ((double) level + 300.0 * Math.pow(2.0, (double) level / 7.0));
			xp += difference;

			XP_FOR_LEVEL[level - 1] = xp / 4;
		}
	}

	public static int getXpForLevel(int level)
	{
		if (level < 2 || level > MAX_VIRT_LEVEL)
		{
			throw new IllegalArgumentException();
		}

		// XP_FOR_LEVEL[0] is XP for level 2
		return XP_FOR_LEVEL[level - 2];
	}

	public static int getLevelForXp(int xp)
	{
		if (xp < 0)
		{
			throw new IllegalArgumentException();
		}

		int low = 0;
		int high = XP_FOR_LEVEL.length - 1;

		while (low <= high)
		{
			int mid = low + (high - low) / 2;
			int xpForLevel = XP_FOR_LEVEL[mid];

			if (xp < xpForLevel)
			{
				high = mid - 1;
			}
			else if (xp > xpForLevel)
			{
				low = mid + 1;
			}
			else
			{
				break;
			}
		}

		return high + 2;
	}
}
