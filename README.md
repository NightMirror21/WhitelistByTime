# WhitelistByTime

## Statistics
![BStats](https://bstats.org/signatures/bukkit/WhitelistByTime.svg)

## Features
- Full customization
- HEX support
- Placeholders (Can be turned off)
- Memorizing players by nickname case-sensitive or not (Customized)
- Storing data in any database type
- Executing the /whitelist command both in the console and in the game
- Convenience of specifying the time and checking how much is left

## Commands and Permissions
**/whitelist on/off** - *whitelistbytime.turn*\
**/whitelist add [nickname] (time)** - *whitelistbytime.add*\
**/whitelist remove [nickname]** - *whitelistbytime.remove*\
**/whitelist check [nickname]** - *whitelistbytime.check*\
**/whitelist checkme** - *whitelistbytime.checkme*\
**/whitelist time set/add/remove [nickname] [time]** - *whitelistbytime.time*\
**/whitelist reload** - *whitelistbytime.reload*\
**/whitelist getall** - *whitelistbytime.getall*
- (time) - time for which the player will be added to the whitelist\
 Example: 2d 3h 10m\
 Leave this value empty if you want to add player forever

## Placeholders:
All output can be configured in the config\
\
**%wlbytime_in_whitelist%** - *In whitelist or not*\
**%wlbytime_time_left%** - *How much is left in whitelist*