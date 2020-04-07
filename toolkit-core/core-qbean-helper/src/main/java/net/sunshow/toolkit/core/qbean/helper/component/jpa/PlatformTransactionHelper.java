package net.sunshow.toolkit.core.qbean.helper.component.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 事务管理操作组件
 * author: sunshow.
 */
public class PlatformTransactionHelper {

    @Autowired
    private PlatformTransactionManager transactionManager;

    public <R> R executeTransaction(Callable<R> callable, Consumer<R> onNext, int transactionDefinition) {
        TransactionDefinition def = new DefaultTransactionDefinition(transactionDefinition);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            R result = callable.call();

            transactionManager.commit(status);

            if (onNext != null) {
                onNext.accept(result);
            }

            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public <R> R executeTransaction(Callable<R> callable, Consumer<R> onNext) {
        return this.executeTransaction(callable, onNext, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public <R> R executeTransaction(Callable<R> callable) {
        return this.executeTransaction(callable, null);
    }

}
