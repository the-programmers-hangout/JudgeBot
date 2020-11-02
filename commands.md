# Commands

## Key 
| Symbol      | Meaning                        |
| ----------- | ------------------------------ |
| (Argument)  | Argument is not required.      |

## Configuration
| Commands      | Arguments   | Description                                                    |
| ------------- | ----------- | -------------------------------------------------------------- |
| configuration | GuildConfig | Update configuration parameters for this guild (conversation). |
| setup         |             | Configure a guild to use Judgebot.                             |

## Infraction
| Commands  | Arguments               | Description                                             |
| --------- | ----------------------- | ------------------------------------------------------- |
| cleanse   | Member                  | Use this to delete (permanently) as user's infractions. |
| strike, s | Member, (Integer), Text | Strike a user.                                          |
| warn, w   | Member, Text            | Warn a user.                                            |

## Mute
| Commands | Arguments          | Description                                             |
| -------- | ------------------ | ------------------------------------------------------- |
| gag      | Member             | Mute a user for 5 minutes while you deal with something |
| mute     | Member, Time, Text | Mute a user for a specified time.                       |
| unmute   | Member             | Unmute a user.                                          |

## Notes
| Commands     | Arguments            | Description                                       |
| ------------ | -------------------- | ------------------------------------------------- |
| cleansenotes | Member               | Use this to delete (permanently) as user's notes. |
| deleteNote   | Member, Integer      | Use this to add a delete a note from a user.      |
| note         | Member, Note Content | Use this to add a note to a user.                 |

## Rules
| Commands     | Arguments | Description                   |
| ------------ | --------- | ----------------------------- |
| addRule      |           | Add a rule to this guild.     |
| archiveRule  |           | Archive a rule in this guild. |
| editRule     |           | Edit a rule in this guild.    |
| listRules    |           | List the rules of this guild. |
| rule         | Rule      | List a rule from this guild.  |
| ruleHeadings |           | List the rules of this guild. |

## User
| Commands   | Arguments                           | Description                                                |
| ---------- | ----------------------------------- | ---------------------------------------------------------- |
| ban        | Member, (Delete message days), Text | Ban a member from this guild.                              |
| history, h | Member                              | Use this to view a user's record.                          |
| status, st | Member                              | Use this to view a user's status card.                     |
| unban      | User                                | Unban a banned member from this guild.                     |
| whatpfp    | User                                | Perform a reverse image search of a User's profile picture |

## Utility
| Commands | Arguments | Description          |
| -------- | --------- | -------------------- |
| Help     | (Command) | Display a help menu. |

