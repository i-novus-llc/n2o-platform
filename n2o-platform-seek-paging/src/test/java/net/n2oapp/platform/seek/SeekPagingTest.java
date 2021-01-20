package net.n2oapp.platform.seek;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@DataJpaTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = {"classpath:application.properties"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class SeekPagingTest {

    protected LocalDate randomLocalDate() {
        if (ThreadLocalRandom.current().nextBoolean())
            return null;
        return LocalDate.ofYearDay(
            2020,
            ThreadLocalRandom.current().nextInt(1, 5)
        );
    }

    protected String randomString(int length) {
        if (ThreadLocalRandom.current().nextBoolean())
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ) {
            char c = (char) ThreadLocalRandom.current().nextInt(48, 120);
            if (Character.isWhitespace(c))
                continue;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    protected <T> T randomFrom(List<T> list) {
        if (list.isEmpty())
            return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    protected BigInteger randomBigInteger() {
        if (ThreadLocalRandom.current().nextBoolean())
            return null;
        return BigInteger.valueOf(ThreadLocalRandom.current().nextInt(1, 4));
    }

}