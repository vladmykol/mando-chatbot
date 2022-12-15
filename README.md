# Mando - create free chatbot

[![Build status](https://travis-ci.com/vladmykol/mando-chatbot.svg?branch=master)](https://travis-ci.com/vladmykol/mando-chatbot)

It's a free chatbot builder platform powered by SpringBoot and ApacheOpenNLP libs. <a href="https://medium.vladmykol.com/my-nlp-chatbot-from-idea-to-500-users-3fadce3335b9">Read more...</a> 

![Bot Preview](./src/main/resources/static/bot-promo.jpg)

Use Mando chatbot to create and manage your own chatbot using Telegram.

Just try yourself here -> <a href="https://t.me/create_free_chatbot">Open Telegram Bot Mando</a>

## Quick Start

**Prerequisites**

Please be sure that the following components are installed on your computer before running locally:

- JDK 11
- Gradlew
- MongoDb

**Custom properties**

You also need to set the following properties.

~~~~
# telegram
TELEGRAM_BOT_NAME=yourBotName
TELEGRAM_BOT_KEY=yourBotApiKey
TELEGRAM_BOT_OWNER_USERID=yourTelegramChatId
# db
MONGO_URL=mongodb://localhost:27017/bot-db
# openAI
GPT3_TOKEN=youApiKey
~~~~

## Current issues

- no unit test coverage
- overcomplicated Telegram workflow

## Deploy

### Setup new server

1. Create [new server and access it via SSH](https://www.banjocode.com/post/hosting/setup-server-hetzner/)
1. Install [dokku](https://dokku.com/docs/getting-started/installation/#1-install-dokku) on your newly created server
1. Run the following command to set up new application on dokku

```
dokku git:allow-host github.com
dokku mongo:create bot-db
dokku apps:create bot
dokku resource:limit --memory 500 bot
dokku checks:disable bot
dokku config:set bot TELEGRAM_BOT_NAME=???
   TELEGRAM_BOT_KEY=???
   TELEGRAM_BOT_OWNER_USERID=??? JAVA_OPTS='-Xmx200m'
   GPT3_TOKEN=???
dokku mongo:link bot-db bot
```

4. Create
   personal [access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-token)
   in GitHub account
4. Add that toke to dokku  
   `dokku git:auth github.com ?username? ?personal-access-token?`

### Deploy steps

1. Push the latest changes to GitHub
1. Stop app to free up memory for a build otherwise, deploy will fail if you have less than 2GB RAM `dokku ps:stop bot`
1. Login with SSH to your server and run

```
dokku git:sync --build bot https://github.com/vladmykol/mando-chatbot.git
```

3. Check logs for errors
   `dokku logs bot -n 999999999999999999 | grep -i "error"`

## Bot features
### Announce
Change the following message property with a new announcement message
, and it will be sent automatically to all users of bot once after starting. `bot.main.whats_new=`



░░░░░░░░░▄░░░░░░░░░░░░░░▄░░░░   
░░░░░░░░▌▒█░░░░░░░░░░░▄▀▒▌░░░   
░░░░░░░░▌▒▒█░░░░░░░░▄▀▒▒▒▐░░░     
░░░░░░░▐▄▀▒▒▀▀▀▀▄▄▄▀▒▒▒▒▒▐░░░   
░░░░░▄▄▀▒░▒▒▒▒▒▒▒▒▒█▒▒▄█▒▐░░░   
░░░▄▀▒▒▒░░░▒▒▒░░░▒▒▒▀██▀▒▌░░░     
░░▐▒▒▒▄▄▒▒▒▒░░░▒▒▒▒▒▒▒▀▄▒▒▌░░     
░░▌░░▌█▀▒▒▒▒▒▄▀█▄▒▒▒▒▒▒▒█▒▐░░     
░▐░░░▒▒▒▒▒▒▒▒▌██▀▒▒░░░▒▒▒▀▄▌░     
░▌░▒▄██▄▒▒▒▒▒▒▒▒▒░░░░░░▒▒▒▒▌░     
▀▒▀▐▄█▄█▌▄░▀▒▒░░░░░░░░░░▒▒▒▐░     
▐▒▒▐▀▐▀▒░▄▄▒▄▒▒▒▒▒▒░▒░▒░▒▒▒▒▌     
▐▒▒▒▀▀▄▄▒▒▒▄▒▒▒▒▒▒▒▒░▒░▒░▒▒▐░     
░▌▒▒▒▒▒▒▀▀▀▒▒▒▒▒▒░▒░▒░▒░▒▒▒▌░     
░▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒░▒░▒░▒▒▄▒▒▐░░     
░░▀▄▒▒▒▒▒▒▒▒▒▒▒░▒░▒░▒▄▒▒▒▒▌░░     
░░░░▀▄▒▒▒▒▒▒▒▒▒▒▄▄▄▀▒▒▒▒▄▀░░░     
░░░░░░▀▄▄▄▄▄▄▀▀▀▒▒▒▒▒▄▄▀░░░░░     
░░░░░░░░░▒▒▒▒▒▒▒▒▒▒▀▀░░░░░░░░   
HELP
Spread the word & support to make it better
