package net.n2oapp.platform.selection.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * One to one mapping between {@link Selection} fields and {@link Mapper} methods.
 * <br><br>
 * For example, if we have such entity:
 *
 *      <br><pre>
 *      EmployeeEntity {
 *          String name;
 *          String surname;
 *      }
 *      </pre>
 *
 *      And such model (DTO) for this entity:
 *      <br><pre>
 *      Employee {
 *          String name;
 *          String surname;
 *      }
 *      </pre>
 *
 *      Then selection would look like:
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
 *      And mapper should look like:
 *      <br><pre>
 *      EmployeeMapper implements Mapper<Employee> {
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
    String value();
}
