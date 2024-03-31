<img src="./images/header.svg">

![BStats](https://bstats.org/signatures/bukkit/WhitelistByTime.svg)
*Statistics are obtained through bstats metrics.*

## About
This is a plugin for a minecraft server. It allows you to add players for a certain time or permanently.

## Commands and Permissions
| Command                                          | Permission              |
|--------------------------------------------------|-------------------------|
| /whitelistByTime on/off                                | whitelistbytime.turn    |
| /whitelistByTime add  [nickname] (time)                | whitelistbytime.turn    |
| /whitelistByTime remove [nickname]                     | whitelistbytime.remove  |
| /whitelistByTime check [nickname]                      | whitelistbytime.check   |
| /whitelistByTime checkme                               | whitelistbytime.checkme |
| /whitelistByTime time set/add/remove [nickname] [time] | whitelistbytime.time    |
| /whitelistByTime reload                                | whitelistbytime.reload  |
| /whitelistByTime getall (page)                         | whitelistbytime.getall  |

Note:
- [nickname] - this argument is required.
- (page) - page of list, if not specified, defaults to 1.
- (time) - the duration for which the player will be added to the whitelistByTime.\
  Example: 2d 3h 10m.\
  Leave this value empty if you want to add the player permanently.

## Placeholders:
All output can be configured in the config.

*%wlbytime_in_whitelist%* - In whitelistByTime or not.\
*%wlbytime_time_left%* - How much is left in whitelistByTime.

## FAQ
1. *Why does the plugin weigh so much?*\
   Because it contains libraries such as omrlite (database), caffein (cache), bstats (metrics), elytrium-serializer (configs).
2. *I have a problem/idea where should I write?*\
   Create an issue in this repository and describe it in detail. I would be happy to get any feedback!
3. *Will there be bungee\velocity support?*\
   Yes, it will, as soon as I learn how to use their api.
4. *What is the difference in the whitelistbytime between paper and spigot?*\
   The difference lies in the formatting of the messages. In Spigot, strings are used, whereas in Paper, components are utilized. To create colored messages in Spigot, you can use the '&' sign or hex codes (e.g., #ffffffff). However, in Paper, only MiniMessage formatting is supported.
