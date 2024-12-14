package com.pytestarchitect;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;

public class LineMarkerTest extends LightPlatformCodeInsightFixture4TestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testLineMarker() {
        myFixture.configureByFile("example.py");
        var infoList = myFixture.doHighlighting();
        boolean classMarkerFound = infoList.stream().anyMatch(info -> info.getGutterIconRenderer() != null && info.getGutterIconRenderer().getTooltipText().contains("Generate tests for class"));
        boolean functionMarkerFound = infoList.stream().anyMatch(info -> info.getGutterIconRenderer() != null && info.getGutterIconRenderer().getTooltipText().contains("Generate tests for function"));

        assertTrue("Found class marker", classMarkerFound);
        assertTrue("Found function marker", functionMarkerFound);

        }
}
