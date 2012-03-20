package edu.ualberta.med.biobank.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.proxy.HibernateProxyHelper;

@MappedSuperclass
public abstract class AbstractBiobankModel implements IBiobankModel {
    private static final long serialVersionUID = 1L;

    private Integer version;
    private Integer id;

    @Override
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "ID", nullable = false)
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    public Integer getVersion() {
        return this.version;
    }

    /**
     * DO NOT CALL this method unless, maybe, for tests. Hibernate manages
     * setting this value.
     * 
     * @param version
     */
    @Deprecated
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (that == null) return false;
        // FIXME: Necessary for inheritance with proxies, works for now
        // Need to come up with a better solution that does not fail when
        // subclasses have their own tables
        if (proxiedClass(that).isAssignableFrom(proxiedClass(this))
            || proxiedClass(this).isAssignableFrom(proxiedClass(that)))
            if (that instanceof IBiobankModel) {
                Integer thatId = ((IBiobankModel) that).getId();
                if (getId() != null && getId().equals(thatId)) return true;
            }
        return false;
    }

    @Override
    public int hashCode() {
        if (getId() == null) return 0;
        return getId().hashCode();
    }

    @Transient
    public boolean isNew() {
        return getId() == null;
    }

    private static Class<?> proxiedClass(Object o) {
        return HibernateProxyHelper.getClassWithoutInitializingProxy(o);
    }
}
