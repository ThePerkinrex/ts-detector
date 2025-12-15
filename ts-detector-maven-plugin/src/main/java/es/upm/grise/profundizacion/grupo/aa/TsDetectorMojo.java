package es.upm.grise.profundizacion.grupo.aa;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import es.upm.grise.profundizacion.grupo.aa.mapping.MappingDetector;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "ts-detect", defaultPhase = LifecyclePhase.TEST)
public class TsDetectorMojo extends AbstractMojo {

    @Inject // JSR-330 compatible DI
    private ToolchainManager toolchainManager;

    @Parameter(defaultValue = "${project}")
    private MavenProject projectInformation;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Parameter(name = "ts-detect.java.executable")
    private String javaExecutablePath;

    private String getJavaExecutable() {
        if (javaExecutablePath != null) {
            return javaExecutablePath;
        }
        Toolchain tc = toolchainManager.getToolchainFromBuildContext("jdk", session);

        if (tc != null) {
            getLog().info("Toolchain in ts-detect-maven-plugin: " + tc);
            // when the executable to use is explicitly set by user in mojo's parameter, ignore toolchains.
            javaExecutablePath = tc.findTool("java");
        }
        if (javaExecutablePath == null) {
            getLog().warn("No toolchain found, using javahome");
            String javaHome = System.getProperty("java.home");
            File java = new File(javaHome, "bin/java");
            javaExecutablePath = java.getAbsolutePath();
        }
        return javaExecutablePath;
    }

    private File getSubDir(File parent, String name) throws IOException {
        File child = new File(parent, name);
        if (!child.exists() && !child.mkdirs()) throw new IOException("Couldn't mkdir");
        return child;
    }

    private File getPluginDir() throws IOException {
        return getSubDir(new File(projectInformation.getBuild().getDirectory()), "ts-detect");
    }

    private File getPluginSubDir(String name) throws IOException {
        return getSubDir(getPluginDir(), name);
    }

    private File extractResourceToFile(String name) throws IOException {
        File target = new File(getPluginDir(), name);
        if (target.exists()) return target;
        URL url = getClass().getResource("/" + name);
        if (url == null) {
            throw new FileNotFoundException("Resource for " + name + " not found");
        }
        if (!target.createNewFile()) {
            throw new IOException("Couldn't create resource file for " + name);
        }
        try (InputStream in = url.openStream(); OutputStream out = new FileOutputStream(target)) {
            in.transferTo(out);
        }
        if (!target.setExecutable(true)) {
            throw new IOException("Couldn't make the jar executable");
        }
        return target;
    }

    @SafeVarargs
    static <T> T[] prepend(T[] arr, T... firstElement) {
        final int N = arr.length;
        final int M = firstElement.length;
        arr = java.util.Arrays.copyOf(arr, N + M);
        System.arraycopy(arr, 0, arr, M, N);
        System.arraycopy(firstElement, 0, arr, 0, M);
        return arr;
    }

    private void runJar(File wd, String java, String jar, String... args) throws MojoExecutionException {
        ProcessBuilder pb = new ProcessBuilder(prepend(args, java, "-jar", jar));
        pb.directory(wd);
        pb.inheritIO();  // optional
        try {
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                throw new MojoExecutionException("Worker JAR exited with " + exit);
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to run worker JAR", e);
        }
    }

    private List<File> findTestFiles(String javaExecutable, File testFileDetector) throws IOException, MojoExecutionException {
        getLog().info("Running test detector");
        File wd = getPluginSubDir("file-detector");
        runJar(wd, javaExecutable, testFileDetector.getAbsolutePath(), projectInformation.getBuild().getTestSourceDirectory());

        File[] files = wd.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("Output_Class_");
            }
        });
        if (files == null) {
            throw new IOException("Error reading wd");
        }
        File csv = Arrays.stream(files).min(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return -o1.getName().compareTo(o2.getName());
            }
        }).orElseThrow(() -> new MojoExecutionException("Unable to find any result"));
        getLog().info("CSV: " + csv);
        List<File> res;
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            res = br.lines().skip(1).map(line -> new File(line.split(",", 2)[0])).collect(Collectors.toList());
        }
        return res;
    }

    private List<UnitTestPair> findUnitTestPairs(String javaExecutable, File sourceDir, List<File> tests) throws IOException, MojoExecutionException {
        getLog().info("Running test mapping");

        MappingDetector mappingDetector = new MappingDetector();

        List<UnitTestPair> res = new ArrayList<>(tests.size());
        for(File f : tests) {
            UnitTestPair p = mappingDetector.detectMapping(f, sourceDir);
            if(p == null) {
                getLog().warn("No prod file found for " + f);
            }else{
                res.add(p);
            }
        }


        return res;
    }

    private void findSmells(String javaExecutable, File testSmellDetector, List<UnitTestPair> pairs) throws IOException, MojoExecutionException, MojoFailureException {

        getLog().info("Running smell detector");
        File wd = getPluginSubDir("smell-detector");

        File input = new File(wd, "input.csv");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(input))) {
            for(UnitTestPair p : pairs) {
                w.write(projectInformation.getName() + "," + p.getUnitTest().getAbsolutePath() + "," + p.getProdFile().getAbsolutePath() + System.lineSeparator());
            }
        }

        runJar(wd, javaExecutable, testSmellDetector.getAbsolutePath(), input.getAbsolutePath());

        File[] files = wd.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("Output_TestSmellDetection_");
            }
        });
        if (files == null) {
            throw new IOException("Error reading wd");
        }
        File csv = Arrays.stream(files).min(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return -o1.getName().compareTo(o2.getName());
            }
        }).orElseThrow(() -> new MojoExecutionException("Unable to find any result"));
        getLog().info("CSV: " + csv);

        List<TestSmellRow> res;
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            res = br.lines().skip(1).map(TestSmellRow::new).collect(Collectors.toList());
        }

        boolean anyFails = false;

        for(TestSmellRow s : res) {
            getLog().error(s.getSummary());
            anyFails |= s.isFailing();
        }

        if (anyFails) {
            throw new MojoFailureException("Test smells were found");
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            String javaExecutable = getJavaExecutable();
            File testFileDetector = extractResourceToFile("TestFileDetector.jar");
            File testSmellDetector = extractResourceToFile("TestSmellDetector.jar");


            getLog().info("Using java at " + javaExecutable);
            getLog().info("testFileDetector at " + testFileDetector);
            getLog().info("testSmellDetector at " + testSmellDetector);
            getLog().info("target at " + projectInformation.getBuild().getDirectory());
            getLog().info("test sources at " + projectInformation.getBuild().getTestSourceDirectory());

            List<File> testFiles = findTestFiles(javaExecutable, testFileDetector);

            for(File f : testFiles) {
                getLog().info(" + " + f);
            }

            List<UnitTestPair> pairs = findUnitTestPairs(javaExecutable, new File(projectInformation.getBuild().getSourceDirectory()).getParentFile(), testFiles);
            for(UnitTestPair p : pairs) {
                getLog().info(" + " + p);
            }

            findSmells(javaExecutable, testSmellDetector, pairs);
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }

    }
}
