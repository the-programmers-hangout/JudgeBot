# judgebot

> A Discord bot to manage guild rules, infractions and punishments for guild members that don't play nicely.

# Setup
This bot can be setup and run locally using Docker and Docker-Compose. To start Judgebot locally, do the following:
```
$ cp .env.example .env
```
Edit the .env file with your favourite editor, filling out the token, default prefix and mongo configuration.

```
$ docker-compose up --build --detach
```
