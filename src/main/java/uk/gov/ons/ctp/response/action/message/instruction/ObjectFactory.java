//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.05.01 at 08:29:04 AM BST
//

package uk.gov.ons.ctp.response.action.message.instruction;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the uk.gov.ons.ctp.response.action.message.instruction package.
 *
 * <p>An ObjectFactory allows you to programatically construct new instances of the Java
 * representation for XML content. The Java representation of XML content can consist of schema
 * derived interfaces and classes representing the binding of schema type definitions, element
 * declarations and model groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes
   * for package: uk.gov.ons.ctp.response.action.message.instruction
   */
  public ObjectFactory() {}

  /** Create an instance of {@link ActionInstruction } */
  public ActionInstruction createActionInstruction() {
    return new ActionInstruction();
  }

  /** Create an instance of {@link ActionCancel } */
  public ActionCancel createActionCancel() {
    return new ActionCancel();
  }

  /** Create an instance of {@link ActionUpdate } */
  public ActionUpdate createActionUpdate() {
    return new ActionUpdate();
  }

  /** Create an instance of {@link ActionRequest } */
  public ActionRequest createActionRequest() {
    return new ActionRequest();
  }

  /** Create an instance of {@link ActionContact } */
  public ActionContact createActionContact() {
    return new ActionContact();
  }

  /** Create an instance of {@link ActionAddress } */
  public ActionAddress createActionAddress() {
    return new ActionAddress();
  }

  /** Create an instance of {@link ActionEvent } */
  public ActionEvent createActionEvent() {
    return new ActionEvent();
  }

  /** Create an instance of {@link Action } */
  public Action createAction() {
    return new Action();
  }
}
