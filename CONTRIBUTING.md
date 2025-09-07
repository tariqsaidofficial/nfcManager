# Contributing to NFC Manager

Thank you for your interest in contributing to NFC Manager! This document provides guidelines and information for contributors.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Contributing Guidelines](#contributing-guidelines)
5. [Pull Request Process](#pull-request-process)
6. [Issue Reporting](#issue-reporting)
7. [Development Standards](#development-standards)
8. [Community](#community)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of background, experience level, or identity.

### Expected Behavior

- Be respectful and constructive
- Focus on what's best for the community
- Show empathy towards other contributors
- Accept constructive criticism gracefully

### Unacceptable Behavior

- Harassment or discriminatory language
- Personal attacks or trolling
- Publishing private information
- Inappropriate conduct

## Getting Started

### Prerequisites

- **Android Studio**: Arctic Fox or newer
- **Java**: JDK 17 or higher
- **Android SDK**: API 30+ (Android 11+)
- **Git**: For version control
- **Device/Emulator**: With NFC support

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/nfc-manager.git
   cd nfc-manager
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/nfc-manager.git
   ```

## Development Setup

### 1. Environment Setup

```bash
# Verify Java version (must be 17+)
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java17
```

### 2. Build Project

```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

### 3. Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

## Contributing Guidelines

### Types of Contributions

We welcome various types of contributions:

- 🐛 **Bug fixes**
- ✨ **New features**
- 📚 **Documentation improvements**
- 🎨 **UI/UX enhancements**
- 🔧 **Code refactoring**
- 🧪 **Test improvements**
- 🌐 **Translations**

### Contribution Workflow

1. **Check existing issues** - Avoid duplicate work
2. **Create/discuss issue** - For significant changes
3. **Fork and branch** - Create feature branch
4. **Develop and test** - Follow coding standards
5. **Submit pull request** - With clear description
6. **Code review** - Address feedback
7. **Merge** - After approval

### Branch Naming

Use descriptive branch names:

```
feature/nfc-background-monitoring
bugfix/notification-permission-crash
docs/update-installation-guide
refactor/repository-layer-cleanup
```

### Commit Messages

Follow conventional commit format:

```
type(scope): brief description

Longer description if needed

Fixes #123
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

**Examples:**
```
feat(nfc): add background monitoring service

Implements continuous NFC state monitoring with
configurable intervals and battery optimization.

Fixes #45
```

```
fix(ui): resolve theme switching crash on Android 12

The theme switching was causing a crash due to
improper context handling in the settings screen.

Closes #78
```

## Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] Lint checks pass
- [ ] Documentation updated
- [ ] Screenshots for UI changes
- [ ] Self-review completed

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] Tested on multiple devices

## Screenshots (if applicable)
[Add screenshots for UI changes]

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests pass
```

### Review Process

1. **Automated checks** - CI/CD pipeline
2. **Code review** - By maintainers
3. **Testing** - Manual verification
4. **Approval** - Required before merge
5. **Merge** - Squash and merge preferred

## Issue Reporting

### Bug Reports

Use the bug report template:

```markdown
## Bug Description
Clear description of the bug

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Device: [e.g., Samsung Galaxy S21]
- Android Version: [e.g., Android 12]
- App Version: [e.g., 1.0.0]

## Screenshots/Logs
[If applicable]
```

### Feature Requests

Use the feature request template:

```markdown
## Feature Description
Clear description of the proposed feature

## Problem Statement
What problem does this solve?

## Proposed Solution
How should this be implemented?

## Alternatives Considered
Other approaches considered

## Additional Context
Any other relevant information
```

## Development Standards

### Code Style

Follow the project's code style guide:

- **Kotlin**: Official Kotlin style guide
- **Naming**: CamelCase for functions, PascalCase for classes
- **Formatting**: 120 character line limit
- **Documentation**: KDoc for public APIs

### Architecture

- **Pattern**: MVVM with Clean Architecture
- **DI**: Hilt for dependency injection
- **UI**: Jetpack Compose
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow

### Testing

- **Unit Tests**: For ViewModels and Repository
- **Integration Tests**: For database operations
- **UI Tests**: For critical user flows
- **Coverage**: Aim for 80%+ coverage

### Security

- **Data**: Local-only storage
- **Permissions**: Request minimal permissions
- **Encryption**: Encrypt sensitive data
- **Validation**: Validate all inputs

## Community

### Communication

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Pull Requests**: Code contributions
- **Email**: For security issues

### Getting Help

- Check existing documentation
- Search closed issues
- Ask in GitHub Discussions
- Contact maintainers

### Recognition

Contributors are recognized:

- In the CHANGELOG.md
- In the app's About section
- Through GitHub contributor stats
- Special mentions for significant contributions

## Development Phases

### Current Focus (Phase 2)

- Real NFC integration
- Background monitoring
- Notification system
- Permission handling

### Future Phases

- Design polish (Phase 3)
- Security enhancements (Phase 4)
- Advanced features (Phase 5)
- Testing & quality (Phase 6)

## Resources

### Documentation

- [Architecture Guide](docs/developer-guide.md)
- [API Documentation](docs/api-documentation.md)
- [User Guide](docs/user-guide.md)
- [Troubleshooting](docs/troubleshooting.md)

### External Resources

- [Android Developer Docs](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Nothing OS Design](https://nothing.tech/)

## License

By contributing to NFC Manager, you agree that your contributions will be licensed under the same license as the project.

---

**Thank you for contributing to NFC Manager! Together, we're building a better, more secure NFC experience for everyone.** 🚀
