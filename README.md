# WhitelistByTime

## Statistics
Statistics are obtained through bstats metrics
![BStats](https://bstats.org/signatures/bukkit/WhitelistByTime.svg)

## Features
- Customization
- HEX support
- Placeholders (can be turned off)
- Memorizing players by nickname (case-sensitive or customizable)
- Storing data in any SQL database
- Convenience of specifying the time and checking the remaining duration

## Commands and Permissions
| Command                                          | Permission              |
|--------------------------------------------------|-------------------------|
| /whitelist on/off                                | whitelistbytime.turn    |
| /whitelist add  [nickname] (time)                | whitelistbytime.turn    |
| /whitelist remove [nickname]                     | whitelistbytime.remove  |
| /whitelist check [nickname]                      | whitelistbytime.check   |
| /whitelist checkme                               | whitelistbytime.checkme |
| /whitelist time set/add/remove [nickname] [time] | whitelistbytime.time    |
| /whitelist reload                                | whitelistbytime.reload  |
| /whitelist getall                                | whitelistbytime.getall  |
Note:
- [nickname] - this argument is required
- (time) - The duration for which the player will be added to the whitelist.\
  Example: 2d 3h 10m.\
  Leave this value empty if you want to add the player permanently.

## Placeholders:
All output can be configured in the config

*%wlbytime_in_whitelist%* - In whitelist or not\
*%wlbytime_time_left%* - How much is left in whitelist