package net.n2oapp.platform.test.autoconfigure;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@EnableTestcontainersPg
public class TestcontainersPgTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDataSource() throws Exception {
        try (ResultSet rs = dataSource.getConnection().createStatement().executeQuery("SELECT datname FROM pg_database where datname like 'db_%'")) {
            Assert.assertTrue(rs.next());
        }
    }
}
