//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.19 at 11:04:28 AM CEST 
//
package edu.kit.dama.util.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="digitalObject">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="digitalObjectId" type="{http://kitdatamanager.net/dama/basemetadata}id"/>
 *                   &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="label" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *                   &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *                   &lt;element name="uploadDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *                   &lt;element name="uploader" type="{http://kitdatamanager.net/dama/basemetadata}user" minOccurs="0"/>
 *                   &lt;element name="experimenters" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="experimenter" type="{http://kitdatamanager.net/dama/basemetadata}user" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="investigation" type="{http://kitdatamanager.net/dama/basemetadata}investigation"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "digitalObject"
})
@XmlRootElement(name = "basemetadata")
public class BaseMetadata {

    @XmlElement(required = true)
    protected BaseMetadata.DigitalObject digitalObject;

    /**
     * Gets the value of the digitalObject property.
     *
     * @return possible object is {@link BaseMetadata.DigitalObject }
     *
     */
    public BaseMetadata.DigitalObject getDigitalObject() {
        return digitalObject;
    }

    /**
     * Sets the value of the digitalObject property.
     *
     * @param value allowed object is {@link BaseMetadata.DigitalObject }
     *
     */
    public void setDigitalObject(BaseMetadata.DigitalObject value) {
        this.digitalObject = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="digitalObjectId" type="{http://kitdatamanager.net/dama/basemetadata}id"/>
     *         &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="label" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
     *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
     *         &lt;element name="uploadDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
     *         &lt;element name="uploader" type="{http://kitdatamanager.net/dama/basemetadata}user" minOccurs="0"/>
     *         &lt;element name="experimenters" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="experimenter" type="{http://kitdatamanager.net/dama/basemetadata}user" maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="investigation" type="{http://kitdatamanager.net/dama/basemetadata}investigation"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "digitalObjectIdentifier",
        "note",
        "label",
        "startDate",
        "endDate",
        "uploadDate",
        "uploader",
        "experimenters",
        "investigation"
    })
    public static class DigitalObject {

        @XmlElement(required = true)
        protected String digitalObjectIdentifier;
        protected String note;
        protected String label;
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar startDate;
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endDate;
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar uploadDate;
        protected User uploader;
        protected BaseMetadata.DigitalObject.Experimenters experimenters;
        @XmlElement(required = true)
        protected Investigation investigation;

        /**
         * Gets the value of the digitalObjectId property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getDigitalObjectIdentifier() {
            return digitalObjectIdentifier;
        }

        /**
         * Sets the value of the digitalObjectId property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setDigitalObjectIdentifier(String value) {
            this.digitalObjectIdentifier = value;
        }

        /**
         * Gets the value of the note property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getNote() {
            return note;
        }

        /**
         * Sets the value of the note property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setNote(String value) {
            this.note = value;
        }

        /**
         * Gets the value of the label property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the value of the label property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setLabel(String value) {
            this.label = value;
        }

        /**
         * Gets the value of the startDate property.
         *
         * @return possible object is {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getStartDate() {
            return startDate;
        }

        /**
         * Sets the value of the startDate property.
         *
         * @param value allowed object is {@link XMLGregorianCalendar }
         *
         */
        public void setStartDate(XMLGregorianCalendar value) {
            this.startDate = value;
        }

        /**
         * Gets the value of the endDate property.
         *
         * @return possible object is {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getEndDate() {
            return endDate;
        }

        /**
         * Sets the value of the endDate property.
         *
         * @param value allowed object is {@link XMLGregorianCalendar }
         *
         */
        public void setEndDate(XMLGregorianCalendar value) {
            this.endDate = value;
        }

        /**
         * Gets the value of the uploadDate property.
         *
         * @return possible object is {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getUploadDate() {
            return uploadDate;
        }

        /**
         * Sets the value of the uploadDate property.
         *
         * @param value allowed object is {@link XMLGregorianCalendar }
         *
         */
        public void setUploadDate(XMLGregorianCalendar value) {
            this.uploadDate = value;
        }

        /**
         * Gets the value of the uploader property.
         *
         * @return possible object is {@link User }
         *
         */
        public User getUploader() {
            return uploader;
        }

        /**
         * Sets the value of the uploader property.
         *
         * @param value allowed object is {@link User }
         *
         */
        public void setUploader(User value) {
            this.uploader = value;
        }

        /**
         * Gets the value of the experimenters property.
         *
         * @return possible object is
         *     {@link BaseMetadata.DigitalObject.Experimenters }
         *
         */
        public BaseMetadata.DigitalObject.Experimenters getExperimenters() {
            return experimenters;
        }

        /**
         * Sets the value of the experimenters property.
         *
         * @param value allowed object is
         *     {@link BaseMetadata.DigitalObject.Experimenters }
         *
         */
        public void setExperimenters(BaseMetadata.DigitalObject.Experimenters value) {
            this.experimenters = value;
        }

        /**
         * Gets the value of the investigation property.
         *
         * @return possible object is {@link Investigation }
         *
         */
        public Investigation getInvestigation() {
            return investigation;
        }

        /**
         * Sets the value of the investigation property.
         *
         * @param value allowed object is {@link Investigation }
         *
         */
        public void setInvestigation(Investigation value) {
            this.investigation = value;
        }

        /**
         * <p>
         * Java class for anonymous complex type.
         *
         * <p>
         * The following schema fragment specifies the expected content
         * contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="experimenter" type="{http://kitdatamanager.net/dama/basemetadata}user" maxOccurs="unbounded"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "experimenter"
        })
        public static class Experimenters {

            @XmlElement(required = true)
            protected List<User> experimenter;

            /**
             * Gets the value of the experimenter property.
             *
             * <p>
             * This accessor method returns a reference to the live list, not a
             * snapshot. Therefore any modification you make to the returned
             * list will be present inside the JAXB object. This is why there is
             * not a <CODE>set</CODE> method for the experimenter property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getExperimenter().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link User }
             *
             *
             */
            public List<User> getExperimenter() {
                if (experimenter == null) {
                    experimenter = new ArrayList<User>();
                }
                return this.experimenter;
            }

        }

    }

}
