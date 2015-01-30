# Network Intent Composition

Network Intent Composition project will enable the controller to manage and direct network services and network resources based on describing the Intent for network behaviors and network policies. Intents are described to the controller through a new NorthBound Interface, which provides generalized and abstracted policy semantics instead of Openflow-like flow rules. The Intent based NBI allows for a descriptive way to get what is desired from the infrastructure, unlike the current SDN interfaces which are based on describing how to provide different services. This NBI will accommodate orchestration services and network and business oriented SDN applications, including OpenStack Neutron, Service Function Chaining, and Group Based Policy. The Network Intent Composition function will use existing OpenDaylight Network Service Functions and Southbound Plugins to control both virtual and physical network devices. The Network Intent Composer will be designed to be protocol agnostic such that any control protocol can be used such as Openflow, OVSDB, I2RS, Netconf, SNMP etc.

## OpenDaylight Intent Yang Model Proposal
---

This project represents a proposal for a Yang model that defines a purposefully simplistic intent model. By providing
a simplistic model it allows the community as a whole to experiment and grow the model when the community runs into
pain points. 

## Request Model
---
As a side effect of the intent model a simple request model was included as well. This model represents a simplistic
model of how a request based capability might be added on top of MD-SAL allowing ODL to support an interaction model
where a client submits an asynchronous request and is able to asynchronous monitor is status until completion.

