package net.n2oapp.platform.selection.api;

import org.springframework.lang.NonNull;

/**
 * Интерфейс, который группирует запросы и тем самым позволяет избежать проблемы {@code N+1}<br><br>
 *
 * Методы, помеченные {@link SelectionKey} могут быть двух видов:<br><br>
 * 1) Обязательный метод, возвращающий {@link java.util.Map}.<br>
 * Ключом в Map является идентификатор отображаемой сущности, а значением -- либо Fetcher, либо список Fetcher-ов.<br><br>
 *
 * 2) Опциональный метод в пределах того же {@link SelectionKey}, который возвращает вложеннный {@link Joiner}.<br><br>
 *
 * Так же методы, помеченные {@link SelectionKey}, предпочтительно должны быть as-lazy-as-possible.
 * Например, для сущностей {@code JPA} и их репозиториев это означает,
 *  что все ассоциации должны быть lazy. Например:
 *  <pre>
 *       &#064;Entity
 *       public class ParentEntity {
 *           &#064;JoinColumn
 *           &#064;ManyToOne(fetch = FetchType.LAZY)
 *           private ChildEntity child;
 *       }
 *  </pre>
 *  Иногда можно сделать исключение (для JPA это например если в JOIN-ed таблице небольшое кол-во записей)<br><br>
 *
 *  Пример простого Joiner-а:
 *  <pre>
 *      // Предположим, что первичным ключом сотрудника является Integer.
 *      // Так же у сотрудника есть организация, которой он принадлежит и отношение определено как LAZY
 *      public class EmployeeJoiner&lt;EmployeeDTO, Integer, EmployeeEntity, EmployeeFetcher&gt; {
 *
 *          private final OrganisationRepository repository;
 *
 *          public EmployeeJoiner(OrganisationRepository repository) {
 *              this.repository = repository;
 *          }
 *
 *          &#064;SelectionKey("organisation")
 *          public Map&lt;Integer, Fetcher&lt;Organisation&gt;&gt; joinOrganisation(Collection&lt;Employee&gt; employees) {
 *             organisationRepository.findEmployeeOrganisations(employees); // Делаем prefetch организаций
 *             Map&lt;Integer, Fetcher&lt;Organisation&gt;&gt; result = new HashMap&lt;&gt;();
 *             for (Employee employee : employees) {
 *                 if (employee.getOrganisation() != null) { // так как мы сделали prefetch -- hibernate проставил их в нашу сущность EmployeeEntity и запроса в базу при доступе к организации уже не будет
 *                     result.put(employee.getId(), new OrganisationFetcherImpl(employee.getOrganisation()));
 *                 }
 *             }
 *             return result;
 *         }
 *
 *         //Вложенный joiner (он сделает join полей организации)
 *         &#064;SelectionKey("organisation")
 *         public OrganisationJoiner organisationJoiner() {
 *             return new OrganisationJoinerImpl();
 *         }
 *
 *      }
 *  </pre>
 *  <br>
 *
 *  В классе {@link JoinUtil} есть удобные методы для различных отношений (односторонний ToOne, двусторонний OneToMany и т.д)
 *
 *  @param <T> Тип элементов (моделей DTO)
 *  @param <ID> Тип идентификаторов сущностей
 *  @param <E> Тип отображаемой сущности
 *  @param <F> {@link Fetcher}, с которым может работать этот Joiner
 */
public interface Joiner<T, ID, E, F extends Fetcher<T>> {

    /**
     * @param entity Сущность, полученная через {@link #getUnderlyingEntity(Fetcher)}
     * @return Идентификатор, по которому будет происходить группировка результатов запроса (не {@code null})
     */
    @NonNull ID getId(@NonNull E entity);

    /**
     * @return Сущность, которую отображает {@code fetcher}
     */
    @NonNull E getUnderlyingEntity(@NonNull F fetcher);

}
