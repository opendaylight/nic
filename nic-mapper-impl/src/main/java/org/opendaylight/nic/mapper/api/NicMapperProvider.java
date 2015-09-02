/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapper.api;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.mapper.rev150831.NicMapperKey;

public interface NicMapperProvider extends AutoCloseable {

    public List<String> getMappedValues(NicMapperKey key);
}

