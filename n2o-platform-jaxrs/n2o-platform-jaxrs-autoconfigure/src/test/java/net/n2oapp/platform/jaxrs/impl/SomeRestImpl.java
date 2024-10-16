package net.n2oapp.platform.jaxrs.impl;

import net.n2oapp.platform.i18n.Message;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.api.*;
import net.n2oapp.platform.jaxrs.seek.RequestedPageEnum;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekRequest;
import net.n2oapp.platform.jaxrs.seek.SeekedPageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

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

    public static final List<SeekPivot> EXPECTED_PIVOTS = List.of(
            SeekPivot.of("id", "54,3"),
            SeekPivot.of("date", "1,970-01-01"),
            SeekPivot.of("plain-text", ",ABRACA:::::DAB:::::RA\\Хыхыхы"),
            SeekPivot.of("triplesix", ",異体字")
    );

    @Override
    public Page<SomeModel> search(SomeCriteria criteria) {
        List<SomeModel> content = findAll(criteria);
        return new PageImpl<>(content, criteria, count(criteria));
    }

    @Override
    public Page<AbstractModel> searchModel(SomeCriteria criteria) {
        return new PageImpl<>(Collections.singletonList(new StringModel("ABRACADABRA")), criteria, 1);
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
        SomeModel someModel = new SomeModel(id);
        someModel.setDate(new Date());
        someModel.setDateEnd(LocalDateTime.now());
        someModel.setName("SOME_NAME");
        return someModel;
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

    @Override
    public String timeoutSuccess() throws InterruptedException {
        Thread.sleep(500);
        return "timeout success";
    }

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

    @Override
    public List<AbstractModel<?>> getListOfAbstractModels() {
        return List.of(new StringModel("1"), new IntegerModel(2));
    }

    @Override
    public List<ListModel> getListModels() {
        return List.of(
                new ListModel(List.of(new IntegerModel(0), new IntegerModel(1), new IntegerModel(2))),
                new ListModel(List.of(new IntegerModel(3), new IntegerModel(4), new IntegerModel(5))));
    }

    @Override
    public SeekedPageImpl<String> searchSeeking(SeekRequest request) {
        if (
                request.getPage() == RequestedPageEnum.NEXT &&
                        request.getSize() == 1000 &&
                        request.getSort().equals(Sort.by(List.of(Sort.Order.asc("id"), Sort.Order.desc("name")))) &&
                        request.getPivots().equals(EXPECTED_PIVOTS)
        ) {
            return SeekedPageImpl.of(List.of("ok!"), true, false);
        }
        return null;
    }

    @Override
    public Map<String, String> mapQueryParam(Map<String, String> map) {
        return map;
    }

    @Override
    public Map<String, String> mapQueryParamViaHolder(MapParamHolder holder) {
        return holder.getMap();
    }

    @Override
    public SomeModel queryString(String param) {
        SomeModel someModel = new SomeModel(1L);
        someModel.setDate(new Date());
        someModel.setDateEnd(LocalDateTime.now());
        someModel.setName(param);
        return someModel;
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
