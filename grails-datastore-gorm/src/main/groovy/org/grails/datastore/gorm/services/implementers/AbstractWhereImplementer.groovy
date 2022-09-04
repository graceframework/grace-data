package org.grails.datastore.gorm.services.implementers

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit

import grails.gorm.DetachedCriteria
import grails.gorm.services.Where

import org.grails.datastore.gorm.query.transform.DetachedCriteriaTransformer
import org.grails.datastore.mapping.reflect.AstUtils

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.grails.datastore.mapping.reflect.AstUtils.processVariableScopes

/**
 * Abstract implementation for queries annotated with {@link Where}
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@CompileStatic
abstract class AbstractWhereImplementer extends AbstractReadOperationImplementer implements AnnotatedServiceImplementer<Where> {


    public static final int POSITION = FindAllByImplementer.POSITION - 100

    @Override
    int getOrder() {
        return POSITION
    }

    @Override
    boolean isAnnotated(ClassNode domainClass, MethodNode methodNode) {
        AstUtils.findAnnotation(methodNode, Where) != null
    }

    @Override
    boolean doesImplement(ClassNode domainClass, MethodNode methodNode) {
        if (isAnnotated(domainClass, methodNode)) {
            return isCompatibleReturnType(domainClass, methodNode, methodNode.returnType, methodNode.name)
        }
        return false
    }

    @Override
    void doImplement(ClassNode domainClassNode, MethodNode abstractMethodNode, MethodNode newMethodNode, ClassNode targetClassNode) {
        AnnotationNode annotationNode = AstUtils.findAnnotation(abstractMethodNode, Where)
        abstractMethodNode.annotations.remove(annotationNode)
        SourceUnit sourceUnit = abstractMethodNode.declaringClass.module.context

        Expression expr = annotationNode.getMember("value")
        if (expr instanceof ClosureExpression) {
            ClosureExpression originalClosureExpression = (ClosureExpression) expr
            ClosureExpression closureExpression = AstUtils.makeClosureAwareOfArguments(newMethodNode, originalClosureExpression)
            DetachedCriteriaTransformer transformer = new DetachedCriteriaTransformer(sourceUnit)
            transformer.transformClosureExpression(domainClassNode, closureExpression)

            BlockStatement body = (BlockStatement) newMethodNode.getCode()

            Expression argsExpression = findArgsExpression(newMethodNode)
            VariableExpression queryVar = varX('$query')
            // def query = new DetachedCriteria(Foo)
            body.addStatement(
                    declS(queryVar, ctorX(getDetachedCriteriaType(domainClassNode), args(classX(domainClassNode.plainNodeReference))))
            )
            Expression connectionId = findConnectionId(newMethodNode)

            if (connectionId != null) {
                body.addStatement(
                        assignS(queryVar, callX(queryVar, "withConnection", connectionId))
                )
            }
            body.addStatement(
                    assignS(queryVar, callX(queryVar, "build", closureExpression))
            )
            Expression queryExpression = callX(queryVar, getQueryMethodToExecute(domainClassNode, newMethodNode), argsExpression != null ? argsExpression : AstUtils.ZERO_ARGUMENTS)
            body.addStatement(
                    buildReturnStatement(domainClassNode, abstractMethodNode, newMethodNode, queryExpression)
            )
            processVariableScopes(sourceUnit, targetClassNode, newMethodNode)
        }
        else {
            AstUtils.error(sourceUnit, annotationNode, "@Where value must be a closure")
        }
    }

    protected ClassNode getDetachedCriteriaType(ClassNode domainClassNode) {
        ClassHelper.make(DetachedCriteria)
    }

    protected Statement buildReturnStatement(ClassNode domainClass, MethodNode abstractMethodNode, MethodNode methodNode, Expression queryExpression) {
        returnS(queryExpression)
    }

    protected String getQueryMethodToExecute(ClassNode domainClass, MethodNode newMethodNode) {
        "find"
    }

    @Override
    Iterable<String> getHandledPrefixes() {
        return Collections.emptyList()
    }

}
