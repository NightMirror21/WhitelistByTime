<img src="./images/header.svg">

![BStats](https://bstats.org/signatures/bukkit/WhitelistByTime.svg)
*Statistics are obtained through bstats metrics.*

## Links
[SpigotMC](https://www.spigotmc.org/resources/whitelistbytime-1-12-2-1-20-4.98946/) - over 4500 downloads (26/01/2025).

## About
This is a plugin for a minecraft server. It allows you to add players for a certain time or permanently.

**The plugin is made and tested for paper 1.21.4, also supports folia.**

## Commands and Permissions
| Command                                                | Permission              |
|--------------------------------------------------------|-------------------------|
| /whitelistByTime add [nickname] (time)                 | whitelistbytime.add     |
| /whitelistByTime remove [nickname]                     | whitelistbytime.remove  |
| /whitelistByTime check [nickname]                      | whitelistbytime.check   |
| /whitelistByTime checkme                               | whitelistbytime.checkme |
| /whitelistByTime time set/add/remove [nickname] [time] | whitelistbytime.time    |
| /whitelistByTime getall (page)                         | whitelistbytime.getall  |
| /whitelistByTime freeze [nickname] [time]              | whitelistbytime.freeze  |

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
   No, I plan to develop the plugin for paper only.