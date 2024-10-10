package ru.nightmirror.wlbytime.predicates;

import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;

import java.util.function.Predicate;

public class InactivePlayerPredicate implements Predicate<PlayerDao> {
    @Override
    public boolean test(PlayerDao playerDao) {
        // todo implements
        return false;
    }
}
