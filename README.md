<img src="./images/header.svg">

## Links
[SpigotMC](https://www.spigotmc.org/resources/whitelistbytime-1-21-4.98946/) [Modrinth](https://modrinth.com/plugin/whitelistbytime) [GitHub](https://github.com/NightMirror21/WhitelistByTime)

## About
This is a plugin for a minecraft server. It allows you to add players for a certain time or permanently.

**The plugin is developed and tested for Paper 1.20.x-1.21.x. It also supports Folia and Paper forks.**

## Features
- Fully **customizable**.
- Supports [**MiniMessage**](https://docs.advntr.dev/minimessage/format.html).
- Compatible with **any SQL database**.
- **Case-sensitive** nickname support.
- Player **freezing** feature.
- PlaceholderAPI (**PAPI**) support.
- **Lightweight**: minimal load on the **main thread**.
- **Multi-server** support: utilizes **SQL transactions**.
- **Safe**: the code is extensively covered by **automated tests**.

## Commands and Permissions
| Command                                                | Permission              |
|--------------------------------------------------------|-------------------------|
| /whitelist add [nickname] (time)                       | whitelistbytime.add     |
| /whitelist remove [nickname]                           | whitelistbytime.remove  |
| /whitelist check [nickname]                            | whitelistbytime.check   |
| /whitelist checkme                                     | whitelistbytime.checkme |
| /whitelist time set/add/remove [nickname] [time]       | whitelistbytime.time    |
| /whitelist getall (page)                               | whitelistbytime.getall  |
| /whitelist freeze [nickname] [time]                    | whitelistbytime.freeze  |
| /whitelist on/off/status                               | whitelistbytime.toggle  |

### Notes:
- `[nickname]` - required argument.
- `(page)` - list page number; defaults to 1 if not specified.
- `(time)` - the duration for which the player will be whitelisted.  
  Example: `2d 3h 10m`.  
  Leave empty to whitelist permanently.

## Placeholders
All output messages can be customized in the configuration.

- `%wlbytime_in_whitelist%` - Indicates if a player is in the whitelist.
- `%wlbytime_time_left%` - Time remaining in the whitelist.

## FAQ
1. **Why is the plugin file size large?**  
   It includes libraries such as ORMLite (database), Caffeine (cache), bStats (metrics), and Elytrium-Serializer (config handling).

2. **I have an issue or a suggestion. Where should I report it?**  
   Open an issue in this repository with a detailed description. Any feedback is welcome!

3. **Will there be support for BungeeCord/Velocity?**  
   No, the plugin is being developed exclusively for Paper and Folia.

## Configs by default
### `settings.yml`
```yaml
#Automatically unfreeze player time when they join the server if their time is frozen
unfreeze-time-on-player-join: false

#Enable whitelist checks for player login
whitelist-enabled: true

#Enable the expiration monitor, which checks players' expiration status
expire-monitor-enabled: true
#Interval in milliseconds for the expiration monitor to check players and remove them from the database if expired
expire-monitor-interval-ms: 5000

#Enable the last join monitor, which checks players' last join timestamps
last-join-monitor-enabled: false
#Threshold in seconds between the player's last join and the current time. If exceeded, the player is removed by the monitor
last-join-expiration-threshold-seconds: 2678400
#Interval in milliseconds for the last join monitor to check players and remove them if their last join exceeds the threshold
last-join-monitor-interval-ms: 3600000

#Enable case-sensitive nickname checking
nickname-case-sensitive: true

#Symbols representing time units for years
year-time-units:
  - "y"
#Symbols representing time units for months
month-time-units:
  - "mo"
#Symbols representing time units for weeks
week-time-units:
  - "w"
#Symbols representing time units for days
day-time-units:
  - "d"
#Symbols representing time units for hours
hour-time-units:
  - "h"
#Symbols representing time units for minutes
minute-time-units:
  - "m"
#Symbols representing time units for seconds
second-time-units:
  - "s"
```
### `database.yml`
```yaml
#'sqlite' or 'mysql'
type: "sqlite"

#If not sqlite or h2
address: "localhost:3030"
name: "wlbytime"

#Params for connection
params:
  - "autoReconnect=true"

#If using user and password
use-user-and-password: false
user: "user"
password: "qwerty123"
```
### `placeholders.yml`
```yaml
placeholders-enabled: false

#%wlbytime_in_whitelist% - In whitelist or not or frozen
in-whitelist-true: "✔"
in-whitelist-false: "✖"
frozen: "❄️"

#%wlbytime_time_left% - How much is left in whitelist
forever: "∞"
time-left: "%time%"
time-left-with-freeze: "❄️%time%❄️"
```
### `messages.yml`
```yaml
not-permission: "You do not have permission!"
incorrect-arguments: "Incorrect argument(s)"

you-not-in-whitelist-or-frozen-kick: "Sorry, but you are not in whitelist or frozen"

player-removed-from-whitelist: "%nickname% successfully removed from whitelist"
player-already-in-whitelist: "%nickname% already in whitelist"
player-not-in-whitelist: "%nickname% not in whitelist"
check-me-not-in-whitelist: "You are not in whitelist"
check-me-frozen: "You are frozen for %time%"

#For command with time
successfully-added-for-time: "%nickname% added to whitelist for %time%"
check-still-in-whitelist-for-time: "%nickname% will be in whitelist still %time%"
check-me-still-in-whitelist-for-time: "You will remain on the whitelist for %time%"

#For command without time
successfully-added: "%nickname% added to whitelist forever"
check-still-in-whitelist: "%nickname% will be in whitelist forever"
check-me-still-in-whitelist-forever: "You are permanently whitelisted"

list-header: "> Whitelist:"
list-element: "| %nickname% [%time-or-status%]"
list-empty: "Whitelist is empty"
list-footer: "Page %page% / %max-page% (To show another page run /whitelist getall <page>)"
page-not-exists: "Page %page% not exists, max page is %max-page%"

#How many records will be displayed per page
entries-for-page: 10

#For '%time-or-status%' in list
forever: "forever"
frozen: "frozen for %time%"
active: "active for %time%"
expired: "expired"

set-time: "Now %nickname% will be in whitelist for %time%"
add-time: "Added %time% to %nickname%"
remove-time: "Removed %time% from %nickname%"
cant-add-time-cause-player-is-forever: "Can't add time cause %nickname% is forever"
cant-remove-time-cause-player-is-forever: "Can't add time cause %nickname% is forever"
time-is-incorrect: "Time is incorrect"
cant-add-time: "Can't add time"
cant-remove-time: "Can't remove time"

player-frozen: "Player %nickname% frozen for %time%"
player-already-frozen: "Player %nickname% already frozen"
player-expired: "Player %nickname% expired"

whitelist-enabled: "Whitelist enabled"
whitelist-disabled: "Whitelist disabled"
whitelist-already-enabled: "Whitelist already enabled"
whitelist-already-disabled: "Whitelist already disabled"
whitelist-status-enabled: "Whitelist is enabled"
whitelist-status-disabled: "Whitelist is disabled"

help:
  - "> WhitelistByTime - Help"
  - "| /whitelist add [nickname] (time)"
  - "| /whitelist remove [nickname]"
  - "| /whitelist check [nickname]"
  - "| /whitelist checkme"
  - "| /whitelist getall"
  - "| /whitelist freeze [nickname] [time]"
  - "| /whitelist time set/add/remove [nickname] [time]"
  - "| /whitelist on"
  - "| /whitelist off"
  - "| /whitelist status"
  - "| (time) - time for which the player will be added to the whitelist"
  - "| Example: 2d 3h 10m"
  - "| Leave this value empty if you want to add player forever"
```

### `commands.yml`
```yaml
#Permissions for whitelist subcommands
add-permission: "wlbytime.add"
check-permission: "wlbytime.check"
check-me-permission: "wlbytime.checkme"
freeze-permission: "wlbytime.freeze"
get-all-permission: "wlbytime.getall"
toggle-permission: "wlbytime.toggle"
remove-permission: "wlbytime.remove"
time-permission: "wlbytime.time"
```

## Stats
![BStats](https://bstats.org/signatures/bukkit/WhitelistByTime.svg)
*Statistics are obtained through bstats metrics.*
