# Mando - create free chatbot

[![Build status](https://travis-ci.com/mykovolod/mando-chatbot.svg?branch=master)](https://travis-ci.com/mykovolod/mando-chatbot) 
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.mykovolod%3Amovieland&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.mykovolod%3Amovieland)

This is free chatbot builder platform powered by SpringBoot and ApacheOpenNLP libs

![Bot Preview](./src/main/resources/static/bot-promo.jpg)

Use Mando chatbot to create and menage your own chatbot using Telegram

Just try yourself here -> <a href="https://t.me/create_free_chatbot">Open Telegram Bot Mando</a>

## Quick Start

**Prerequisites**

Please be sure that the following components are installed on your computer before running locally:

- JDK 11
- Gradlew
- MongoDb

**Custom properties**

You also you need to set the following properties 

~~~~
# telegram
TELEGRAM_BOT_NAME=yourBotName
TELEGRAM_BOT_KEY=yourBotApiKey
TELEGRAM_BOT_OWNER_USERID=yourTelegramChatId
# db
MONGO_URL=mongodb://localhost:27017/yourDataBaseName
~~~~

## Current issues

- no unit test coverage
- overcomplicated Telegram workflow

