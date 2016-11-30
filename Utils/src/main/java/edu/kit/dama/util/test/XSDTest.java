/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.util.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import javax.xml.bind.Marshaller;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 *
 * @author jejkal
 */
public class XSDTest {

    public static void main(String[] args) throws Exception {
        String xsd = "http://datamanager.kit.edu/dama/basemetadata/2015-08/basemetadata.xsd";
        String destination = "generated";
        String outPackage = "edu.kit.dama.model.bmd";
        new File(destination).mkdirs();
        CommandLine cmdLine = CommandLine.parse("xjc -d " + destination + " -p " + outPackage + " " + xsd);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);

        executor.setStreamHandler(new PumpStreamHandler(System.out, System.out));

        int exitCode = executor.execute(cmdLine);
        System.out.println("EX " + exitCode);
        File outPath = new File(destination + "/" + outPackage.replace(".", "/"));
        //cmdLine = CommandLine.parse("/usr/bin/javac *.java");
        StringBuilder files = new StringBuilder();
        for (File f : outPath.listFiles()) {
            if (f.getName().endsWith("java")) {
                files.append(" ").append(f.getAbsolutePath());
            }
        }

        cmdLine = CommandLine.parse("/usr/bin/javac" + files.toString());
        executor = new DefaultExecutor();
        executor.setExitValue(0);

        executor.setStreamHandler(new PumpStreamHandler(System.out, System.out));
        exitCode = executor.execute(cmdLine);
        System.out.println("EX " + exitCode);
        URLClassLoader loader = new URLClassLoader(new URL[]{new File("./" + destination).toURI().toURL()}, ClassLoader.getSystemClassLoader());

        ///// ---> This only works for some cases....depending on the schema it is not possible to create a POJO automatically, e.g. DublinCore is not possible
        Class c = loader.loadClass(outPackage + ".Basemetadata");
        Object o = new PodamFactoryImpl(new CustomPodamProviderStrategy()).manufacturePojo(c);
        Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(c).createMarshaller();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        marshaller.marshal(o, bout);

        System.out.println(bout.toString());

    }

}
