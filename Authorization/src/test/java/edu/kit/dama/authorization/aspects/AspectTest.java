/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.authorization.aspects;

/**
 *
 * @author mf6319
 */
public class AspectTest {

//    UserId member;
//    UserId admin;
//    GroupId guestGroup;
//    GroupId adminGroup;
//    Role guestRole;
//    Role adminRole;
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        TestUtil.setUpClass();
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @Before
//    public void setUp() {
//        TestUtil.setUpMethod();
//
//        member = StringUserId.of("1");
//        admin = StringUserId.of("0");
//        guestGroup = StringGroupId.of("2");
//        adminGroup = StringGroupId.of("1");
//        guestRole = GUEST;
//        adminRole = ADMINISTRATOR;
//    }
//
//    @After
//    public void tearDown() {
//        TestUtil.tearDownMethod();
//    }
//
//    @Test
//    public void testSufficientRole() {
//        System.out.println("testSufficientRole");
//
//        AuthorizationContext authCtx = new AuthorizationContext(admin, adminGroup, adminRole);
//        secureMethod("", authCtx);
//    }
//
//    @Test(expected = UnauthorizedAccessAttemptException.class)
//    public void testUnsufficientRole() {
//        System.out.println("testUnsufficientRole");
//
//        AuthorizationContext authCtx = new AuthorizationContext(member, guestGroup, guestRole);
//        secureMethod("", authCtx);
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testSecureMethodWithoutContext() {
//        System.out.println("testSecureMethodWithoutContext");
//
//        secureMethodWithoutContext("");
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testSecureMethodWithoutContextAnnotation() {
//        System.out.println("testSecureMethodWithoutContextAnnotation");
//
//        AuthorizationContext authCtx = new AuthorizationContext(admin, adminGroup, adminRole);
//        secureMethodWithoutContextAnnotation("", authCtx);
//    }
//    
//    @Test
//    public void testSecuredMethod() {
//        System.out.println("testSecureMethodWithoutContextAnnotation");
//
//        AuthorizationContext authCtx = new AuthorizationContext(member, guestGroup, adminRole);
//        ISecurableResource sr = new ISecurableResource() {
//            
//            SecurableResourceIdImpl srid;
//
//            @Override
//            public ISecurableResourceId getSecurableResourceId() {
//                return srid;
//            }
//        };
//        SecurableResourceIdImpl srid = new SecurableResourceIdImpl();
//        srid.setDomain("");
//        srid.setDomainUniqueId("10");        
//        securedMethod(srid, authCtx);
//    }
//
//    /* This would be nice, but doesn't work, even with annotating the annotation
//     * definitions with @Inherited because:
//     * 
//     * Note that this meta-annotation type has no effect if the annotated type
//     * is used to annotate anything other than a class.
//     * Note also that this meta-annotation only causes annotations to be inherited
//     * from superclasses; annotations on implemented interfaces have no effect.
//     */
////    @Test
////    public void testSecureMethodInSubclassWithUnsufficientRole() {
////        AuthorizationContext authCtx = new AuthorizationContext(member, guestGroup, guestRole);
////        SubFoo subFoo = new SubFoo();
////
////        subFoo.secureMethod(authCtx);
////    }
//
//    @Access(minimalRole = MANAGER)
//    public void secureMethod(String foo, @Context AuthorizationContext authCtx) {
//    }
//
//    @Access(minimalRole = MANAGER)
//    public void secureMethodWithoutContext(String foo) {
//    }
//
//    @Access(minimalRole = MANAGER)
//    public void secureMethodWithoutContextAnnotation(String foo, AuthorizationContext authCtx) {
//    }
//    
//    @SecuredMethod(roleRequired = MANAGER)
//    public void securedMethod(@SecuredArgument SecurableResourceIdImpl a, @Context AuthorizationContext authCtx) throws UnauthorizedAccessAttemptException{
//    }
//
//    abstract class Foo {
//
//        @Access(minimalRole = MANAGER)
//        abstract public void secureMethod(@Context AuthorizationContext authCtx);
//    }
//
//    class SubFoo extends Foo {
//
//        @Override
//        public void secureMethod(AuthorizationContext authCtx) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
}
