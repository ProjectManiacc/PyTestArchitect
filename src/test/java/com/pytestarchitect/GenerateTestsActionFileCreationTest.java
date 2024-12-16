package com.pytestarchitect;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;

public class GenerateTestsActionFileCreationTest extends LightPlatformCodeInsightFixture4TestCase {

    @Test
    public void testGeneratedTestsFileCreationForClass() throws Exception {
        myFixture.configureByText("example.py",
                "class Person:\n" +
                        "    def __init__(self, name, age):\n" +
                        "        self.name = name\n" +
                        "        self.age = age\n"
        );

        int classPos = myFixture.getFile().getText().indexOf("class Person:");
        myFixture.getEditor().getCaretModel().moveToOffset(classPos);

        GenerateTestAction action = new GenerateTestAction();
        myFixture.testAction(action);

        VirtualFile projectDir = myFixture.getProject().getBaseDir();
        VirtualFile testsDir = projectDir.findChild("tests");
        assertNotNull("tests directory not found", testsDir);

        VirtualFile testFile = testsDir.findChild("test_person.py");
        assertNotNull("test_person.py file not found", testFile);

        String testFileContent = new String(testFile.contentsToByteArray(), testFile.getCharset());
        assertTrue("test_person.py should contain test_init function", testFileContent.contains("def test_init(self):"));

    }

    @Test
    public void testGeneratedTestsFileCreationForFunction() throws Exception {
        myFixture.configureByText("example.py",
                "def foo():\n    return 42\n"
        );

        int functionPos = myFixture.getFile().getText().indexOf("def foo:");
        myFixture.getEditor().getCaretModel().moveToOffset(functionPos);

        GenerateTestAction action = new GenerateTestAction();
        myFixture.testAction(action);

        VirtualFile projectDir = myFixture.getProject().getBaseDir();
        VirtualFile testsDir = projectDir.findChild("tests");
        assertNotNull("tests directory not found", testsDir);

        VirtualFile testFile = testsDir.findChild("test_foo.py");
        assertNotNull("test_foo.py file not found", testFile);

        String testFileContent = new String(testFile.contentsToByteArray(), testFile.getCharset());
        assertTrue("test_foo.py should contain test_foo function", testFileContent.contains("def test_foo(self):"));
    }
}
