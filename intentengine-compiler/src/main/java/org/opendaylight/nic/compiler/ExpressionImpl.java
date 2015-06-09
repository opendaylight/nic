//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.nic.compiler.api.Expression;
import org.opendaylight.nic.compiler.api.Term;
import org.opendaylight.nic.compiler.api.TermLabel;
import org.opendaylight.nic.compiler.api.TermType;

public class ExpressionImpl implements Expression {

    private final Map<TermType, TermImpl> termMap;

    // private boolean isEmpty() {
    // return this.termMap.isEmpty();
    // }

    public boolean isAll() {
        return !isNull() && this.termMap.isEmpty();
    }

    public ExpressionImpl(Map<TermType, TermImpl> termMap) {
        super();
        this.termMap = termMap;
    }

    @Override
    public Set<? extends TermLabel> getTermTypeLabels() {
        Set<TermLabel> labels = new HashSet<>();
        for (TermType tt : termMap.keySet())
            labels.add(tt.label());
        return labels;
    }

    @Override
    public Term getTerm(TermLabel termTypeLabel) {
        // FIXME: Needs more optimal implementation. How do I convert TermLabel
        // to TermType quickly?
        for (Entry<TermType, TermImpl> entry : termMap.entrySet()) {
            if (entry.getKey().label().equals(termTypeLabel))
                return entry.getValue();
        }
        return null;
    }

    private Set<TermType> getTermTypes() {
        return termMap.keySet();
    }

    @Override
    public Collection<TermImpl> getTerms() {
        return termMap.values();
    }

    @Override
    public Set<Entry<TermLabel, Term>> getEntries() {
        Map<TermLabel, Term> duplicate = new HashMap<>();
        for (Entry<TermType, TermImpl> entry : termMap.entrySet()) {
            duplicate.put(entry.getKey().label(), entry.getValue());
        }
        return duplicate.entrySet();
    }

    TermImpl getTerm(TermType tt) {
        TermImpl term = termMap.get(tt);
        if (term == null) {
            return TermImpl.getInstance(tt);
        }
        return term;
    }

    /**
     * This is needed to distinguish an empty expression from a fully wild
     * carded expression.
     * 
     * Both have no terms and so look identical. This instance is defined to be
     * empty.
     */
    public static final ExpressionImpl EXPRESSION_NULL = getInstance();

    public boolean isNull() {
        return this == EXPRESSION_NULL;
    }

    /**
     * Adds the given term to the list of terms in this expression. Note that
     * this operation is only allows if a term does not already exist with the
     * same type.
     * 
     * @param term
     *            term to add
     * @throws IllegalArgumentException
     *             if the term is of the same type as an existing term already
     *             in the expression
     */
    public void addTerm(TermImpl term) {
        if (null != termMap.get(term.getType()))
            throw new IllegalArgumentException(
                    "Term conflicts with existing type");

        termMap.put(term.getType(), term);
    }

    /**
     * Terms not present in an expression are wildcarded, which means they
     * include the full range. This api returns the Term in the Expression, and
     * if one is not present it returns a term which includes the full possible
     * range for the {@link TermType}.
     * 
     */
    public TermImpl getFullTerm(TermType tt) {
        TermImpl term = getTerm(tt);
        if (term.isEmpty()) {
            return TermImpl.getInstanceMax(tt);
        }
        return term;
    }

    /**
     * Public factory for constructing Expressions.
     * <p>
     * Note that Terms not in Expression include all values (are wildcarded).
     */
    public static ExpressionImpl getInstance() {
        ExpressionImpl e = new ExpressionImpl();
        return e;
    }

    /**
     * Public factory for constructing Expressions.
     * <p>
     * Note that Terms not in Expression include all values (are wildcarded).
     */
    public static ExpressionImpl getInstance(Collection<TermImpl> termCollection) {
        ExpressionImpl e = new ExpressionImpl();

        for (TermImpl t : termCollection) {
            if (t.equals(TermImpl.getInstanceMax(t.getType()))) {
                continue; // don't add max intervals
            }
            e.termMap.put(t.getType(), t);
        }
        return e;
    }

