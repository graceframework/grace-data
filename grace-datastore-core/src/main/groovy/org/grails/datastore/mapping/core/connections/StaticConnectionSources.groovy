package org.grails.datastore.mapping.core.connections

import groovy.transform.CompileStatic
import org.springframework.core.env.PropertyResolver

import org.grails.datastore.mapping.core.DatastoreUtils

/**
 * A static non-mutable implementation for existing for a set of existing {@link ConnectionSource} instances
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class StaticConnectionSources<T, S extends ConnectionSourceSettings> extends AbstractConnectionSources<T, S> {

    protected final Map<String, ConnectionSource<T, S>> connectionSourceMap = new LinkedHashMap<>();

    StaticConnectionSources(ConnectionSource<T, S> defaultConnectionSource, Iterable<ConnectionSource<T, S>> otherConnectionSources, PropertyResolver configuration = DatastoreUtils.createPropertyResolver(null)) {
        super(defaultConnectionSource, new SingletonConnectionSources.NullConnectionFactory<T, S>(), configuration)

        connectionSourceMap.put(ConnectionSource.DEFAULT, defaultConnectionSource)
        for (ConnectionSource<T, S> source in otherConnectionSources) {
            connectionSourceMap.put(source.name, source)
        }
    }

    @Override
    Iterable<ConnectionSource<T, S>> getAllConnectionSources() {
        return connectionSourceMap.values()
    }

    @Override
    ConnectionSource<T, S> getConnectionSource(String name) {
        return connectionSourceMap.get(name)
    }

    @Override
    ConnectionSource<T, S> addConnectionSource(String name, PropertyResolver configuration) {
        throw new UnsupportedOperationException("Cannot add a connection source it a SingletonConnectionSources")
    }

    @Override
    protected Iterable<String> getConnectionSourceNames(ConnectionSourceFactory<T, S> connectionSourceFactory, PropertyResolver configuration) {
        return connectionSourceMap.keySet()
    }

    @Override
    Iterator<ConnectionSource<T, S>> iterator() {
        return connectionSourceMap.values().iterator()
    }
}
