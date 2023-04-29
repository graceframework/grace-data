package grails.gorm.rx.multitenancy

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.core.connections.ConnectionSources
import org.grails.datastore.mapping.multitenancy.AllTenantsResolver
import org.grails.datastore.mapping.multitenancy.MultiTenancySettings
import org.grails.datastore.mapping.multitenancy.MultiTenantCapableDatastore
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.rx.RxDatastoreClient
import org.grails.gorm.rx.api.RxGormEnhancer

/**
 * Tenants implementation for RxGORM
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
@Slf4j
class Tenants {
    /**
     * Execute the given closure for each tenant.
     *
     * @param callable The closure
     * @return The result of the closure
     */
    static void eachTenant(@DelegatesTo(RxDatastoreClient) Closure callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findSingleDatastoreClient()
        eachTenantInternal(datastoreClient, callable)
    }

    /**
     * Execute the given closure for each tenant.
     *
     * @param callable The closure
     * @return The result of the closure
     */
    static void eachTenant(Class<? extends RxDatastoreClient> datastoreClass, @DelegatesTo(RxDatastoreClient) Closure callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findDatastoreClientByType(datastoreClass)
        eachTenantInternal(datastoreClient, callable)
    }

    /**
     * Execute the given closure for each tenant for the given datastore. This method will create a new datastore session for the scope of the call and hence is designed to be used to manage the connection life cycle
     * @param callable The closure
     * @return The result of the closure
     */
    static void eachTenant(MultiTenantCapableDatastore multiTenantCapableDatastore, Closure callable) {
        MultiTenancySettings.MultiTenancyMode multiTenancyMode = multiTenantCapableDatastore.multiTenancyMode
        if (multiTenancyMode == MultiTenancySettings.MultiTenancyMode.DATABASE) {
            if (multiTenantCapableDatastore.tenantResolver instanceof AllTenantsResolver) {
                def tenantIds = ((AllTenantsResolver) multiTenantCapableDatastore.tenantResolver).resolveTenantIds()
                for (tenantId in tenantIds) {
                    withId(multiTenantCapableDatastore, tenantId, callable)
                }
            }
            else {
                ConnectionSources connectionSources = multiTenantCapableDatastore.connectionSources
                for (ConnectionSource connectionSource in connectionSources.allConnectionSources) {
                    def tenantId = connectionSource.name
                    if (tenantId != ConnectionSource.DEFAULT) {
                        withId(multiTenantCapableDatastore, tenantId, callable)
                    }
                }
            }
        }
        else if (multiTenancyMode.isSharedConnection()) {
            TenantResolver tenantResolver = multiTenantCapableDatastore.tenantResolver
            if (tenantResolver instanceof AllTenantsResolver) {
                for (tenantId in ((AllTenantsResolver) tenantResolver).resolveTenantIds()) {
                    withId(multiTenantCapableDatastore, tenantId, callable)
                }
            }
            else {
                throw new UnsupportedOperationException("Multi tenancy mode $multiTenancyMode is configured, but the configured TenantResolver does not implement the [org.grails.datastore.mapping.multitenancy.AllTenantsResolver] interface")
            }
        }
        else {
            throw new UnsupportedOperationException("Method not supported in multi tenancy mode $multiTenancyMode")
        }
    }

    /**
     * @return The current tenant id
     */
    static Serializable currentId() {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findSingleDatastoreClient()
        def tenantId = grails.gorm.multitenancy.Tenants.CurrentTenant.get()
        if (tenantId != null) {
            return tenantId
        }
        else {
            return datastoreClient.getTenantResolver().resolveTenantIdentifier()
        }
    }

    /**
     * The current id for the given datastore
     *
     * @param multiTenantCapableDatastore The multi tenant capable datastore
     * @return The current id
     */
    static Serializable currentId(MultiTenantCapableDatastore multiTenantCapableDatastore) {
        def tenantId = CurrentTenant.get()
        if (tenantId != null) {
            log.debug "Found tenant id [$tenantId] bound to thread local"
            return tenantId
        }
        else {
            TenantResolver tenantResolver = multiTenantCapableDatastore.getTenantResolver()
            Serializable tenantIdentifier = tenantResolver.resolveTenantIdentifier()
            log.debug "Resolved tenant id [$tenantIdentifier] from resolver [${tenantResolver.getClass().simpleName}]"
            return tenantIdentifier
        }
    }

    /**
     * @return The current tenant id for the given datastore type
     */
    static Serializable currentId(Class<? extends RxDatastoreClient> datastoreClass) {
        RxDatastoreClient datastore = RxGormEnhancer.findDatastoreClientByType(datastoreClass)
        def tenantId = grails.gorm.multitenancy.Tenants.CurrentTenant.get()
        if (tenantId != null) {
            log.debug "Found tenant id [$tenantId] bound to thread local"
            return tenantId
        }
        else {
            def tenantResolver = datastore.getTenantResolver()
            def tenantIdentifier = tenantResolver.resolveTenantIdentifier()
            log.debug "Resolved tenant id [$tenantIdentifier] from resolver [${tenantResolver.getClass().simpleName}]"
            return tenantIdentifier
        }
    }

    /**
     * Execute the given closure without any tenant id. In Multi tenancy mode SINGLE this will execute against the default data source. If multi tenancy mode
     * MULTI this will execute without including the "tenantId" on any query. Use with caution.
     *
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withoutId(Closure<T> callable) {
        Datastore datastore = GormEnhancer.findSingleDatastore()
        if (datastore instanceof MultiTenantCapableDatastore) {
            MultiTenantCapableDatastore multiTenantCapableDatastore = (MultiTenantCapableDatastore) datastore
            return withoutId(multiTenantCapableDatastore, callable)
        }
        else {
            throw new UnsupportedOperationException("Datastore implementation does not support multi-tenancy")
        }
    }

    /**
     * Execute the given closure without tenant id for the given datastore. This method will create a new datastore session for the scope of the call and hence is designed to be used to manage the connection life cycle
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withoutId(MultiTenantCapableDatastore multiTenantCapableDatastore, Closure<T> callable) {
        return CurrentTenant.withoutTenant {
            if (multiTenantCapableDatastore.getMultiTenancyMode().isSharedConnection()) {
                def i = callable.parameterTypes.length
                if (i == 0) {
                    return callable.call()
                }
                else {
                    return multiTenantCapableDatastore.withSession { session ->
                        return callable.call(session)
                    }
                }
            }
            else {
                return multiTenantCapableDatastore.withNewSession(ConnectionSource.DEFAULT) { session ->
                    def i = callable.parameterTypes.length
                    switch (i) {
                        case 0:
                            return callable.call()
                            break
                        case 1:
                            return callable.call(ConnectionSource.DEFAULT)
                            break
                        case 2:
                            return callable.call(ConnectionSource.DEFAULT, session)
                        default:
                            throw new IllegalArgumentException("Provided closure accepts too many arguments")
                    }

                }
            }
        } as T
    }

    /**
     * Execute the given closure with the current tenant
     *
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withCurrent(@DelegatesTo(RxDatastoreClient) Closure<T> callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findSingleDatastoreClient()
        def tenantIdentifier = datastoreClient.getTenantResolver().resolveTenantIdentifier()
        return withTenantIdInternal(datastoreClient, tenantIdentifier, callable)
    }

    /**
     * Execute the given closure with the current tenant
     *
     * @param datastoreClass The datastore class
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withCurrent(Class<? extends RxDatastoreClient> datastoreClass, @DelegatesTo(RxDatastoreClient) Closure<T> callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findDatastoreClientByType(datastoreClass)
        def tenantIdentifier = datastoreClient.getTenantResolver().resolveTenantIdentifier()
        return withTenantIdInternal(datastoreClient, tenantIdentifier, callable)
    }

    /**
     * Execute the given closure with given tenant id
     * @param tenantId The tenant id
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withId(Serializable tenantId, @DelegatesTo(RxDatastoreClient) Closure<T> callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findSingleDatastoreClient()
        return withTenantIdInternal(datastoreClient, tenantId, callable)
    }

    /**
     * Execute the given closure with given tenant id
     * @param tenantId The tenant id
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withId(Class<? extends RxDatastoreClient> datastoreClass, Serializable tenantId, @DelegatesTo(RxDatastoreClient) Closure<T> callable) {
        RxDatastoreClient datastoreClient = RxGormEnhancer.findDatastoreClientByType(datastoreClass)
        return withTenantIdInternal(datastoreClient, tenantId, callable)
    }

    /**
     * Execute the given closure with given tenant id for the given datastore. This method will create a new datastore session for the scope of the call and hence is designed to be used to manage the connection life cycle
     * @param tenantId The tenant id
     * @param callable The closure
     * @return The result of the closure
     */
    static <T> T withId(MultiTenantCapableDatastore multiTenantCapableDatastore, Serializable tenantId, Closure<T> callable) {
        return CurrentTenant.withTenant(tenantId) {
            if (multiTenantCapableDatastore.getMultiTenancyMode().isSharedConnection()) {
                def i = callable.parameterTypes.length
                if (i == 2) {
                    return multiTenantCapableDatastore.withSession { session ->
                        return callable.call(tenantId, session)
                    }
                }
                else {
                    switch (i) {
                        case 0:
                            return callable.call()
                            break
                        case 1:
                            return callable.call(tenantId)
                            break
                        default:
                            throw new IllegalArgumentException("Provided closure accepts too many arguments")
                    }
                }
            }
            else {
                return multiTenantCapableDatastore.withNewSession(tenantId) { session ->
                    def i = callable.parameterTypes.length
                    switch (i) {
                        case 0:
                            return callable.call()
                            break
                        case 1:
                            return callable.call(tenantId)
                            break
                        case 2:
                            return callable.call(tenantId, session)
                        default:
                            throw new IllegalArgumentException("Provided closure accepts too many arguments")
                    }

                }
            }
        } as T
    }

    private static <T> T withTenantIdInternal(RxDatastoreClient datastoreClient, Serializable tenantIdentifier, Closure<T> callable) {
        return grails.gorm.multitenancy.Tenants.CurrentTenant.withTenant(tenantIdentifier) {
            callable.setDelegate(datastoreClient)
            def i = callable.parameterTypes.length
            switch (i) {
                case 0:
                    return callable.call()
                    break
                case 1:
                    return callable.call(tenantIdentifier)
                    break
                default:
                    throw new IllegalArgumentException("Provided closure accepts too many arguments")
            }
        }
    }

    protected static void eachTenantInternal(RxDatastoreClient datastoreClient, Closure callable) {
        MultiTenancySettings.MultiTenancyMode multiTenancyMode = datastoreClient.multiTenancyMode
        ConnectionSources connectionSources = datastoreClient.connectionSources
        if (multiTenancyMode == MultiTenancySettings.MultiTenancyMode.DATABASE) {
            for (ConnectionSource connectionSource in connectionSources.allConnectionSources) {
                def tenantId = connectionSource.name
                if (tenantId != ConnectionSource.DEFAULT) {
                    withTenantIdInternal(datastoreClient, tenantId, callable)
                }
            }
        }
        else if (multiTenancyMode.isSharedConnection()) {
            TenantResolver tenantResolver = datastoreClient.tenantResolver
            if (tenantResolver instanceof AllTenantsResolver) {
                for (tenantId in ((AllTenantsResolver) tenantResolver).resolveTenantIds()) {
                    withTenantIdInternal(datastoreClient, tenantId, callable)
                }
            }
            else {
                throw new UnsupportedOperationException("Multi tenancy mode $multiTenancyMode is configured, but the configured TenantResolver does not implement the [org.grails.datastore.mapping.multitenancy.AllTenantsResolver] interface")
            }
        }
        else {
            throw new UnsupportedOperationException("Method not supported in multi tenancy mode $multiTenancyMode")
        }
    }

    @CompileStatic
    protected static class CurrentTenant {

        private static final ThreadLocal<Serializable> currentTenantThreadLocal = new ThreadLocal<>()

        /**
         * @return Obtain the current tenant
         */
        static Serializable get() {
            currentTenantThreadLocal.get()
        }

        /**
         * Set the current tenant
         *
         * @param tenantId The tenant id
         */
        private static void set(Serializable tenantId) {
            currentTenantThreadLocal.set(tenantId)
        }

        private static void remove() {
            currentTenantThreadLocal.remove()
        }

        /**
         * Execute with the current tenant
         *
         * @param callable The closure
         * @return The result of the closure
         */
        static <T> T withTenant(Serializable tenantId, Closure<T> callable) {
            def previous = get()
            try {
                set(tenantId)
                callable.call(tenantId)
            } finally {
                if (previous == null) {
                    remove()
                }
                else {
                    set(previous)
                }
            }
        }

        /**
         * Execute without current tenant
         *
         * @param callable The closure
         * @return The result of the closure
         */
        static <T> T withoutTenant(Closure<T> callable) {
            def previous = get()
            try {
                set(ConnectionSource.DEFAULT)
                callable.call()
            } finally {
                if (previous == null) {
                    remove()
                }
                else {
                    set(previous)
                }
            }
        }
    }

}
