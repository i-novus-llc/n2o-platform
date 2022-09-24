package net.n2oapp.platform.test.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.ResultSet;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@EnableTestcontainersPg
public class TestcontainersPgTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDataSource() throws Exception {
        try (ResultSet rs = dataSource.getConnection().createStatement().executeQuery("SELECT datname FROM pg_database where datname like 'db_%'")) {
            Assertions.assertTrue(rs.next());
        }
    }
}
