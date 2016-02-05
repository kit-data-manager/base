/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.util;


/**
 *
 * @author mf6319
 */
public class ProcessingTest {

//  public static void main(String[] args) throws Exception {
//   // final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//    final ExecutionEnvironment env = ExecutionEnvironment.createRemoteEnvironment("hph-c-053.lsdf.kit.edu", 6165, "D:\\GRID\\src\\Libraries\\KDM\\trunk\\Commons\\target\\Commons-1.1-SNAPSHOT.jar");
//
//    MyEntity e1 = new MyEntity(1l, "Who's there?", -1L);
//
//    MyEntity e2 = new MyEntity(2l, "Here is e2.", 1L);
//    MyEntity e3 = new MyEntity(3l, "Here is e3.", 1L);
//
//    DataSet<MyEntity> vertex1 = env.fromElements(e1, e2);
//    // DataSet<MyEntity> vertex2 = env.fromElements(e1,e3);
//
//    DataSet<Integer> wordCounts = vertex1.map(new LineSplitter());
//   // // DataSet<Integer> wordCounts1 = vertex2.map(new LineSplitter());
//    // vertex1.join(vertex2).where("predecessor").equalTo("id").with(new FlatJoinFunction<MyEntity, MyEntity, Integer>() {
//
//    //  @Override
//    //  public void join(MyEntity in1, MyEntity in2, Collector<Integer> clctr) throws Exception {
//    //    clctr.collect(0);
//    //  }
//    // }).print();
//    //   wordCounts.writeAsText("file:///d:/out1.txt");
//    // wordCounts1.writeAsText("file:///d:/out2.txt");
//    // wordCounts.print();
//    wordCounts.writeAsText("file:///myOut.txt");
//    env.execute("Word Count Example");
//
//  }
//
//  public static class LineSplitter implements MapFunction<MyEntity, Integer> {
//
//    @Override
//    public Integer map(MyEntity t) throws Exception {
//      BufferedReader brStdOut = null;
//      BufferedReader brStdErr = null;
//      int result = -1;
//      try {
//        String line;
//        StringBuilder stdOut = new StringBuilder();
//        StringBuilder stdErr = new StringBuilder();
//        Process p = Runtime.getRuntime().exec("/bin/echo " + t.getContent());
//        brStdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        brStdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//        while ((line = brStdOut.readLine()) != null) {
//          stdOut.append(line).append("\n");
//        }
//        brStdOut.close();
//        while ((line = brStdErr.readLine()) != null) {
//          stdErr.append(line).append("\n");
//        }
//        brStdErr.close();
//        result = p.waitFor();
//        System.out.println(stdOut.toString());
//      } catch (Exception err) {
//        err.printStackTrace();
//      } finally {
//        if (brStdErr != null) {
//          try {
//            brStdErr.close();
//          } catch (IOException ex) {
//          }
//        }
//        if (brStdOut != null) {
//          try {
//            brStdOut.close();
//          } catch (IOException ex) {
//          }
//        }
//      }
//      return result;
//    }
//
//  }

}
