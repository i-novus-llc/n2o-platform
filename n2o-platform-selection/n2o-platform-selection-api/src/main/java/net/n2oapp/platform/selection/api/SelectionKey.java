package net.n2oapp.platform.selection.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Связывает методы {@link Selection} и {@link Fetcher} один к одному.
 * <br><br>
 * Например, если у нас есть сущность EmployeeEntity:
 *
 *      <br><pre>
 *      EmployeeEntity {
 *          String name;
 *          String surname;
 *      }
 *      </pre>
 *
 *      И есть DTO для нее:
 *      <br><pre>
 *      Employee {
 *          String name;
 *          String surname;
 *      }
 *      </pre>
 *
 *      Тогда выборка будет такой:
 *      <br><pre>
 *      EmployeeSelection implements Selection<Employee> {
 *
 *              {@code @SelectionKey(name)}
 *              SelectionEnum fetchName;
 *
 *              {@code @SelectionKey(surname)}
 *              SelectionEnum fetchSurname;
 *
 *      }
 *      </pre>
 *
 *      А fetcher:
 *      <br><pre>
 *      EmployeeFetcher implements Fetcher<Employee> {
 *
 *          {@code @SelectionKey(name)}
 *          void setName(Employee employee)
 *
 *          {@code @SelectionKey(surname)}
 *          void setSurname(Employee employee)
 *
 *      }
 *      </pre>
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SelectionKey {

    /**
     * @return Строковое представление ключа, по которому связывается {@link Selection} и {@link Fetcher}
     *
     * В общем случае данный ключ должен присутствовать и в реализации fetcher-а и в реализации выборки.
     * Однако при наличии ключа у fetcher-а и отсутствии его в выборке логического противоречия не возникает
     * (клиент мог указать выборку для некоторого типа, а fetcher отображает его дочерний тип),
     * поэтому такой вариант тоже возможен.
     * Но присутствие ключа в fetcher-е и отсутствие его в выборке -- это ошибка.
     */
    String value();

}
