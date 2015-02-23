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

/**
 * Provides parsing capabilities for attribute expressions.
 * Attribute expressions are Strings which contain Attributes,
 * logical operators (and/or/not), and parentheses.
 * Attributes are string identifiers.
 *
 * @author Duane Mentze
 */
class PrefixParser {

	//instances are not needed
	private PrefixParser() {}
	
    /**
     * Performs a prefix (aka RPN) parse on a List of {@link Token}.
     * <p>
     * This implementation is based on the shunting-yard algorithm.
     * 
     * @param tokens to parse
     * @return {@link PrefixExpression}
     */
    public static Queue<Token> parse(List<Token> tokens) {
    	Queue<Token> queue = new LinkedList<Token>();
    	LinkedList<Token> operatorStack = new LinkedList<Token>();
    	
    	for (Token token: tokens) {
    		//System.out.println("\n----------------------\nsQUEUE\n" + queue + "\nstack\n" + operatorStack);
    		switch(token.type()) {
    		
	    		//operands
    			case LABEL:
    			case IP:
    			case ANY:
    			case ALL:
	    		    handleOperands(queue, token);
	    			break;
	    		
	    		//operators
	    		case AND:
	    		case OR:
	    		case NOT: 
	   		    	handleOperators(queue, operatorStack, token);
		   			break;
	   		    	
	    		//left parentheses
	    		case LEFTPAREN:
	    		    handleLeftParentheses(queue, operatorStack, token);

	    			break;
    			
	    		//right parentheses
	    		case RIGHTPAREN: 
	    		    if (!handleRightParentheses(queue, operatorStack, token)) {
	    		    	throw new IllegalArgumentException("parse failure on right parenthesis");
	    		    }

	   			break;
	    			
	    		default:
	    			throw new IllegalArgumentException("parse failure on illegal token");
	   		}
    	}
	
    	//move remaining operators on stack to queue
		Token topOfStack;
		while ( (topOfStack = operatorStack.peekLast() ) != null) {
			if (!topOfStack.type().isOperator()) {
    			throw new IllegalArgumentException("parse failure, expected operator, token: " + topOfStack);
			}
			queue.add(operatorStack.removeLast());
    	}
    		
		return queue;
    }
	

    /**
     * Handles parsing of operands which are attributes by adding them to th prefix output queue.
     * 
     * @param queue Prefix output queue
     * @param token Token to handle
     */
    private static void handleOperands(Queue<Token> queue, Token token) {
    	queue.add(token);
    }

    /**
     * Handles parsing of operators (and, or, not), which is the incoming {@link Token}
     * <p>
     * Moves operands, which are {@link Token}, from the stack to the queue 
     * while either of these conditions are met:
     * <ul>
     * <li>incoming token is left associative and its priority is less than or equal to the top of stack token priority</li>
     * <li>incoming token is right associative and its priority is less than the top of stack token priority</li>
     * </ul>
     * <p>
     * After the operands are popped, the incoming operand {@link Token} is pushed onto the stack.
     * 
     * @param queue Prefix output queue
     * @param stack Operand stack
     * @param token Token to handle
     */
    private static void handleOperators(Queue<Token> queue, LinkedList<Token> stack, Token token) {

		Token topOfStack;
		while (	
				(	//stack not empty	
					(topOfStack = stack.peekLast() ) != null 
				) && //this check must be first to ensure non-null topOfStack
		 		(	
		 			(
		 				(token.type().associativity()==Associativity.LEFT) && 
		 				(token.type().priority() <= topOfStack.type().priority()) 
		 			) ||
		 			(
			 				(token.type().associativity()==Associativity.RIGHT) && 
			 				(token.type().priority() < topOfStack.type().priority()) 
		 			)
		 		)
		 	  )
		 	{	//pop top of stack onto queue
				queue.add(stack.removeLast());
		 	}
		//finally add token to stack
		stack.addLast(token);
    }
    
    /**
     * Handles parsing of left parentheses by simply adding it to the operand stack.
     * 
     * @param stack Operand stack
     * @param token Token to handle
     */
    private static void handleLeftParentheses(Queue<Token> queue, LinkedList<Token> stack, Token token) {
		stack.add(token);
    }
    
    /**
     * Handles parsing of right parentheses {@link Token}
     * Moves operands from the stack to the queue until a left parentheses is found.
     * 
     * @param queue Prefix output queue
     * @param stack Operand stack
     * @param token Token to handle
     * @return
     */
    private static boolean handleRightParentheses(Queue<Token> queue, LinkedList<Token> stack, Token token) {
		Token topOfStack;
		while (	
				(	//stack not empty	
					(topOfStack = stack.peekLast() ) != null 
				) && //this check must be first to ensure non-null topOfStack
		 		(	
		 				(!topOfStack.type().equals(TokenType.LEFTPAREN)) 
		 			)
		 	  )
		 	{	//pop top of stack onto queue
				queue.add(stack.removeLast());
		 	}

		//it is a parse error for a left parentheses to not be found
		if (topOfStack==null) {
			return false;
		}

		//left parentheses found so pop it off and return
		stack.removeLast();
		return true;
    }
    

    
	
}
