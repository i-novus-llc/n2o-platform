package net.n2oapp.platform.test.autoconfigure;

import net.n2oapp.platform.test.autoconfigure.pg.EnableTestcontainersPg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.ResultSet;

// DEBUG по org.testcontainers показывает, с какой версией Docker API прошёл ping
// и был ли откат на 1.32 — без этого в логе видна только ошибка отката
@SpringBootTest(classes = Application.class, properties = "logging.level.org.testcontainers=DEBUG")
@EnableTestcontainersPg
class TestcontainersPgTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSource() throws Exception {
        try (ResultSet rs = dataSource.getConnection().createStatement().executeQuery("SELECT datname FROM pg_database where datname like 'db_%'")) {
            Assertions.assertTrue(rs.next());
        }
    }
}
