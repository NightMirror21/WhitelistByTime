package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GetAllCommand implements Command {

    private static final int DEFAULT_PAGE = 1;

    MessagesConfig messages;
    EntryService service;
    TimeConvertor convertor;

    @Override
    public String getPermission() {
        return "wlbytime.getall";
    }

    @Override
    public String getName() {
        return "getall";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        int page = parsePage(args, issuer);
        if (page == -1) {
            return;
        }

        List<Entry> entries = new ArrayList<>(service.getEntries())
                .stream().sorted(Comparator.comparing(Entry::getNickname))
                .toList();

        if (entries.isEmpty()) {
            issuer.sendMessage(messages.getListEmpty());
            return;
        }

        int entriesPerPage = messages.getEntriesForPage();
        int totalEntries = entries.size();
        int maxPage = calculateMaxPage(totalEntries, entriesPerPage);

        if (!validatePage(page, maxPage, issuer)) {
            return;
        }

        issuer.sendMessage(messages.getListHeader());
        sendPaginatedEntries(entries, page, entriesPerPage, totalEntries, issuer);
        sendPageFooter(page, maxPage, issuer);
    }

    private int calculateMaxPage(int totalEntries, int entriesPerPage) {
        return (totalEntries + entriesPerPage - 1) / entriesPerPage;
    }

    private void sendPaginatedEntries(List<Entry> entries, int page, int entriesPerPage, int totalEntries, CommandIssuer issuer) {
        int startIdx = (page - 1) * entriesPerPage;
        int endIdx = Math.min(startIdx + entriesPerPage, totalEntries);
        for (int i = startIdx; i < endIdx; i++) {
            Entry entry = entries.get(i);
            String message = formatEntryMessage(entry);
            issuer.sendMessage(message);
        }
    }

    private void sendPageFooter(int page, int maxPage, CommandIssuer issuer) {
        issuer.sendMessage(messages.getListFooter()
                .replace("%page%", String.valueOf(page))
                .replace("%max-page%", String.valueOf(maxPage))
        );
    }

    private int parsePage(String[] args, CommandIssuer issuer) {
        if (args.length == 0) return DEFAULT_PAGE;
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return -1;
        }
    }

    private boolean validatePage(int page, int maxPage, CommandIssuer issuer) {
        if (page > maxPage) {
            issuer.sendMessage(messages.getPageNotExists()
                    .replace("%page%", String.valueOf(page))
                    .replace("%max-page%", String.valueOf(maxPage))
            );
            return false;
        }
        return true;
    }

    private String formatEntryMessage(Entry entry) {
        String timeOrStatus = getTimeOrStatus(entry);
        return messages.getListElement()
                .replace("%nickname%", entry.getNickname())
                .replace("%time-or-status%", timeOrStatus);
    }

    private String getTimeOrStatus(Entry entry) {
        if (entry.isForever()) {
            return messages.getForever();
        } else if (entry.isFreezeActive()) {
            String remainingTime = convertor.getTimeLine(entry.getLeftFreezeTime());
            return messages.getFrozen().replace("%time%", remainingTime);
        } else if (entry.isActive()) {
            String remainingTime = convertor.getTimeLine(entry.getLeftActiveTime());
            return messages.getActive().replace("%time%", remainingTime);
        } else {
            return messages.getExpired();
        }
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return generatePages(20);
        }
        return Set.of();
    }

    private Set<String> generatePages(int total) {
        Set<String> pages = new HashSet<>();
        for (int i = 1; i <= total; i++) {
            pages.add(String.valueOf(i));
        }
        return pages;
    }
}
