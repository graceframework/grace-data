package org.grails.datastore.mapping.config

import groovy.transform.CompileStatic
import groovy.transform.Internal
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.lang.Nullable

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.services.Service

/**
 * Variant of {#link MethodInvokingFactoryBean} which returns the correct data service type instead of {@code java.lang.Object} so the Autowire with type works correctly.
 */
@Internal
@CompileStatic
class DatastoreServiceMethodInvokingFactoryBean extends MethodInvokingFactoryBean {

    private Class<?> serviceClass

    DatastoreServiceMethodInvokingFactoryBean(Class<?> serviceClass) {
        this.serviceClass = serviceClass
    }

    @Nullable
    private ConfigurableBeanFactory beanFactory

    @Override
    Class<?> getObjectType() {
        return serviceClass
    }

    @Override
    protected Object invokeWithTargetException() throws Exception {
        Object object = super.invokeWithTargetException()
        if (object) {
            ((Service) object).setDatastore((Datastore) targetObject)
            if (beanFactory instanceof AutowireCapableBeanFactory) {
                ((AutowireCapableBeanFactory) beanFactory).autowireBeanProperties(object, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false)
            }
        }
        object
    }

    @Override
    void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory)
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory
        }
    }
}
