# WhitelistByTime

## Features
- Working at 1.12.2 - 1.19
- Full customization
- API
- Memorizing players by nickname case-sensitive
- Storing data in SQLite or MySQL
- Executing the /whitelist command both in the console and in the game
- Convenience of specifying the time and checking how much is left

## Commands
**/whitelist add [nickname] (time)** - *whitelistbytime.add*\
**/whitelist remove [nickname]** - *whitelistbytime.remove*\
**/whitelist check [nickname]** - *whitelistbytime.check*\
**/whitelist time [nickname] [time]** - *whitelistbytime.time"*\
**/whitelist reload** - *whitelistbytime.reload*\
**/whitelist getall** - *whitelistbytime.getall*
- (time) - time for which the player will be added to the whitelist\
 Example: 2d 3h 10m\
 Leave this value empty if you want to add player forever

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
public class EventHandler implements Listener {
    
    private final Logger log = Logger.getLogger("MySuperPlugin");

    @org.bukkit.event.EventHandler
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

# Checks the player in the whitelist
checker-thread: true
# The delay through which the thread will check players. In seconds
checker-delay: 1


####### DATABASE #######

is-mysql-enabled: false

# If MySQL is disabled, plugin using SQLite
database-file-name: data.db

# If MySQL is enabled
mysql-connection: mysuperdomain.com/database_name:3306
mysql-user: user
mysql-password: qwerty123



####### MESSAGES #######

minecraft-commands:
 plugin-reloaded: '&6Plugin reloaded!'
 not-permission: '&cYou do not have permission!'

 you-not-in-whitelist: '&6Sorry, but you are not in whitelist'
 player-removed-from-whitelist: '&e%player% successfully removed from whitelist'
 player-already-in-whitelist: '&e%player% already in whitelist'
 player-not-in-whitelist: '&e%player% not in whitelist'

 # For command with time
 successfully-added-for-time: '&a%player% added to whitelist for %time%'
 still-in-whitelist-for-time: '&a%player% will be in whitelist still %time%'

 # For command without time
 successfully-added: '&a%player% added to whitelist forever'
 still-in-whitelist: '&a%player% will be in whitelist forever'

 list-title: '&a> Whitelist:'
 list-player: '&a| &f%player% &7[%time%]'
 list-empty: '&aWhitelist is empty'

 set-time: 'Now &a%player% &fwill be in whitelist for &a%time%'
 add-time: 'Added &a%time% &fto &a%player%'
 remove-time: 'Removed &a%time% &ffrom &a%player%'

 forever: 'forever'

 help:
  - '&a> WhitelistByTime - Help'
  - '&a| &f/whitelist add [nickname] (time)'
  - '&a| &f/whitelist remove [nickname]'
  - '&a| &f/whitelist check [nickname]'
  - '&a| &f/whitelist reload'
  - '&a| &f/whitelist getall'
  - '&a| &f/whitelist time set/add/remove [nickname] [time]'
  - '&a| &f(time) - time for which the player will be added to the whitelist'
  - '&a| &fExample: 2d 3h 10m'
  - '&a| &fLeave this value empty if you want to add player forever'



time-units:
 year: 'y'
 month: 'mo'
 week: 'w'
 day: 'd'
 hour: 'h'
 minute: 'm'
 second: 's'
```