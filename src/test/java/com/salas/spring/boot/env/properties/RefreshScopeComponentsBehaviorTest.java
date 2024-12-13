package com.salas.spring.boot.env.properties;

import com.salas.spring.boot.env.properties.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshScopeComponentsBehaviorTest extends PropertiesUpdateBaseTest {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    @Autowired
    private Client client;
    @LocalServerPort
    private int port;

    @BeforeAll
    public void init() {
        client.setPort(port);
    }

    @Order(value = 0)
    @Test
    void ids_assigned_in_alphabetical_order() {
        InjectionBasedBean injectionBased = context.getBean(InjectionBasedBean.class);
        assertIdAndProperty(injectionBased, 1, "initial-value");

        PropResolverBasedBean resolverBased = context.getBean(PropResolverBasedBean.class);
        assertIdAndProperty(resolverBased, 2, "initial-value");

        RefreshScopeInjectionBasedBean refreshableInjected = context.getBean(RefreshScopeInjectionBasedBean.class);
        assertIdAndProperty(refreshableInjected, 3, "initial-value");

        RefreshScopeResolverBasedBean refreshableResolver = context.getBean(RefreshScopeResolverBasedBean.class);
        assertIdAndProperty(refreshableResolver, 4, "initial-value");

        SimpleRefreshScopedBean simpleBean = context.getBean(SimpleRefreshScopedBean.class);
        assertIdAndProperty(simpleBean, 5, "constant");
    }

    @Order(value = 1)
    @Test
    void updatePropertyViaActuator_AndVerifyChanges() {
        client.callActuatorUrl(port + "/actuator/env", changePropRequest());

        InjectionBasedBean injectionBased = context.getBean(InjectionBasedBean.class);
        assertIdAndProperty(injectionBased, 1, "initial-value");

        PropResolverBasedBean resolverBased = context.getBean(PropResolverBasedBean.class);
        assertIdAndProperty(resolverBased, 2, "changed-value");

        RefreshScopeInjectionBasedBean refreshableInjected = context.getBean(RefreshScopeInjectionBasedBean.class);
        assertIdAndProperty(refreshableInjected, 3, "initial-value");
    }

    @Order(value = 2)
    @Test
    void callRefresh_refreshableBeans_willBeUpdated() {
        client.callActuatorUrl(port + "/actuator/refresh", changePropRequest());
        InjectionBasedBean injectionBased = context.getBean(InjectionBasedBean.class);
        assertIdAndProperty(injectionBased, 1, "initial-value");

        RefreshScopeInjectionBasedBean refreshableInjectionBased = context.getBean(RefreshScopeInjectionBasedBean.class);
        assertIdAndProperty(refreshableInjectionBased, 6, "changed-value");

        RefreshScopeResolverBasedBean refreshableResolver = context.getBean(RefreshScopeResolverBasedBean.class);
        assertIdAndProperty(refreshableResolver, 7, "changed-value");

        SimpleRefreshScopedBean simpleBean = context.getBean(SimpleRefreshScopedBean.class);
        assertIdAndProperty(simpleBean, 8, "constant");
    }

    @Order(value = 3)
    @Test
    void oneMoreRefresh_triggersOneMoreUpdate() {
        RefreshScopeInjectionBasedBean refreshableInjectionBased = context.getBean(RefreshScopeInjectionBasedBean.class);
        RefreshScopeResolverBasedBean refreshableResolver = context.getBean(RefreshScopeResolverBasedBean.class);
        SimpleRefreshScopedBean simpleBean = context.getBean(SimpleRefreshScopedBean.class);

        client.callActuatorUrl(port + "/actuator/refresh", changePropRequest());

        InjectionBasedBean injectionBased = context.getBean(InjectionBasedBean.class);
        assertIdAndProperty(injectionBased, 1, "initial-value");

        assertIdAndProperty(refreshableInjectionBased, 9, "changed-value");

        assertIdAndProperty(refreshableResolver, 10, "changed-value");

        assertIdAndProperty(simpleBean, 11, "constant");
    }

    @Order(value = 4)
    @Test
    void refreshableBeans_areSubclassed() {
        InjectionBasedBean injectionBased = context.getBean(InjectionBasedBean.class);
        assertThat(injectionBased.getClass()).isEqualTo(InjectionBasedBean.class);

        RefreshScopeInjectionBasedBean refreshableBean = context.getBean(RefreshScopeInjectionBasedBean.class);
        assertThat(refreshableBean.getClass()).isNotEqualTo(RefreshScopeInjectionBasedBean.class);
        assertThat(RefreshScopeInjectionBasedBean.class).isAssignableFrom(refreshableBean.getClass());
    }

    @Order(value = 5)
    @Test
    void refreshability_isDefinedInBeanDefinition() {
        BeanDefinition regular = configurableListableBeanFactory.getBeanDefinition("injectionBasedBean");
        assertThat(regular.getScope()).isEqualTo(BeanDefinition.SCOPE_SINGLETON);
        assertThat(regular.getBeanClassName()).isEqualTo("com.salas.spring.boot.env.properties.util.InjectionBasedBean");

        BeanDefinition refreshable = configurableListableBeanFactory.getBeanDefinition("refreshScopeInjectionBasedBean");
        assertThat(refreshable.getBeanClassName()).isEqualTo("org.springframework.cloud.context.scope.GenericScope$LockedScopedProxyFactoryBean");
        assertThat(Objects.requireNonNull(
                refreshable.getOriginatingBeanDefinition()).getScope())
                .isEqualTo(RefreshAutoConfiguration.REFRESH_SCOPE_NAME);
    }


    private Map<String, String> changePropRequest() {
        return Map.of("name", "prop.for.refresh", "value", "changed-value");
    }

    private void assertIdAndProperty(IdAndPropAware bean, int id, String value) {
        assertThat(bean.getId()).isEqualTo(id);
        assertThat(bean.getProperty()).isEqualTo(value);
    }
}
