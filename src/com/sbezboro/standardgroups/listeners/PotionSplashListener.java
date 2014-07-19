package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionSplashListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public PotionSplashListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		boolean bad = false;
		ThrownPotion potion = event.getPotion();
		GroupManager groupManager = subPlugin.getGroupManager();

		for (PotionEffect effect : potion.getEffects()) {
			if (effect.getType().equals(PotionEffectType.POISON) ||
					effect.getType().equals(PotionEffectType.BLINDNESS) ||
					effect.getType().equals(PotionEffectType.SLOW) ||
					effect.getType().equals(PotionEffectType.CONFUSION) ||
					effect.getType().equals(PotionEffectType.WEAKNESS)) {
				bad = true;
				break;
			}
		}

		if (bad) {
			for (LivingEntity entity : event.getAffectedEntities()) {
				StandardPlayer player = plugin.getStandardPlayer(entity);

				if (player != null) {
					Group group = groupManager.getGroupByLocation(player.getLocation());

					if (group != null && group.isSafearea()) {
						event.setIntensity(entity, 0);
					}
				}
			}
		}
	}
}
