package com.mykovolod.mando.utils;

//    command has type /commandName~param1~param2~param3... - for command with param
//    or /commandName - for commands without params

import org.apache.commons.lang3.StringUtils;

public class StringParamUtil {
    private static final String SEPARATOR = "~";
    private static final String SEPARATOR_LINK = "@";
    private static final String START_COMMAND_WITH_SEPARATOR = "/start ";
    private static final String COMMAND_SYMBOL = "/";
    private static final String ID_SYMBOL_START = "Id: ";
    private static final String ID_SYMBOL_END = ";";

    public static String constructIdInMsg(String id) {
        return ID_SYMBOL_START + id + ID_SYMBOL_END;
    }

    public static String extractIdFromMsg(String msg) {
        return StringUtils.substringBetween(msg, ID_SYMBOL_START, ID_SYMBOL_END);
    }

    public static String constructCommand(String commandName, String[] params) {
        final var command = COMMAND_SYMBOL + commandName;
        if (params == null || params.length == 0) {
            return command;
        } else {
            return command + SEPARATOR + joinWithSeparator(params);
        }
    }

    public static String joinWithSeparator(String... params) {
        return String.join(SEPARATOR, params);
    }

    public static String extractCommandName(String str) {
        if (isCommand(str)) {
            var endCommandIndex = str.indexOf(SEPARATOR);
            if (endCommandIndex < 0) {
                endCommandIndex = str.indexOf(SEPARATOR_LINK);
            }
            if (endCommandIndex > 0) {
                return str.substring(COMMAND_SYMBOL.length(), endCommandIndex);
            } else {
                return str.substring(COMMAND_SYMBOL.length());
            }
        } else {
            return null;
        }
    }

    public static boolean isCommand(String str) {
        return (str != null && str.startsWith(COMMAND_SYMBOL));
    }


    public static String[] extractCommandParam(String str) {
        if (hasCommandParams(str)) {
            final var startCommandParamIndex = str.indexOf(SEPARATOR);
            if (startCommandParamIndex > 0) {
                final var commands = str.substring(startCommandParamIndex + SEPARATOR.length());
                return commands.split(SEPARATOR);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean hasCommandParams(String str) {
        return (str != null && str.indexOf(SEPARATOR) > 0 && str.indexOf(SEPARATOR) + 1 < str.length());
    }

    public static String extractStartParam(String str) {
        final var startParamIndex = str.indexOf(START_COMMAND_WITH_SEPARATOR);
        if (startParamIndex == 0) { //only when message start with this command
            return str.substring(startParamIndex + START_COMMAND_WITH_SEPARATOR.length());
        } else {
            return null;
        }
    }
}

