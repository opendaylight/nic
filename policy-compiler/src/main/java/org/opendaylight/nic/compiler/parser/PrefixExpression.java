//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.EndpointGroup;

/**
 * 
 *
 * @author Duane Mentze
 */
public class PrefixExpression {
	
	private Queue<Token> queue;
	
	public Queue<Token> getQueue() {
		return new LinkedList<Token>(queue);
	}
	
	public boolean isAll() {
		if (queue.size()==1) {
			Token t = queue.peek();
			if ( (t.type().isAttribute()) && (t.value().equals("*")) ) {
				return true;
			}
		}
		return false;
	}
	
	public PrefixExpression(EndpointGroup group) {
		List<Token> tokens = TokenParser.parse(group.group());
		this.queue = PrefixParser.parse(tokens);
	}
	
	public PrefixExpression(PrefixExpression pe) {
		this.queue = new LinkedList<Token>(pe.queue);
	}
	
	public PrefixExpression and(PrefixExpression other) {
		
		PrefixExpression result = new PrefixExpression(this);
		PrefixExpression otherCopy = new PrefixExpression(other);
		
		Token t;
		while ( (t = otherCopy.queue.poll() )!=null) {
			result.queue.add(t);
		}
		t = new Token(TokenType.AND, TokenType.AND.name());
		result.queue.add(t);
		return result;
	}
	
	public PrefixExpression not() {
		PrefixExpression result = new PrefixExpression(this);
		Token t = new Token(TokenType.NOT, TokenType.NOT.name());
		result.queue.add(t);
		return result;
	}
	
	
	public static boolean isValid(String endpointGroupExpression) {
		List<Token> tokens;
		try {
			tokens = TokenParser.parse(endpointGroupExpression);
		}
		catch (IllegalArgumentException exception) {
			return false;
		}
		return isValid(tokens);
		
	}

	public static boolean isValid(List<Token> tokens) {
		try {
			PrefixParser.parse(tokens);
			return true;
		}
		catch (IllegalArgumentException exception) {
			return false;
		}
	}
	
	public boolean isMember(Endpoint endpoint) {
		
		if (queue.isEmpty()) {
			return false;
		}

		Set<EndpointAttribute> attributes = endpoint.attributes(); 		
		
		LinkedList<EvalNode> stack = new LinkedList<EvalNode>();
		
		Queue<EvalNode> q = new LinkedList<EvalNode>();

		//make a queue of EvalNodes
		for (Token t: queue) {
			q.add(new EvalNode(t,attributes));
		}
		
		while (!q.isEmpty()) {
			EvalNode node = q.remove();
			TokenType tt = node.type();
			if (tt.isAttribute()) {
				stack.addLast(node);
				continue;
			}
			else if (tt.isOperator()) {
				tt.evaluate(stack);
			}
			else {
				throw new IllegalArgumentException("illegal token" + tt);
			}
		}

		if (stack.size()!=1) {
			throw new IllegalStateException("cannot evaluate, wrong stack size");
		}
		
		EvalNode result = stack.removeLast();
		if (!result.type().isAttribute()) {
			throw new IllegalStateException("cannot evaluate, wrong tokey type: " + result.type());			
		}
		
		return result.value;
	}
	
	private static class StringEval {
	    public StringEval(TokenType tt, String value) {
            this.tt = tt;
            this.value = value;
        }
        private TokenType tt;
	    private String value;
	    public TokenType type() {
	        return tt;
	    }
	    public String value() {
	        return value;
	    }
	    
	}
	
	public String prefixToInfix() {
		
		if (queue.isEmpty()) {
			return "";
		}

		LinkedList<StringEval> stack = new LinkedList<StringEval>();

		Queue<StringEval> q = new LinkedList<StringEval>();
		//make a queue of strings for what is inside the queue
		for (Token t: queue) {
		    
		    //use operator names
		    if (t.type().isOperator()) {
		        q.add(new StringEval(t.type(), " " + t.type().toString() + " "));    
		    }
		    else if (t.type().isAttribute()) {
		        //use attribute values
		        q.add(new StringEval(t.type(), " " + t.value().toString() + " "));    		        
		    }
		    else {
		        q.add(new StringEval(t.type(), " " + t + " "));
		    }
		}

		while (!q.isEmpty()) {
			StringEval node = q.remove();
			TokenType tt = node.type();
			if (tt.isAttribute()) {
				stack.addLast(node);
				continue;
			}
			else if (tt.isOperator()) {
				//tt.evaluate(stack);
			    if (tt==TokenType.AND || tt==TokenType.OR) {
			        StringEval left = stack.removeLast();
			        StringEval right = stack.removeLast();
			        if ( (left==null) || (right==null) ) {
			            throw new IllegalStateException("not enough tokens");
			        }
			        if ( (!left.type().isAttribute()) || (!right.type().isAttribute()) ) {        	
			            throw new IllegalStateException("bad token type in stack");        		
			        }
			        String s = String.format(" ( %s %s %s ) ", left.value(), tt.toString(), right.value());
			        stack.addLast(new StringEval(left.type(), s));
			    }
			    else if (tt==TokenType.NOT) {
			        StringEval n = stack.removeLast();
			        if ( n==null ) {
			            throw new IllegalStateException("not enough tokens");
			        }
			        if (!n.type().isAttribute() ) {
			            throw new IllegalStateException("bad token type in stack");        		
			        }
			        String s = String.format(" ( %s %s ) ", tt.toString(), n.value());
			        stack.addLast(new StringEval(n.type(), s));
			    }
    			    else 			    {
    				throw new IllegalArgumentException("illegal token" + tt);			        
			    }
			}
			else {
				throw new IllegalArgumentException("illegal token" + tt);
			}
		}

		if (stack.size()!=1) {
			throw new IllegalStateException("cannot evaluate, wrong stack size");
		}
		
		StringEval result = stack.removeLast();
		if (!result.type().isAttribute()) {
			throw new IllegalStateException("cannot evaluate, wrong tokey type: " + result.type());			
		}
		
		return result.value;
	}
	
	@Override
	public String toString() {
		return "PrefixExpression { " + queue + "}";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrefixExpression other = (PrefixExpression) obj;
		if (queue == null) {
			if (other.queue != null)
				return false;
		} else if (!queue.equals(other.queue))
			return false;
		return true;
	}
	
	
}