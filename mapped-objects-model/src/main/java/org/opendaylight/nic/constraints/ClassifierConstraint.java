/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.constraints;

import org.opendaylight.nic.mapped.MappedObject;

public class ClassifierConstraint implements MappedObject{

    private class Pair<F, S> {
        private F first; //first member of pair
        private S second; //second member of pair

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public void setFirst(F first) {
            this.first = first;
        }

        public void setSecond(S second) {
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }

    public static String type = "ClassifierConstraint";
    private Integer portMin;
    private Integer portMax;
    private String protocol;

    public ClassifierConstraint(Integer portMin, Integer portMax, String protocol){
        this.portMin = portMin;
        this.portMax = portMax;
        this.protocol = protocol;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String key() {
        StringBuilder key = new StringBuilder();
        key.append(protocol.toUpperCase());
        key.append("/");
        if(portMax.equals(portMin)) {
            key.append(portMax);
        }
        else {
            key.append(portMin);
            key.append("-");
            key.append(portMax);
        }

        return key.toString();
    }

    public Pair<Integer, Integer> intersection(ClassifierConstraint other){

        if(other == null)
            return null;

        if(!this.protocol.equals(other.getProtocol())) {
            return null;
        }

        Pair<Integer, Integer> range = new Pair<>(this.getPortMin(), this.getPortMax());
        Pair<Integer, Integer> otherRange = new Pair<>(other.getPortMin(), other.getPortMax());
        Pair<Integer, Integer> intersection = null;

        //get the range with the smaller starting point (min) and greater start (max)
        Pair<Integer, Integer> rangeMin = (range.getFirst() < otherRange.getFirst()  ? range : otherRange);
        Pair<Integer, Integer> rangeMax = (rangeMin.equals(range) ? otherRange : range);

        //min ends before max starts -> no intersection
        if (rangeMin.getSecond() < rangeMax.getFirst()) {
            return null; //the ranges don't intersect
        }

        intersection = new Pair<>(rangeMax.getFirst(), (rangeMin.getSecond() < rangeMax.getSecond()) ? rangeMin.getSecond() : rangeMax.getSecond());

        return intersection;

    }

    public Integer getPortMin() {
        return portMin;
    }

    public Integer getPortMax() {
        return portMax;
    }

    public String getProtocol() {
        return protocol;
    }
}