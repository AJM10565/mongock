package com.github.cloudyrock.mongock.driver.mongodb.springdata.v2.decorator.operation.executable.insert;

import com.github.cloudyrock.mongock.driver.api.lock.guard.decorator.Invokable;
import org.springframework.data.mongodb.core.ExecutableInsertOperation;

public interface ExecutableInsertDecorator<T> extends
    Invokable,
    ExecutableInsertOperation.ExecutableInsert<T>,
    TerminatingInsertDecorator<T>,
    InsertWithCollectionDecorator<T>,
    InsertWithBulkModeDecorator<T> {

  ExecutableInsertOperation.ExecutableInsert<T> getImpl();
}
