package git.doomshade.datamining.data;

import git.doomshade.datamining.TestEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DatabaseTest {
    public static void main(String[] args) {
        TestEntity test = new TestEntity();
        Configuration c = new Configuration();

        ServiceRegistry reg = new StandardServiceRegistryBuilder()
                .applySettings(c.getProperties())
                .build();
        final SessionFactory sf = c.buildSessionFactory(reg);

        Session session = sf.openSession();

        Transaction tx = session.beginTransaction();

        session.save(test);

        tx.commit();
    }
}
