package org.grails.datastore.mapping.core.connections

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic
import org.springframework.core.env.PropertyResolver

/**
 * Default implementation of the {@link ConnectionSources} interface.
 * This implementation reads {@link ConnectionSource} implementations from configuration and stores them in-memory
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class InMemoryConnectionSources<T, S extends ConnectionSourceSettings> extends AbstractConnectionSources<T, S> {

    protected final Map<String, ConnectionSource<T, S>> connectionSourceMap = new ConcurrentHashMap<>()

    InMemoryConnectionSources(ConnectionSource<T, S> defaultConnectionSource, ConnectionSourceFactory<T, S> connectionSourceFactory,
                              PropertyResolver configuration) {
        super(defaultConnectionSource, connectionSourceFactory, configuration)
        this.connectionSourceMap.put(ConnectionSource.DEFAULT, defaultConnectionSource)

        for (String name : getConnectionSourceNames(connectionSourceFactory, configuration)) {
            if (name.equals("dataSource")) continue // data source is reserved name for the default
            ConnectionSource<T, S> connectionSource = connectionSourceFactory.create(name, configuration, defaultConnectionSource.getSettings())
            if (connectionSource != null) {
                this.connectionSourceMap.put(name, connectionSource)
            }
        }
    }

    @Override
    Iterable<ConnectionSource<T, S>> getAllConnectionSources() {
        return Collections.unmodifiableCollection(this.connectionSourceMap.values())
    }

    @Override
    ConnectionSource<T, S> getConnectionSource(String name) {
        return this.connectionSourceMap.get(name)
    }

    @Override
    ConnectionSource<T, S> addConnectionSource(String name, PropertyResolver configuration) {
        if (name == null) {
            throw new IllegalArgumentException("Argument [name] cannot be null")
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Argument [configuration] cannot be null")
        }

        ConnectionSource<T, S> connectionSource = connectionSourceFactory.createRuntime(name, configuration, (S) this.defaultConnectionSource.getSettings())
        if (connectionSource == null) {
            throw new IllegalStateException("ConnectionSource factory returned null")
        }
        this.connectionSourceMap.put(name, connectionSource)

        for (listener in listeners) {
            listener.newConnectionSource(connectionSource)
        }
        return connectionSource
    }

}
