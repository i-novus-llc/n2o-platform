package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;

/*
  Сортировка Accept заголовка, отдающая предпочтение json.
  Таким образом, если сервер получит запрос с "Accept=application/xml,application/json" (клиенту неважно,
  в каком формате он получит свои данные) -- сервер всегда выберет json.
  Если Accept заголовка в запросе не будет -- по-умолчанию будет добавлен json.
 */
@Provider(value = Provider.Type.InInterceptor)
public class JaxRsAcceptHeaderSorter extends AbstractPhaseInterceptor<Message> {

    public JaxRsAcceptHeaderSorter() {
        super(Phase.RECEIVE);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "java:S3740"})
    public void handleMessage(Message message) {
        Object o = message.get(ACCEPT);
        if (o instanceof String) {
            message.put(ACCEPT, sort((String) o));
        } else if (o == null) {
            message.put(ACCEPT, MediaType.APPLICATION_JSON);
        }
        Object protocolHeaders = message.get(PROTOCOL_HEADERS);
        if (protocolHeaders != null && !(protocolHeaders instanceof Map))
            return;
        if (protocolHeaders == null) {
            protocolHeaders = new HashMap<>();
            message.put(PROTOCOL_HEADERS, protocolHeaders);
        }
        Map headers = (Map) protocolHeaders;
        Object accept = headers.get(ACCEPT);
        if (accept != null && !(accept instanceof List))
            return;
        if (accept == null) {
            accept = new ArrayList<>(1);
            ((ArrayList<String>) accept).add(MediaType.APPLICATION_JSON);
            headers.put(ACCEPT, accept);
        } else {
            List list = (List) accept;
            if (!list.isEmpty()) {
                ListIterator iterator = list.listIterator();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    if (next instanceof String) {
                        String s = sort((String) next);
                        iterator.set(s);
                    }
                }
            }
        }
    }

    private String sort(String accept) {
        String[] split = accept.split(",");
        Arrays.sort(split, (s1, s2) -> {
            boolean s1Xml = StringUtils.containsIgnoreCase(s1, "xml");
            boolean s2Xml = StringUtils.containsIgnoreCase(s2, "xml");
            if (s1Xml && !s2Xml)
                return 1;
            else if (!s1Xml && s2Xml)
                return -1;
            else
                return 0;
        });
        return String.join(",", split);
    }

}
