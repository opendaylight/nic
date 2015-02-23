//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.parser;

import java.util.LinkedList;
import java.util.Set;

import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointAttribute;


/**
 * Enum contains types of tokens found in an Attribute Expression 
 * 
 * @author Duane Mentze
 */
public enum TokenType {
	


    AND("^[Aa][Nn][Dd]$", Associativity.LEFT, 10, true, false) {
        @Override
        public void evaluate(LinkedList<EvalNode> stack) {
        	EvalNode left = stack.removeLast();
        	EvalNode right = stack.removeLast();
        	if ( (left==null) || (right==null) ) {
        		throw new IllegalStateException("not enough tokens");
        	}
        	if ( (!left.type().isAttribute()) || (!right.type().isAttribute()) ) {
        		throw new IllegalStateException("bad token type in stack");        		
        	}
        	stack.addLast(left.and(right));
        }
    },     
    OR("^[Oo][Rr]$", Associativity.LEFT, 10, true, false) {
        @Override
        public void evaluate(LinkedList<EvalNode> stack) {
        	EvalNode left = stack.removeLast();
        	EvalNode right = stack.removeLast();
        	if ( (left==null) || (right==null) ) {
        		throw new IllegalStateException("not enough tokens");
        	}
        	if ( (!left.type().isAttribute()) || (!right.type().isAttribute()) ) {        	
        		throw new IllegalStateException("bad token type in stack");        		
        	}
        	stack.addLast(left.or(right));
        }
    }, 
    NOT("^[Nn][Oo][Tt]$", Associativity.RIGHT, 20, true, false) {
        @Override
        public void evaluate(LinkedList<EvalNode> stack) {
        	EvalNode node = stack.removeLast();
        	if ( (node==null) ) {
        		throw new IllegalStateException("not enough tokens");
        	}
        	if ( (!node.type().isAttribute()) ) {
        		throw new IllegalStateException("bad token type in stack");        		
        	}
        	stack.addLast(node.not());
        }
    },
    
    LEFTPAREN("^[(]$", Associativity.NONE, 0, false, false),
    RIGHTPAREN("^[)]$", Associativity.NONE, 0, false, false),
    
    LABEL("^[a-zA-Z][\\w_]*(?<![aA][nN][yY])$", Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return attributes.contains(ep);
        }
    },

    
    IP(TokenTypePatterns.IP_PATTERN, Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return attributes.contains(ep);
        }
    	
    },
    ANY("^[aA][nN][yY]$", Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return true;
        }
        @Override
        public boolean isAny() {
        	return true;
        }
    	
    },    	    
    ALL("^[*]{1}$", Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return !attributes.isEmpty();
        }
        @Override
        public boolean isAll() {
        	return true;
        }
    },
        
    PATTERN(/*"^.+$"*/"patt", Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return false;
        }
    };   
    
    /* TODO ATT_IP_RANGE ipsubnet 
     * IPv4 supports the '/' CIDR notation.  A regex for 0-32 is  [0-9]|[12][0-9]|[3][0-2]  
     * IPV6 supports the ::/ notation, for 0-128 */
    
    /* TODO Label plus wild card
    ATT_LABEL_WC("^\\w+[*]{1}$", Associativity.NONE, 0, false, true) {
        public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
        	return false;
        }
    },    
    */

    
	//private static final String LegalNextChar = "(?=([ \t\f\r\n()]|$))";
    public final String pattern;
    private final Associativity associativity;
    private final int priority;
    private final boolean isOperator;
    private final boolean isAttribute;

    private TokenType(String pattern, Associativity associativity, int priority, boolean isOperator, boolean isAttribute) {
        this.pattern = pattern;
        this.associativity = associativity;
        this.priority = priority;
        this.isOperator = isOperator;
        this.isAttribute = isAttribute;
    }
    
    public void evaluate(LinkedList<EvalNode> stack) {
    	throw new IllegalStateException("cannot evaluate this type of term");
    }
    
    public boolean isMember(Endpoint ep, Set<EndpointAttribute> attributes) {
    	return false;
    }
    
    public boolean isAttribute() { return isAttribute; }
    public String pattern() { return this.pattern; }
    public Associativity associativity() { return this.associativity; }
    public int priority() { return this.priority; }
    public boolean isOperator() { return this.isOperator; }
    public boolean isAll() { return false;}
    public boolean isAny() { return false;}
    
}