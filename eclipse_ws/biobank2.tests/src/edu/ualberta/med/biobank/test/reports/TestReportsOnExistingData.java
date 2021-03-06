package edu.ualberta.med.biobank.test.reports;

import edu.ualberta.med.biobank.test.AllTestsSuite;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SpecimenRequestTest.class, ContainerCapacityTest.class,
    ContainerEmptyLocationsTest.class, FTAReportTest.class,
    FvLPatientVisitsTest.class, PatientVisitSummaryTest.class,
    QaSpecimensTest.class, SpecimenTypePvCountTest.class,
    SpecimenTypeSUsageTest.class })
public final class TestReportsOnExistingData {
    @BeforeClass
    public static void setUp() throws Exception {
        WritableApplicationService appService = AllTestsSuite.appService;
        Assert.assertNotNull("setUp: appService is null", appService);

        ReportDataSource dataSource = new CachedReportDataSource(appService);

        AbstractReportTest.setReportDataSource(dataSource);
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }
}
