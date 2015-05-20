/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Stores the Intent details as a map
 */
public class VTNRendererUtility {

    static Map<String, List<IntentWrapper>> hashMapIntentUtil = new HashMap<String, List<IntentWrapper>>();

    public static void storeIntentDetail(Map hashmapIntent)
    {
        hashMapIntentUtil.putAll(hashmapIntent);
    }
}
