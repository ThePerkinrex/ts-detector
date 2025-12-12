package es.upm.grise.profundizacion.grupo.aa;

public class TestSmellRow {

    private String app;
    private String testClass;
    private String testFilePath;
    private String productionFilePath;
    private String relativeTestFilePath;
    private String relativeProductionFilePath;

    private int numberOfMethods;
    private int assertionRoulette;
    private int conditionalTestLogic;
    private int constructorInitialization;
    private int defaultTest;
    private int emptyTest;
    private int exceptionCatchingThrowing;
    private int generalFixture;
    private int mysteryGuest;
    private int printStatement;
    private int redundantAssertion;
    private int sensitiveEquality;
    private int verboseTest;
    private int sleepyTest;
    private int eagerTest;
    private int lazyTest;
    private int duplicateAssert;
    private int unknownTest;
    private int ignoredTest;
    private int resourceOptimism;
    private int magicNumberTest;
    private int dependentTest;

    public TestSmellRow(String csvRow) {
        String[] parts = csvRow.split(",", -1);

        this.app = parts[0];
        this.testClass = parts[1];
        this.testFilePath = parts[2];
        this.productionFilePath = parts[3];
        this.relativeTestFilePath = parts[4];
        this.relativeProductionFilePath = parts[5];

        this.numberOfMethods = Integer.parseInt(parts[6]);
        this.assertionRoulette = Integer.parseInt(parts[7]);
        this.conditionalTestLogic = Integer.parseInt(parts[8]);
        this.constructorInitialization = Integer.parseInt(parts[9]);
        this.defaultTest = Integer.parseInt(parts[10]);
        this.emptyTest = Integer.parseInt(parts[11]);
        this.exceptionCatchingThrowing = Integer.parseInt(parts[12]);
        this.generalFixture = Integer.parseInt(parts[13]);
        this.mysteryGuest = Integer.parseInt(parts[14]);
        this.printStatement = Integer.parseInt(parts[15]);
        this.redundantAssertion = Integer.parseInt(parts[16]);
        this.sensitiveEquality = Integer.parseInt(parts[17]);
        this.verboseTest = Integer.parseInt(parts[18]);
        this.sleepyTest = Integer.parseInt(parts[19]);
        this.eagerTest = Integer.parseInt(parts[20]);
        this.lazyTest = Integer.parseInt(parts[21]);
        this.duplicateAssert = Integer.parseInt(parts[22]);
        this.unknownTest = Integer.parseInt(parts[23]);
        this.ignoredTest = Integer.parseInt(parts[24]);
        this.resourceOptimism = Integer.parseInt(parts[25]);
        this.magicNumberTest = Integer.parseInt(parts[26]);
        this.dependentTest = Integer.parseInt(parts[27]);
    }

    public String getApp() {
        return app;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public String getProductionFilePath() {
        return productionFilePath;
    }

    public String getRelativeTestFilePath() {
        return relativeTestFilePath;
    }

    public String getRelativeProductionFilePath() {
        return relativeProductionFilePath;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public int getAssertionRoulette() {
        return assertionRoulette;
    }

    public int getConditionalTestLogic() {
        return conditionalTestLogic;
    }

    public int getConstructorInitialization() {
        return constructorInitialization;
    }

    public int getDefaultTest() {
        return defaultTest;
    }

    public int getEmptyTest() {
        return emptyTest;
    }

    public int getExceptionCatchingThrowing() {
        return exceptionCatchingThrowing;
    }

    public int getGeneralFixture() {
        return generalFixture;
    }

    public int getMysteryGuest() {
        return mysteryGuest;
    }

    public int getPrintStatement() {
        return printStatement;
    }

    public int getRedundantAssertion() {
        return redundantAssertion;
    }

    public int getSensitiveEquality() {
        return sensitiveEquality;
    }

    public int getVerboseTest() {
        return verboseTest;
    }

    public int getSleepyTest() {
        return sleepyTest;
    }

    public int getEagerTest() {
        return eagerTest;
    }

    public int getLazyTest() {
        return lazyTest;
    }

    public int getDuplicateAssert() {
        return duplicateAssert;
    }

    public int getUnknownTest() {
        return unknownTest;
    }

    public int getIgnoredTest() {
        return ignoredTest;
    }

    public int getResourceOptimism() {
        return resourceOptimism;
    }

    public int getMagicNumberTest() {
        return magicNumberTest;
    }

    public int getDependentTest() {
        return dependentTest;
    }

    public String getSummary() {

        StringBuilder sb = new StringBuilder();

        sb.append("Test file: ").append(relativeTestFilePath).append("\n");
        sb.append("Production file: ").append(relativeProductionFilePath).append("\n");

        // collect smells and percentages
        int totalMethods = numberOfMethods;

        // array of smell names and their values
        int[] values = {
                assertionRoulette,
                conditionalTestLogic,
                constructorInitialization,
                defaultTest,
                emptyTest,
                exceptionCatchingThrowing,
                generalFixture,
                mysteryGuest,
                printStatement,
                redundantAssertion,
                sensitiveEquality,
                verboseTest,
                sleepyTest,
                eagerTest,
                lazyTest,
                duplicateAssert,
                unknownTest,
                ignoredTest,
                resourceOptimism,
                magicNumberTest,
                dependentTest
        };

        String[] names = {
                "Assertion Roulette",
                "Conditional Test Logic",
                "Constructor Initialization",
                "Default Test",
                "Empty Test",
                "Exception Catching Throwing",
                "General Fixture",
                "Mystery Guest",
                "Print Statement",
                "Redundant Assertion",
                "Sensitive Equality",
                "Verbose Test",
                "Sleepy Test",
                "Eager Test",
                "Lazy Test",
                "Duplicate Assert",
                "Unknown Test",
                "Ignored Test",
                "Resource Optimism",
                "Magic Number Test",
                "Dependent Test"
        };

        boolean anySmell = false;

        sb.append("\nDetected test smells:\n");

        for (int i = 0; i < values.length; i++) {
            int v = values[i];
            if (v > 0) {
                anySmell = true;
                double pct = totalMethods > 0 ? (100.0 * v / totalMethods) : 0;
                sb.append("- ").append(names[i])
                        .append(": ").append(v)
                        .append(" (").append(String.format("%.1f", pct)).append("%)")
                        .append("\n");
            }
        }

        if (!anySmell) {
            sb.append("No test smells detected.\n");
        }

        return sb.toString();
    }

    public boolean isFailing() {
        // array of smell names and their values
        int[] values = {
                assertionRoulette,
                conditionalTestLogic,
                constructorInitialization,
                defaultTest,
                emptyTest,
                exceptionCatchingThrowing,
                generalFixture,
                mysteryGuest,
                printStatement,
                redundantAssertion,
                sensitiveEquality,
                verboseTest,
                sleepyTest,
                eagerTest,
                lazyTest,
                duplicateAssert,
                unknownTest,
                ignoredTest,
                resourceOptimism,
                magicNumberTest,
                dependentTest
        };

        boolean anySmell = false;
        for (int i = 0; i < values.length && !anySmell; i++) {
            anySmell = values[i] > 0;
        }
        return anySmell;
    }

}

