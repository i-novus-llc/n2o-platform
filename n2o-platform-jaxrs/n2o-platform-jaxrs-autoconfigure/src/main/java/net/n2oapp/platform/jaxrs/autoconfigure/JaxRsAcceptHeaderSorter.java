package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;

/*
  Сортировка Accept заголовка, отдающая предпочтение json.
  Таким образом, если сервер получит запрос с "Accept=application/xml,application/json" (клиенту неважно,
  в каком формате он получит свои данные) -- сервер всегда выберет json.
 */
@Provider(value = Provider.Type.InInterceptor)
public class JaxRsAcceptHeaderSorter extends AbstractPhaseInterceptor<Message> {

    public JaxRsAcceptHeaderSorter() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.containsKey(ACCEPT)) {
            Object o = message.get(ACCEPT);
            if (o.getClass() == String.class) {
                message.put(ACCEPT, sort((String) o));
            }
        }
        if (message.containsKey(PROTOCOL_HEADERS)) {
            Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
            if (headers.containsKey(ACCEPT)) {
                List<String> accept = headers.get(ACCEPT);
                if (!accept.isEmpty()) {
                    ListIterator<String> iterator = accept.listIterator();
                    while (iterator.hasNext()) {
                        String s = sort(iterator.next());
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