    private ExpressionImpl() {
        termMap = new HashMap<TermType, TermImpl>();
    }

    @Override
    public String toString() {
        return "Expression { " + termMap.values() + " }";
    }

    /**
     * Method performs operator > logic: this > other.
     * <p>
     * Returns true if this is a superset of other for every {@link TermImpl}.
     * For every {@link TermImpl} this has, it must be a superset of other. For
     * every term other has that this does not, this is a superset already
     * because Terms not in an expression are wildcarded, which means includes
     * all values.
     */
    public boolean greaterThan(ExpressionImpl other) {
        /**
         * Need to handle cases of All, Null, Value: for this and other
         */

        /** this=All, other=[Null,Value] */
        if (isAll() && !other.isAll()) {
            return true;
        }

        /** this=[All], other=[All] */
        if (isAll() && other.isAll()) {
            return false;
        }

        /** this=Null, other=[*] */
        if (isNull()) {
            return false;
        }

        /** this=[All,value], other=[Null] */
        if (!isNull() && other.isNull()) {
            return true;
        }

        /** this=[value], other=[value] */
        Set<TermType> termTypeUnion = new HashSet<TermType>(this.getTermTypes());
        termTypeUnion.addAll(other.getTermTypes());
        boolean greaterThan = false;

        for (TermType tt : termTypeUnion) {

            // Calculate intersection between t and other
            TermImpl thisTerm = getFullTerm(tt);
            TermImpl otherTerm = other.getFullTerm(tt);

            if (thisTerm.greaterThan(otherTerm)) {
                greaterThan = true;
            } else if (thisTerm.lessThan(otherTerm)) {
                // cannot be less then for any Term
                return false;
            }
            // else they are equal for this term, which is OK
        }
        return greaterThan;
    }

    /**
     * Method performs <em>sub</em> operator logic: this - other.
     * <p>
     * The difference of two expressions may be a disjunction of multiple
     * expressions, so this function returns a Set of expressions.
     */
    public Set<ExpressionImpl> sub(ExpressionImpl other) {

        // First check for shared terms. Terms are ANDed (&)
        //
        // If expression A and B each has terms q,r,s,t as follows
        // A(q[3,5]) r[3,5] u[5,8]) which means:
        // A( q[3,5]&r[3,5]&s[1,10]&t[1,10]&u[5,8]
        // B(q4,6]) s[3,5] u[4,9]) which means:
        // B( q[4,6]&r[1,10]&s[3,5]&t[1,10]&u[4,9]
        //
        // For term q: They overlap (no wildcards)
        // For term r: b>a (b is wildcarded)
        // For term s: a>b (a is wildcarded)
        // For term t: a=b (both are wild carded)
        // For term u: b>a (no wildcard)

        /**
         * Need to handle cases of All, Null, Value: for this and other
         */

        /** this=All, other=[All] */
        if (isAll() && other.isAll()) {
            // returning a set with no expressions - empty
            return new HashSet<ExpressionImpl>();
        }

        /** this=Null, other=[All, Null,Value] */
        if (isNull()) {
            // returning a set with no expressions - empty
            return new HashSet<ExpressionImpl>();
        }

        /** this=[All,value], other=[Null] */
        if (other.isNull()) {
            Set<ExpressionImpl> listWithThis = new HashSet<ExpressionImpl>();
            listWithThis.add(this);
            return listWithThis;
        }

        Set<ExpressionImpl> s = new HashSet<ExpressionImpl>();
        Set<TermType> termTypeUnion = new HashSet<TermType>(this.getTermTypes());
        termTypeUnion.addAll(other.getTermTypes());

        for (TermType tt : termTypeUnion) {

            // calculate difference between this and other Full Terms
            TermImpl thisTerm = getFullTerm(tt);
            TermImpl otherTerm = other.getFullTerm(tt);
            TermImpl termDifference = thisTerm.sub(otherTerm);

            // if thisTerm==termDifference, then this is orthogonal to other
            // returning here ensures a non-redundant expression is returned
            if (thisTerm.equals(termDifference)) {
                Set<ExpressionImpl> listWithThis = new HashSet<ExpressionImpl>();
                listWithThis.add(this);
                return listWithThis;
            }

            // if no difference, move to next shared term
            if (termDifference.isEmpty()) {
                continue;
            }

            // there is a difference, use it plus all other Terms in this to
            // create expression
            Collection<TermImpl> c = new HashSet<TermImpl>();
            c.add(termDifference);
            for (TermImpl t : getTerms()) {
                if (t.getType() == tt) {
                    continue;
                }
                c.add(t);
            }

            // add new expression to result set
            s.add(getInstance(c));
        }
        if (s.isEmpty()) {
            // returning a set with no expressions - empty
            return new HashSet<ExpressionImpl>();
        }
        return s;
    }

