/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.constraints;

import org.opendaylight.nic.mapped.MappedObject;

public class ClassifierConstraint extends MappedObject{

    public static final String PORT_MIN = "portMin";
    public static final String PORT_MAX = "portMax";
    public static final String PROTOCOL = "protocol";

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

    public static String TYPE = "ClassifierConstraint";

    public void setPortMax(Integer portMax) {
        this.properties.put(PORT_MAX, portMax.toString());
    }

    public void setProtocol(String protocol) {
        this.properties.put(PROTOCOL, protocol);
    }

    public void setPortMin(Integer portMin) {
        this.properties.put(PORT_MAX, portMin.toString());
    }

    public ClassifierConstraint(Integer portMin, Integer portMax, String protocol){
        setPortMin(portMin);
        setPortMax(portMax);
        setProtocol(protocol);
        this.type = TYPE;
    }

    public String type() {
        return type;
    }

    public String key() {
        StringBuilder key = new StringBuilder();
        key.append(getProtocol().toUpperCase());
        key.append("/");
        if(getPortMax().equals(getPortMin())) {
            key.append(getPortMax());
        }
        else {
            key.append(getPortMin());
            key.append("-");
            key.append(getPortMax());
        }

        return key.toString();
    }

    public Pair<Integer, Integer> intersection(ClassifierConstraint other){

        if(other == null) {
            return null;
        }

        if(!getProtocol().equals(other.getProtocol())) {
            return null;
        }

        Pair<Integer, Integer> range = new Pair<>(getPortMin(), getPortMax());
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
        return Integer.parseInt(this.properties.get(PORT_MIN));
    }

    public Integer getPortMax() {
        return Integer.parseInt(this.properties.get(PORT_MAX));
    }

    public String getProtocol() {
        return this.properties.get(PROTOCOL);
    }

    public void setKey(String key) { 
        this.key = key;
    }

    public static ClassifierConstraint fromMappedObject(MappedObject obj) {
        ClassifierConstraint constraint = new ClassifierConstraint(Integer.parseInt(obj.getProperty(PORT_MIN)), Integer.parseInt(obj.getProperty(PORT_MAX)), obj.getProperty(PROTOCOL));
        return constraint;
    }
}
