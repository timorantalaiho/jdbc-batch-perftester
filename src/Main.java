import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        final int howManyInsertsToDo = 1000 * 1000;

        if (args.length < 1) {
            System.err.println("Please provide PostgreSQL JDBC URL as command line parameter.");
            System.err.println("E.g. jdbc:postgresql://localhost/test?user=fred&password=secret");
            System.exit(1);
        }
        String dbUrl = args[0];
        System.out.println("Inserting " + howManyInsertsToDo +
            " rows to test table and then dropping it...");
        System.out.println("Inserted " + howManyInsertsToDo
            + " rows, took " + new Tester(dbUrl).testPerformance(howManyInsertsToDo) + " ms");
    }

    private static class Tester {
        private final String url;
        private Optional<Connection> connection = Optional.empty();
        private Optional<PreparedStatement> statement = Optional.empty();

        public Tester(String url) {
            this.url = url;
        }

        public long testPerformance(int howManyInsertsToDo) {
            long elapsedMillis;
            try {
                connection = Optional.of(DriverManager.getConnection(url));
                connection.get().setAutoCommit(false);
                runSql("drop table if exists test");
                runSql("create table test (x integer primary key)");
                long start = System.currentTimeMillis();
                runBatchInserts(howManyInsertsToDo);
                elapsedMillis = System.currentTimeMillis() - start;
                runSql("drop table test");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                statement.ifPresent(Tester::close);
                connection.ifPresent(Tester::close);
            }
            return elapsedMillis;
        }

        private void runBatchInserts(int howManyInsertsToDo) {
            try {
                Connection c = this.connection.get();
                statement = Optional.of(c.prepareStatement("insert into test values (?)"));
                for (int i = 0; i < howManyInsertsToDo; i++) {
                    statement.get().setLong(1, i);
                    statement.get().addBatch();
                }
                statement.get().executeBatch();
                connection.get().commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void runSql(String sql) {
            try {
                connection.get().prepareStatement(sql);
                statement = Optional.of(connection.get().prepareStatement(sql));
                statement.get().execute();
                connection.get().commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private static void close(AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
