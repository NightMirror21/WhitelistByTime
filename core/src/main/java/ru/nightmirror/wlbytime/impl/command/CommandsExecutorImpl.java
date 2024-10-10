package ru.nightmirror.wlbytime.impl.command;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.command.CommandsExecutor;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.WrappedCommandSender;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.models.PlayerData;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandsExecutorImpl implements CommandsExecutor {

    PlayerDao playerDao;
    WhitelistByTime whitelistByTime;
    TimeConvertor timeConvertor;
    MessagesConfig messages;

    public CommandsExecutorImpl(PlayerDao playerDao, WhitelistByTime whitelistByTime, TimeConvertor timeConvertor) {
        this.playerDao = playerDao;
        this.whitelistByTime = whitelistByTime;
        messages = whitelistByTime.getMessages();
        this.timeConvertor = timeConvertor;
    }

    @Override
    public void reload(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.reload"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        whitelistByTime.reload();
        sender.sendMessage(messages.getPluginReloaded());
    }

    @Override
    public void help(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.help"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        messages.getHelp().forEach(sender::sendMessage);
    }

    @Override
    public void getAll(WrappedCommandSender sender, String[] strings) {
        if (!sender.hasPermission("whitelistbytime.getall")) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        playerDao.getPlayers()
                .thenApply(list -> list.stream().filter(PlayerData::canPlay).toList())
                .thenApply(list -> list.stream().sorted(Comparator.comparingLong(PlayerData::calculateUntil).reversed()).toList())
                .thenAccept(players -> handlePlayerList(sender, strings, players));
    }

    private void handlePlayerList(WrappedCommandSender sender, String[] strings, List<PlayerData> players) {
        if (players.isEmpty()) {
            sender.sendMessage(messages.getListEmpty());
            return;
        }

        int page = getPage(strings);
        int displayOnPage = 5;
        int maxPage = calculateMaxPage(players.size(), displayOnPage);

        if (page > maxPage) {
            sender.sendMessage(messages.getPageNotExists().replaceAll("%page%", String.valueOf(page)));
            return;
        }

        List<PlayerData> toDisplay = getPlayersToDisplay(players, page, displayOnPage);
        sendPlayerList(sender, toDisplay, page, maxPage);
    }

    private int getPage(String[] strings) {
        return strings.length > 1 ? tryToConvert(strings[1]).orElse(1) : 1;
    }

    private int calculateMaxPage(int size, int displayOnPage) {
        return size % displayOnPage != 0 ? size / displayOnPage + 1 : size / displayOnPage;
    }

    private List<PlayerData> getPlayersToDisplay(List<PlayerData> players, int page, int displayOnPage) {
        int startIndex = (page - 1) * displayOnPage;
        int endIndex = Math.min(page * displayOnPage, players.size());
        return new ArrayList<>(players.subList(startIndex, endIndex));
    }

    private void sendPlayerList(WrappedCommandSender sender, List<PlayerData> players, int page, int maxPage) {
        sender.sendMessage(messages.getListTitle());

        players.forEach(player -> {
            String time = getPlayerTime(player);
            sender.sendMessage(messages.getListPlayer().replace("%player%", player.getNickname()).replace("%time%", time.trim()));
        });

        if (maxPage > 1) {
            sender.sendMessage(messages.getListPageableCommands().replace("%current-page%", String.valueOf(page)).replace("%max-page%", String.valueOf(maxPage)));
        }
    }

    private String getPlayerTime(PlayerData player) {
        if (player.isForever()) {
            return messages.getForever();
        } else if (player.isFrozen()) {
            return messages.getFrozen();
        } else if (player.isNotInWhitelist()) {
            return messages.getExpired();
        } else {
            return timeConvertor.getTimeLine(player.calculateUntil() - System.currentTimeMillis());
        }
    }

    @Override
    public void remove(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.remove"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        String removeNickname = strings[1];
        playerDao.getPlayer(removeNickname).thenCompose(playerDataOptional -> {
            if (playerDataOptional.isEmpty()) {
                sender.sendMessage(messages.getPlayerNotInWhitelist()
                        .replaceAll("%player%", removeNickname));
                return CompletableFuture.completedFuture(null);
            } else {
                PlayerData playerData = playerDataOptional.get();
                playerData.setUntil(0L);
                return playerDao.createOrUpdate(playerData)
                        .thenRun(() -> sender.sendMessage(messages.getPlayerRemovedFromWhitelist()
                                .replaceAll("%player%", removeNickname)));
            }
        });
    }

    @Override
    public void switchFreeze(WrappedCommandSender sender, String[] strings) {
        if (!sender.hasPermission("whitelistbytime.switchfreeze")) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        String nickname = strings[1];
        playerDao.getPlayer(nickname).thenCompose(dataOptional -> {
            if (dataOptional.isPresent() && dataOptional.get().canPlay()) {
                PlayerData data = dataOptional.get();
                data.switchFreeze();

                String messageKey = data.isFrozen() ? messages.getPlayerFrozen() : messages.getPlayerUnfrozen();
                sender.sendMessage(messageKey.replaceAll("%player%", nickname));

                return playerDao.createOrUpdate(data);
            } else {
                sender.sendMessage(messages.getPlayerNotInWhitelist().replaceAll("%player%", nickname));
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public void check(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.check"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        String checkNickname = strings[1];
        playerDao.getPlayer(checkNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (!player.canPlay()) {
                sender.sendMessage(messages.getPlayerNotInWhitelist()
                        .replaceAll("%player%", checkNickname));
            } else if (player.isForever()) {
                sender.sendMessage(messages.getStillInWhitelist()
                        .replaceAll("%player%", checkNickname));
            } else {
                String time = timeConvertor.getTimeLine(player.calculateUntil() - System.currentTimeMillis());
                sender.sendMessage(messages.getStillInWhitelistForTime()
                        .replaceAll("%player%", checkNickname)
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(messages.getPlayerNotInWhitelist()
                .replaceAll("%player%", checkNickname))));
    }

    @Override
    public void checkme(WrappedCommandSender sender) {
        if (!(sender.hasPermission("whitelistbytime.checkme"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        playerDao.getPlayer(sender.getNickname()).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (!player.canPlay()) {
                sender.sendMessage(messages.getPlayerNotInWhitelist()
                        .replaceAll("%player%", sender.getNickname()));
            } else if (player.isForever()) {
                sender.sendMessage(messages.getCheckMeStillInWhitelist());
            } else {
                String time = timeConvertor.getTimeLine(player.calculateUntil() - System.currentTimeMillis());
                sender.sendMessage(messages.getCheckMeStillInWhitelistForTime()
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(messages.getPlayerNotInWhitelist()
                .replaceAll("%player%", sender.getNickname()))));
    }

    @Override
    public void add(WrappedCommandSender sender, String[] strings) {
        if (!sender.hasPermission("whitelistbytime.add")) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        String addNickname = strings[1];
        playerDao.getPlayer(addNickname).thenAccept(playerOptional -> {
            if (playerOptional.isPresent()) {
                sender.sendMessage(messages.getPlayerAlreadyInWhitelist().replaceAll("%player%", addNickname));
            } else {
                long until = parseUntil(strings);
                PlayerData newPlayerData = new PlayerData(addNickname, until);
                playerDao.createOrUpdate(newPlayerData).thenRun(() -> {
                    String message = (until == -1L) ? messages.getSuccessfullyAdded().replaceAll("%player%", addNickname)
                            : messages.getSuccessfullyAddedForTime()
                            .replaceAll("%player%", addNickname)
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L));
                    sender.sendMessage(message);
                });
            }
        });
    }

    private long parseUntil(String[] strings) {
        long current = System.currentTimeMillis();
        long until = current;

        if (strings.length > 2) {
            for (int i = 2; i < strings.length; i++) {
                until += timeConvertor.getTimeMs(strings[i]);
            }
        }

        if (until == current) until = -1L;
        return until;
    }

    @Override
    public void time(WrappedCommandSender sender, String[] strings) {
        if (!sender.hasPermission("whitelistbytime.time")) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }
        String nickname = strings[2];
        playerDao.getPlayer(nickname).thenAccept(playerOptional -> {
            long until = System.currentTimeMillis();
            for (int i = 3; i < strings.length; i++) {
                until += timeConvertor.getTimeMs(strings[i]);
            }
            handleTimeCommand(strings[1], sender, playerOptional.orElse(null), nickname, until);
        });
    }

    private void handleTimeCommand(String commandType, WrappedCommandSender sender, PlayerData player, String nickname, long until) {
        switch (commandType) {
            case "set" -> {
                if (player == null) {
                    handleTimeSetNull(sender, nickname, until);
                } else {
                    handleTimeSet(sender, player, nickname, until);
                }
            }
            case "add" -> {
                if (player == null) {
                    handleTimeAddNull(sender, nickname, until);
                } else {
                    handleTimeAdd(sender, player, nickname, until);
                }
            }
            case "remove" -> {
                if (player == null) {
                    handleTimeRemoveNull(sender, nickname, until);
                } else {
                    handleTimeRemove(sender, player, nickname, until);
                }
            }
            default -> messages.getHelp().forEach(sender::sendMessage);
        }
    }

    private void handleTimeSet(WrappedCommandSender sender, PlayerData player, String nickname, long until) {
        player.setUntil(until);
        playerDao.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.getSetTime()
                .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                .replace("%player%", nickname)));
    }

    private void handleTimeSetNull(WrappedCommandSender sender, String nickname, long until) {
        PlayerData player = new PlayerData(nickname, until);
        player.setUntil(until);
        playerDao.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.getSuccessfullyAddedForTime()
                .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                .replace("%player%", nickname)));
    }

    private void handleTimeAdd(WrappedCommandSender sender, PlayerData player, String nickname, long until) {
        if (!player.canPlay()) {
            player.setUntil(until);
            playerDao.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.getSuccessfullyAddedForTime()
                    .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                    .replace("%player%", nickname)));
        } else {
            player.setUntil(player.calculateUntil() + (until - System.currentTimeMillis()));
            playerDao.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.getAddTime()
                    .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                    .replace("%player%", nickname)));
        }
    }

    private void handleTimeAddNull(WrappedCommandSender sender, String nickname, long until) {
        PlayerData player = new PlayerData(nickname, until);
        playerDao.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.getSuccessfullyAddedForTime()
                .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                .replace("%player%", nickname)));
    }

    private void handleTimeRemove(WrappedCommandSender sender, PlayerData player, String nickname, long until) {
        if (player.canPlay()) {
            player.setUntil(player.calculateUntil() - (until - System.currentTimeMillis()));
            playerDao.createOrUpdate(player).thenRun(() -> {
                if ((player.calculateUntil() - (until - System.currentTimeMillis())) > System.currentTimeMillis()) {
                    sender.sendMessage(messages.getRemoveTime()
                            .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replace("%player%", nickname));
                } else {
                    sender.sendMessage(messages.getPlayerRemovedFromWhitelist()
                            .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replace("%player%", nickname));
                }
            });
        } else {
            sender.sendMessage(messages.getPlayerNotInWhitelist()
                    .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis()))
                    .replace("%player%", nickname));
        }
    }

    private void handleTimeRemoveNull(WrappedCommandSender sender, String nickname, long until) {
        sender.sendMessage(messages.getPlayerNotInWhitelist()
                .replace("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis()))
                .replace("%player%", nickname));
    }

    @Override
    public void turn(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.turn"))) {
            sender.sendMessage(messages.getNotPermission());
            return;
        }

        if (strings[0].equalsIgnoreCase("on")) {
            if (whitelistByTime.isWhitelistEnabled()) {
                sender.sendMessage(messages.getWhitelistAlreadyEnabled());
            } else {
                sender.sendMessage(messages.getWhitelistEnabled());
                whitelistByTime.setWhitelistEnabled(true);
            }
        } else {
            if (!whitelistByTime.isWhitelistEnabled()) {
                sender.sendMessage(messages.getWhitelistAlreadyDisabled());
            } else {
                sender.sendMessage(messages.getWhitelistDisabled());
                whitelistByTime.setWhitelistEnabled(false);
            }
        }
    }

    @Override
    public void execute(WrappedCommandSender sender, String[] strings) {
        if (strings.length == 0 || strings[0].isEmpty()) {
            help(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("add")) {
            add(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("remove")) {
            remove(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("switchfreeze")) {
            switchFreeze(sender, strings);
        } else if (strings[0].equals("on") || strings[0].equals("off")) {
            turn(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("check")) {
            check(sender, strings);
        } else if (strings.length == 1 && strings[0].equals("checkme")) {
            checkme(sender);
        } else if (strings[0].equals("reload")) {
            reload(sender, strings);
        } else if (strings.length > 3 && strings[0].equals("time")) {
            time(sender, strings);
        } else if (strings[0].equals("getall")) {
            getAll(sender, strings);
        } else {
            help(sender, strings);
        }
    }

    private Optional<Integer> tryToConvert(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
