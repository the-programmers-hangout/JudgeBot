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
| removeInfo | LowerMemberArg, Info ID      | Remove an information message from a member record. |

## Infraction
| Commands         | Arguments                        | Description                                                                                                                           |
| ---------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| badpfp           | (cancel), LowerMemberArg         | Notifies the user that they should change their profile pic and applies a 30 minute mute. Bans the user if they don't change picture. |
| cleanse          | LowerMemberArg                   | Use this to delete (permanently) as user's infractions.                                                                               |
| removeInfraction | LowerMemberArg, Infraction ID    | Use this to delete (permanently) an infraction from a user.                                                                           |
| strike, s        | LowerMemberArg, (Weight), Reason | Strike a user.                                                                                                                        |
| warn, w          | LowerMemberArg, Reason           | Warn a user.                                                                                                                          |

## Mute
| Commands | Arguments                    | Description                                             |
| -------- | ---------------------------- | ------------------------------------------------------- |
| gag      | LowerMemberArg               | Mute a user for 5 minutes while you deal with something |
| mute     | LowerMemberArg, Time, Reason | Mute a user for a specified time.                       |
| unmute   | LowerMemberArg               | Unmute a user.                                          |

## Notes
| Commands     | Arguments               | Description                                       |
| ------------ | ----------------------- | ------------------------------------------------- |
| cleansenotes | LowerMemberArg          | Use this to delete (permanently) as user's notes. |
| deleteNote   | LowerMemberArg, Note ID | Use this to add a delete a note from a user.      |
| note         | User, Note Content      | Use this to add a note to a user.                 |

## Rules
| Commands    | Arguments | Description                                                                                       |
| ----------- | --------- | ------------------------------------------------------------------------------------------------- |
| addRule     |           | Add a rule to this guild.                                                                         |
| archiveRule |           | Archive a rule in this guild.                                                                     |
| editRule    |           | Edit a rule in this guild.                                                                        |
| longRules   | (Message) | List the rules (with descriptions) of this guild. Pass a message ID to edit existing rules embed. |
| rule        | Rule      | List a rule from this guild.                                                                      |
| rules       | (Message) | List the rules of this guild. Pass a message ID to edit existing rules embed.                     |

## User
| Commands     | Arguments                                 | Description                                                |
| ------------ | ----------------------------------------- | ---------------------------------------------------------- |
| ban          | LowerUserArg, (Delete message days), Text | Ban a member from this guild.                              |
| getBanReason | User                                      | Get a ban reason for a banned user                         |
| history, h   | User                                      | Use this to view a user's record.                          |
| selfHistory  |                                           | View your infraction history (contents will be DM'd)       |
| setBanReason | User, Reason                              | Set a ban reason for a banned user                         |
| unban        | User                                      | Unban a banned member from this guild.                     |
| whatpfp      | User                                      | Perform a reverse image search of a User's profile picture |

## Utility
| Commands | Arguments | Description          |
| -------- | --------- | -------------------- |
| Help     | (Command) | Display a help menu. |

