package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class LiquidFlowListener extends SubPluginEventListener<StandardGroups> implements Listener {
    public LiquidFlowListener(StandardPlugin plugin, StandardGroups subPlugin) {
        super(plugin, subPlugin);
    }

    // Prevent liquids flowing across group boundaries because this can lead
    // to various kinds of griefing. In the worst case, if both lava and water
    // can flow across group boundaries, cobble monsters can form in the spawn
    // area.

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Location fromLocation = event.getBlock().getLocation();
        Location toLocation = event.getToBlock().getLocation();

        GroupManager groupManager = subPlugin.getGroupManager();

        Group fromGroup = groupManager.getGroupByLocation(fromLocation);
        Group toGroup = groupManager.getGroupByLocation(toLocation);

        if (fromGroup != toGroup) {
            if (subPlugin.getPreventLavaGriefing() && event.getBlock().getType() == Material.LAVA) {
                event.setCancelled(true);
            } else if (subPlugin.getPreventWaterGriefing() && event.getBlock().getType() == Material.WATER) {
                event.setCancelled(true);
            }
        }
    }
}
