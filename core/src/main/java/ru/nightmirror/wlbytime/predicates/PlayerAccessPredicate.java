package ru.nightmirror.wlbytime.predicates;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.models.PlayerData;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerAccessPredicate  {

    boolean caseSensitive;
    PlayerDao playerDao;

    public boolean test(PlayerData playerDao) {
        // todo implements
        return true;
    }

    public boolean test(String nickname) {
        // todo implements
        return true;
    }
}
