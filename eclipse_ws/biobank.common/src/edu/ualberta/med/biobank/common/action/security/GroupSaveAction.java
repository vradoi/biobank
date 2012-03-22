package edu.ualberta.med.biobank.common.action.security;

import java.util.Set;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.IdResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.security.UserManagerPermission;
import edu.ualberta.med.biobank.model.Group;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.util.SetDiff;
import edu.ualberta.med.biobank.util.SetDiff.Pair;

public class GroupSaveAction implements Action<IdResult> {
    private static final long serialVersionUID = 1L;
    private static final Permission PERMISSION = new UserManagerPermission();

    private final GroupSaveInput input;

    public GroupSaveAction(GroupSaveInput input) {
        this.input = input;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return PERMISSION.isAllowed(context);
    }

    @Override
    public IdResult run(ActionContext context) throws ActionException {
        User executingUser = context.getUser();
        Group group = context.get(Group.class, input.getGroupId(), new Group());

        checkFullyManageable(group, executingUser);

        group.setName(input.getName());
        group.setDescription(input.getDescription());

        Set<User> users = context.load(User.class, input.getUserIds());
        group.getUsers().clear();
        group.getUsers().addAll(users);

        setMemberships(group);

        checkFullyManageable(group, executingUser);

        context.getSession().saveOrUpdate(group);

        return new IdResult(group.getId());
    }

    private void checkFullyManageable(Group group, User executingUser) {
        if (!group.isFullyManageable(executingUser)) {
            // TODO: better message
            throw new ActionException("group is not manageable");
        }
    }

    private void setMemberships(Group group) {
        SetDiff<Membership> diff = new SetDiff<Membership>(
            group.getMemberships(), input.getMemberships());

        for (Membership m : diff.getAdditions()) {
            group.getMemberships().add(m);
            m.setPrincipal(group);
        }

        for (Membership m : diff.getRemovals()) {
            group.getMemberships().remove(m);
        }

        for (Pair<Membership> pair : diff.getIntersection()) {
            Membership oldM = pair.getOld();
            Membership newM = pair.getNew();

            oldM.getPermissions().clear();
            oldM.getPermissions().addAll(newM.getPermissions());

            oldM.getRoles().clear();
            oldM.getRoles().addAll(newM.getRoles());

            oldM.setRank(newM.getRank());
            oldM.setLevel(newM.getLevel());
            oldM.setCenter(newM.getCenter());
            oldM.setStudy(newM.getStudy());
        }

        for (Membership m : group.getMemberships()) {
            m.reducePermissions();
        }
    }
}
