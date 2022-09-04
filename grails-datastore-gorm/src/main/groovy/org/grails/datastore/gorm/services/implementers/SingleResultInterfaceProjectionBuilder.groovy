package org.grails.datastore.gorm.services.implementers

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.Statement

import org.grails.datastore.gorm.services.ServiceImplementer

import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Support trait for building interface projections
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@CompileStatic
trait SingleResultInterfaceProjectionBuilder extends InterfaceProjectionBuilder {

    Statement buildInterfaceProjection(ClassNode targetDomainClass, MethodNode abstractMethodNode, Expression queryMethodCall, Expression args, MethodNode newMethodNode) {
        ClassNode declaringClass = newMethodNode.declaringClass
        ClassNode interfaceNode = (ClassNode) newMethodNode.getNodeMetaData(ServiceImplementer.RETURN_TYPE) ?: abstractMethodNode.returnType
        MethodNode methodTarget = buildInterfaceImpl(interfaceNode, declaringClass, targetDomainClass, abstractMethodNode)
        ClassNode innerClassNode = methodTarget.getDeclaringClass()

        VariableExpression delegateVar = varX('$delegate', innerClassNode)
        VariableExpression targetVar = varX('$target', targetDomainClass)
        MethodCallExpression setTargetCall = callX(delegateVar, '$setTarget', targetVar)
        setTargetCall.setMethodTarget(methodTarget)
        block(
                declS(delegateVar, ctorX(innerClassNode)),
                declS(targetVar, queryMethodCall),
                ifS(notNullX(targetVar), block(
                        stmt(setTargetCall),
                        returnS(delegateVar)
                ))
        )
    }

}
