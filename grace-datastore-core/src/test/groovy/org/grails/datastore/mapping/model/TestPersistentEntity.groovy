package org.grails.datastore.mapping.model

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.config.Entity

/**
 * @author Graeme Rocher
 * @since 1.1
 */
@SuppressWarnings('unchecked')
@CompileStatic
class TestPersistentEntity extends AbstractPersistentEntity {

    private TestClassMapping classMapping

    TestPersistentEntity(Class type, MappingContext ctx) {
        super(type, ctx)
    }

    @Override
    ClassMapping getMapping() { new TestClassMapping(this, context) }

    class TestClassMapping extends AbstractClassMapping<Entity> {

        private Entity mappedForm

        TestClassMapping(PersistentEntity entity, MappingContext context) {
            super(entity, context)
            mappedForm = context.mappingFactory.createMappedForm(TestPersistentEntity.this)
        }

        @Override
        Entity getMappedForm() {
            return mappedForm
        }

    }

}
