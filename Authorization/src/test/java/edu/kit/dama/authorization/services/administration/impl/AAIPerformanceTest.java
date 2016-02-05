/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.authorization.services.administration.impl;

/**
 *
 * @author jejkal
 */
public class AAIPerformanceTest {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
//        UserService ser = new UserServiceImpl();
//
//        UserId adminUser = StringUserId.of("0");
//        GroupId adminGroup = StringGroupId.of("0");
//
//        AuthorizationContext ctx = new AuthorizationContext(adminUser, adminGroup, Role.ADMINISTRATOR);
        //long s = System.currentTimeMillis();
      /*  for (int i = 1; i <= 1000; i++) {
        ser.register(new StringUserId(Integer.toString(i)), Role.MEMBER, ctx);
        }
        
        //41s for 1000 users
        //System.out.println("D " + (System.currentTimeMillis() - s));
         */

        /*//long s = System.currentTimeMillis();
        GroupServiceImpl impl = new GroupServiceImpl();
        for (int i = 1; i <= 100; i++) {
        impl.create(StringGroupId.of(Integer.toString(i)), StringUserId.of(Integer.toString(i * 10)), ctx);
        }
        //12s for 100 groups
        //System.out.println("D " + (System.currentTimeMillis() - s));
         */
        /*  GroupServiceImpl impl = new GroupServiceImpl();
        int group = 1;
        //long s = System.currentTimeMillis();
        for (int i = 1; i <= 1000; i++) {
        if (i % 10 == 0) {
        group++;
        } else {
        impl.addUser(StringGroupId.of(Integer.toString(group)), StringUserId.of(Integer.toString(i)), Role.MEMBER, ctx);
        }
        }
        //System.out.println("D " + (System.currentTimeMillis() - s));
        //63s for 900 Memberships to 100 groups
         */
        /*ResourceServiceImpl resSrv = new ResourceServiceImpl();
        long s = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
        resSrv.registerResource(StringResourceId.of(Integer.toString(i)), ctx);
        }
        System.out.println("D " + (System.currentTimeMillis() - s));*/
        //371s for 10000 resources

//        ResourceServiceImpl resSrv = new ResourceServiceImpl();
//        long s = System.currentTimeMillis();
//        for (int i = 0; i < 5000; i++) {
//            resSrv.revokeAllGrants(StringResourceId.of(Integer.toString(i)), ctx);
//        }
//        System.out.println("D " + (System.currentTimeMillis() - s));
        //255 s for 5000 grant changes


        /* ResourceServiceImpl resSrv = new ResourceServiceImpl();
        long s = System.currentTimeMillis();
        int noGrant = 0;
        int otherGrant = 0;
        for (int i = 5000; i < 10000; i++) {
        if (resSrv.getGrants(StringResourceId.of(Integer.toString(i)), ctx).equals(Role.NO_ACCESS)) {
        noGrant++;
        } else {
        otherGrant++;
        }
        }
        System.out.println("NO: " + noGrant);
        System.out.println("OTHER: " + otherGrant);
        System.out.println("D " + (System.currentTimeMillis() - s));
        //251s for 5000 grant queries*/
    }
}
