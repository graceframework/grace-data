package org.grails.datastore.mapping.reflect

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.SourceUnit
import spock.lang.Specification

/**
 * Created by graemerocher on 19/04/2017.
 */
class AstUtilsSpec extends Specification {

    void "test implements interface"() {
        given:
        ClassNode node = ClassHelper.make("Test")
        def itfc = ClassHelper.make("ITest")
        node.addInterface(itfc)

        expect:
        AstUtils.implementsInterface(node, itfc)
        AstUtils.implementsInterface(node, itfc.name)
        !AstUtils.implementsInterface(node, "Another")
    }

    void "Check domain 'Post' annotated with Grails '@Entity'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
@grails.persistence.Entity
class Post {
}
''')

        ClassNode classNode = new ClassNode(clazz)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' annotated with GORM '@Entity'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
@grails.gorm.annotation.Entity
class Post {
}
''')

        ClassNode classNode = new ClassNode(clazz)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' annotated with JPA '@Entity'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
@javax.persistence.Entity
class Post {
}
''')

        ClassNode classNode = new ClassNode(clazz)

        expect:
        !AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' within 'grails-app/domain'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
class Post {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/grails-app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/grails-app/domain/org/grails/demo/Post.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' within 'app/domain'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
class Post {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/app/domain/org/grails/demo/Post.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'PostEntity' within 'grails-app/models'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
class PostEntity {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/grails-app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/grails-app/models/org/grails/demo/PostEntity.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check Post within 'grails-app/models'"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
class Post {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/grails-app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/grails-app/models/org/grails/demo/Post.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        !AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' within 'grails-app/models' and annotated with @Artefact('Domain')"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
@grails.artefact.Artefact("Domain")
class Post {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/grails-app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/grails-app/rest/org/grails/demo/Post.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        AstUtils.isDomainClass(classNode)
    }

    void "Check domain 'Post' within 'grails-app/domain' and allow abstract"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class<?> clazz = gcl.parseClass('''
@grails.artefact.Artefact("Domain")
abstract class Post {
}
''')

        SourceUnit sourceUnit = Mock()
        ModuleNode moduleNode = new ModuleNode(sourceUnit)
        moduleNode.putNodeMetaData('PROJECT_DIR', '/Users/grails/grails-demo-project')
        moduleNode.putNodeMetaData('GRAILS_APP_DIR', '/Users/grails/grails-demo-project/grails-app')
        sourceUnit.getAST() >> moduleNode
        sourceUnit.getName() >> '/Users/grails/grails-demo-project/grails-app/domain/org/grails/demo/Post.groovy'

        ClassNode classNode = new ClassNode(clazz)
        classNode.setModule(moduleNode)

        expect:
        AstUtils.isDomainClass(classNode)
    }

}
