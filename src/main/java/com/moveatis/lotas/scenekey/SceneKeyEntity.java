package com.moveatis.lotas.scenekey;

import com.moveatis.lotas.category.CategoryGroupEntity;
import com.moveatis.lotas.scene.SceneEntity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Entity
public class SceneKeyEntity implements Serializable {

    @OneToOne(mappedBy = "sceneKey")
    private CategoryGroupEntity categoryGroup;

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @OneToOne
    private SceneEntity sceneEntity;
    
    @Column(unique=true)
    private String key;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SceneKeyEntity)) {
            return false;
        }
        SceneKeyEntity other = (SceneKeyEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.moveatis.lotas.scenekey.SceneKeyEntity[ id=" + id + " ]";
    }
    
}