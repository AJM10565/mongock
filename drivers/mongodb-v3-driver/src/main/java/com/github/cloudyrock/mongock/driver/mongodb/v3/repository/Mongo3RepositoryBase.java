package com.github.cloudyrock.mongock.driver.mongodb.v3.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import io.changock.driver.core.common.Repository;
import io.changock.migration.api.exception.ChangockException;
import io.changock.utils.field.FieldInstance;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public abstract class Mongo3RepositoryBase<DOMAIN_CLASS> implements Repository<DOMAIN_CLASS, Document> {

  private final static Logger logger = LoggerFactory.getLogger(Mongo3RepositoryBase.class);
  private final static int INDEX_ENSURE_MAX_TRIES = 3;

  private final String[] uniqueFields;
  private final String fullCollectionName;
  private final boolean indexCreation;
  private boolean ensuredCollectionIndex = false;
  protected MongoCollection<Document> collection;

  public Mongo3RepositoryBase(MongoCollection<Document> collection, String[] uniqueFields, boolean indexCreation) {
    this.collection = collection;
    this.fullCollectionName = collection.getNamespace().getDatabaseName() + "." + collection.getNamespace().getCollectionName();
    this.uniqueFields = uniqueFields;
    this.indexCreation = indexCreation;
  }

  @Override
  public synchronized void initialize() {
    if (!this.ensuredCollectionIndex) {
      ensureIndex(INDEX_ENSURE_MAX_TRIES);
      this.ensuredCollectionIndex = true;
    }
  }

  private void ensureIndex(int tryCounter) {
    if (tryCounter <= 0) {
      throw new ChangockException("Max tries " + INDEX_ENSURE_MAX_TRIES + " index  creation");
    }
    if (!isIndexFine()) {
      if (!indexCreation) {
        throw new ChangockException("Index creation not allowed, but not created or wrongly created for collection " + collection.getNamespace().getCollectionName());
      }
      cleanResidualUniqueKeys();
      if (!isRequiredIndexCreated()) {
        createRequiredUniqueIndex();
      }
      ensureIndex(--tryCounter);
    }
  }

  protected boolean isIndexFine() {
    return getResidualKeys().isEmpty() && isRequiredIndexCreated();
  }

  protected void cleanResidualUniqueKeys() {
    logger.debug("Removing residual uniqueKeys for collection [{}]", getCollectionName());
    getResidualKeys().stream()
        .peek(index -> logger.debug("Removed residual uniqueKey [{}] for collection [{}]", index.toString(), getCollectionName()))
        .forEach(this::dropIndex);
  }

  private List<Document> getResidualKeys() {
    return StreamSupport.stream(collection.listIndexes().spliterator(), false)
        .filter(this::doesNeedToBeRemoved)
        .collect(Collectors.toList());
  }

  protected boolean doesNeedToBeRemoved(Document index) {
    return !isIdIndex(index) && isUniqueIndex(index) && !isRightIndex(index);
  }

  protected boolean isIdIndex(Document index) {
    return  ((Document) index.get("key")).get("_id") != null;
  }

  protected boolean isRequiredIndexCreated() {
    return StreamSupport.stream(collection.listIndexes().spliterator(), false).anyMatch(this::isRightIndex);
  }

  protected void createRequiredUniqueIndex() {
    collection.createIndex(getIndexDocument(uniqueFields), new IndexOptions().unique(true));
    logger.debug("Index in collection [{}] was recreated", getCollectionName());
  }

  protected boolean isRightIndex(Document index) {
    final Document key = (Document) index.get("key");
    boolean keyContainsAllFields = Stream.of(uniqueFields).allMatch(uniqueField -> key.get(uniqueField) != null);
    boolean onlyTheseFields = key.size() == uniqueFields.length;
    return keyContainsAllFields && onlyTheseFields && isUniqueIndex(index);
  }

  protected boolean isUniqueIndex(Document index) {
    return fullCollectionName.equals(index.getString("ns")) //changes namespace: [database].[collection]
        && index.getBoolean("unique", false);// checks it'unique
  }

  private String getCollectionName() {
    return collection.getNamespace().getCollectionName();
  }

  protected Document getIndexDocument(String[] uniqueFields) {
    final Document indexDocument = new Document();
    Stream.of(uniqueFields).forEach(field -> indexDocument.append(field, 1));
    return indexDocument;
  }

  protected void dropIndex(Document index) {
    collection.dropIndex(index.get("name").toString());
  }

  @Override
  public Document mapFieldInstances(List<FieldInstance> fieldInstanceList) {
    Document document = new Document();
    fieldInstanceList.forEach(def -> document.append(def.getName(), def.getValue()));
    return document;
  }


}
