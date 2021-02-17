package com.github.cloudyrock.mongock.driver.mongodb.v3.repository;

import com.github.cloudyrock.mongock.driver.mongodb.test.template.MongoChangeEntryRepositoryITestBase;
import com.github.cloudyrock.mongock.driver.mongodb.test.template.util.MongoDBDriverTestAdapter;
import com.github.cloudyrock.mongock.driver.mongodb.v3.MongoDb3DriverTestAdapterImpl;
import com.github.cloudyrock.mongock.exception.MongockException;
import org.bson.Document;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class Mongo3ChangeEntryRepositoryITest extends MongoChangeEntryRepositoryITestBase {


  protected void initializeRepository(boolean indexCreation) {
    repository = Mockito.spy(new Mongo3ChangeEntryRepository<>(getDataBase().getCollection(CHANGELOG_COLLECTION_NAME), indexCreation, ReadWriteConfiguration.getDefault()));
    repository.initialize();
  }
  protected void initializeRepository(boolean indexCreation, ReadWriteConfiguration readWriteConfiguration) {
    repository = Mockito.spy(new Mongo3ChangeEntryRepository<>(getDataBase().getCollection(CHANGELOG_COLLECTION_NAME), indexCreation, readWriteConfiguration));
    repository.initialize();
  }

  @Test
  public void shouldCreateUniqueIndex_whenEnsureIndex_IfNotCreatedYet() throws MongockException {
    initializeRepository(true);

    //then
    verify((Mongo3ChangeEntryRepository)repository, times(1)).createRequiredUniqueIndex();
    // and not
    verify((Mongo3ChangeEntryRepository)repository, times(0)).dropIndex(any(Document.class));
  }

  @Test
  public void shouldNoCreateUniqueIndex_whenEnsureIndex_IfAlreadyCreated() throws MongockException {
    initializeRepository(true);
    // given
    repository = Mockito.spy(new Mongo3ChangeEntryRepository(getDataBase().getCollection(CHANGELOG_COLLECTION_NAME), true, ReadWriteConfiguration.getDefault()));

    doReturn(true).when((Mongo3ChangeEntryRepository)repository).isUniqueIndex(any(Document.class));

    // when
    repository.initialize();

    //then
    verify((Mongo3ChangeEntryRepository)repository, times(0)).createRequiredUniqueIndex();
    // and not
    verify((Mongo3ChangeEntryRepository)repository, times(0)).dropIndex(new Document());
  }

  @Override
  protected MongoDBDriverTestAdapter getAdapter(String collectionName) {
    return new MongoDb3DriverTestAdapterImpl(getDataBase().getCollection(collectionName));
  }
}
