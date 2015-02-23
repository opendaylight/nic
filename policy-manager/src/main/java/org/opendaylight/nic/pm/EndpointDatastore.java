//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.Port;
import org.opendaylight.nic.common.SegmentId;
import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.EndpointId;
import org.opendaylight.nic.intent.impl.EndpointAttributeImpl;
import org.opendaylight.nic.services.ApplicationService;
import org.opendaylight.nic.services.EndpointService;
import org.opendaylight.nic.services.impl.EndpointServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.endpoints.endpoint.Attributes;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A temporary framework for storing endpoints and their attributes. Before
 * Lithium release, this should be replaced by a proper implementation of
 * {@link EndpointService}.
 *
 * @author Shaun Wackerly
 */
public class EndpointDatastore implements EndpointService, AutoCloseable,
        DataChangeListener {

    private static final Logger log = LoggerFactory
            .getLogger(EndpointDatastore.class);
    private final ListenerRegistration<DataChangeListener> dataChangeReg;
    private final EndpointService delegate;
    private final NetworkIntentCompilerTestFramework nicFramework;
    private final Device dummyDevice;

    /**
     * Constructs an endpoint datastore.
     *
     * @param as
     *            application service
     * @param dataBroker
     *            data broker
     * @param dev
     *            device to which all endpoints will be assigned
     */
    public EndpointDatastore(ApplicationService as, DataBroker dataBroker,
            Device dev, NetworkIntentCompilerTestFramework nicFramework) {
        delegate = new EndpointServiceImpl(as);
        dummyDevice = dev;
        this.nicFramework = nicFramework;

        InstanceIdentifier<Endpoint> path = InstanceIdentifier
                .builder(Endpoints.class).child(Endpoint.class).toInstance();
        dataChangeReg = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, path, this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void close() throws Exception {
        log.debug("EndpointDatastore.close()");
        dataChangeReg.close();
    }

    /**
     * Adds an endpoint to this datastore, based on the YANG model.
     *
     * @param ep
     *            yang endpoint
     */
    private void addEndpoint(Endpoint ep) {
        InetAddress ip = null;

        // Attempt to parse the IP address out of the ID
        try {
            ip = InetAddress.getByName(ep.getId());
        } catch (UnknownHostException e) {
            log.debug("Couldn't parse endpoint IP from '" + ep.getId() + "'");
        }

        add(new MyEndpoint(ep, ip, dummyDevice));
        log.info("Added endpoint: " + ep.getId());
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        log.debug("CHANGE: create=" + change.getCreatedData().size()
                + ", update=" + change.getUpdatedData().size() + ", remove="
                + change.getRemovedPaths().size());

        // Handle created data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change
                .getCreatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Endpoint))
                continue;

            Endpoint ep = (Endpoint) obj;
            addEndpoint(ep);
        }

        // Handle updated data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change
                .getUpdatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Endpoint))
                continue;

            Endpoint ep = (Endpoint) obj;
            addEndpoint(ep);
        }

        // Handle removed data
        for (InstanceIdentifier<?> path : change.getRemovedPaths()) {
            if (path.getTargetType() != Endpoint.class) {
                log.debug("Skipping removal of target type {}",
                        path.getTargetType());
                continue;
            }

            log.warn("Endpoint deletion is not supported");
        }

        // Notify the framework to recompile
        nicFramework.recompile();
    }

    @Override
    public void register(EndpointAttribute attribute, AppId app) {
        delegate.register(attribute, app);
    }

    @Override
    public void apply(EndpointAttribute attribute, EndpointId ep) {
        delegate.apply(attribute, ep);
    }

    @Override
    public void add(org.opendaylight.nic.intent.Endpoint endpoint) {
        delegate.add(endpoint);
    }

    @Override
    public void remove(org.opendaylight.nic.intent.Endpoint endpoint) {
        delegate.remove(endpoint);
    }

    @Override
    public org.opendaylight.nic.intent.Endpoint get(EndpointId id) {
        return delegate.get(id);
    }

    @Override
    public Map<EndpointId, org.opendaylight.nic.intent.Endpoint> getAll() {
        return delegate.getAll();
    }

    /**
     * A private implementation of {@link EndpointId}.
     */
    private class MyEndpointId implements
            org.opendaylight.nic.intent.EndpointId {
        private final String id;

        MyEndpointId(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MyEndpointId other = (MyEndpointId) obj;
            return id.equals(other.id);
        }
    }

    /**
     * A private implementation of {@link Endpoint}.
     */
    private class MyEndpoint extends
            org.opendaylight.nic.intent.impl.IpEndpoint {
        private final EndpointId id;
        private final Set<EndpointAttribute> attributes;
        private InetAddress ip = null;

        MyEndpoint(Endpoint ep, InetAddress ip, Device dev) {
            super(null, null, null, dev);
            this.id = new MyEndpointId(ep.getId());
            this.attributes = new HashSet<>();
            for (Attributes atts : ep.getAttributes()) {
                attributes.add(new EndpointAttributeImpl(atts.getAttribute()));
            }

        }

        @Override
        public EndpointId id() {
            return id;
        }

        @Override
        public Set<EndpointAttribute> attributes() {
            return Collections.unmodifiableSet(attributes);
        }

        @Override
        public InetAddress ip() {
            return ip;
        }

        @Override
        public SegmentId segId() {
            return null;
        }

        @Override
        public Port port() {
            return null;
        }

        @Override
        public Device device() {
            return null;
        }
    }
}
