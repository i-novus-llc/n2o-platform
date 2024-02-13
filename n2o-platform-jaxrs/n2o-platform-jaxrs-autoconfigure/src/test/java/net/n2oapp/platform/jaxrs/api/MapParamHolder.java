package net.n2oapp.platform.jaxrs.api;

import jakarta.ws.rs.QueryParam;
import java.util.Map;

public class MapParamHolder {

    @QueryParam("map")
    private Map<String, String> map;

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

}
