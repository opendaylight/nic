//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------

package org.opendaylight.nic.compiler.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.common.ApplicationImpl;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.intent.Classifier;
import org.opendaylight.nic.intent.EndpointGroup;
import org.opendaylight.nic.intent.Expression;
import org.opendaylight.nic.intent.Interval;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyId;
import org.opendaylight.nic.intent.Term;
import org.opendaylight.nic.intent.impl.EndpointGroupImpl;

/**
 * Applications control the network behavior by setting {@link Policy}s.
 *
 * @author Duane Mentze
 */
public class PolicyImpl implements Policy {

	private PolicyId id;
	private String name;
	private ApplicationImpl application;
	private EndpointGroupImpl src;
	private EndpointGroupImpl dst;
	private ClassifierImpl classifier;
	private Map<ActionLabel,AuxiliaryData> actions;
	private boolean isExclusive;


	@Override
	public String name() {
		return name;
	}

	@Override
	public org.opendaylight.nic.intent.PolicyId PolicyId() {
		return id;
	}

	@Override
	public Application application() {
		return application;
	}

	@Override
	public EndpointGroup src() {
		return src;
	}

	@Override
	public EndpointGroup dst() {
		return dst;
	}

	@Override
	public ClassifierImpl classifier() {
		return classifier;
	}

	@Override
	public Map<ActionLabel,AuxiliaryData> actionLabelToAuxDataMap() {
		return actions;
	}


	@Override
	public boolean isExclusive() {
		return isExclusive;
	}

	//public boolean isComposable() {
	//	return actionSet.isComposable();
	//}


	public static PolicyImpl createPolicyImpl( org.opendaylight.nic.intent.Policy policy, Map<TermLabel,TermType> termTypes) {
		return new PolicyImpl(	policy.name(),
								policy.application(),
								policy.src(),
								policy.dst(),
								createClassifierImpl(policy.classifier(), termTypes),
								policy.actionLabelToAuxDataMap(),
								policy.isExclusive());
	}


	private PolicyImpl(String policyName, Application application, EndpointGroup src,
			EndpointGroup dst, ClassifierImpl classifier, Map<ActionLabel,AuxiliaryData> map,
			boolean isExclusive) {
		this.name = policyName;
		this.application = new ApplicationImpl(application);
		this.src = new EndpointGroupImpl(src);
		this.dst = new EndpointGroupImpl(dst);
		this.classifier = classifier;
		this.actions = new HashMap<>(map);
		this.isExclusive = isExclusive;
	}


	private static ClassifierImpl createClassifierImpl(Classifier classifierRequest, Map<TermLabel,TermType> termTypes) {

		Collection<ExpressionImpl> expressionCollection = new HashSet<ExpressionImpl>();
		for (Expression er: classifierRequest.getExpressions()) {
			Collection<TermImpl> termCollection = new HashSet<TermImpl>();
			for (Term tr: er.getTerms()) {
				Collection<IntervalImpl> intervalCollection = new HashSet<IntervalImpl>();
				for (Interval i: tr.getIntervals()) {
					IntervalImpl interval = IntervalImpl.getInstance(i.start(), i.end());
					intervalCollection.add(interval);
				}
				TermType tt = termTypes.get(tr.typeLabel());
				if (tt == null) {
				    throw new IllegalArgumentException("Couldn't find type for label: "+tr.typeLabel()+", registered types are: "+termTypes.entrySet());
				}
				TermImpl term = TermImpl.getInstance(termTypes.get(tr.typeLabel()), intervalCollection);
				termCollection.add(term);
			}
			ExpressionImpl expression = ExpressionImpl.getInstance(termCollection);
			expressionCollection.add(expression);
		}

		ClassifierImpl classifier = ClassifierImpl.getInstance(expressionCollection);
		return classifier;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result
				+ ((application == null) ? 0 : application.hashCode());
		result = prime * result
				+ ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isExclusive ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		PolicyImpl other = (PolicyImpl) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (classifier == null) {
			if (other.classifier != null)
				return false;
		} else if (!classifier.equals(other.classifier))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isExclusive != other.isExclusive)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PolicyImpl [id=" + id + ", name=" + name + ", application="
				+ application + ", src=" + src + ", dst=" + dst
				+ ", classifier=" + classifier + ", actions=" + actions
				+ ", isExclusive=" + isExclusive + "]";
	}



}
