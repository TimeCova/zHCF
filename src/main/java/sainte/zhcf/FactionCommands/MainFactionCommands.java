package sainte.zhcf.FactionCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sainte.zhcf.zHCF;

import java.util.HashMap;
import java.util.stream.Collectors;

public class MainFactionCommands implements CommandExecutor {
    private zHCF plugin;


    public MainFactionCommands(zHCF plugin) {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /faction <create | disband>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            default:
                player.sendMessage("FACTION");
                return false;
            case "create":
                if (args.length != 2) {
                    player.sendMessage("Usage: /faction create <faction-name>");
                    return false;
                }

                String factionNameCreate = args[1];
                if (plugin.getFactions().containsKey(factionNameCreate)) {
                    player.sendMessage(factionNameCreate + " already exists");
                    return false;
                }
                String playerNameCreate = player.getName();
                for (zHCF.Faction f : plugin.getFactions().values()) {
                    if (f.getMembers().contains(playerNameCreate)) {
                        player.sendMessage("You are already in a faction.");
                        return false;
                    }
                }

                zHCF.Faction createFaction = new zHCF.Faction(player.getName());
                createFaction.getMembers().add(player.getName());
                plugin.getFactions().put(factionNameCreate, createFaction);
                player.sendMessage("Successfully created faction " + factionNameCreate);
                return true;

            case "disband":
                String playerNameDisband = player.getName();
                HashMap<String, zHCF.Faction> factions = plugin.getFactions();

                for (String factionNameDisband : factions.keySet()) {
                    zHCF.Faction disbandFaction = factions.get(factionNameDisband);
                    if (disbandFaction.getLeader().equals(playerNameDisband)) {
                        factions.remove(factionNameDisband);
                        plugin.saveFactions();
                        player.sendMessage("Faction disbanded successfully.");
                        return true;
                    }
                }

                player.sendMessage("You are not a leader of any faction.");
                return true;
            case "who":
                if (args.length != 2) {
                    player.sendMessage("Usage: /faction who <faction-name>");
                    return false;
                }

                String factionName = args[1].toLowerCase();
                if (!plugin.getFactions().containsKey(factionName)) {
                    player.sendMessage("Faction " + args[1] + " does not exist.");
                    return false;
                }

                zHCF.Faction faction = plugin.getFactions().get(factionName);
                player.sendMessage("Faction Name: " + factionName);
                player.sendMessage("Leader: " + faction.getLeader());
                player.sendMessage("Co-Leaders: " + faction.getCoLeaders().stream().filter(member -> !member.equals(faction.getLeader())).collect(Collectors.joining(", ")));
                player.sendMessage("Captains: " + faction.getCaptains().stream().filter(member -> !member.equals(faction.getLeader())).collect(Collectors.joining(", ")));
                player.sendMessage("Members: " + faction.getMembers().stream().filter(member -> !member.equals(faction.getLeader())).collect(Collectors.joining(", ")));
                return true;

            case "invite":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return false;
                }

                String playerName = player.getName();
                HashMap<String, zHCF.Faction> factionsMap = plugin.getFactions();

                for (zHCF.Faction factionData : factionsMap.values()) {
                    if (factionData.getLeader().equals(playerName)) {
                        if (args.length != 2) {
                            player.sendMessage("Usage: /f invite <player>");
                            return false;
                        }

                        String targetName = args[1];
                        if (factionData.getMembers().contains(targetName)) {
                            player.sendMessage(targetName + " is already in your faction.");
                            return false;
                        }

                        Player target = Bukkit.getServer().getPlayer(targetName);
                        if (target == null) {
                            player.sendMessage(targetName + " is not online.");
                            return false;
                        }

                        target.sendMessage(playerName + " has invited you to join their faction.");
                        player.sendMessage("Invitation sent to " + targetName);
                        factionData.addInvitedPlayer(targetName);
                        return true;
                    }
                }

                player.sendMessage("You are not a leader of any faction.");
                break;
            case "join":
                if (sender instanceof Player) {
                    Player players = (Player) sender;
                    String playerNames = player.getName();
                    HashMap<String, zHCF.Faction> factionsMaps = plugin.getFactions();

                    for (zHCF.Faction factionData : factionsMaps.values()) {
                        if (factionData.getMembers().contains(playerNames) ||
                                factionData.getCaptains().contains(playerNames) ||
                                factionData.getCoLeaders().contains(playerNames)) {
                            player.sendMessage("You are already in a faction, please leave your current faction before joining a new one.");
                            return false;
                        }
                    }

                    if (args.length != 2) {
                        player.sendMessage("Usage: /f join <faction name>");
                        return false;
                    }

                    String factionNames = args[1];
                    if (!factionsMaps.containsKey(factionNames)) {
                        player.sendMessage("Faction not found.");
                        return false;
                    }

                    zHCF.Faction factions1 = factionsMaps.get(factionNames);
                    if (!factions1.getInvitedPlayers().contains(playerNames)) {
                        player.sendMessage("You have not been invited to join this faction.");
                        return false;
                    }

                    factions1.addMember(playerNames);
                    factions1.removeInvitedPlayer(playerNames);
                    player.sendMessage("You have joined the faction " + factionNames);
                    return true;
                }