    /**
     * Method performs <em>and</em> operator logic: this AND other.
     * <p>
     * The and of two expressions is the intersection of all {@link TermImpl}s.
     */
    public ExpressionImpl and(ExpressionImpl other) {

        /**
         * Need to handle cases of All, Null, Value: for this and other
         */

        /** handle NULL */
        if (this.isNull() || other.isNull()) {
            return EXPRESSION_NULL;
        }

        /** handle both All */
        if (this.isAll() && other.isAll()) {
            return getInstance();
        }

        /** handle this=All */
        if (this.isAll()) {
            return getInstance(other.getTerms());
        }

        /** handle All */
        if (other.isAll()) {
            return getInstance(this.getTerms());
        }

        Collection<TermImpl> c = new HashSet<TermImpl>();
        Set<TermType> termTypeUnion = new HashSet<TermType>(this.getTermTypes());
        termTypeUnion.addAll(other.getTermTypes());

        for (TermType tt : termTypeUnion) {

            // Calculate intersection between t and other
            TermImpl thisTerm = getFullTerm(tt);
            TermImpl otherTerm = other.getFullTerm(tt);
            TermImpl termIntersection = thisTerm.and(otherTerm);

            // if no intersection, return
            if (termIntersection.isEmpty()) {
                return EXPRESSION_NULL;
            }

            // there is an intersection, add it to the expression
            c.add(termIntersection);
        }
        if (c.isEmpty()) {
            return EXPRESSION_NULL;
        }
        return getInstance(c);
    }

    /**
     * Method performs <em>add</em> operator logic: this + other.
     * <p>
     * This results in the union all {@link TermImpl}s.
     */
    public ExpressionImpl add(ExpressionImpl other) {

        /**
         * Need to handle cases of All, Null, Value: for this and other
         */

        /** handle NULL */
        if (this.isNull() && other.isNull()) {
            return EXPRESSION_NULL;
        }

        /** handle this=NULL */
        if (this.isNull()) {
            return getInstance(other.getTerms());
        }

        /** handle other=NULL */
        if (other.isNull()) {
            return getInstance(this.getTerms());
        }

        /** handle both All */
        if (this.isAll() || other.isAll()) {
            return getInstance();
        }

        Collection<TermImpl> c = new HashSet<TermImpl>();
        Set<TermType> termTypeUnion = new HashSet<TermType>(this.getTermTypes());
        termTypeUnion.addAll(other.getTermTypes());

        for (TermType tt : termTypeUnion) {

            // Calculate union between t and other
            TermImpl thisTerm = getFullTerm(tt);
            TermImpl otherTerm = other.getFullTerm(tt);
            TermImpl termIntersection = thisTerm.add(otherTerm);

            // if union results in full range, then do not add to expression
            if (termIntersection.equals(TermImpl.getInstanceMax(tt))) {
                continue;
            }

            // there is an intersection, add it to the expression
            c.add(termIntersection);
        }

        // empty here means expression is fully wildcarded
        if (c.isEmpty()) {
            return getInstance();
        }
        return getInstance(c);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((termMap == null) ? 0 : termMap.hashCode());
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
        ExpressionImpl other = (ExpressionImpl) obj;
        if (termMap == null) {
            if (other.termMap != null)
                return false;
        } else if (!termMap.equals(other.termMap))
            return false;
        return true;
    }

}
