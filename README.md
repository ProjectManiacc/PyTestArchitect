# PyTestArchitect


<!-- Plugin description -->

PyTestArchitect is a PyCharm plugin designed to simplify the creation of Python unit tests. With the power of AI, this plugin automatically generates test cases for functions and classes, saving time and ensuring better code coverage.

## Features

- **AI-Powered Test Generation**: Automatically generate `pytest` test cases for Python functions and classes using OpenAI.
- **Intuitive Usage**: Quickly generate tests using the gutter icon, `Ctrl + Alt + G` keyboard shortcut(default, configurable), or the context menu option by right-clicking on a class or function.
- **Saving Results**: Automatically saves generated tests to a `tests` directory in your project.

## Prerequisites

- Python must be installed and accessible via the `python` command.
- OpenAI API key for accessing the AI model.
- PyCharm Professional or IntelliJ IDEA Ultimate with the Python plugin installed.
- PyTest must be installed in your Python environment.

## Installation

1. Download and install the plugin via the **JetBrains Plugin Marketplace**:
    - Go to **File > Settings > Plugins**.
    - Search for `PyTestArchitect`.
    - Click **Install** and restart the IDE.

2. Set up the OpenAI API key:
    - Add the environment variable `OPENAI_API_KEY` with your API key:

      ```bash
      export OPENAI_API_KEY=your_api_key
      ```

    - For Windows:

      ```cmd
      setx OPENAI_API_KEY "your_api_key"
      ```

## Usage

### 1. Generate Tests from the Editor
- Place your cursor inside a Python class or function.
- Press `Ctrl + Alt + G` (or your custom shortcut) to generate tests.

### 2. Generate Tests from the Gutter Icon
- Click the **Gutter Icon** next to a Python function or class.
- Tests are automatically generated and saved to the `tests` directory.

### 3. Generate Tests from the Context Menu
- Right-click inside the editor or on a Python file.
- Select **Generate Test** from the menu.

## How It Works

1. **Context Analysis**:
    - Extracts the name, source code, and import path of the selected Python function or class.

2. **Syntax Validation**:
    - Checks the Python file for valid syntax before generating tests.

3. **AI Test Generation**:
    - Sends the source code to OpenAI's API.
    - Receives AI-generated `pytest` test cases.

4. **Save Tests**:
    - Saves the generated test cases in a file named `test_<name>.py` inside the `tests` directory.

## Notifications and Error Handling

- **Invalid Syntax**: `"Invalid syntax detected in the file. Please fix errors before generating tests."`
- **Empty File**: `"The file is empty. Please add some code to generate tests."`
- **Invalid API Key**: `"Invalid API key. Please verify your configuration."`
- **Network Issues**: `"Unable to connect to API. Check your network connection."`
- **Unexpected Errors**: `"An unexpected error occurred: <error message>."`

## Development and Contribution

### Prerequisites
- **Gradle**: Used for building the plugin.
- **IntelliJ IDEA Plugin SDK**: Ensure it's installed and configured in the project.

### Building and Running
1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/pytest-architect.git
   cd pytest-architect
    ```
2. Build the plugin:

   ```bash
   ./gradlew runIde
   ```
   
## Customization

### Change the Shortcut

1. Open **File > Settings > Keymap**.
2. Search for **"Generate Test."**
3. Right-click and select **Add Keyboard Shortcut**.

## Known Issues

- Generating tests for larger classes might take some time, depending on the complexity of the code and network latency.

## Future Enhancements

- Support for generating tests for multiple functions or classes at once.
- Customizable AI settings (e.g., temperature, model type).
- Enhanced error handling for edge cases like large files or unsupported syntax.

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues, please submit a bug report or feature request on the [GitHub Issues](https://github.com/ProjectManiacc/PyTestArchitect/issues) page.


<!-- Plugin description end -->