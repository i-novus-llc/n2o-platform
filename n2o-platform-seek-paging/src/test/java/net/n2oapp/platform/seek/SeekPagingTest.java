package net.n2oapp.platform.seek;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@DataJpaTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = {"classpath:application.properties"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class SeekPagingTest {

    protected <T> T randomFrom(List<T> list) {
        if (list.isEmpty())
            return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    protected Integer randomInteger() {
        if (ThreadLocalRandom.current().nextBoolean())
            return null;
        return ThreadLocalRandom.current().nextInt(1, 11);
    }

}