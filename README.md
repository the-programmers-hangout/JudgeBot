# judgebot

> A Discord bot to manage guild rules, infractions and punishments for guild members that don't play nicely.

# Setup
```
$ cp .env.example .env
```

Edit the .env file with your favourite editor, filling out the token and default prefix.

You will need to have a MongoDB instance running locally that the bot can connect to. By default, the bot expects Mongo to be running at:
```
mongodb://localhost:27017
```
This can be changed in the configuration file if needed.
