/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for patient_data_responseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="patient_data_responseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}responseType">
 *       &lt;sequence>
 *         &lt;element name="page" type="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}pageType"/>
 *         &lt;element ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}patient_data"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "patient_data_responseType", propOrder = {
    "page",
    "patientData"
})
public class PatientDataResponseType
    extends ResponseType
{

    @XmlElement(required = true)
    protected PageType page;
    @XmlElement(name = "patient_data", namespace = "http://www.i2b2.org/xsd/hive/pdo/1.1/", required = true)
    protected PatientDataType patientData;

    /**
     * Gets the value of the page property.
     * 
     * @return
     *     possible object is
     *     {@link PageType }
     *     
     */
    public PageType getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     * @param value
     *     allowed object is
     *     {@link PageType }
     *     
     */
    public void setPage(PageType value) {
        this.page = value;
    }

    /**
     * Gets the value of the patientData property.
     * 
     * @return
     *     possible object is
     *     {@link PatientDataType }
     *     
     */
    public PatientDataType getPatientData() {
        return patientData;
    }

    /**
     * Sets the value of the patientData property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatientDataType }
     *     
     */
    public void setPatientData(PatientDataType value) {
        this.patientData = value;
    }

    public void addPatientData(PatientDataType additionalData){
        if (this.patientData == null){
            this.patientData = additionalData;
        }
        else {
            if (this.patientData.getConceptSet() != null)
                this.patientData.getConceptSet().getConcept().addAll(additionalData.getConceptSet().getConcept());
            else
                this.patientData.setConceptSet(additionalData.getConceptSet());
            if (this.patientData.getPatientSet() != null)
                this.patientData.getPatientSet().getPatient().addAll(additionalData.getPatientSet().getPatient());
            else
                this.patientData.setPatientSet(additionalData.getPatientSet());
            if (this.patientData.getEidSet() != null)
                this.patientData.getEidSet().getEid().addAll(additionalData.getEidSet().getEid());
            else
                this.patientData.setEidSet(additionalData.getEidSet());
            if (this.patientData.getEventSet() != null)
                this.patientData.getEventSet().getEvent().addAll(additionalData.getEventSet().getEvent());
            else
                this.patientData.setEventSet(additionalData.getEventSet());
            if (this.patientData.getModifierSet() != null)
                this.patientData.getModifierSet().getModifier().addAll(additionalData.getModifierSet().getModifier());
            else
                this.patientData.setModifierSet(additionalData.getModifierSet());
            if (this.patientData.getObservationSet() != null)
                this.patientData.combineObservationSet(additionalData.getObservationSet());
            else
                this.patientData.setObservationSet(additionalData.getObservationSet());
            if (this.patientData.getObserverSet() != null)
                this.patientData.getObserverSet().getObserver().addAll(additionalData.getObserverSet().getObserver());
            else
                this.patientData.setObserverSet(additionalData.getObserverSet());
            if (this.patientData.getPidSet() != null)
                this.patientData.getPidSet().getPid().addAll(additionalData.getPidSet().getPid());
            else
                this.patientData.setPidSet(additionalData.getPidSet());
        }
    }

}
