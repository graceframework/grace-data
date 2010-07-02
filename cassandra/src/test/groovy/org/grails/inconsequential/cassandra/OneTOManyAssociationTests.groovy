package org.grails.inconsequential.cassandra

import grails.persistence.Entity
import org.grails.inconsequential.core.ObjectDatastoreConnection
import org.junit.Test

/**
 * @author Graeme Rocher
 * @since 1.1
 */
class OneToManyAssociationTests extends AbstractCassandraTest {
  @Test
  void testOneToManyAssociation() {
    def ds = new CassandraDatastore()
    ds.mappingContext.addPersistentEntity(Author)
    ObjectDatastoreConnection conn = ds.connect(null)

    def a = new Author(name:"Stephen King")
    a.books = [ new Book(title:"The Stand"), new Book(title:"It")] as Set

    conn.persist(a)

    a = conn.retrieve(Author, new CassandraKey(a.id))

    assert a != null
    assert "Stephen King" == a.name
    assert a.books != null
    assert 2 == a.books.size()

    def b1 = a.books.find { it.title == 'The Stand'}
    assert b1 != null
    assert b1.id != null
    assert "The Stand" == b1.title

  }

}

@Entity
class Author {
  UUID id
  String name
  Set books
  static hasMany = [books:Book]
}
@Entity
class Book {
  UUID id
  String title
}

