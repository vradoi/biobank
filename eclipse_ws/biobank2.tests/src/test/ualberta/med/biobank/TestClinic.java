package test.ualberta.med.biobank;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;

//FIXME to be implemented by Delphine
public class TestClinic extends TestDatabase {

    private SiteWrapper site;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<SiteWrapper> sites = SiteWrapper.getAllSites(appService);
        if (sites.size() > 0) {
            site = sites.get(0);
        } else {
            site = new SiteWrapper(appService);
            site.setName("Site - Clinic Test");
            site.setStreet1("street");
            site.persist();
        }
    }

    @Test
    public void testGettersAndSetters() throws BiobankCheckException, Exception {
        ClinicWrapper clinic = new ClinicWrapper(appService);
        clinic.setSite(site);
        clinic.setStreet1("street1");
        clinic.persist();
        testGettersAndSetters(clinic);
    }

    @Test
    public void testSetAddress() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetName() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetName() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetActivityStatus() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetActivityStatus() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetComment() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetComment() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSite() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetSite() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStreet1() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetStreet1() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStreet2() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetStreet2() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetCity() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetCity() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetProvince() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetProvince() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPostalCode() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetPostalCode() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckClinicNameUnique() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetWrappedClass() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetContactCollectionBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetContactCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetContactCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStudyCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPatientVisitCollection() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetPatientVisitCollectionCollectionOfPatientVisitBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetPatientVisitCollectionCollectionOfPatientVisitWrapper() {
        fail("Not yet implemented");
    }

    @Test
    public void testCompareTo() {
        fail("Not yet implemented");
    }

    @Test
    public void testReload() {
        fail("Not yet implemented");
    }

    @Test
    public void testDelete() {
        fail("Not yet implemented");
    }

    @Test
    public void testReset() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckIntegrity() {
        fail("Not yet implemented");
    }

    @Test
    public void testEqualsObject() {
        fail("Not yet implemented");
    }

}
