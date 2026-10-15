package com.edam.minijson.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class SerializationContext {

    Set<Object> activeObjects;

    public SerializationContext() {
        this.activeObjects = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public boolean addObject(Object obj) {
        return activeObjects.add(obj);
    }

    public void removeObject(Object obj) {
        activeObjects.remove(obj);
    }
}
