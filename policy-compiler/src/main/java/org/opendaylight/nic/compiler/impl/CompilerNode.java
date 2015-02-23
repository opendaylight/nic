//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------


package org.opendaylight.nic.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.compiler.ActionSet;
import org.opendaylight.nic.compiler.ActionSetList;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.CompilerAction;
import org.opendaylight.nic.compiler.CompilerEndpointGroup;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.intent.Action;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointId;
import org.opendaylight.nic.intent.Expression;
import org.opendaylight.nic.intent.Interval;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.Term;

/**
 * Nodes are used in the compilation process as an intermediate representation
 * for Policies.  They contain a source, destination and classifier along with the ActionSets
 * of all composed policies used to create this Node.  ActionSetList is a list of
 * ActionSet that are ordered according to application priority.
 * <p>
 * Node isExclusive if any ActionSet is exclusive.  Node isObserver if all ActionSets
 * are observers.
 *
 * @author Duane Mentze
 */

public class CompilerNode implements CompiledPolicy {

	private String name;
	private CompilerEndpointGroup src;
	private CompilerEndpointGroup dst;
	private ClassifierImpl classifier;
	private ActionSetList actionSetList;
	private boolean isExclusive;
	private boolean isObserver;
	private long maxApplicationPriority;
	private long maxActionPrecedence;
	private Set<Endpoint> srcMembers;
	private Set<Endpoint> dstMembers;
	private Set<CompilerNode> delegates;
	private Set<CompilerNode> parents;
	private CompilerNode dominant;


	public CompilerEndpointGroup srcCompilerEndpointGroup() {
		return src;
	}

	public CompilerEndpointGroup dstCompilerEndpointGroup() {
		return dst;
	}

	public boolean isExclusive() {
		return isExclusive;
	}


	@Override
	public Set<Endpoint> src() {
		return srcMembers();
	}

	@Override
	public Set<Endpoint> dst() {
		return dstMembers();
	}

	@Override
	public ClassifierImpl classifier() {
		return classifier;
	}

	@Override
	public List<Set<Action>> actions() {
	    List<Set<Action>> actions = new ArrayList<>();
		for (ActionSet as : actionSetList.getList()) {
		    Set<Action> actSet = new HashSet<>();
		    for (Action a : as.getActions())
		        actSet.add(a);
		    actions.add(actSet);
		}
		return actions;
	}

	public String name() {
		return name;
	}

	public ActionSetList list() {
		return actionSetList;
	}

	public boolean isObserver() {
		return isObserver;
	}

	public long maxApplicationPriority() {
		return maxApplicationPriority;
	}

	public long maxActionPrecedence() {
		return maxActionPrecedence;
	}

	public Set<Endpoint> srcMembers() {
		return srcMembers;
	}

	public Set<Endpoint> dstMembers() {
		return dstMembers;
	}

	public Set<CompilerNode> delegates() {
		return delegates;
	}

	public Set<CompilerNode> parents() {
		return parents;
	}


	private static ActionSetList createActionSetList(Policy policy, Map<ActionLabel, ActionType> labelToActionTypeMap) {

		//get reference to policy ActionLabel->AuxiliaryData map
	    Map<ActionLabel,AuxiliaryData> labelToDataMap = policy.actionLabelToAuxDataMap();

	    //use labelToDataMap to get ActionLabl set
	    Set<ActionLabel> actionLabels = labelToDataMap.keySet();

		//create new set to hold CompilerActions
		Set<CompilerAction> compilerActionSet = new HashSet<CompilerAction>();

	    //use actionLabels to iterate through all actions and create action set
		for (ActionLabel actionLabel: actionLabels) {
			ActionType actionType = labelToActionTypeMap.get(actionLabel);
			if (actionType==null) {
		        throw new IllegalStateException("Cannot find action: " + actionLabel);
			}
			AuxiliaryData auxData = labelToDataMap.get(actionLabel);
			if (auxData == null) {
		        throw new IllegalStateException("Cannot find action data: " + actionLabel);
			}
			CompilerAction ca = new CompilerAction(actionType, auxData);
			compilerActionSet.add(ca);
		}

		//create ActionSet based on built compilerActionSet
		ActionSet actionSet = new ActionSetImpl(compilerActionSet, policy);

		//create a List of ActionSet's and add actionSet to it
		List<ActionSet> asList = new LinkedList<ActionSet>();
		asList.add(actionSet);

		ActionSetList actionSetList = new ActionSetListImpl(asList);
		return actionSetList;
	}

