//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Provides parsing capabilities for attribute expressions. Attribute
 * expressions are Strings which contain Attributes, logical operators
 * (and/or/not), and parentheses. Attributes are string identifiers.
 *
 * @author Duane Mentze
 */
public class TokenParser {

    private final Pattern tokenPatterns;
    // private final Pattern splitterPatterns;

    /**
     * This is a private singleton class.
     */
    private final static TokenParser INSTANCE = new TokenParser();

    /**
     * Creates a compiled regular expression {@link Pattern} out of
     * {@link TokenType}
     */
    private TokenParser() {
        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (TokenType Token : TokenType.values()) {
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)",
                    Token.name(), Token.pattern()));
        }
        tokenPatterns = Pattern.compile(new String(tokenPatternsBuffer
                .substring(1)));
    }

    /**
     * Public api for parsing an attribute expression into {@link TokenType}
     *
     * @param attributeExpression
     * @return List<Token> a list of {@link Token}
     * @throws IllegalArgumentException
     * 
     */
    public static List<Token> parse(String attributeExpression) {
        return INSTANCE.tokenParse(attributeExpression);
    }

    /**
     * @param attributeExpression
     * @return List<Token> a list of {@link Token}
     * @throws IllegalArgumentException
     */
    private List<Token> tokenParse(String attributeExpression) {

        // first make sure parens are surrounded by whitespace
        String expression = attributeExpression.replaceAll("\\(", " \\( ")
                .replaceAll("\\)", " \\) ");

        String[] stringTokens = expression.trim().split("\\s+");
        List<Token> tokens = new ArrayList<Token>();

        for (String st : stringTokens) {
            Matcher matcher = tokenPatterns.matcher(st);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("unexpected token: " + st);
            }
            if (matcher.group(TokenType.AND.name()) != null) {
                tokens.add(new Token(TokenType.AND, matcher.group(TokenType.AND
                        .name())));
                continue;
            } else if (matcher.group(TokenType.OR.name()) != null) {
                tokens.add(new Token(TokenType.OR, matcher.group(TokenType.OR
                        .name())));
                continue;
            } else if (matcher.group(TokenType.NOT.name()) != null) {
                tokens.add(new Token(TokenType.NOT, matcher.group(TokenType.NOT
                        .name())));
                continue;
            } else if (matcher.group(TokenType.LEFTPAREN.name()) != null) {
                tokens.add(new Token(TokenType.LEFTPAREN, matcher
                        .group(TokenType.LEFTPAREN.name())));
                continue;
            } else if (matcher.group(TokenType.RIGHTPAREN.name()) != null) {
                tokens.add(new Token(TokenType.RIGHTPAREN, matcher
                        .group(TokenType.RIGHTPAREN.name())));
                continue;
            } else if (matcher.group(TokenType.LABEL.name()) != null) {
                tokens.add(new Token(TokenType.LABEL, matcher
                        .group(TokenType.LABEL.name())));
                continue;
            } else if (matcher.group(TokenType.IP.name()) != null) {
                tokens.add(new Token(TokenType.IP, matcher.group(TokenType.IP
                        .name())));
                continue;
            } else if (matcher.group(TokenType.ANY.name()) != null) {
                tokens.add(new Token(TokenType.ANY, matcher.group(TokenType.ANY
                        .name())));
                continue;
            } else if (matcher.group(TokenType.ALL.name()) != null) {
                tokens.add(new Token(TokenType.ALL, matcher.group(TokenType.ALL
                        .name())));
                continue;
            } else if (matcher.group(TokenType.PATTERN.name()) != null) {
                String s = matcher.group(TokenType.PATTERN.name());
                try {
                    Pattern.compile(s); // verify the syntax of the pattern
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("invalid pattern syntax");
                }
                tokens.add(new Token(TokenType.PATTERN, s));
                continue;
            } else {
                throw new IllegalArgumentException("unexpected token");
            }
        }

        return tokens;
    }

}
