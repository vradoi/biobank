
package edu.ualberta.med.biobank.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;

public class SiteStudies {

    private static Map<String, StudyWrapper> studiesMap = null;

    public static void createStudies(SiteWrapper site) throws Exception {
        studiesMap = new HashMap<String, StudyWrapper>();

        StudyWrapper study;

        study = addStudy(site, "Acute Heart Failure-Emergency Management",
            "AHFEM", null);
        study.setStudyPvAttr("Worksheet", "text");

        study = addStudy(site, "Blood Borne Pathogens Surveillance Project",
            "BBPSP", null);
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Consent", "select_multiple", new String [] {
            "Surveillance", "Genetic Predisposition", "Previous Samples",
            "Genetic Mutation" });
        study.setStudyPvAttr("Worksheet", "text");
        study.setContactCollection(Arrays.asList(SiteClinics.getContact("Morna Brown")));
        study.persist();

        study = addStudy(
            site,
            "Centre of Excellence for Gastrointestinal Inflammation and Immunity Research",
            "CEGIIR", null);
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        addStudy(site, "Canadian Health Infant Longitudinal Development Study",
            "CHILD", null);

        study = addStudy(
            site,
            "Exploring the Renoprotective effects of fluid prophylaxis strategies for Contrast Induced Nephropathy (Study)",
            "ERCIN", "Precath visit - only urine is collected");
        study.setStudyPvAttr("Visit Type", "select_single", new String [] {
            "Baseline", "Precath", "6hr Post", "24hr Post", "48-72hr Post" });
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Kidney Disease Cohort Study", "KDCS", null);
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Consent", "select_multiple",
            new String [] { "Genetic" });
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Kingston Merger Study", "KMS", null);
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Man-Chui Poon Study", "MPS", null);
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Novartis Hepatitis C Study", "NHS", null);
        study.setStudyPvAttr("Biopsy Length", "number");
        study.setStudyPvAttr("Visit Type", "select_single", new String [] {
            "D0", "D2", "D4", "Wk2", "Wk4", "M2", "M8", "M12", "M18", "M24" });
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Retroviral Study", "RVS", null);
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Tonelli Chronic Kidney Study", "TCKS", null);
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();

        study = addStudy(site, "Vascular Access Study", "VAS", null);
        study.setStudyPvAttr("PBMC Count", "number");
        study.setStudyPvAttr("Worksheet", "text");
        study.persist();
    }

    private static StudyWrapper addStudy(SiteWrapper site, String name,
        String nameShort, String comment) throws Exception {
        StudyWrapper study = new StudyWrapper(site.getAppService());
        study.setSite(site);
        study.setName(name);
        study.setNameShort(nameShort);
        study.setComment(comment);
        study.persist();
        study.reload();
        studiesMap.put(nameShort, study);
        System.out.println("Added study " + nameShort);
        return study;
    }

    public static StudyWrapper getStudy(String name) throws Exception {
        StudyWrapper study = studiesMap.get(name);
        if (study == null) {
            throw new Exception("study with name \"" + name
                + "\" does not exist");
        }
        return study;
    }

}
