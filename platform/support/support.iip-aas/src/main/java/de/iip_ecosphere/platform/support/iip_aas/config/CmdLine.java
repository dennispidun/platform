/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.util.List;

/**
 * Simple command line utilities. May be backed by a real command line parser.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CmdLine {

    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_ARG_NAME_SEP = ".";
    public static final String PARAM_VALUE_SEP = "=";

    /**
     * Parses given {@code text} into individual arguments considering double quotes for string escapement with space 
     * as a command separator.
     * 
     * @param text the text to parse
     * @param cmds the commands (to be modified as a side effect)
     */
    public static void parseToArgs(String text, List<String> cmds) {
        parseToArgs(text, cmds, ' ');
    }

    /**
     * Parses given {@code text} into individual arguments considering double quotes for string escapement and 
     * a given command separator.
     * 
     * @param text the text to parse
     * @param cmds the commands (to be modified as a side effect)
     * @param separator the separator (usually ' ')
     */
    public static void parseToArgs(String text, List<String> cmds, char separator) {
        boolean inQuote = false;
        text = text.trim();
        int lastStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ('"' == c) {
                inQuote = !inQuote;
            } 
            if (separator == c && !inQuote || i + 1 == text.length()) {
                String cmd = text.substring(lastStart, i + 1).trim(); 
                if (cmd.length() > 0) {
                    cmds.add(cmd);
                }
                lastStart = i + 1;
            }
        }
    }
    
    /**
     * Emulates reading a Spring-like parameter if the configuration is not yet in place.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static String getArg(String[] args, String argName, String dflt) {
        String result = dflt;
        String prefix = PARAM_PREFIX + argName + PARAM_VALUE_SEP;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (arg.startsWith(prefix)) {
                result = arg.substring(prefix.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Returns an int command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static int getIntArg(String[] args, String argName, int dflt) {
        int result;
        try {
            result = Integer.parseInt(getArg(args, argName, String.valueOf(dflt)));
        } catch (NumberFormatException e) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Returns a Boolean command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static boolean getBooleanArg(String[] args, String argName, boolean dflt) {
        return Boolean.valueOf(getArg(args, argName, String.valueOf(dflt)));
    }    

}
