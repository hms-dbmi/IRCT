/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Yu
 *
 */
public class IRCTBaseTest {
	
	private static final EntityManagerFactory emf ;
    private static final ThreadLocal<EntityManager> threadLocal;
    EntityManager em;
    
    static {
        emf = Persistence.createEntityManagerFactory("primary");
        threadLocal = new ThreadLocal<EntityManager>();
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            // set your flush mode here
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }
    
    

	
}
