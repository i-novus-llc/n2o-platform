package net.n2oapp.platform.loader.client;

import org.springframework.core.io.*;

import java.net.URI;

/**
 * Клиентский загрузчик данных
 */
public interface ClientLoader {

    /**
     * Загрузить данные
     *
     * @param server  Адрес api сервера
     * @param subject Владелец данных
     * @param target  Цель
     * @param file    Файл ресурса с данными
     */
    void load(URI server, String subject, String target, Resource file);
}
