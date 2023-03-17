package ru.nightmirror.wlbytime.interfaces.api;

import java.util.List;
import java.util.Map;

public interface IAPI {
    /**
     * Add player to whitelist
     * @param nickname Nickname of player in minecraft
     * @param until (ms) Time until which the player will be in the white list.
     *              -1 = the player will be on the white list forever
     * @return Is successful
     */
    boolean addPlayer(final String nickname, final long until);

    /**
     * Check if player in whitelist
     * @param nickname Nickname of player in minecraft
     * @return Is contains
     */
    boolean checkPlayer(final String nickname);

    /**
     * Get the timestamp until which the player will be on the whitelist
     * @param nickname Mickname of player in minecraft
     * @return Milliseconds. -1 if forever
     */
    long getUntil(final String nickname);

    /**
     * Get the formatting time how long will the player still be on the whitelist
     * @param nickname Nickname of player in minecraft
     * @return Formatting time
     */
    String getUntilString(final String nickname);

    /**
     * Remove player from whitelist
     * @param nickname Nickname of player in minecraft
     * @return Is successful
     */
    boolean removePlayer(final String nickname);

    /**
     * Get all player's in whitelist
     * @return Nicknames of players
     */
    Map<String, Long> getAllPlayers();

    /**
     *
     * @param nickname Nickname of player in minecraft
     * @param until (ms) Time until which the player will be in the white list.
     *      *              -1 = the player will be on the white list forever
     * @return Is successful
     */
    boolean setUntil(final String nickname, final long until);


    /**
     *
     * @return If WhitelistByTime working and filtering players
     */
    boolean isEnabled();
}
