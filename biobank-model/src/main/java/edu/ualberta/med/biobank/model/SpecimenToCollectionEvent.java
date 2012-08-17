package edu.ualberta.med.biobank.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * Joins {@link Specimen}s to {@link CollectionEvent}s. Intended only for
 * {@link Specimen} s that were <em>directly</em> collected from a
 * {@link Patient} (e.g. blood directly drawn or urine directly collected).
 * However, it is possible that the directly collected {@link Specimen}s were
 * discarded and/or are not tracked in the system, so the
 * {@link #isOriginalSpecimen()} value is used to determine if the
 * {@link #getSpecimen()} truly was directly collected from the {@link Patient}.
 * <p>
 * {@link Specimen}s can have more than one {@link CollectionEvent}
 * <ol>
 * <li>when a {@link Specimen} is associated with {@link CollectionEvent}s from
 * multiple {@link Study}s, or</li>
 * <li>when a {@link Specimen} is associated with multiple
 * {@link CollectionEvent}s from a single {@link Study}, probably because it was
 * not directly collected from a {@link Patient}, or</li>
 * <li>some combination of the above</li>
 * </ol>
 * 
 * @author Jonathan Ferland
 */
@Audited
@Entity
@Table(name = "SPECIMEN_TO_COLLECTION_EVENT")
public class SpecimenToCollectionEvent
    implements Serializable {
    private static final long serialVersionUID = 1L;

    private SpecimenToCollectionEventId id;
    private Specimen specimen;
    private CollectionEvent collectionEvent;
    private Boolean originalSpecimen;

    @EmbeddedId
    public SpecimenToCollectionEventId getId() {
        return id;
    }

    public void setId(SpecimenToCollectionEventId id) {
        this.id = id;
    }

    @MapsId("specimenId")
    @NotNull(message = "{SpecimenToCollectionEvent.specimen.NotNull}")
    @ManyToOne
    @JoinColumn(name = "SPECIMEN_ID", nullable = false)
    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    @MapsId("collectionEventId")
    @NotNull(message = "{SpecimenToCollectionEvent.collectionEvent.NotNull}")
    @ManyToOne
    @JoinColumn(name = "COLLECTION_EVENT_ID", nullable = false)
    public CollectionEvent getCollectionEvent() {
        return collectionEvent;
    }

    public void setVisit(CollectionEvent collectionEvent) {
        this.collectionEvent = collectionEvent;
    }

    /**
     * @return true if the {@link #specimen} was collected <em>directly</em>
     *         from the {@link #collectionEvent}'s
     *         {@link CollectionEvent#getPatient()}, otherwise return false.
     */
    @NotNull(message = "{SpecimenToCollectionEvent.originalSpecimen.NotNull}")
    @Column(name = "IS_ORIGINAL_SPECIMEN", nullable = false)
    public Boolean isOriginalSpecimen() {
        return originalSpecimen;
    }

    public void setOriginalSpecimen(Boolean originalSpecimen) {
        this.originalSpecimen = originalSpecimen;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((specimen == null) ? 0 : specimen.hashCode());
        result = prime * result
            + ((collectionEvent == null) ? 0 : collectionEvent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SpecimenToCollectionEvent other = (SpecimenToCollectionEvent) obj;
        if (specimen == null) {
            if (other.specimen != null) return false;
        } else if (!specimen.equals(other.specimen)) return false;
        if (collectionEvent == null) {
            if (other.collectionEvent != null) return false;
        } else if (!collectionEvent.equals(other.collectionEvent))
            return false;
        return true;
    }

    @Embeddable
    public static class SpecimenToCollectionEventId
        implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer specimenId;
        private Integer collectionEventId;

        public Integer getSpecimenId() {
            return specimenId;
        }

        public void setSpecimenId(Integer specimenId) {
            this.specimenId = specimenId;
        }

        public Integer getCollectionEventId() {
            return collectionEventId;
        }

        public void setCollectionEventId(Integer collectionEventId) {
            this.collectionEventId = collectionEventId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                + ((specimenId == null) ? 0 : specimenId.hashCode());
            result = prime * result
                + ((collectionEventId == null) ? 0 : collectionEventId
                    .hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            SpecimenToCollectionEventId other =
                (SpecimenToCollectionEventId) obj;
            if (specimenId == null) {
                if (other.specimenId != null) return false;
            } else if (!specimenId.equals(other.specimenId)) return false;
            if (collectionEventId == null) {
                if (other.collectionEventId != null) return false;
            } else if (!collectionEventId.equals(other.collectionEventId))
                return false;
            return true;
        }
    }
}
