## Overview
This is a very simple log analyser that takes a log file hard coded on the main class and outputs to console all the events. It is fully customizable from the main class, using a Builder pattern to build the log file processor instance and freely able to change the thresholds for warnings and errors. 
The application is a maven project, currently only to bring JUnit for unit testing but can be expanded to include other dependencies further down the line.

## Improvements
As this is a very simple application, the improvements are many.

### Testing 
- Improve coverage of testing, expand to include end to end integration testing rather than just unit tests.
- Create test files to test integration, and refactor processLogEntry back to private as it shouldn't be package visible.

### Architectural
- Refactor the ConsoleLogger class to handle test cases more gracefully rather than a bunch of static methods. Possibly split to a seperate ConsoleLoggerTest class instead as the current code breaks OWASP Top 10
- Rather than hardcoded values on the main class, allow users to pass in their options or specify them in a configuration file.
- Print the errors and warnings to another file rather than just logging it on console (although this depends on user requirement).

