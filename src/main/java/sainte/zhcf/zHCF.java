package sainte.zhcf;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sainte.zhcf.FactionCommands.MainFactionCommands;

public final class zHCF extends JavaPlugin {

    private HashMap<String, Faction> factions;

    @Override
    public void onEnable() {
        factions = new HashMap<>();
        loadFactions();
        getCommand("faction").setExecutor(new MainFactionCommands(this));
    }

    @Override
    public void onDisable() {
        saveFactions();
    }
    public HashMap<String, Faction> getFactions() {
        return factions;
    }

    private void loadFactions() {
        File file = new File(getDataFolder(), "factions.yml");

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String factionName : config.getKeys(false)) {
            String leader = config.getString(factionName + ".leader");
            List<String> members = config.getStringList(factionName + ".members");
            List<String> captains = config.getStringList(factionName + ".captains");
            List<String> coLeaders = config.getStringList(factionName + ".coLeaders");

            Faction faction = new Faction(leader);
            faction.getMembers().addAll(members);
            faction.getCaptains().addAll(captains);
            faction.getCoLeaders().addAll(coLeaders);

            factions.put(factionName, faction);
        }
    }

    public void saveFactions() {
        File file = new File(getDataFolder(), "factions.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (Map.Entry<String, Faction> entry : factions.entrySet()) {
            String factionName = entry.getKey();
            Faction faction = entry.getValue();

            config.set(factionName + ".leader", faction.getLeader());
            config.set(factionName + ".members", faction.getMembers());
            config.set(factionName + ".captains", faction.getCaptains());
            config.set(factionName + ".coLeaders", faction.getCoLeaders());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().severe("Failed to save factions: " + e.getMessage());
        }
    }
    public static class Faction {
        private String leader;
        private List<String> members;
        private List<String> captains;
        private List<String> coLeaders;
        private List<String> invitedPlayers;
        private String name;
        public void addMember(String playerName) {
            members.add(playerName);
        }
        public void removeMember(String playerName) {
            members.remove(playerName);
        }
        public void invitePlayer(String player) {
            invitedPlayers.add(player);
        }
        public void revokeInvitation(String player) {
            invitedPlayers.remove(player);
        }
        public void addInvitedPlayer(String playerName) {
            this.invitedPlayers.add(playerName);
        }
        public Faction(String leader) {
            this.leader = leader;
            this.members = new ArrayList<>();
            this.captains = new ArrayList<>();
            this.coLeaders = new ArrayList<>();
            this.invitedPlayers = new ArrayList<>();
        }
        public List<String> getInvitedPlayers() {
            return invitedPlayers;
        }
        public void removeInvitedPlayer(String playerName) {
            invitedPlayers.remove(playerName);
        }
        public String getLeader() {
            return leader;
        }
        public List<String> getMembers() {
            return members;
        }
        public List<String> getCaptains() {
            return captains;
        }
        public List<String> getCoLeaders() {
            return coLeaders;
        }
    }
}
