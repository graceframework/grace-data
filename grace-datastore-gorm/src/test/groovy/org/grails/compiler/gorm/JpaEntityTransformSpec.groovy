package org.grails.compiler.gorm

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.validation.annotation.Validated
import spock.lang.Specification

/**
 * Created by graemerocher on 22/12/16.
 */
class JpaEntityTransformSpec extends Specification {

    void "test the JPA entity transform the entity correctly"() {
        given:
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class customerClass = gcl.parseClass('''
import javax.persistence.*
import javax.validation.constraints.Digits
@Entity
class Customer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long myId
    @Digits
    String firstName;
    String lastName;
    
    @javax.persistence.OneToMany
    Set<Customer> related

}

''')
        ClassPropertyFetcher cpf = ClassPropertyFetcher.forClass(customerClass)
        def instance = customerClass.newInstance()
        instance.myId = 1L
        expect:
        !GormEntity.isAssignableFrom(customerClass)

        when:
        def ann = customerClass.getAnnotation(Validated)
        def pd = cpf.getPropertyDescriptor(GormProperties.IDENTITY)
        then:
        ann == null
        pd == null

        when:
        customerClass.getDeclaredMethod("getId")
        customerClass.getDeclaredMethod('addToRelated', Object)
        customerClass.getDeclaredMethod('removeFromRelated', Object)
        then:
        thrown(NoSuchMethodException)
    }
}