	private static Set<Endpoint> calcMembers(CompilerEndpointGroup group, Map<EndpointId,Endpoint> endpointMap) {
		Collection<Endpoint> endpoints = endpointMap.values();
		return calcMembers(group,endpoints);
	}

	private static Set<Endpoint> calcMembers(CompilerEndpointGroup group, Collection<Endpoint> endpoints) {
		Set<Endpoint> result = new HashSet<Endpoint>();
		for (Endpoint ep: endpoints) {
			if (group.isMember(ep)) {
				result.add(ep);
			}
		}
		return result;
	}

	public CompilerNode createDominantNode(CompilerNode other, Map<EndpointId,Endpoint> endpointMap) {
		CompilerNode n = createNode(this);

		StringBuilder buffer = new StringBuilder();
		buffer.append(String.format("%s/%s/12.1.dom", this.name, other.name()));
		n.name = new String(buffer);

		n.src = this.srcCompilerEndpointGroup().and(other.srcCompilerEndpointGroup());
		n.dst = this.dstCompilerEndpointGroup().and(other.dstCompilerEndpointGroup());
		n.classifier = this.classifier().and(other.classifier());

		n.parents.add(this);
		n.parents.add(this);
		n.dominant = this;

		//pre-compute the endpoints in the node
		n.srcMembers = calcMembers(n.src, endpointMap);
		n.dstMembers = calcMembers(n.dst, endpointMap);

		return n;

	}

	public CompilerNode createNonOverlapNode(CompilerNode other, String nameSuffix,
			CompilerEndpointGroup src, CompilerEndpointGroup dst, ClassifierImpl c,
			List<CompilerNode> list, Map<EndpointId,Endpoint> endpointMap) {

		CompilerNode n = createNode(this);

		StringBuilder buffer = new StringBuilder();
		buffer.append(String.format("%s/%s/%s", this.name, other.name(),nameSuffix));
		n.name = new String(buffer);

		n.src = src;
		n.dst = dst;
		n.classifier = c;

		n.parents.clear();
		n.parents.add(this);
		this.delegates().add(n);

		//note:  dominant node was retained from 'this' during construction

		//pre-compute the endpoints in the node
		n.srcMembers = calcMembers(n.src, endpointMap);
		n.dstMembers = calcMembers(n.dst, endpointMap);

		//TODO if members is empty (both the set and the ANY/ALL are accounted for - then don't add node to list

		list.add(n);
		return n;
	}


	static  CompilerNode createNode(org.opendaylight.nic.intent.Policy policyRequest,
							Map<EndpointId,Endpoint> endpointMap,
							Map<ActionLabel, ActionType> labelToActionTypeMap,
							Map<TermLabel,TermType> termTypeLabelToTermTypeMap) {

		//create PolicyImpl from Policy
		PolicyImpl policy = PolicyImpl.createPolicyImpl(policyRequest, termTypeLabelToTermTypeMap);

		//create ActionSetList using asList
		ActionSetList actionSetList = createActionSetList(policy, labelToActionTypeMap);

		//create CompilerEndpointGroups
		CompilerEndpointGroup srcEpg = CompilerEndpointGroup.getInstance(policy.src());
		CompilerEndpointGroup dstEpg= CompilerEndpointGroup.getInstance(policy.dst());

		//now construct node from PolicyImpl, actionSetList, src/dst Members
		return new CompilerNode(policy.name(),
				srcEpg,
				dstEpg,
				policy.classifier(),
				actionSetList,
				policy.isExclusive(),
				actionSetList.isObserver(),
				policy.application().priority(),
				actionSetList.maxActionPrecedence(),
				calcMembers(srcEpg, endpointMap),
				calcMembers(dstEpg, endpointMap));
	}

	static CompilerNode createNode(CompilerNode node) {

		return new CompilerNode(node.name(),
				node.srcCompilerEndpointGroup(),
				node.dstCompilerEndpointGroup(),
				node.classifier(),
				node.list(),
				node.isExclusive(),
				node.list().isObserver(),
				node.maxApplicationPriority(),
				node.list().maxActionPrecedence(),
				node.srcMembers(),
				node.dstMembers());
	}

