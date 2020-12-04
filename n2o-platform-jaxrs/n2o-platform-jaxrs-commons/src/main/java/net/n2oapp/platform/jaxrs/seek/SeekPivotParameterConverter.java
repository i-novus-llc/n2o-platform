package net.n2oapp.platform.jaxrs.seek;

import net.n2oapp.platform.jaxrs.TypedParamConverter;

public class SeekPivotParameterConverter implements TypedParamConverter<SeekPivot> {

    @Override
    public Class<SeekPivot> getType() {
        return SeekPivot.class;
    }

    @Override
    public SeekPivot fromString(String value) {
        StringBuilder name = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '\\') {
                if (i + 1 < value.length() && value.charAt(i + 1) == ':') { // escaped colon
                    i += 2;
                    name.append(':');
                } else {
                    name.append('\\');
                    i++;
                }
            } else if (c == ':') {
                break;
            } else {
                name.append(c);
                i++;
            }
        }
        if (i >= value.length())
            throw new IllegalArgumentException("No name provided in " + value);
        String lastSeenValue = value.substring(i + 1);
        return new SeekPivot(name.toString(), lastSeenValue);
    }

    @Override
    public String toString(SeekPivot value) {
        return value.getName().replace(":", "\\:") + ":" + value.getLastValue();
    }

}
