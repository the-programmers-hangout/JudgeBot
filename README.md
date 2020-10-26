# judgebot

> A Discord bot to manage guild rules, infractions and punishments for guild members that don't play nicely.

# Setup
Running the bot locally expects a token to be passed in via command line arguments (via intelliJ build configurations).

You will also need to have a MongoDB instance running locally that the bot can connect to. By default, the bot expects Mongo to be running at:
```
mongodb://localhost:27017
```
This can be changed in the configuration file if needed.
