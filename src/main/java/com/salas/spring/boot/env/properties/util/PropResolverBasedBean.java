package com.salas.spring.boot.env.properties.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropResolverBasedBean extends IdAndPropAware {
    private final PropertyResolver propertyResolver;

    public String getProperty() {
        return propertyResolver.getProperty("prop.for.refresh");
    }

}
