package net.n2oapp.platform.loader.server;

class TestModel {
    private String code;
    private String name;

    public TestModel(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public TestModel() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
