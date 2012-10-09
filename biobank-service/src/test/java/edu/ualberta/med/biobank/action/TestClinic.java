package edu.ualberta.med.biobank.action;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import edu.ualberta.med.biobank.action.clinic.ClinicDeleteAction;
import edu.ualberta.med.biobank.action.clinic.ClinicGetInfoAction;
import edu.ualberta.med.biobank.action.clinic.ClinicGetInfoAction.ClinicInfo;
import edu.ualberta.med.biobank.action.clinic.ClinicSaveAction;
import edu.ualberta.med.biobank.action.clinic.ClinicSaveAction.ContactSaveInfo;
import edu.ualberta.med.biobank.action.helper.ClinicHelper;
import edu.ualberta.med.biobank.action.helper.CollectionEventHelper;
import edu.ualberta.med.biobank.action.helper.SiteHelper.Provisioning;
import edu.ualberta.med.biobank.model.Address;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.type.ActivityStatus;

public class TestClinic extends ActionTest {

    @Rule
    public TestName testname = new TestName();

    private String name;

    private ClinicSaveAction clinicSaveAction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        name = getMethodNameR();

        clinicSaveAction = ClinicHelper.getSaveAction(name, name,
            ActivityStatus.ACTIVE, getR().nextBoolean());
    }

    @Test
    public void saveNew() throws Exception {
        clinicSaveAction.setName(name);

        Address address = new Address();
        address.setCity(name);
        clinicSaveAction.setAddress(address);
        clinicSaveAction.setContactSaveInfos(null);
        try {
            exec(clinicSaveAction);
            Assert.fail(
                "should not be allowed to add site with null site ids");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }

        // success path
        clinicSaveAction
            .setContactSaveInfos(new HashSet<ContactSaveInfo>());
        exec(clinicSaveAction);
    }

    @Test
    public void checkGetAction() throws Exception {
        Provisioning provisioning = new Provisioning(getExecutor(), name);

        CollectionEventHelper.createCEventWithSourceSpecimens(getExecutor(),
            provisioning.patientIds.get(0), provisioning.clinicId);

        ClinicInfo clinicInfo =
            exec(new ClinicGetInfoAction(provisioning.clinicId));

        Assert.assertEquals(ActivityStatus.ACTIVE,
            clinicInfo.clinic.getActivityStatus());
        Assert.assertEquals(new Long(1), clinicInfo.patientCount);
        Assert.assertEquals(new Long(1), clinicInfo.collectionEventCount);
        Assert.assertEquals(1, clinicInfo.contacts.size());
        Assert.assertEquals(1, clinicInfo.studyInfos.size());
        Assert.assertEquals(name + "_clinic_city", clinicInfo.clinic
            .getAddress()
            .getCity());
    }

    @Test
    public void comments() {
        // save with no comments
        Integer clinicId = exec(clinicSaveAction).getId();
        ClinicInfo clinicInfo =
            exec(new ClinicGetInfoAction(clinicId));
        Assert.assertEquals(0, clinicInfo.clinic.getComments().size());

        clinicInfo = addComment(clinicId);
        Assert.assertEquals(1, clinicInfo.clinic.getComments().size());

        clinicInfo = addComment(clinicId);
        Assert.assertEquals(2, clinicInfo.clinic.getComments().size());

        // TODO: check full name on each comment's user
        // for (Comment comment : clinicInfo.clinic.getCommentCollection()) {
        //
        // }
    }

    private ClinicInfo addComment(Integer clinicId) {
        ClinicSaveAction clinicSaveAction = ClinicHelper.getSaveAction(
            exec(new ClinicGetInfoAction(clinicId)));
        clinicSaveAction.setCommentText(nameGe);
        exec(clinicSaveAction).getId();
        return exec(new ClinicGetInfoAction(clinicId));
    }

    @Test
    public void contacts() throws Exception {
        Set<ContactSaveInfo> contactsAll = new HashSet<ContactSaveInfo>();
        Set<ContactSaveInfo> set1 = new HashSet<ContactSaveInfo>();
        Set<ContactSaveInfo> set2 = new HashSet<ContactSaveInfo>();

        for (int i = 0; i < 10; ++i) {
            ContactSaveInfo contactSaveInfo = new ContactSaveInfo();
            contactSaveInfo.name = name + "_contact" + i;

            contactsAll.add(contactSaveInfo);
            if (i < 5) {
                set1.add(contactSaveInfo);
            } else {
                set2.add(contactSaveInfo);
            }
        }

        clinicSaveAction.setContactSaveInfos(contactsAll);
        Integer clinicId = exec(clinicSaveAction).getId();

        ClinicInfo clinicInfo =
            exec(new ClinicGetInfoAction(clinicId));
        Assert.assertEquals(getContactNamesFromSaveInfo(contactsAll),
            getContactNames(clinicInfo.contacts));

        // remove Set 2 from the clinic, Set 1 should be left
        clinicSaveAction =
            ClinicHelper.getSaveAction(clinicInfo);
        clinicSaveAction.setContactSaveInfos(set1);
        exec(clinicSaveAction);

        clinicInfo = exec(new ClinicGetInfoAction(clinicId));
        Assert.assertEquals(getContactNamesFromSaveInfo(set1),
            getContactNames(clinicInfo.contacts));

        // remove all
        clinicSaveAction =
            ClinicHelper.getSaveAction(clinicInfo);
        clinicSaveAction.setContactSaveInfos(new HashSet<ContactSaveInfo>());
        exec(clinicSaveAction);

        clinicInfo = exec(new ClinicGetInfoAction(clinicId));
        Assert.assertTrue(clinicInfo.contacts.isEmpty());

        // check that this clinic no longer has any contacts
        Query q = session.createQuery("SELECT COUNT(*) FROM "
            + Contact.class.getName()
            + " ct WHERE ct.clinic.id=?");
        q.setParameter(0, clinicId);
        Assert.assertTrue(HibernateUtil.getCountFromQuery(q).equals(0L));
    }

    private Set<String> getContactNamesFromSaveInfo(
        Collection<ContactSaveInfo> contactSaveInfos) {
        Set<String> result = new HashSet<String>();
        for (ContactSaveInfo contactSaveInfo : contactSaveInfos) {
            result.add(contactSaveInfo.name);
        }
        return result;
    }

    private Set<String> getContactNames(Collection<Contact> contacts) {
        Set<String> result = new HashSet<String>();
        for (Contact contact : contacts) {
            result.add(contact.getName());
        }
        return result;
    }

    @Test
    public void delete() {
        // delete a study with no patients and no other associations
        Integer clinicId = exec(clinicSaveAction).getId();
        ClinicInfo clinicInfo =
            exec(new ClinicGetInfoAction(clinicId));
        exec(new ClinicDeleteAction(clinicInfo.clinic));

        // hql query for clinic should return empty
        Query q =
            session.createQuery("SELECT COUNT(*) FROM "
                + Clinic.class.getName() + " WHERE id=?");
        q.setParameter(0, clinicId);
        Long result = HibernateUtil.getCountFromQuery(q);

        Assert.assertTrue(result.equals(0L));
    }
}