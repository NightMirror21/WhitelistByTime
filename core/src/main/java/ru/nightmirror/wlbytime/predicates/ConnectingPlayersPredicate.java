package ru.nightmirror.wlbytime.predicates;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;

import java.util.function.Predicate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectingPlayersPredicate implements Predicate<String> {

    WhitelistByTime plugin;
    PlayerAccessPredicate playerAccessPredicate;

    @Override
    public boolean test(String nickname) {
        if (!plugin.isWhitelistEnabled()) return true;

        if (playerAccessPredicate.test(nickname)) {
            // todo unfreeze if freezed
            return true;
        } else {
            return false;
        }
    }
}
