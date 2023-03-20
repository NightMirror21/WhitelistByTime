# WhitelistByTime

## Features
- Working at 1.12.2 - 1.19.4
- Full customization
- HEX support
- API
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
**/whitelist time [nickname] [time]** - *whitelistbytime.time"*\
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

## API

For usage API download .jar and add it to your project.

**Events:**\
PlayerAddedToWhitelistEvent\
PlayerRemovedFromWhitelist

### Example API usage:
```java
public class Command implements CommandExecutor {
    
    private final IAPI api = WhitelistByTime.getAPI();
    
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (String nickname : WhitelistByTimeAPI.getAllPlayers()) {
            api.removePlayer(nickname);
        }
        
        commandSender.sendMessage("Success!");
        return true;
    }
}
```

### Example PlayerAddedToWhitelistEvent usage:

```java
public class EventListener implements Listener {
    
    private final Logger log = Logger.getLogger("MySuperPlugin");

    @EventHandler
    public void onPlayerRemoved(PlayerRemovedFromWhitelistEvent event) {
        if (event.getNickname().equals("Notch")) {
            log.warning("Someone tried to remove Notch from whitelist!");

            event.setCancelled(true);
        }
    }
}
```

## Config
```yaml
####### SETTINGS #######

# Is whitelist enabled by default on startup
enabled: true
# Checks the player in the whitelist
checker-thread: true
# The delay through which the thread will check players. In seconds
checker-delay: 1
# Check the case of the nickname
case-sensitive: true


####### DATABASE #######

type: 'sqlite'

# If not sqlite or h2
address: 'localhost:3030'
name: 'minecraft'
table: 'whitelist'

# If using user and password
use-user-and-password: false
user: 'user'
password: 'qwerty123'

####### PLACEHOLDERS HOOK #######

placeholders-enabled: true

# %wlbytime_in_whitelist% - In whitelist or not
in-whitelist-true: '&a✔'
in-whitelist-false: '&c✖'

# %wlbytime_time_left% - How much is left in whitelist
time-left: '&a%time%'

# Show less information (without hours, minutes, seconds) when time left more than one day
less-info-time-left: false
less-info-expires-today: 'Expires in'

####### MESSAGES #######

minecraft-commands:
 plugin-reloaded: '&6Plugin reloaded!'
 not-permission: '&cYou do not have permission!'

 whitelist-enabled: '&aWhitelistByTime enabled'
 whitelist-already-enabled: '&aWhitelistByTime already enabled'
 whitelist-disabled: '&aWhitelistByTime disabled'
 whitelist-already-disabled: '&aWhitelistByTime already disabled'

 you-not-in-whitelist-kick:
  - '#d2d301Sorry, but you are not in whitelist'
  - 'Bye!'

 player-removed-from-whitelist: '&e%player% &fsuccessfully removed from whitelist'
 player-already-in-whitelist: '&e%player% &falready in whitelist'
 player-not-in-whitelist: '&e%player% &fnot in whitelist'

 # For command with time
 successfully-added-for-time: '&a%player% &fadded to whitelist for &a%time%'
 still-in-whitelist-for-time: '&a%player% &fwill be in whitelist still &a%time%'
 checkme-still-in-whitelist-for-time: '&fYou will remain on the whitelist for &a%time%'

 # For command without time
 successfully-added: '&a%player% &fadded to whitelist forever'
 still-in-whitelist: '&a%player% &fwill be in whitelist forever'
 checkme-still-in-whitelist: '&fYou are permanently whitelisted'

 list-title: '&a> Whitelist:'
 list-player: '&a| &f%player% &7[%time%]'
 list-empty: '&aWhitelist is empty'

 set-time: 'Now &a%player% &fwill be in whitelist for &a%time%'
 add-time: 'Added &a%time% &fto &a%player%'
 remove-time: 'Removed &a%time% &ffrom &a%player%'

 forever: 'forever'

 help:
  - '&a> WhitelistByTime - Help'
  - '&a| &f/whitelist on/off'
  - '&a| &f/whitelist add [nickname] (time)'
  - '&a| &f/whitelist remove [nickname]'
  - '&a| &f/whitelist check [nickname]'
  - '&a| &f/whitelist checkme'
  - '&a| &f/whitelist reload'
  - '&a| &f/whitelist getall'
  - '&a| &f/whitelist time set/add/remove [nickname] [time]'
  - '&a| &f(time) - time for which the player will be added to the whitelist'
  - '&a| &fExample: 2d 3h 10m'
  - '&a| &fLeave this value empty if you want to add player forever'



time-units:
 year:
  - 'y'
 month:
  - 'mo'
 week:
  - 'w'
 day:
  - 'd'
 hour:
  - 'h'
 minute:
  - 'm'
 second:
  - 's'
```
