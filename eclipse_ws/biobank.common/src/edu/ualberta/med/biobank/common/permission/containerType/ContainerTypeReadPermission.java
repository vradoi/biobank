package edu.ualberta.med.biobank.common.permission.containerType;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.User;

public class ContainerTypeReadPermission implements Permission {
    private static final long serialVersionUID = 1L;

    private final Integer typeId;

    public ContainerTypeReadPermission(Integer typeId) {
        this.typeId = typeId;
    }

    public ContainerTypeReadPermission(ContainerType type) {
        this(type.getId());
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        ContainerType cType =
            new ActionContext(user, session).load(ContainerType.class, typeId);
        return PermissionEnum.CONTAINER_TYPE_READ.isAllowed(user,
            cType.getSite());
    }
}