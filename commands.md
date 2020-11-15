# Commands

## Key 
| Symbol      | Meaning                        |
| ----------- | ------------------------------ |
| (Argument)  | Argument is not required.      |

## Configuration
| Commands      | Arguments     | Description                                                    |
| ------------- | ------------- | -------------------------------------------------------------- |
| configuration | (GuildConfig) | Update configuration parameters for this guild (conversation). |
| setup         |               | Configure a guild to use Judgebot.                             |

## Information
| Commands   | Arguments                    | Description                                         |
| ---------- | ---------------------------- | --------------------------------------------------- |
| info       | LowerMemberArg, Info Content | Send an information message to a guild member       |
| removeInfo | LowerMemberArg, Integer      | Remove an information message from a member record. |

## Infraction
| Commands         | Arguments                       | Description                                                                                                                           |
| ---------------- | ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| badpfp           | (cancel), LowerMemberArg        | Notifies the user that they should change their profile pic and applies a 30 minute mute. Bans the user if they don't change picture. |
| cleanse          | LowerMemberArg                  | Use this to delete (permanently) as user's infractions.                                                                               |
| removeInfraction | LowerMemberArg, Infraction ID   | Use this to delete (permanently) an infraction from a user.                                                                           |
| strike, s        | LowerMemberArg, (Integer), Text | Strike a user.                                                                                                                        |
| warn, w          | LowerMemberArg, Text            | Warn a user.                                                                                                                          |

## Mute
| Commands | Arguments                  | Description                                             |
| -------- | -------------------------- | ------------------------------------------------------- |
| gag      | LowerMemberArg             | Mute a user for 5 minutes while you deal with something |
| mute     | LowerMemberArg, Time, Text | Mute a user for a specified time.                       |
| unmute   | LowerMemberArg             | Unmute a user.                                          |

## Notes
| Commands     | Arguments               | Description                                       |
| ------------ | ----------------------- | ------------------------------------------------- |
| cleansenotes | LowerMemberArg          | Use this to delete (permanently) as user's notes. |
| deleteNote   | LowerMemberArg, Integer | Use this to add a delete a note from a user.      |
| note         | User, Note Content      | Use this to add a note to a user.                 |

## Rules
| Commands    | Arguments | Description                                       |
| ----------- | --------- | ------------------------------------------------- |
| addRule     |           | Add a rule to this guild.                         |
| archiveRule |           | Archive a rule in this guild.                     |
| editRule    |           | Edit a rule in this guild.                        |
| longRules   |           | List the rules (with descriptions) of this guild. |
| rule        | Rule      | List a rule from this guild.                      |
| rules       |           | List the rules of this guild.                     |

## User
| Commands     | Arguments                                   | Description                                                |
| ------------ | ------------------------------------------- | ---------------------------------------------------------- |
| ban          | LowerMemberArg, (Delete message days), Text | Ban a member from this guild.                              |
| getBanReason | User                                        | Get a ban reason for a banned user                         |
| history, h   | User                                        | Use this to view a user's record.                          |
| selfHistory  |                                             | View your infraction history (contents will be DM'd)       |
| setBanReason | User, Text                                  | Set a ban reason for a banned user                         |
| status, st   | User                                        | Use this to view a user's status card.                     |
| unban        | User                                        | Unban a banned member from this guild.                     |
| whatpfp      | User                                        | Perform a reverse image search of a User's profile picture |

## Utility
| Commands | Arguments | Description          |
| -------- | --------- | -------------------- |
| Help     | (Command) | Display a help menu. |

