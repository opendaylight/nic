//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.parser;

import java.util.Set;

import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.impl.EndpointAttributeImpl;

/**
 *
 * @author Duane Mentze
 */
class EvalNode {
	
	Token t;
	boolean value;
	
	public TokenType type() {
		return t.type();
	}
	
	EvalNode(Token t, Set<EndpointAttribute> attributes) {

		this.t = t;
		value = true;
		
		if (t.type().isOperator()) {
			return;
		}

		if (!t.type().isAttribute()) {
			throw new IllegalArgumentException("token type must be an attribute");
		}

		if (isBoolValue()) {
			EndpointAttribute ea = new EndpointAttributeImpl(t.value());								
			value = attributes.contains(ea);
		}
	}
	
	private boolean isBoolValue() {
		return (type()==TokenType.LABEL || type()==TokenType.IP);		
	}
	
	EvalNode(Token t, boolean value) {
		this.t = t;
		this.value = value;
	}
	
	public EvalNode and(EvalNode other) {
		if (isBoolValue()) {
			if (other.isBoolValue()) {
				// bool, bool
				return new EvalNode(t, value && other.value);
			}
			else {
				//bool, !bool
				return new EvalNode(t, value);
			}
		}
		else {
			if (other.isBoolValue()) {
				// !bool, bool
				return new EvalNode(other.t, other.value);				
			}
			else {
				// !bool, !bool
				if (this.type().isAll()) {
					return new EvalNode(this.t, true);
				}
				else {
					return new EvalNode(other.t, true);
				}
			}
		}
	}
	
	public EvalNode or(EvalNode other) {
		if (isBoolValue()) {
			if (other.isBoolValue()) {
				// bool, bool
				if (value) {
					return new EvalNode(t, true);
				}
				else if (other.value) {
					return new EvalNode(other.t, true);
				}
				else {
					return new EvalNode(t,false);
				}
			}
			else {
				//bool, !bool
				return new EvalNode(other.t, true);
			}
		}
		else {
			if (other.isBoolValue()) {
				// !bool, bool
				return new EvalNode(t,true);
			}
			else {
				// !bool, !bool
				if (this.type().isAny()) {
					return new EvalNode(this.t, true);
				}
				else {
					return new EvalNode(other.t, true);
				}
			}
		}
	}
	
	public EvalNode not() {
		if (isBoolValue()) {
			return new EvalNode(t, !value);			
		}
		else {
			return new EvalNode(new Token(TokenType.LABEL,"<empty>"), false);
		}
	}

	@Override
	public String toString() {
		return "EvalNode {t=" + t + ", value=" + value + "}";
	}
	
	
	
}