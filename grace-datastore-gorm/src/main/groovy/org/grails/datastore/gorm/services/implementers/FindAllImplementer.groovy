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
package org.grails.datastore.gorm.services.implementers

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Ordered
import org.grails.datastore.mapping.reflect.AstUtils

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS

/**
 * Automatically implements {@link grails.gorm.services.Service} interface methods that start with "list" and
 * return an iterable of domain classes
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@CompileStatic
class FindAllImplementer extends AbstractDetachedCriteriaServiceImplementor implements Ordered, IterableServiceImplementer<GormEntity> {

    static final List<String> HANDLED_PREFIXES = ['list', 'find', 'get', 'retrieve']

    @Override
    protected boolean isCompatibleReturnType(ClassNode domainClass, MethodNode methodNode, ClassNode returnType, String prefix) {
        return AstUtils.isIterableOrArrayOfDomainClasses(returnType)
    }

    @Override
    Iterable<String> getHandledPrefixes() {
        HANDLED_PREFIXES
    }

    @Override
    protected boolean lookupById() {
        return false
    }

    @Override
    protected ClassNode resolveDomainClassFromSignature(ClassNode currentDomainClassNode, MethodNode methodNode) {
        ClassNode returnType = methodNode.returnType
        if (returnType.isArray()) {
            return returnType.componentType
        }
        else {
            return returnType.genericsTypes[0].type
        }
    }

    @Override
    void implementById(ClassNode domainClassNode, MethodNode abstractMethodNode, MethodNode newMethodNode, ClassNode targetClassNode, BlockStatement body, Expression byIdLookup) {
        // no-op
    }

    @Override
    void implementWithQuery(ClassNode domainClassNode, MethodNode abstractMethodNode, MethodNode newMethodNode, ClassNode targetClassNode, BlockStatement body, VariableExpression detachedCriteriaVar, Expression queryArgs) {
        ClassNode returnType = (ClassNode) newMethodNode.getNodeMetaData(RETURN_TYPE) ?: newMethodNode.returnType
        Expression methodCall = callX(detachedCriteriaVar, "list", queryArgs)
        if (returnType.isArray()) {
            methodCall = castX(returnType.plainNodeReference, methodCall)
        }
        body.addStatement(
                returnS(methodCall)
        )
    }

}
