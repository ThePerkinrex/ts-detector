package es.upm.grise.profundizacion.grupo.aa;

import java.io.File;

public class UnitTestPair {
    private final File unitTest;
    private final File prodFile;

    public UnitTestPair(File unitTest, File prodFile) {
        this.unitTest = unitTest;
        this.prodFile = prodFile;
    }

    public File getUnitTest() {
        return unitTest;
    }

    public File getProdFile() {
        return prodFile;
    }

    @Override
    public String toString() {
        return "UnitTestPair{" +
                "unitTest=" + unitTest +
                ", prodFile=" + prodFile +
                '}';
    }
}
