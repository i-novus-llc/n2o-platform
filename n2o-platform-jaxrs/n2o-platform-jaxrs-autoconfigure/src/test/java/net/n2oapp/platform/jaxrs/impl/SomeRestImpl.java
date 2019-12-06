package net.n2oapp.platform.jaxrs.impl;

import net.n2oapp.platform.i18n.Message;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Реализация REST сервиса для демонстрации возможностей библиотеки
 */
@Controller
public class SomeRestImpl implements SomeRest {

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public Page<SomeModel> search(SomeCriteria criteria) {
        List<SomeModel> content = findAll(criteria);
        return new PageImpl<>(content, criteria, count(criteria));
    }

    @Override
    public Page<AbstractModel> searchModel(SomeCriteria criteria) {
        return new PageImpl<>(Collections.singletonList(new StringModel()), criteria, 1);
    }

    @Override
    public List<SomeModel> searchWithoutTotalElements(SomeCriteria criteria) {
        return findAll(criteria);
    }

    @Override
    public Long count(SomeCriteria criteria) {
        return 100L;
    }

    @Override
    public SomeModel getById(Long id) {
        return new SomeModel(id);
    }

    @Override
    public Long create(SomeModel model) {
        return 1L;
    }

    @Override
    public void update(SomeModel model) {
        if (model.getId() == null)
            throw new IllegalArgumentException("Field [id] mustn't be null");
        if (model.getId() < 0)
            throw new UserException(new Message("example.idPositive").set(model.getId()));
    }

    @Override
    public void delete(Long id) {
        // заглушка
    }

    @Override
    public void throwErrors() {
        List<Message> messages = new ArrayList<>(3);
        messages.add(new Message("user.error1", "раз"));
        messages.add(new Message("user.error1", "два"));
        messages.add(new Message("user.error2"));
        throw new UserException(messages);
    }

    @SuppressWarnings("all")
    @Override
    public String timeoutSuccess() throws InterruptedException {
        Thread.sleep(500);
        return "timeout success";
    }

    @SuppressWarnings("all")
    @Override
    public String timeoutFailure() throws InterruptedException {
        Thread.sleep(1500);
        return "timeout failure";
    }

    @Override
    public List<LocalDateTime> searchBySetOfTypedList(Set<List<LocalDateTime>> setOfList) {
        return setOfList.stream().findFirst().get();
    }

    @Override
    public Map<String, String> searchBySetOfTypedMap(Map<String, String> map) {
        return map;
    }

    @Override
    public Map<String, String> authHeader() {
        return Map.of("Authorization", httpHeaders.getHeaderString("Authorization"));
    }

    private List<SomeModel> findAll(SomeCriteria criteria) {
        return LongStream.range(criteria.getOffset(), criteria.getOffset() + criteria.getPageSize())
                .mapToObj(id -> model(id, criteria))
                .collect(Collectors.toList());
    }

    private SomeModel model(long id, SomeCriteria criteria) {
        SomeModel model = new SomeModel(id);
        model.setDate(criteria.getDateBegin());
        model.setName(criteria.getLikeName());
        model.setDateEnd(criteria.getDateEnd());
        return model;
    }
}
