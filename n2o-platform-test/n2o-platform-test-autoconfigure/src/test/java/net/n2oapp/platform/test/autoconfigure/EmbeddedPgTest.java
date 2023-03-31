package net.n2oapp.platform.test.autoconfigure;

import net.n2oapp.platform.test.autoconfigure.pg.EnableEmbeddedPg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@SpringBootTest(classes = Application.class)
@EnableEmbeddedPg
class EmbeddedPgTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSource() throws Exception {
        try(final Connection connection = dataSource.getConnection();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT datname FROM pg_database where datname like 'db_%'")) {
            Assertions.assertTrue(resultSet.next());
        }
    }
}
