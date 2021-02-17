package com.github.cloudyrock.mongock.driver.mongodb.test.template;

import com.github.cloudyrock.mongock.driver.api.entry.ChangeState;
import com.github.cloudyrock.mongock.driver.core.entry.ChangeEntryRepository;
import com.github.cloudyrock.mongock.driver.mongodb.test.template.util.IntegrationTestBase;
import com.github.cloudyrock.mongock.exception.MongockException;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Date;


public abstract class MongoChangeEntryRepositoryITestBase extends IntegrationTestBase {

  protected ChangeEntryRepository repository;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void shouldThrowException_WhenNoIndexCreation_IfIndexNoPreviouslyCreated() throws MongockException {
    exceptionRule.expect(MongockException.class);
    exceptionRule.expectMessage("Index creation not allowed, but not created or wrongly created for collection");
    initializeRepository(false);
  }

  @Test
  public void shouldBeOk_WhenNoIndexCreation_IfIndexAlreadyCreated() throws MongockException {
    getDefaultAdapter().createIndex(getIndexDocument(new String[]{"executionId", "author", "changeId"}), new IndexOptions().unique(true));
    initializeRepository(false);
  }

  @Test
  public void shouldReturnFalse_whenHasNotBeenExecuted_IfThereIsWithSameIdAndAuthorAndStateNull() {
    performTest(true, null, true);
  }

  @Test
  public void shouldReturnFalse_whenHasNotBeenExecuted_IfThereIsWithSameIdAndAuthorAndNoState() {
    performTest(false, null, true);
  }

  @Test
  public void shouldReturnFalse_whenHasNotBeenExecuted_IfThereIsWithSameIdAndAuthorAndStateEXECUTED() {
    performTest(true, ChangeState.EXECUTED.toString(), true);
  }

  @Test
  public void shouldReturnTrue_whenHasNotBeenExecuted_IfThereIsWithSameIdAndAuthorAndStateIGNORED() {
    performTest(true, ChangeState.IGNORED.toString(), false);
  }

  @Test
  public void shouldReturnTrue_whenHasNotBeenExecuted_IfThereIsWithSameIdAndAuthorAndStateFAILED() {
    performTest(true, ChangeState.FAILED.toString(), false);
  }

  private void performTest(boolean withState, String state, boolean assertResult) {
    initializeRepository(true);
    String changeId = "changeId";
    String author = "author";
    String executionId = "executionId";
    createAndInsertChangeEntry(withState, state, changeId, author, executionId);
    Assert.assertEquals("pre-requisite: changeEntry should be added", 1,
        getDefaultAdapter().countDocuments(new Document().append("changeId", changeId).append("author", author)));

    Assert.assertEquals(assertResult, repository.isAlreadyExecuted(changeId, author));
  }


  private void createAndInsertChangeEntry(boolean withState, String state, String changeId, String author, String executionId) {
    initializeRepository(true);
    Document existingEntry = new Document()
        .append("executionId", executionId)
        .append("changeId", changeId)
        .append("author", author)
        .append("timestamp", Date.from(Instant.now()))
        .append("changeLogClass", "anyClass")
        .append("changeSetMethod", "anyMethod")
        .append("metadata", null);
    if (withState) {
      existingEntry = existingEntry.append("state", state);
    }
    getDefaultAdapter().insertOne(existingEntry);
  }

  protected abstract void initializeRepository(boolean indexCreation);

  protected Document getIndexDocument(String[] uniqueFields) {
    final Document indexDocument = new Document();
    for (String field : uniqueFields) {
      indexDocument.append(field, 1);
    }
    return indexDocument;
  }
}
