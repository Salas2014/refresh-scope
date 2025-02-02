package com.salas.spring.boot.env.properties.util;


import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public abstract class IdAndPropAware {

    private static final AtomicInteger ID_REGISTRY = new AtomicInteger(0);
    private final int id = ID_REGISTRY.incrementAndGet();
    public abstract String getProperty();
}
