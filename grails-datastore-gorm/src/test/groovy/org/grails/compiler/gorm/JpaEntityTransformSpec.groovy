package org.grails.compiler.gorm

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Transient
import jakarta.validation.constraints.Digits

import org.springframework.validation.annotation.Validated
import spock.lang.Specification

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher

/**
 * Created by graemerocher on 22/12/16.
 */
class JpaEntityTransformSpec extends Specification {

    void "test the JPA entity transform the entity correctly"() {
        given:
        ClassPropertyFetcher cpf = ClassPropertyFetcher.forClass(Customer)
        expect:
        GormEntity.isAssignableFrom(Customer)
        Customer.getAnnotation(Validated)
        Customer.getDeclaredMethod("getId").returnType == Long
        Customer.getDeclaredMethod("getId").getAnnotation(Transient)
        cpf.getPropertyDescriptor(GormProperties.IDENTITY)
        Customer.getDeclaredMethod('addToRelated', Object)
        Customer.getDeclaredMethod('removeFromRelated', Object)
    }
}

@Entity
class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long myId
    @Digits
    String firstName;
    String lastName;

    @OneToMany
    Set<Customer> related

}