	public CompilerNode(String name, CompilerEndpointGroup src, CompilerEndpointGroup dst,
			ClassifierImpl classifier, ActionSetList list, boolean isExclusive,
			boolean isObserver, long maxApplicationPriority,
			long maxActionPrecedence, Set<Endpoint> srcMembers,
			Set<Endpoint> dstMembers) {
		super();
		this.name = name;
		this.src = src;
		this.dst = dst;
		this.classifier = classifier;
		this.actionSetList = new ActionSetListImpl(list.getList());
		this.isExclusive = isExclusive;
		this.isObserver = isObserver;
		this.maxApplicationPriority = maxApplicationPriority;
		this.maxActionPrecedence = maxActionPrecedence;
		this.srcMembers = srcMembers;
		this.dstMembers = dstMembers;
		this.delegates = new HashSet<CompilerNode>();
		this.parents = new HashSet<CompilerNode>();
		this.dominant = null;
	}

	public boolean isComposable(CompilerNode other) {

		// an exclusive policy is not composable
        if (this.isExclusive() || other.isExclusive()) {
        	return false;
        }

        // two policies with only composable action sets are composable
        if (this.list().isComposable() && other.list().isComposable() ) {
        	return true;
        }

        // a observer policy is composable with a non-exclusive policy
        if (this.isObserver() ||other.isObserver()) {
        	return true;
        }
        return false;
	}

	private long maxApplicationPriority(CompilerNode other) {
		return this.maxApplicationPriority > other.maxApplicationPriority() ?
				this.maxApplicationPriority : other.maxApplicationPriority();

	}
	private long maxActionPrecedence(CompilerNode other) {
		return this.maxActionPrecedence> other.maxActionPrecedence() ?
				this.maxActionPrecedence : other.maxActionPrecedence();

	}


	public void compose(CompilerNode other) {
		// combine other into self
		StringBuilder buffer = new StringBuilder();
		buffer.append(String.format("%s/%s/12.1.compose", this.name, other.name()));
		this.name = new String(buffer);

		//set dominant to none
		this.dominant = null;

		src = src.and(other.srcCompilerEndpointGroup());

		dst = dst.and(other.dstCompilerEndpointGroup());

        this.classifier = this.classifier.and(other.classifier());
        this.actionSetList = this.list().compose(other.list());
        this.maxApplicationPriority = this.maxApplicationPriority(other);
        this.isExclusive = this.isExclusive() ||  other.isExclusive();
        this.isObserver = this.isObserver() && other.isObserver();
        this.maxActionPrecedence = this.maxActionPrecedence(other);

        // pre-compute the endpoints affected by this node

        //use intersection set operation (retainAll) to calculate newSrcSet
        Set<Endpoint> newSrcSet = new HashSet<Endpoint>(this.srcMembers);
        newSrcSet.retainAll(other.srcMembers());
        this.srcMembers = newSrcSet;

        //use intersection set operation (retainAll) to calculate newDstSet
        Set<Endpoint> newDstSet = new HashSet<Endpoint>(this.dstMembers);
        newDstSet.retainAll(other.dstMembers());
        this.dstMembers = newDstSet;
	}

