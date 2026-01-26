# Contributing to JLens

Thank you for your interest in contributing to JLens! We welcome all kinds of contributions, from bug reports to feature requests and code changes.

## Development Environment

- **Java**: JDK 17 or higher.
- **Build Tool**: Maven 3.9 or higher.
- **Node.js**: Required for testing and building npm packages.
- **Python**: Required for testing and building pypi packages.

## Code Standards

- **Formatting**: Use 4 spaces for indentation.
- **Naming**: Follow standard Java naming conventions.
- **Language**: Use English for all code, comments, and documentation.
- **Dependencies**: Keep dependencies to a minimum and only use established libraries.

## Testing Requirements

- **Unit Tests**: Every new feature or bug fix must include corresponding unit tests.
- **Coverage**: Maintain a code coverage of at least 85% for core logic.
- **Integration Tests**: Ensure integration tests pass before submitting changes.

## Commit Guidelines

We follow a structured commit message format to maintain a clear project history.

- **Language**: Use Chinese for commit messages.
- **Format**: `<type>(<scope>): <subject>`
- **Single Responsibility**: Each commit should represent a single logical change.
- **Types**:
  - `feat`: New feature
  - `fix`: Bug fix
  - `docs`: Documentation changes
  - `ref`: Code refactor
  - `test`: Test additions or corrections
  - `build`: Build system or distribution changes

## Branching Model

- Submit Pull Requests to the `main` branch.
- Ensure your branch is up-to-date with `main` before submitting.

## Documentation

- **User Guide**: `docs/user-guide/`
- **Developer Docs**: `docs/developer-docs/`
- **Design Plans**: `docs/plans/`

When adding new tools, please update the corresponding documentation in both English and Chinese.
