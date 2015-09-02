/*
 * Copyright 2014 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.entities.Role;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author mf6319
 */
@SupportedAnnotationTypes(value = {"edu.kit.dama.authorization.annotations.FilterOutput", "edu.kit.dama.authorization.annotations.SecuredMethod"})
public class FilterOutputValidationProcessor extends AbstractProcessor {

  /**
   * The messager.
   */
  private Messager messager;

  /**
   * The default constructor.
   */
  public FilterOutputValidationProcessor() {
    super();
  }

  @Override
  public final synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Elements elementUtils = processingEnv.getElementUtils();
    final Types typeUtils = processingEnv.getTypeUtils();
    //check all supported annotations (see SupportedAnnotationTypes)
    for (TypeElement annotation : annotations) {
      if (annotation.getQualifiedName().toString().equals(FilterOutput.class.getCanonicalName())) {
        final Set<? extends Element> filteredMethods = roundEnv.getElementsAnnotatedWith(annotation);
        DeclaredType wildcardMap = typeUtils.getDeclaredType(elementUtils.getTypeElement("java.util.Collection"), typeUtils.getWildcardType(null, null));
        for (Element m : filteredMethods) {
          if (m.getKind() == ElementKind.METHOD) {
            final ExecutableElement method = (ExecutableElement) m;
            boolean isCollectionDerived = typeUtils.isAssignable(method.getReturnType(), wildcardMap);
            if (!isCollectionDerived) {
              printErrorMessage("The return type methods annotated by @FilterOutput must be assignable to java.util.Collection", m);
            }
            FilterOutput foutAnnotation = m.getAnnotation(FilterOutput.class);
            if (Role.NO_ACCESS.equals(foutAnnotation.roleRequired())) {
              printWarningMessage("@FilterOutput is set to NO_ACCESS. Probably no 'roleRequired' attribute was provided.", m);
            }
          }
        }
      } else if (annotation.getQualifiedName().toString().equals(SecuredMethod.class.getCanonicalName())) {
        final Set<? extends Element> securedMethods = roundEnv.getElementsAnnotatedWith(annotation);
        for (Element m : securedMethods) {
          if (m.getKind() == ElementKind.METHOD) {
            SecuredMethod securedMethodAnnotation = m.getAnnotation(SecuredMethod.class);
            if (Role.NO_ACCESS.equals(securedMethodAnnotation.roleRequired())) {
              printWarningMessage("@SecuredMethod is set to NO_ACCESS. Probably no 'roleRequired' attribute was provided.", m);
            }
          }
        }
      }
    }
    /*
     final TypeElement filterAnnotations = elementUtils.getTypeElement("edu.kit.dama.authorization.annotations.FilterOutput");
     final Set<? extends Element> filteredMethods = roundEnv.getElementsAnnotatedWith(filterAnnotations);
     DeclaredType wildcardMap = typeUtils.getDeclaredType(elementUtils.getTypeElement("java.util.Collection"), typeUtils.getWildcardType(null, null));

     for (Element m : filteredMethods) {
     if (m.getKind() == ElementKind.METHOD) {
     final ExecutableElement method = (ExecutableElement) m;
     boolean isCollectionDerived = typeUtils.isAssignable(method.getReturnType(), wildcardMap);
     if (!isCollectionDerived) {
     printErrorMessage("The return type methods annotated by @FilterOutput must be assignable to java.util.Collection", m);
     }
     FilterOutput foutAnnotation = m.getAnnotation(FilterOutput.class);
     if (Role.NO_ACCESS.equals(foutAnnotation.roleRequired())) {
     printWarningMessage("@FilterOutput is set to NO_ACCESS. Probably no 'roleRequired' attribute was provided.", m);
     }
     }
     }

     final TypeElement securedMethodAnnotations = elementUtils.getTypeElement("edu.kit.dama.authorization.annotations.SecuredMethod");
     final Set<? extends Element> securedMethods = roundEnv.getElementsAnnotatedWith(securedMethodAnnotations);
     for (Element m : securedMethods) {
     if (m.getKind() == ElementKind.METHOD) {
     SecuredMethod securedMethodAnnotation = m.getAnnotation(SecuredMethod.class);
     if (Role.NO_ACCESS.equals(securedMethodAnnotation.roleRequired())) {
     printWarningMessage("@SecuredMethod is set to NO_ACCESS. Probably no 'roleRequired' attribute was provided.", m);
     }
     }
     }
     */
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
