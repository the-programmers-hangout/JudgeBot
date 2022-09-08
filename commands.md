# Commands

## Key 
| Symbol      | Meaning                        |
|-------------|--------------------------------|
| [Argument]  | Argument is not required.      |
| /Category   | This is a subcommand group.    |

## /BanReason
| Commands | Arguments    | Description                                 |
|----------|--------------|---------------------------------------------|
| get      | User         | Get the ban reason for a user if it exists. |
| set      | User, Reason | Set a ban reason for a user.                |

## /Configuration
| Commands | Arguments                          | Description                                         |
|----------|------------------------------------|-----------------------------------------------------|
| channel  | ChannelType, Channel               | Set the Alert or Logging channels                   |
| reaction | Reaction, Emoji                    | Set the reactions used as various command shortcuts |
| role     | RoleType, Operation, Role          | Add or remove configured roles                      |
| setup    | LogChannel, AlertChannel, MuteRole | Configure a guild to use Judgebot.                  |
| view     |                                    | View guild configuration                            |

## /Message
| Commands | Arguments       | Description                                                                                   |
|----------|-----------------|-----------------------------------------------------------------------------------------------|
| remove   | Member, ID      | Remove a message record from a member. Only removes from history record, user DM will remain. |
| send     | Member, Content | Send an information message to a guild member                                                 |

## /Note
| Commands | Arguments         | Description                            |
|----------|-------------------|----------------------------------------|
| add      | User, Content     | Use this to add a note to a user.      |
| delete   | User, ID          | Use this to delete a note from a user. |
| edit     | User, ID, Content | Use this to edit a note.               |

## /Rule
| Commands | Arguments | Description                   |
|----------|-----------|-------------------------------|
| add      |           | Add a rule to this guild.     |
| archive  |           | Archive a rule in this guild. |
| edit     | Rule      | Edit a rule in this guild.    |

## Admin
| Commands          | Arguments            | Description                                                 |
|-------------------|----------------------|-------------------------------------------------------------|
| activePunishments |                      | View active punishments for a guild.                        |
| pointDecay        | Option, LowerUserArg | Freeze point decay for a user                               |
| removeInfraction  | User, ID             | Use this to delete (permanently) an infraction from a user. |
| reset             | LowerUserArg, choice | Reset a user's notes, infractions or whole record           |

## Context
| Commands           | Arguments | Description                                                                                     |
|--------------------|-----------|-------------------------------------------------------------------------------------------------|
| contextUserBadpfp  | User      | Apply a badpfp to a user (please use via the 'Apps' menu instead of as a command)               |
| contextUserHistory | User      | View a condensed history for this user (please use via the 'Apps' menu instead of as a command) |
| report             | Message   | Report a message to staff (please use via the 'Apps' menu instead of as a command)              |

## Infraction
| Commands | Arguments                     | Description                                                                  |
|----------|-------------------------------|------------------------------------------------------------------------------|
| badname  | Member                        | Rename a guild member that has a bad name.                                   |
| badpfp   | Option, Member                | Mutes a user and prompts them to change their pfp with a 30 minute ban timer |
| strike   | Member, Rule, Text, [Weight]  | Strike a user.                                                               |
| warn     | Member, Rule, Reason, [Force] | Warn a user.                                                                 |

## Mute
| Commands | Arguments            | Description                       |
|----------|----------------------|-----------------------------------|
| gag      | User                 | Mute a user for 5 minutes         |
| mute     | Member, Time, Reason | Mute a user for a specified time. |
| timeout  | Member, Time         | Time a user out                   |
| unmute   | Member               | Unmute a user.                    |

## Rules
| Commands  | Arguments | Description                                                                                       |
|-----------|-----------|---------------------------------------------------------------------------------------------------|
| longRules | [Message] | List the rules (with descriptions) of this guild. Pass a message ID to edit existing rules embed. |
| rules     | [Message] | List the rules of this guild. Pass a message ID to edit existing rules embed.                     |
| viewRule  | Rule      | List a rule from this guild.                                                                      |

## User
| Commands        | Arguments                    | Description                                                     |
|-----------------|------------------------------|-----------------------------------------------------------------|
| alt             | Option, Main, [Alt]          | Link, Unlink or view a user's alt accounts                      |
| ban             | LowerUserArg, Reason, [Days] | Ban a member from this guild.                                   |
| deletedMessages | User                         | View a users messages deleted using the delete message reaction |
| history         | User                         | Use this to view a user's record.                               |
| unban           | User, [Thin-Ice]             | Unban a banned member from this guild.                          |
| whatpfp         | User                         | Perform a reverse image search of a User's profile picture      |

## Utility
| Commands    | Arguments | Description                   |
|-------------|-----------|-------------------------------|
| Help        | [Command] | Display a help menu.          |
| info        |           | Bot info for Judgebot         |
| selfHistory |           | View your infraction history. |

