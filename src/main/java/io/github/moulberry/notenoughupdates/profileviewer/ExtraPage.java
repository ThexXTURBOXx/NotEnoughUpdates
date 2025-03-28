/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

public class ExtraPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_extra = new ResourceLocation("notenoughupdates:pv_extra.png");
	private TreeMap<Integer, Set<String>> topKills = null;
	private TreeMap<Integer, Set<String>> topDeaths = null;
	private int deathScroll = 0;
	private int killScroll = 0;
	private int mouseDWheel = 0;


	public ExtraPage(GuiProfileViewer instance) {
		super(instance);
		getInstance().killDeathSearchTextField.setSize(80, 12);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (getInstance().killDeathSearchTextField.getFocus()) {
			getInstance().killDeathSearchTextField.keyTyped(typedChar, keyCode);
			killScroll = 0;
			deathScroll = 0;
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		//Note: don't know why it made me make it return a boolean, but it fixed the error, so I left it alone.

		//Dimensions: X: guiLeft + xStart + xOffset * 3, Y: guiTop + yStartBottom + 77, Width: 80, Height: 12
		if (mouseX >= GuiProfileViewer.getGuiLeft() + 22 + 103 * 3 &&
			mouseX <= GuiProfileViewer.getGuiLeft() + 22 + 103 * 3 + 80 &&
			mouseY >= GuiProfileViewer.getGuiTop() + 105 + 77 && mouseY <= GuiProfileViewer.getGuiTop() + 105 + 77 + 12) {
			getInstance().killDeathSearchTextField.mouseClicked(mouseX, mouseY, mouseButton);
			getInstance().playerNameTextField.otherComponentClick();
			return true;
		}

		getInstance().killDeathSearchTextField.otherComponentClick();

		return false;
	}

	// pls update in the future tyvm !!!
	final static HashMap<String, Integer> slayers = new HashMap<String, Integer>() {
		{
			put("zombie", 5);
			put("spider", 4);
			put("wolf", 4);
			put("enderman", 4);
			put("blaze", 4);
		}
	};

	public void drawEssence(
		JsonObject profileInfo,
		float xStart,
		float yStartTop,
		float xOffset,
		float yOffset) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		yStartTop = yStartTop + 78;
		if (Constants.PARENTS == null || !Constants.PARENTS.has("ESSENCE_WITHER")) {
			Utils.showOutdatedRepoNotification();
			return;
		}
		JsonObject parents = Constants.PARENTS;
		JsonArray essenceArray = parents.get("ESSENCE_WITHER").getAsJsonArray();

		for (int i = 0; i < essenceArray.size(); i++) {

			JsonElement jsonElement = essenceArray.get(i);
			String essenceName = jsonElement.getAsString();

			TreeMap<String, JsonObject> itemInformation = NotEnoughUpdates.INSTANCE.manager.getItemInformation();
			if (!itemInformation.containsKey(essenceName)) {
				Utils.showOutdatedRepoNotification();
				return;
			}
			String displayName = itemInformation.get(essenceName).getAsJsonObject().get("displayname").getAsString();
			if (profileInfo.has(essenceName.toLowerCase())) {
				int essenceNumber = profileInfo.get(essenceName.toLowerCase()).getAsInt();

				Utils.renderAlignedString(
					EnumChatFormatting.GOLD + displayName,
					EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(essenceNumber, 0),
					guiLeft + xStart + xOffset,
					guiTop + yStartTop + yOffset * i,
					76
				);
			}
		}
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_extra);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		ProfileViewer.Profile profile = GuiProfileViewer.getProfile();
		String profileId = GuiProfileViewer.getProfileId();
		JsonObject profileInfo = profile.getProfileInformation(profileId);
		if (profileInfo == null) return;
		Map<String, ProfileViewer.Level> skyblockInfo = profile.getSkyblockInfo(profileId);

		float xStart = 22;
		float xOffset = 103;
		float yStartTop = 27;
		float yStartBottom = 105;
		float yOffset = 10;

		float bankBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "banking.balance"), 0);
		float purseBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "coin_purse"), 0);

		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Bank Balance",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(bankBalance),
			guiLeft + xStart,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Purse",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(purseBalance),
			guiLeft + xStart,
			guiTop + yStartTop + yOffset,
			76
		);

		{
			String first_join = getTimeSinceString(profileInfo, "first_join");
			if (first_join != null) {
				Utils.renderAlignedString(
					EnumChatFormatting.AQUA + "Joined",
					EnumChatFormatting.WHITE + first_join,
					guiLeft + xStart,
					guiTop + yStartTop + yOffset * 2,
					76
				);
			}
		}
		JsonObject guildInfo = profile.getGuildInformation(null);
		boolean shouldRenderGuild = guildInfo != null && guildInfo.has("name");
		{
			if (shouldRenderGuild) {
				Utils.renderAlignedString(
					EnumChatFormatting.AQUA + "Guild",
					EnumChatFormatting.WHITE + guildInfo.get("name").getAsString(),
					guiLeft + xStart,
					guiTop + yStartTop + yOffset * 3,
					76
				);
			}
		}
		{
			GuiProfileViewer.pronouns.peekValue().flatMap(it -> it).ifPresent(choice -> Utils.renderAlignedString(
				EnumChatFormatting.GREEN + "Pronouns",
				EnumChatFormatting.WHITE + String.join(" / ", choice.render()),
				guiLeft + xStart,
				guiTop + yStartTop + yOffset * (shouldRenderGuild ? 4 : 3),
				76
			));
		}

		float fairySouls = Utils.getElementAsFloat(Utils.getElement(profileInfo, "fairy_souls_collected"), 0);

		int fairySoulMax = 227;
		if (Constants.FAIRYSOULS != null && Constants.FAIRYSOULS.has("Max Souls")) {
			fairySoulMax = Constants.FAIRYSOULS.get("Max Souls").getAsInt();
		}
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Fairy Souls",
			EnumChatFormatting.WHITE.toString() + (int) fairySouls + "/" + fairySoulMax,
			guiLeft + xStart,
			guiTop + yStartBottom,
			76
		);

		if (skyblockInfo != null) {
			float totalSkillLVL = 0;
			float totalTrueSkillLVL = 0;
			float totalSlayerLVL = 0;
			float totalSkillCount = 0;
			float totalSlayerCount = 0;
			float totalSlayerXP = 0;

			List<String> skills = Arrays.asList(
				"taming",
				"mining",
				"foraging",
				"enchanting",
				"farming",
				"combat",
				"fishing",
				"alchemy",
				"carpentry"
			);

			for (Map.Entry<String, ProfileViewer.Level> entry : skyblockInfo.entrySet()) {
				if (skills.contains(entry.getKey())) {
					totalSkillLVL += entry.getValue().level;
					totalTrueSkillLVL += Math.floor(entry.getValue().level);
					totalSkillCount++;
				} else if (slayers.containsKey(entry.getKey())) {
					totalSlayerLVL += entry.getValue().level;
					totalSlayerCount++;
					totalSlayerXP += entry.getValue().totalXp;
				}
			}

			float avgSkillLVL = totalSkillLVL / totalSkillCount;
			float avgTrueSkillLVL = totalTrueSkillLVL / totalSkillCount;
			float avgSlayerLVL = totalSlayerLVL / totalSlayerCount;

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "AVG Skill Level",
				EnumChatFormatting.WHITE.toString() + Math.floor(avgSkillLVL * 10) / 10,
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "True AVG Skill Level",
				EnumChatFormatting.WHITE.toString() + Math.floor(avgTrueSkillLVL * 10) / 10,
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 2,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "AVG Slayer Level",
				EnumChatFormatting.WHITE.toString() + Math.floor(avgSlayerLVL * 10) / 10,
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 3,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "Total Slayer XP",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(totalSlayerXP),
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 4,
				76
			);
		}

		float auctions_bids = Utils.getElementAsFloat(Utils.getElement(profileInfo, "stats.auctions_bids"), 0);
		float auctions_highest_bid = Utils.getElementAsFloat(
			Utils.getElement(profileInfo, "stats.auctions_highest_bid"),
			0
		);
		float auctions_won = Utils.getElementAsFloat(Utils.getElement(profileInfo, "stats.auctions_won"), 0);
		float auctions_created = Utils.getElementAsFloat(Utils.getElement(profileInfo, "stats.auctions_created"), 0);
		float auctions_gold_spent = Utils.getElementAsFloat(Utils.getElement(profileInfo, "stats.auctions_gold_spent"), 0);
		float auctions_gold_earned = Utils.getElementAsFloat(
			Utils.getElement(profileInfo, "stats.auctions_gold_earned"),
			0
		);

		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auction Bids",
			EnumChatFormatting.WHITE.toString() + (int) auctions_bids,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Highest Bid",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_highest_bid),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auctions Won",
			EnumChatFormatting.WHITE.toString() + (int) auctions_won,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 2,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auctions Created",
			EnumChatFormatting.WHITE.toString() + (int) auctions_created,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 3,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Gold Spent",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_gold_spent),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 4,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Gold Earned",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_gold_earned),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 5,
			76
		);

		float pet_milestone_ores_mined = Utils.getElementAsFloat(Utils.getElement(
			profileInfo,
			"stats.pet_milestone_ores_mined"
		), 0);
		float pet_milestone_sea_creatures_killed = Utils.getElementAsFloat(
			Utils.getElement(profileInfo, "stats.pet_milestone_sea_creatures_killed"),
			0
		);

		float items_fished = Utils.getElementAsFloat(Utils.getElement(profileInfo, "stats.items_fished"), 0);
		float items_fished_treasure = Utils.getElementAsFloat(
			Utils.getElement(profileInfo, "stats.items_fished_treasure"),
			0
		);
		float items_fished_large_treasure = Utils.getElementAsFloat(Utils.getElement(
			profileInfo,
			"stats.items_fished_large_treasure"
		), 0);

		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Ores Mined",
			EnumChatFormatting.WHITE.toString() + (int) pet_milestone_ores_mined,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Sea Creatures Killed",
			EnumChatFormatting.WHITE.toString() + (int) pet_milestone_sea_creatures_killed,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset,
			76
		);

		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Items Fished",
			EnumChatFormatting.WHITE.toString() + (int) items_fished,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 3,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Treasures Fished",
			EnumChatFormatting.WHITE.toString() + (int) items_fished_treasure,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 4,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Large Treasures",
			EnumChatFormatting.WHITE.toString() + (int) items_fished_large_treasure,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 5,
			76
		);

		drawEssence(profileInfo, xStart, yStartTop, xOffset, yOffset);

		if (topKills == null) {
			topKills = new TreeMap<>();
			JsonObject stats = profileInfo.get("stats").getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : stats.entrySet()) {
				if (entry.getKey().startsWith("kills_")) {
					if (entry.getValue().isJsonPrimitive()) {
						JsonPrimitive prim = (JsonPrimitive) entry.getValue();
						if (prim.isNumber()) {
							String name = WordUtils.capitalizeFully(entry.getKey().substring("kills_".length()).replace("_", " "));
							Set<String> kills = topKills.computeIfAbsent(prim.getAsInt(), k -> new HashSet<>());
							kills.add(name);
						}
					}
				}
			}
		}
		if (topDeaths == null) {
			topDeaths = new TreeMap<>();
			JsonObject stats = profileInfo.get("stats").getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : stats.entrySet()) {
				if (entry.getKey().startsWith("deaths_")) {
					if (entry.getValue().isJsonPrimitive()) {
						JsonPrimitive prim = (JsonPrimitive) entry.getValue();
						if (prim.isNumber()) {
							String name = WordUtils.capitalizeFully(entry.getKey().substring("deaths_".length()).replace("_", " "));
							Set<String> deaths = topDeaths.computeIfAbsent(prim.getAsInt(), k -> new HashSet<>());
							deaths.add(name);
						}
					}
				}
			}
		}

		getInstance().killDeathSearchTextField.render((int) (guiLeft + xStart + xOffset * 3), (int) (guiTop + yStartBottom + 77));

		float killDeathX = guiLeft + xStart + xOffset * 3;
		if(mouseX >= killDeathX && mouseX <= killDeathX+76) {
			if(mouseY >= guiTop + yStartTop && mouseY <= guiTop + yStartTop + 65) {
				if(mouseDWheel != 0) {if(mouseDWheel > 0) {killScroll-=1;} else {killScroll+=1;}} mouseDWheel = Mouse.getDWheel();
				if(killScroll < 0) {killScroll = 0;}
			} else if(mouseY >= guiTop + yStartBottom && mouseY <= guiTop + yStartBottom + 65) {
				if(mouseDWheel != 0) {if(mouseDWheel > 0) {deathScroll-=1;} else {deathScroll+=1;}} mouseDWheel = Mouse.getDWheel();
				if(deathScroll < 0) {deathScroll = 0;}
			}

		}

		int index = 0;
		int skipCount = 0;
		for (int killCount : topKills.descendingKeySet()) {
			Set<String> kills = topKills.get(killCount);
			for (String killType : kills) {
				boolean isSearch = getInstance().killDeathSearchTextField.getText().isEmpty() || killType.toLowerCase(Locale.ROOT).contains(getInstance().killDeathSearchTextField.getText().toLowerCase(Locale.ROOT));
				float killY = guiTop + yStartTop + yOffset * ((index-skipCount) - killScroll);
				if(!isSearch) skipCount++;
				if(isSearch && killY+6 < guiTop+yStartTop+65 && killY >= guiTop + yStartTop) {
					Utils.renderAlignedString(
						EnumChatFormatting.YELLOW + killType + " Kills",
						EnumChatFormatting.WHITE.toString() + killCount,
						killDeathX,
						killY,
						76
					);
				}


				index++;
			}
		}
		index = 0;
		skipCount = 0;
		for (int deathCount : topDeaths.descendingKeySet()) {
			Set<String> deaths = topDeaths.get(deathCount);
			for (String deathType : deaths) {
				boolean isSearch = getInstance().killDeathSearchTextField.getText().isEmpty() || deathType.toLowerCase(Locale.ROOT).contains(getInstance().killDeathSearchTextField.getText().toLowerCase(Locale.ROOT));
				float deathY = guiTop + yStartBottom + yOffset * ((index-skipCount) - deathScroll);
				if(!isSearch) skipCount++;
				if(isSearch && deathY+6 < guiTop+yStartBottom+65 && deathY >= guiTop + yStartBottom) {
					Utils.renderAlignedString(
						EnumChatFormatting.YELLOW + "Deaths: " + deathType,
						EnumChatFormatting.WHITE.toString() + deathCount,
						killDeathX,
						deathY,
						76
					);
				}
				index++;
			}
		}
	}

	private String getTimeSinceString(JsonObject profileInfo, String path) {
		JsonElement lastSaveElement = Utils.getElement(profileInfo, path);

		if (lastSaveElement != null && lastSaveElement.isJsonPrimitive()) {
			Instant lastSave = Instant.ofEpochMilli(lastSaveElement.getAsLong());
			LocalDateTime lastSaveTime = LocalDateTime.ofInstant(lastSave, TimeZone.getDefault().toZoneId());
			long timeDiff = System.currentTimeMillis() - lastSave.toEpochMilli();
			LocalDateTime sinceOnline = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeDiff), ZoneId.of("UTC"));
			String renderText;

			if (timeDiff < 60000L) {
				renderText = sinceOnline.getSecond() + " seconds ago.";
			} else if (timeDiff < 3600000L) {
				renderText = sinceOnline.getMinute() + " minutes ago.";
			} else if (timeDiff < 86400000L) {
				renderText = sinceOnline.getHour() + " hours ago.";
			} else if (timeDiff < 31556952000L) {
				renderText = sinceOnline.getDayOfYear() + " days ago.";
			} else {
				renderText = lastSaveTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			}
			return renderText;
		}
		return null;
	}
}
