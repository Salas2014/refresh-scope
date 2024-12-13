package com.salas.spring.boot.env.properties.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InjectionBasedBean extends IdAndPropAware {

    @Value("${prop.for.refresh}")
    private String property;

}
