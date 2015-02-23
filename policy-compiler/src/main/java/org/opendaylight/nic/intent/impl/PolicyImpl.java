//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.common.ApplicationImpl;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.intent.Classifier;
import org.opendaylight.nic.intent.EndpointGroup;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyId;

/**
 * A request for a network domain policy.
 *
 * @author Duane Mentze
 */
public class PolicyImpl implements Policy {

	private PolicyId id;
	private String name;
	private ApplicationImpl application;
	private EndpointGroupImpl src;
	private EndpointGroupImpl dst;
	private Classifier classifier;
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
	public Classifier classifier() {
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

	public PolicyImpl(org.opendaylight.nic.intent.PolicyId id,
			String name, ApplicationImpl application, EndpointGroupImpl src,
			EndpointGroupImpl dst, ClassifierImpl classifier,
			Map<ActionLabel,AuxiliaryData> actions, boolean isExclusive) {
		super();
		this.id = id;
		this.name = name;
		this.application = new ApplicationImpl(application);
		this.src = new EndpointGroupImpl(src);
		this.dst = new EndpointGroupImpl(dst);
		this.classifier = new ClassifierImpl(classifier);
		this.actions = new HashMap<ActionLabel,AuxiliaryData>(actions);
		this.isExclusive = isExclusive;
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
		return "PolicyImpl [id=" + id + ", name=" + name
				+ ", application=" + application + ", src=" + src + ", dst="
				+ dst + ", classifier=" + classifier + ", actions=" + actions
				+ ", isExclusive=" + isExclusive + "]";
	}




}
