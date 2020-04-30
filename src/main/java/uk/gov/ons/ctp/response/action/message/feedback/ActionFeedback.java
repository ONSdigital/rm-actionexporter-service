//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.04.30 at 03:22:37 PM BST
//

package uk.gov.ons.ctp.response.action.message.feedback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for ActionFeedback complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActionFeedback">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="actionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="situation">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="outcome" type="{http://ons.gov.uk/ctp/response/action/message/feedback}Outcome"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "ActionFeedback",
    propOrder = {"actionId", "situation", "outcome"})
public class ActionFeedback {

  @XmlElement(required = true)
  protected String actionId;

  @XmlElement(required = true)
  protected String situation;

  @XmlElement(required = true)
  @XmlSchemaType(name = "string")
  protected Outcome outcome;

  /** Default no-arg constructor */
  public ActionFeedback() {
    super();
  }

  /** Fully-initialising value constructor */
  public ActionFeedback(final String actionId, final String situation, final Outcome outcome) {
    this.actionId = actionId;
    this.situation = situation;
    this.outcome = outcome;
  }

  /**
   * Gets the value of the actionId property.
   *
   * @return possible object is {@link String }
   */
  public String getActionId() {
    return actionId;
  }

  /**
   * Sets the value of the actionId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setActionId(String value) {
    this.actionId = value;
  }

  /**
   * Gets the value of the situation property.
   *
   * @return possible object is {@link String }
   */
  public String getSituation() {
    return situation;
  }

  /**
   * Sets the value of the situation property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSituation(String value) {
    this.situation = value;
  }

  /**
   * Gets the value of the outcome property.
   *
   * @return possible object is {@link Outcome }
   */
  public Outcome getOutcome() {
    return outcome;
  }

  /**
   * Sets the value of the outcome property.
   *
   * @param value allowed object is {@link Outcome }
   */
  public void setOutcome(Outcome value) {
    this.outcome = value;
  }
}
