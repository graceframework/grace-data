/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.gorm.multitenancy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import org.grails.datastore.gorm.transform.GormASTTransformationClass;
import org.grails.datastore.mapping.core.connections.ConnectionSourcesProvider;

/**
 * <p>An AST transformation that makes a particular class or method applicable to the tenant id returned by the passed closure. For example:</p>
 *
 * <pre>
 * class FooService {
 *  {@code @Tenant}({ "foo" })
 *   void updateFoo() {
 *       ...
 *   }
 * }
 * </pre>
 *
 * @since 6.1
 * @author Graeme Rocher
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@GroovyASTTransformationClass("org.grails.datastore.gorm.transform.OrderedGormTransformation")
@GormASTTransformationClass("org.grails.datastore.gorm.multitenancy.transform.TenantTransform")
public @interface Tenant {

    /**
     * The tenant resolver
     *
     * @return A closure that resolves the tenant id
     */
    Class value();

    /**
     * If you are using multiple GORM implementations and wish to create a transaction for a specific implementation then use this. For example {@code @Transactional(forDatastore=HibernateDatastore) }
     *
     * @return The type of the datastore
     */
    Class<? extends ConnectionSourcesProvider>[] datastore() default {};

}
