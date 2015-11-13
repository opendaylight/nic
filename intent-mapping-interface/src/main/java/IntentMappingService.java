/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

import java.util.Collection;
import java.util.List;

public interface IntentMappingService {
    void add(String key, String obj);

    void addList(String key, List<String> objs);

    Collection<String> retrieve(String key);

    Collection<String> keys();

    String stringRepresentation(String key);
}
