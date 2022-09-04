/* Copyright (C) 2011 SpringSource
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
package org.grails.datastore.gorm.query.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Transforms regular Groovy-style finders into detached criteria
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class DetachedCriteriaASTTransformation implements ASTTransformation{

    /**
     * The method is invoked when an AST Transformation is active. For local transformations, it is invoked once
     * each time the local annotation is encountered. For global transformations, it is invoked once for every source
     * unit, which is typically a source file.
     *
     * @param nodes  The ASTnodes when the call was triggered. Element 0 is the AnnotationNode that triggered this
     *               annotation to be activated. Element 1 is the AnnotatedNode decorated, such as a MethodNode or ClassNode. For
     *               global transformations it is usually safe to ignore this parameter.
     * @param source The source unit being compiled. The source unit may contain several classes. For global transformations,
     *               information about the AST can be retrieved from this object.
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        DetachedCriteriaTransformer transformer = new DetachedCriteriaTransformer(source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        ClassNode cNode = (ClassNode) parent;
        transformer.visitClass(cNode);
    }

}
