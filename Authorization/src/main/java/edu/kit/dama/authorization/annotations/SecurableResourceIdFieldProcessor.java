/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.authorization.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.tools.Diagnostic;

/**
 * Very simple annotation processor for checking classes containing fields
 * annotated by <i>@SecurableResourceIdField</i>. The processor checks for all
 * occurences of the annotation if:
 *
 * <ul>
 *
 * <li>The annotation is applied to a field -&gt; ERROR if not</li>
 *
 * <li>The field is of type java.lang.String -&gt; ERROR if not</li>
 *
 * <li>The annotation occurs once per class -&gt; WARNING if not</li>
 *
 * <li>The field is also annotated by
 *
 * &#64;Column(nullable=false, unique=true) -&gt; WARNING if not</li>
 *
 * </ul>
 *
 * The last point is only relevant/recommended when using JPA (if the processed
 * class is annotated by <i>@Entity</i>).
 *
 * (see https://github.com/pellaton/spring-configuration-validation-processor)
 * @author jejkal
 */
@SupportedAnnotationTypes(value = "edu.dama.kit.authorization.annotations.SecurableResourceIdField")
public class SecurableResourceIdFieldProcessor extends AbstractProcessor {

  /**
   * The messager.
   */
  private Messager messager;

  /**
   * The default constructor.
   */
  public SecurableResourceIdFieldProcessor() {
    super();
  }

  @Override
  public final synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
  }

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement te : annotations) {
      Map<Element, Integer> occurences = new HashMap<>();
      for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
        if (occurences.get(e.getEnclosingElement()) == null) {
          occurences.put(e.getEnclosingElement(), 1);
        } else {
          printWarningMessage("@SecurableResourceIdField must occur only once per class. Subsequent occurences will be ignored during runtime.", e.getEnclosingElement());
        }

        //field type MUST be string to be able to bring this field in relation to the domainUniqueField of the FilterHelper model
        if (!e.asType().toString().equals("java.lang.String")) {
          printErrorMessage("Only fields of type java.lang.String can be annotated by @SecurableResourceIdField", e);
        }

        //only check if we have a JPA entity
        if (e.getEnclosingElement().getAnnotation(Entity.class) != null) {
          //check @Column annotation,  give advise if needed but produce no error
          Column columnAnnotation = e.getAnnotation(Column.class);
          if (columnAnnotation == null || columnAnnotation.nullable() || !columnAnnotation.unique()) {
            printWarningMessage("While using JPA, it is highly recommended to annotate fields annotated by @SecurableResourceIdField also by @Column(nullable=false, unique=true)", e);
          }
        }
      }
    }
    return false;
  }

  /**
   * Print an error. Errors will stop the annotation processing and the compile
   * process.
   *
   * @param pMessage The error message.
   * @param pElement The element producing the error.
   */
  private void printErrorMessage(String pMessage, Element pElement) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, pMessage, pElement);
  }

  /**
   * Print a warning. Warnings will not stop the annotation processing.
   *
   * @param pMessage The message.
   * @param pElement The element producing the warning.
   */
  private void printWarningMessage(String pMessage, Element pElement) {
    this.messager.printMessage(Diagnostic.Kind.WARNING, pMessage, pElement);
  }
}
