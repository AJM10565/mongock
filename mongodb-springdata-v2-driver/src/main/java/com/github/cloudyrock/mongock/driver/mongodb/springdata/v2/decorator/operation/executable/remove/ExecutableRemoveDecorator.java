package com.github.cloudyrock.mongock.driver.mongodb.springdata.v2.decorator.operation.executable.remove;

import com.github.cloudyrock.mongock.driver.api.lock.guard.decorator.Invokable;
import org.springframework.data.mongodb.core.ExecutableRemoveOperation;

public interface ExecutableRemoveDecorator<T> extends Invokable, ExecutableRemoveOperation.ExecutableRemove<T>, RemoveWithCollectionDecorator<T> {
}
