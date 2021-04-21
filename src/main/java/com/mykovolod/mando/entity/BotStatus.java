package com.mykovolod.mando.entity;

public enum BotStatus {
    ACTIVE,
    FROZEN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
