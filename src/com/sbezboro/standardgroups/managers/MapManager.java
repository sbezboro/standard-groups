package com.sbezboro.standardgroups.managers;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.managers.BaseManager;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class MapManager extends BaseManager {
    private static final int MAP_WIDTH = 9;


    private static final byte[][] MAP_PATTERN = {
            new byte[] {0, 1, 0, 2, 0, 2, 0, 2, 0},
            new byte[] {1, 0, 1, 0, 2, 0, 2, 0, 2},
            new byte[] {0, 1, 0, 1, 0, 2, 0, 2, 0},
            new byte[] {2, 0, 1, 0, 1, 0, 2, 0, 2},
            new byte[] {0, 2, 0, 1, 3, 1, 0, 1, 0},
            new byte[] {2, 0, 2, 0, 1, 0, 1, 0, 1},
            new byte[] {0, 2, 0, 2, 0, 1, 0, 1, 0},
            new byte[] {2, 0, 2, 0, 2, 0, 1, 0, 1},
            new byte[] {0, 2, 0, 2, 0, 2, 0, 1, 0}
    };

    private StandardGroups subPlugin;
    private HashSet<StandardPlayer> mapPlayers;
    private int updateTaskId;

    private class MapLine implements OfflinePlayer {

        private final String contents;

        public MapLine(String contents) {
            this.contents = contents;
        }

        @Override
        public boolean isOnline() {
            return false;
        }

        @Override
        public String getName() {
            return contents;
        }

        @Override
        public UUID getUniqueId() {
            return null;
        }

        @Override
        public PlayerProfile getPlayerProfile() {
            return null;
        }

        @Override
        public boolean isBanned() {
            return false;
        }

        @Override
        public boolean isWhitelisted() {
            return false;
        }

        @Override
        public void setWhitelisted(boolean b) {

        }

        @Override
        public Player getPlayer() {
            return null;
        }

        @Override
        public long getFirstPlayed() {
            return 0;
        }

        @Override
        public long getLastPlayed() {
            return 0;
        }

        @Override
        public boolean hasPlayedBefore() {
            return false;
        }

        @Override
        public Location getBedSpawnLocation() {
            return null;
        }

        @Override
        public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

        }

        @Override
        public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

        }

        @Override
        public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(Statistic statistic) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

        }

        @Override
        public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

        }

        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {

        }

        @Override
        public void setStatistic(Statistic statistic, EntityType entityType, int i) {

        }

        @Override
        public Map<String, Object> serialize() {
            return null;
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean b) {

        }
    }

    public MapManager(StandardPlugin plugin, StandardGroups subPlugin) {
        super(plugin);

        this.subPlugin = subPlugin;
        mapPlayers = new HashSet<StandardPlayer>();

        updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(subPlugin, new Runnable() {
            @Override
            public void run() {
                for (StandardPlayer player : mapPlayers) {
                    try {
                        if (player.isOnline()) {
                            renderMap(player);
                        }
                    } catch (NullPointerException e) {
                        // Do nothing.
                    }
                }
            }
        }, 20, 20);
    }

    public void unload() {
        Bukkit.getScheduler().cancelTask(updateTaskId);
    }

    public boolean toggleMap(final StandardPlayer player) {
        if (mapPlayers.contains(player)) {
            mapPlayers.remove(player);
            removeMap(player);
            return false;
        } else {
            mapPlayers.add(player);
            return true;
        }
    }

    public void updateMap(StandardPlayer player) {
        if (mapPlayers.contains(player)) {
            renderMap(player);
        }
    }

    private void renderMap(StandardPlayer player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam("team");

        team.addEntry(player.getName());

        Objective objective = board.registerNewObjective("Map", "dummy", "Something");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Map");

        String[] mapRows = buildMap(player);

        for (int i = 0; i < mapRows.length; ++i) {
            String data = mapRows[i];

            OfflinePlayer rowData = new MapLine(data.substring(3, 18));

            Score score = objective.getScore(rowData.getName());

            score.setScore(MAP_WIDTH - i);

            Team mapRow = board.registerNewTeam(String.valueOf(i));

            mapRow.addEntry(rowData.getName());

            mapRow.setPrefix(data.substring(0, 3));
            mapRow.setSuffix(data.substring(18));
        }

        player.setScoreboard(board);
    }

    private void removeMap(StandardPlayer player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private String[] buildMap(StandardPlayer player) {
        GroupManager groupManager = subPlugin.getGroupManager();

        Group playerGroup = groupManager.getPlayerGroup(player);

        String[] result = new String[MAP_WIDTH];
        Group[][] surroundingGroups = new Group[MAP_WIDTH][MAP_WIDTH];

        double dir = (player.getLocation().getYaw() / 360) * 2 * Math.PI;

        int dirX = (int) -Math.round(Math.sin(dir));
        int dirZ = (int) Math.round(Math.cos(dir));

        int offset = MAP_WIDTH / 2;

        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int z = 0; z < MAP_WIDTH; ++z) {
                Location playerLocation = player.getLocation();

                Location location = new Location(player.getWorld(),
                        playerLocation.getBlockX() + ((x - offset) << 4),
                        playerLocation.getBlockY(),
                        playerLocation.getBlockZ() + ((z - offset) << 4));

                Group group = groupManager.getGroupByLocation(location);
                if (group != null) {
                    surroundingGroups[x][z] = group;
                }
            }
        }

        for (int i = 0; i < MAP_WIDTH; ++i) {
            String[] chars = new String[MAP_WIDTH];

            for (int x = 0; x < MAP_WIDTH; ++x) {

                switch (MAP_PATTERN[i][x]) {
                    case 0:
                        chars[x] = "▒";
                        break;
                    case 1:
                        chars[x] = "▓";
                        break;
                    case 2:
                        chars[x] = "█";
                        break;
                    case 3:
                        chars[x] = "\u2062";
                        break;
                }

                // Direction indicator
                if (i == offset + dirZ && x == offset + dirX) {
                    chars[x] = "ᚎ";
                }

                Group group = surroundingGroups[x][i];

                if (group == null) {
                    chars[x] = ChatColor.GRAY + chars[x];
                } else if (group == playerGroup) {
                    chars[x] = ChatColor.GREEN + chars[x];
                } else if (playerGroup != null && group.isMutualFriendship(playerGroup)) {
                    chars[x] = ChatColor.DARK_AQUA + chars[x];
                } else if (group.isSafeArea()) {
                    chars[x] = ChatColor.DARK_GREEN + chars[x];
                } else if (group.isNeutralArea()) {
                    chars[x] = ChatColor.GOLD + chars[x];
                } else {
                    chars[x] = ChatColor.YELLOW + chars[x];
                }
            }

            result[i] = StringUtils.join(chars);
        }

        return result;
    }
}