	public CompilerNode resolveConflict(CompilerNode other, List<CompilerNode> list, Map<EndpointId,Endpoint> endpointMap) {
		CompilerNode n = null;

		//resolve first using exclusive property
		if (this.isExclusive() && other.isExclusive()) {
			if (this.maxApplicationPriority() > other.maxApplicationPriority()) {
				n = this.createDominantNode(other, endpointMap);
			}
			else {
				n = other.createDominantNode(this, endpointMap);
			}
		}
		else if (this.isExclusive()) {
			n = this.createDominantNode(other, endpointMap);
		}
		else if (other.isExclusive()) {
			n = other.createDominantNode(this, endpointMap);
		}

		//resolve next with action composition
		else if (this.isComposable(other)) {
			n = createNode(this);
			n.compose(other);
		}

		//resolve by action precedence
		else if (this.maxActionPrecedence()>other.maxActionPrecedence()) {
			n = this.createDominantNode(other, endpointMap);
		}
		else if (this.maxActionPrecedence()<other.maxActionPrecedence()) {
			n = other.createDominantNode(this, endpointMap);
		}

		//resolve by application/policy priority
		else if (this.maxApplicationPriority()>other.maxApplicationPriority()) {
			n = this.createDominantNode(other, endpointMap);
		}
		else if (this.maxApplicationPriority()<other.maxApplicationPriority()) {
			n = other.createDominantNode(this, endpointMap);
		}
		else {
			throw new IllegalStateException("cannot resolve compiler node conflict");
		}

		this.delegates.add(n);
		other.delegates.add(n);
		list.add(n);

		return n;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result
				+ ((dstMembers == null) ? 0 : dstMembers.hashCode());
		result = prime * result + (isExclusive ? 1231 : 1237);
		result = prime * result + (isObserver ? 1231 : 1237);
		result = prime * result + ((actionSetList == null) ? 0 : actionSetList.hashCode());
		result = prime * result
				+ (int) (maxActionPrecedence ^ (maxActionPrecedence >>> 32));
		result = prime
				* result
				+ (int) (maxApplicationPriority ^ (maxApplicationPriority >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parents == null) ? 0 : parents.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result
				+ ((srcMembers == null) ? 0 : srcMembers.hashCode());
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
		CompilerNode other = (CompilerNode) obj;
		if (classifier == null) {
			if (other.classifier != null)
				return false;
		} else if (!classifier.equals(other.classifier))
			return false;
		if (delegates == null) {
			if (other.delegates != null)
				return false;
		} else if (!delegates.equals(other.delegates))
			return false;
		if (dominant == null) {
			if (other.dominant != null)
				return false;
		} else if (!dominant.equals(other.dominant))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (dstMembers == null) {
			if (other.dstMembers != null)
				return false;
		} else if (!dstMembers.equals(other.dstMembers))
			return false;
		if (isExclusive != other.isExclusive)
			return false;
		if (isObserver != other.isObserver)
			return false;
		if (actionSetList == null) {
			if (other.actionSetList != null)
				return false;
		} else if (!actionSetList.equals(other.actionSetList))
			return false;
		if (maxActionPrecedence != other.maxActionPrecedence)
			return false;
		if (maxApplicationPriority != other.maxApplicationPriority)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parents == null) {
			if (other.parents != null)
				return false;
		} else if (!parents.equals(other.parents))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (srcMembers == null) {
			if (other.srcMembers != null)
				return false;
		} else if (!srcMembers.equals(other.srcMembers))
			return false;
		return true;
	}

	@Override
	public String toString() {

		List<String> stringDelegates = new LinkedList<String>();
		for (CompilerNode cn: delegates) {
		    stringDelegates.add(cn.name());
		}

		List<String> stringParents = new LinkedList<String>();
		for (CompilerNode cn: parents) {
		    stringParents.add(cn.name());
		}

		String stringDominant = dominant == null ? "null": dominant.name();


		return "CompilerNode {name=" + name + ", src=" + src + ", dst=" + dst
				+ ", classifier=" + classifier + ", list=" + actionSetList
				+ ", isExclusive=" + isExclusive + ", isObserver=" + isObserver
				+ ", maxApplicationPriority=" + maxApplicationPriority
				+ ", maxActionPrecedence=" + maxActionPrecedence
				+ ", srcMembers=" + srcMembers + ", dstMembers=" + dstMembers
				+ ", dominant=" + stringDominant + " delegates" + stringDelegates + ", "
				+ "parents" + stringParents +"}";


	}

	public String prettyPrint() {

	    StringBuilder r = new StringBuilder();

	    r.append("CompilerNode:\n");
		r.append("  srcMembers=");
	    boolean first = true;
		for (Endpoint e: srcMembers) {
		    if (first) {
		        first = false;
		    }
		    else {
		        r.append(",");
		    }
		    r.append(e.id().toString());
		}
		r.append("\n");
		r.append("  dstMembers=");
		first = true;
		for (Endpoint e: dstMembers) {
		    if (first) {
		        first = false;
		    }
		    else {
		        r.append(",");
		    }
		    r.append(e.id().toString());
		}
		r.append("\n");
	    r.append("  classifier=");
	    for (Expression e : classifier.getExpressions()) {
	        for (Term t : e.getTerms()) {
	            r.append(t.typeLabel()+":");
	            for (Interval i : t.getIntervals()) {
	                r.append(" "+i.start());
	                if (i.end() != i.start()) {
	                    r.append("-"+i.end());
	                }
	            }
	        }
	        r.append("\n");
	    }

	    r.append("  ActionList=");
	    first = true;
		for (ActionSet as: actionSetList.getList()) {
		    for (Action a: as.getActions()) {
		        if (first) {
		            first = false;
		        }
		        else {
		            r.append(",");
		        }
		        r.append(a.label());
		    }
		}
		r.append("\n");
		r.append("\n");
		return r.toString();
	}

}

