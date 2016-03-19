package com.moveatis.lotas.superuser;

import com.moveatis.lotas.interfaces.AbstractBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.moveatis.lotas.interfaces.SuperUsers;
import javax.inject.Named;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Named(value="superUsersEJB")
@Stateless
public class SuperUsersBean extends AbstractBean<SuperUsersEntity> implements SuperUsers {

    @PersistenceContext(unitName = "LOTAS_PERSISTENCE")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public SuperUsersBean() {
        super(SuperUsersEntity.class);
    }
    
}