            case "leave":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return false;
                }

                String playerNameLeave = player.getName();
                HashMap<String, zHCF.Faction> factionsMapLeave = plugin.getFactions();

                for (zHCF.Faction factionData : factionsMapLeave.values()) {
                    if (factionData.getLeader().equals(playerNameLeave) ||
                            factionData.getCoLeaders().contains(playerNameLeave) ||
                            factionData.getMembers().contains(playerNameLeave)) {

                        if (factionData.getLeader().equals(playerNameLeave)) {
                            player.sendMessage("Leaders cannot leave their faction. Transfer or disband the faction.");
                            return false;
                        }

                        factionData.removeMember(playerNameLeave);
                        player.sendMessage("You have left the faction.");
                        return true;
                    }
                }

                player.sendMessage("You are not a member of any faction.");
                return false;

            case "kick":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return false;
                }

                String playerNameKick = player.getName();
                HashMap<String, zHCF.Faction> factionsMapData = plugin.getFactions();

                for (zHCF.Faction factionData : factionsMapData.values()) {
                    if (factionData.getLeader().equals(playerNameKick) ||
                            factionData.getCoLeaders().contains(playerNameKick)) {

                        if (args.length != 2) {
                            player.sendMessage("Usage: /f kick <player>");
                            return false;
                        }

                        String targetName = args[1];
                        if (!factionData.getMembers().contains(targetName)) {
                            player.sendMessage(targetName + " is not a member of your faction.");
                            return false;
                        }

                        // send the target player a message
                        // ...

                        factionData.removeMember(targetName);
                        player.sendMessage(targetName + " has been kicked from the faction.");
                        return true;
                    }
                }

                player.sendMessage("You are not a leader or co-leader of any faction.");
                return false;
            case "promote":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return false;
                }

                String playerNames = player.getName();
                String targetName = args[1];
                HashMap<String, zHCF.Faction> factionsMaps = plugin.getFactions();

                for (zHCF.Faction factionData : factionsMaps.values()) {
                    if (factionData.getLeader().equals(playerNames) || factionData.getCoLeaders().contains(playerNames)) {
                        if (args.length != 2) {
                            player.sendMessage("Usage: /f promote <player>");
                            return false;
                        }

                        if (!factionData.getMembers().contains(targetName)) {
                            player.sendMessage(targetName + " is not a member of your faction.");
                            return false;
                        }

                        if (factionData.getCaptains().contains(targetName)) {
                            factionData.getCaptains().remove(targetName);
                            factionData.getCoLeaders().add(targetName);
                            player.sendMessage(targetName + " has been promoted to co-leader.");
                            return true;
                        } else if (factionData.getMembers().contains(targetName)) {
                            factionData.getMembers().remove(targetName);
                            factionData.getCaptains().add(targetName);
                            player.sendMessage(targetName + " has been promoted to captain.");
                            return true;
                        } else {
                            player.sendMessage(targetName + " is not a member of your faction.");
                            return false;
                        }
                    }
                }

                player.sendMessage("You must be a leader or co-leader to use this command.");
                return false;
            case "demote":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return false;
                }

                Player playerss = (Player) sender;
                String playerNamess = player.getName();
                HashMap<String, zHCF.Faction> factionsMapss = plugin.getFactions();

                for (zHCF.Faction factionData : factionsMapss.values()) {
                    if (factionData.getLeader().equals(playerNamess) || factionData.getCoLeaders().contains(playerNamess)) {
                        if (args.length != 2) {
                            player.sendMessage("Usage: /f demote <player>");
                            return false;
                        }

                        String targetNamess = args[1];
                        if (!factionData.getMembers().contains(targetNamess) && !factionData.getCaptains().contains(targetNamess) && !factionData.getCoLeaders().contains(targetNamess)) {
                            player.sendMessage(targetNamess + " is not a member of your faction.");
                            return false;
                        }

                        if (factionData.getCaptains().contains(targetNamess)) {
                            factionData.getCaptains().remove(targetNamess);
                            factionData.getMembers().add(targetNamess);
                            player.sendMessage(targetNamess + " has been demoted from captain to member.");
                        } else if (factionData.getCoLeaders().contains(targetNamess)) {
                            factionData.getCoLeaders().remove(targetNamess);
                            factionData.getCaptains().add(targetNamess);
                            player.sendMessage(targetNamess + " has been demoted from co-leader to captain.");
                        }
                        return true;
                    }
                }

                player.sendMessage("You are not a leader or co-leader of any faction.");
                break;





        }
        return true;
    }
}
