package org.grails.datastore.gorm.transactions.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation

import grails.gorm.transactions.Rollback

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RollbackTransform extends TransactionalTransform {

    public static final ClassNode MY_TYPE = new ClassNode(Rollback)

    @Override
    protected String getTransactionTemplateMethodName() {
        return "executeAndRollback"
    }

}
