package test.ualberta.med.biobank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import test.ualberta.med.biobank.internal.ClinicHelper;
import test.ualberta.med.biobank.internal.ContactHelper;
import test.ualberta.med.biobank.internal.DbHelper;
import test.ualberta.med.biobank.internal.PatientHelper;
import test.ualberta.med.biobank.internal.PatientVisitHelper;
import test.ualberta.med.biobank.internal.ShipmentHelper;
import test.ualberta.med.biobank.internal.SiteHelper;
import test.ualberta.med.biobank.internal.StudyHelper;
import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Contact;

public class TestClinic extends TestDatabase {

    @Test
    public void testGettersAndSetters() throws BiobankCheckException, Exception {
        String name = "testGettersAndSetters" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);

        testGettersAndSetters(clinic);
    }

    @Test
    public void testGetSetSite() throws Exception {
        String name = "testGetSite" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        SiteWrapper site2 = SiteHelper.addSite(name + "SITE2");

        clinic.setSite(site2);
        clinic.persist();

        clinic.reload();

        Assert.assertFalse(site.equals(clinic.getSite()));

        Assert.assertEquals(site2, clinic.getSite());
    }

    @Test
    public void testGetContactCollection() throws Exception {
        String name = "testGetContactCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        int nber = r.nextInt(5) + 1;
        for (int i = 0; i < nber; i++) {
            ContactHelper.addContact(clinic, name + i);
        }
        clinic.reload();
        List<ContactWrapper> contacts = clinic.getContactCollection();
        int sizeFound = contacts.size();

        Assert.assertEquals(nber, sizeFound);
    }

    @Test
    public void testGetContactCollectionBoolean() throws Exception {
        String name = "testGetContactCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name, true);

        List<ContactWrapper> contacts = clinic.getContactCollection(true);
        if (contacts.size() > 1) {
            for (int i = 0; i < contacts.size() - 1; i++) {
                ContactWrapper contact1 = contacts.get(i);
                ContactWrapper contact2 = contacts.get(i + 1);
                Assert.assertTrue(contact1.compareTo(contact2) <= 0);
            }
        }
    }

    @Test
    public void testSetContactCollectionAdd() throws Exception {
        String name = "testSetContactCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        int nber = r.nextInt(5) + 1;
        for (int i = 0; i < nber; i++) {
            ContactHelper.addContact(clinic, name + i);
        }
        clinic.reload();
        List<ContactWrapper> contacts = clinic.getContactCollection();
        ContactWrapper contact = ContactHelper.newContact(clinic, name + "NEW");
        contacts.add(contact);
        clinic.setContactCollection(contacts);
        clinic.persist();

        clinic.reload();
        // one contact added
        Assert.assertEquals(nber + 1, clinic.getContactCollection().size());
    }

    @Test
    public void testSetContactCollectionRemove() throws Exception {
        String name = "testSetContactCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        int nber = r.nextInt(5) + 1;
        for (int i = 0; i < nber; i++) {
            ContactHelper.addContact(clinic, name + i);
        }
        clinic.reload();
        List<ContactWrapper> contacts = clinic.getContactCollection();
        ContactWrapper contact = DbHelper.chooseRandomlyInList(contacts);
        contacts.remove(contact);
        clinic.setContactCollection(contacts);
        clinic.persist();

        clinic.reload();
        // one contact added
        Assert.assertEquals(nber - 1, clinic.getContactCollection().size());
    }

    @Test
    public void testGetStudyCollection() throws Exception {
        String name = "testGetStudyCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name, true);
        StudyWrapper study1 = StudyHelper.addStudy(site, name + "STUDY1");
        List<ContactWrapper> contacts = new ArrayList<ContactWrapper>();
        contacts.add(DbHelper.chooseRandomlyInList(clinic
            .getContactCollection()));
        study1.setContactCollection(contacts);
        study1.persist();

        ClinicWrapper clinic2 = ClinicHelper.addClinic(site, name + "CLINIC2",
            true);
        StudyWrapper study2 = StudyHelper.addStudy(site, name + "STUDY2");
        contacts = new ArrayList<ContactWrapper>();
        contacts.add(DbHelper.chooseRandomlyInList(clinic
            .getContactCollection()));
        contacts.add(DbHelper.chooseRandomlyInList(clinic2
            .getContactCollection()));
        study2.setContactCollection(contacts);
        study2.persist();

        clinic.reload();

        Assert.assertEquals(2, clinic.getStudyCollection(false).size());
        Assert.assertEquals(1, clinic2.getStudyCollection(false).size());
    }

    @Test
    public void testGetStudyCollectionBoolean() throws Exception {
        String name = "testGetStudyCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name, true);
        StudyWrapper study1 = StudyHelper.addStudy(site, name + "STUDY1");
        List<ContactWrapper> contacts = new ArrayList<ContactWrapper>();
        contacts.add(DbHelper.chooseRandomlyInList(clinic
            .getContactCollection()));
        study1.setContactCollection(contacts);
        study1.persist();
        StudyWrapper study2 = StudyHelper.addStudy(site, name + "STUDY2");
        contacts = new ArrayList<ContactWrapper>();
        contacts.add(DbHelper.chooseRandomlyInList(clinic
            .getContactCollection()));
        study2.setContactCollection(contacts);
        study2.persist();

        clinic.reload();

        List<StudyWrapper> studies = clinic.getStudyCollection(true);
        if (studies.size() > 1) {
            for (int i = 0; i < studies.size() - 1; i++) {
                StudyWrapper s1 = studies.get(i);
                StudyWrapper s2 = studies.get(i + 1);
                Assert.assertTrue(s1.compareTo(s2) <= 0);
            }
        }
    }

    @Test
    public void testPersist() throws Exception {
        String name = "testPersist" + r.nextInt();
        int oldTotal = appService.search(Clinic.class, new Clinic()).size();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicHelper.addClinic(site, name);

        int newTotal = appService.search(Clinic.class, new Clinic()).size();
        Assert.assertEquals(oldTotal + 1, newTotal);
    }

    @Test
    public void testPersistFailAddressNotNul() throws Exception {
        String name = "testPersistFailAddressNotNul" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicHelper.addClinic(site, name + "_1");
        int oldTotal = site.getClinicCollection().size();

        ClinicWrapper clinic = new ClinicWrapper(appService);
        clinic.setName(name);
        clinic.setSite(site);
        try {
            clinic.persist();
            Assert.fail("Should not insert the clinic : no address");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        clinic.setCity("Vesoul");
        clinic.persist();
        site.reload();
        int newTotal = site.getClinicCollection().size();
        Assert.assertEquals(oldTotal + 1, newTotal);
    }

    @Test
    public void testPersistFailSiteNotNul() throws Exception {
        String name = "testPersistFailSiteNotNul" + r.nextInt();
        ClinicWrapper clinic = new ClinicWrapper(appService);
        clinic.setName(name);
        clinic.setCity("Rupt");

        try {
            clinic.persist();
            Assert.fail("Should not insert the clinic : no site");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }
        SiteWrapper site = SiteHelper.addSite(name);
        clinic.setSite(site);
        clinic.persist();
    }

    @Test
    public void testPersistFailNameUnique() throws Exception {
        String name = "testPersistFailNameUnique" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicHelper.addClinic(site, name);
        int oldTotal = site.getClinicCollection().size();

        ClinicWrapper clinic = ClinicHelper.newClinic(site, name);
        try {
            clinic.persist();
            Assert
                .fail("Should not insert the clinic : same name already in database for this site");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }
        clinic.setName(name + "_otherName");
        clinic.persist();
        site.reload();
        int newTotal = site.getClinicCollection().size();
        Assert.assertEquals(oldTotal + 1, newTotal);

        SiteWrapper site2 = SiteHelper.addSite(name + "SITE2");
        ClinicHelper.addClinic(site2, name + "_site2");
        int oldTotalSite2 = site2.getClinicCollection().size();
        // can insert same name in different site
        clinic = ClinicHelper.newClinic(site2, name);
        clinic.persist();
        site.reload();
        site2.reload();
        // only one clinic added
        Assert.assertEquals(oldTotal + 1, site.getClinicCollection().size());
        Assert.assertEquals(oldTotalSite2 + 1, site2.getClinicCollection()
            .size());
    }

    @Test
    public void testDelete() throws Exception {
        String name = "testDelete" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);

        // object is in database
        Clinic clinicInDB = ModelUtils.getObjectWithId(appService,
            Clinic.class, clinic.getId());
        Assert.assertNotNull(clinicInDB);

        clinic.delete();

        clinicInDB = ModelUtils.getObjectWithId(appService, Clinic.class,
            clinic.getId());
        // object is not anymore in database
        Assert.assertNull(clinicInDB);
    }

    @Test
    public void testDeleteWithContacts() throws Exception {
        String name = "testDeleteWithContacts" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        int contactId = ContactHelper.addContact(clinic, name).getId();
        Contact contactInDB = ModelUtils.getObjectWithId(appService,
            Contact.class, contactId);
        Assert.assertNotNull(contactInDB);
        clinic.reload();

        clinic.delete();

        contactInDB = ModelUtils.getObjectWithId(appService, Contact.class,
            contactId);
        Assert.assertNull(contactInDB);
    }

    @Test
    public void testDeleteWithContactsLinkedToStudy() throws Exception {
        String name = "testDeleteWithContacts" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(site, name);
        List<ContactWrapper> studyContacts = new ArrayList<ContactWrapper>();
        studyContacts.add(contact);
        study.setContactCollection(studyContacts);
        study.persist();

        clinic.reload();
        contact.reload();

        try {
            clinic.delete();
            Assert
                .fail("Can't remove a clinic if a study linked to one of its contacts still exists");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDeleteWithShipments() throws Exception {
        String name = "testDeleteWithShipments" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactHelper.addContact(clinic, name);

        ShipmentHelper.addShipmentWithRandomPatient(clinic, name);

        clinic.reload();
        try {
            clinic.delete();
            Assert.fail("Can't remove a clinic if shipments linked to it");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testResetAlreadyInDatabase() throws Exception {
        String name = "testResetAlreadyInDatabase" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        clinic.reload();
        String oldName = clinic.getName();
        clinic.setName("toto");
        clinic.reset();
        Assert.assertEquals(oldName, clinic.getName());
    }

    @Test
    public void testResetNew() throws Exception {
        String name = "testResetAlreadyInDatabase" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.newClinic(site, name);
        clinic.reset();
        Assert.assertEquals(null, clinic.getName());
    }

    @Test
    public void testGetShipmentCollection() throws Exception {
        String name = "testGetShipmentCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(clinic.getSite(), name);
        study.setContactCollection(Arrays.asList(contact));
        study.persist();
        PatientWrapper patient1 = PatientHelper.addPatient(name, study);

        StudyWrapper study2 = StudyHelper.addStudy(clinic.getSite(), name
            + "_2");
        study2.setContactCollection(Arrays.asList(contact));
        study2.persist();
        PatientWrapper patient2 = PatientHelper.addPatient(name + "_2", study2);

        ShipmentHelper.addShipment(clinic, patient1);
        ShipmentHelper.addShipment(clinic, patient2);

        clinic.reload();
        List<ShipmentWrapper> ships = clinic.getShipmentCollection();
        int sizeFound = ships.size();

        Assert.assertEquals(2, sizeFound);
    }

    @Test
    public void testSetShipmentCollectionAdd() throws Exception {
        String name = "testSetShipmentCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(clinic.getSite(), name);
        study.setContactCollection(Arrays.asList(contact));
        study.persist();
        PatientWrapper patient1 = PatientHelper.addPatient(name, study);

        StudyWrapper study2 = StudyHelper.addStudy(clinic.getSite(), name
            + "_2");
        study2.setContactCollection(Arrays.asList(contact));
        study2.persist();
        PatientWrapper patient2 = PatientHelper.addPatient(name + "_2", study2);

        ShipmentHelper.addShipment(clinic, patient1);
        ShipmentHelper.addShipment(clinic, patient2);

        clinic.reload();

        ShipmentWrapper shipment = ShipmentHelper.newShipment(clinic);
        clinic.setShipmentCollection(Arrays.asList(shipment));
        clinic.persist();

        clinic.reload();
        Assert.assertEquals(3, clinic.getShipmentCollection().size());
    }

    @Test
    public void testSetShipmentCollectionRemove() throws Exception {
        String name = "testSetShipmentCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(clinic.getSite(), name);
        study.setContactCollection(Arrays.asList(contact));
        study.persist();
        PatientWrapper patient1 = PatientHelper.addPatient(name, study);

        StudyWrapper study2 = StudyHelper.addStudy(clinic.getSite(), name
            + "_2");
        study2.setContactCollection(Arrays.asList(contact));
        study2.persist();
        PatientWrapper patient2 = PatientHelper.addPatient(name + "_2", study2);

        ShipmentHelper.addShipment(clinic, patient1);
        ShipmentWrapper shipment = ShipmentHelper.addShipment(clinic, patient2);

        clinic.reload();
        List<ShipmentWrapper> shipments = clinic.getShipmentCollection();
        shipments.remove(shipment);
        // need to delete the shipment to do that
        shipment.delete();
        clinic.setShipmentCollection(shipments);
        clinic.persist();

        clinic.reload();
        Assert.assertEquals(1, clinic.getShipmentCollection().size());
    }

    @Test
    public void testHasShipments() throws Exception {
        String name = "testHasShipments" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(clinic.getSite(), name);
        study.setContactCollection(Arrays.asList(contact));
        study.persist();
        PatientWrapper patient1 = PatientHelper.addPatient(name, study);

        StudyWrapper study2 = StudyHelper.addStudy(clinic.getSite(), name
            + "_2");
        study2.setContactCollection(Arrays.asList(contact));
        study2.persist();
        PatientWrapper patient2 = PatientHelper.addPatient(name + "_2", study2);

        Assert.assertFalse(clinic.hasShipments());

        ShipmentWrapper shipment1 = ShipmentHelper
            .addShipment(clinic, patient1);
        ShipmentWrapper shipment2 = ShipmentHelper
            .addShipment(clinic, patient2);

        Assert.assertTrue(clinic.hasShipments());

        clinic.reload();
        shipment1.delete();
        shipment2.delete();
        clinic.reload();

        Assert.assertFalse(clinic.hasShipments());
    }

    @Test
    public void testGetPatientVisitCollection() throws Exception {
        String name = "testGetPatientVisitCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        ContactWrapper contact = ContactHelper.addContact(clinic, name);

        StudyWrapper study = StudyHelper.addStudy(clinic.getSite(), name);
        study.setContactCollection(Arrays.asList(contact));
        study.persist();
        PatientWrapper patient1 = PatientHelper.addPatient(name, study);
        PatientWrapper patient2 = PatientHelper.addPatient(name + "_2", study);
        ShipmentWrapper shipment1 = ShipmentHelper.addShipment(clinic,
            patient1, patient2);
        PatientVisitHelper.addPatientVisit(patient1, shipment1, Utils
            .getRandomDate());
        PatientVisitHelper.addPatientVisit(patient2, shipment1, Utils
            .getRandomDate());

        StudyWrapper study2 = StudyHelper.addStudy(clinic.getSite(), name
            + "_2");
        study2.setContactCollection(Arrays.asList(contact));
        study2.persist();
        PatientWrapper patient3 = PatientHelper.addPatient(name + "_3", study2);
        ShipmentWrapper shipment2 = ShipmentHelper
            .addShipment(clinic, patient3);
        PatientVisitHelper.addPatientVisit(patient3, shipment2, Utils
            .getRandomDate());

        clinic.reload();
        Assert.assertEquals(3, clinic.getPatientVisitCollection().size());
    }

    @Test
    public void testCompareTo() throws Exception {
        String name = "testCompareTo" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ClinicWrapper clinic1 = ClinicHelper.addClinic(site, "QWERTY" + name);
        ClinicWrapper clinic2 = ClinicHelper.addClinic(site, "ASDFG" + name);

        Assert.assertTrue(clinic1.compareTo(clinic2) > 0);
        Assert.assertTrue(clinic2.compareTo(clinic1) < 0);
    }

}