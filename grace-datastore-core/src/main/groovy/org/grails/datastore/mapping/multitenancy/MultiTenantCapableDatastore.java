package org.grails.datastore.mapping.multitenancy;

import java.io.Serializable;

import groovy.lang.Closure;

import org.grails.datastore.mapping.core.Datastore;
import org.grails.datastore.mapping.core.connections.ConnectionSourceSettings;
import org.grails.datastore.mapping.core.connections.ConnectionSourcesProvider;

/**
 * An implementation that is capable of multi tenancy
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public interface MultiTenantCapableDatastore<T, S extends ConnectionSourceSettings> extends Datastore, ConnectionSourcesProvider<T, S> {

    /**
     * @return The multi tenancy mode
     */
    MultiTenancySettings.MultiTenancyMode getMultiTenancyMode();

    /**
     * @return Obtain the tenant resolver
     */
    TenantResolver getTenantResolver();

    /**
     * Obtains the datastore for the given tenant id. In SINGLE mode
     * this will be a unique datastore for each tenant. For MULTI mode
     * a single datastore is used for all tenants
     *
     * @param tenantId The tenant id
     * @return The datastore
     */
    Datastore getDatastoreForTenantId(Serializable tenantId);


    /**
     * Execute a new session with the given tenantId IF the session is not already present. This differs from the regular withNewSession method.
     * The idea is that if there is already a transaction or session bound for the current tenant it should be re-used, otherwise one should be created
     *
     *
     * @param tenantId The tenant id
     * @param callable the callable
     * @param <T1> the return type
     * @return The return value of the closure
     */
    <T1> T1 withNewSession(Serializable tenantId, Closure<T1> callable);

}
