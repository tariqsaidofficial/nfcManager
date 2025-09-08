# Contributing to NFC Manager

Thank you for your interest in contributing to NFC Manager! We welcome contributions from the community and are excited to see what you can bring to this privacy-focused NFC management application.

## 🎯 Project Overview

NFC Manager is a privacy-focused Android application inspired by Nothing OS design aesthetics. The project aims to help users monitor and manage their NFC settings while maintaining complete privacy and security.

## 🤝 How to Contribute

### Types of Contributions

We welcome various types of contributions:

- 🐛 **Bug Reports**: Help us identify and fix issues
- ✨ **Feature Requests**: Suggest new features or improvements
- 💻 **Code Contributions**: Submit bug fixes or new features
- 📚 **Documentation**: Improve or expand documentation
- 🌍 **Translations**: Help localize the app for more languages
- 🎨 **Design**: Contribute UI/UX improvements
- 🧪 **Testing**: Help test the app on different devices

## 🚀 Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Android Studio**: Latest stable version
- **JDK 11+**: Required for Android development
- **Git**: For version control
- **Android SDK**: API level 30+ (Android 11+)
- **Device/Emulator**: With NFC capability for testing

### Development Setup

1. **Fork the Repository**
   ```bash
   # Fork the repo on GitHub, then clone your fork
   git clone https://github.com/tariqsaidofficial/nfcManager.git
   cd nfcManager
   ```

2. **Set Up Development Environment**
   ```bash
   # Navigate to Android project
   cd android
   
   # Verify Java version
   java -version  # Should be 11+
   
   # Clean and build
   ./gradlew clean
   ./gradlew assembleDebug
   ```

3. **Open in Android Studio**
   - File → Open → Select `android/` folder
   - Wait for Gradle sync to complete
   - Build → Make Project

4. **Test Your Setup**
   ```bash
   # Run tests
   ./gradlew test
   
   # Run on device/emulator
   ./gradlew installDebug
   ```

## 🏗️ Project Structure

Understanding the project structure will help you contribute effectively:

```
android/
├── app/
│   ├── src/main/kotlin/com/nothingos/nfcmanager/
│   │   ├── data/                    # Data layer (Room, Repository)
│   │   ├── services/                # Background services
│   │   ├── ui/                      # UI layer (Compose screens)
│   │   ├── viewmodel/               # MVVM ViewModels
│   │   ├── utils/                   # Utility classes
│   │   └── di/                      # Dependency injection (Hilt)
│   └── src/main/res/                # Android resources
├── build.gradle                     # App-level build configuration
└── gradle/                          # Gradle wrapper
```

### Key Technologies

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room for local storage
- **DI**: Hilt for dependency injection
- **Async**: Kotlin Coroutines and StateFlow

## 📝 Coding Standards

### Code Style

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters maximum
- **Naming**: camelCase for variables, PascalCase for classes
- **Imports**: Organize and remove unused imports

### Architecture Guidelines

- **MVVM Pattern**: Separate UI, business logic, and data layers
- **Single Responsibility**: Each class should have one clear purpose
- **Dependency Injection**: Use Hilt for all dependencies
- **Reactive Programming**: Use StateFlow for UI state management
- **Error Handling**: Proper exception handling with user-friendly messages

### Compose Guidelines

- **Composable Functions**: Keep them small and focused
- **State Management**: Hoist state appropriately
- **Performance**: Use `remember` and `LaunchedEffect` correctly
- **Accessibility**: Include content descriptions for screen readers
- **Nothing OS Design**: Follow the established design system

## 🐛 Bug Reports

When reporting bugs, please include:

### Bug Report Template

```markdown
**Bug Description**
A clear description of what the bug is.

**Steps to Reproduce**
1. Go to '...'
2. Click on '...'
3. See error

**Expected Behavior**
What you expected to happen.

**Actual Behavior**
What actually happened.

**Screenshots**
If applicable, add screenshots.

**Device Information**
- Device: [e.g. Samsung Galaxy S23]
- OS Version: [e.g. Android 13]
- App Version: [e.g. 1.0.0]
- NFC Support: [Yes/No]

**Additional Context**
Any other context about the problem.
```

### Before Reporting

- Check if the issue already exists
- Try to reproduce the bug consistently
- Test on multiple devices if possible
- Include relevant logs if available

## ✨ Feature Requests

We love new ideas! When suggesting features:

### Feature Request Template

```markdown
**Feature Description**
A clear description of the feature you'd like to see.

**Problem Statement**
What problem does this feature solve?

**Proposed Solution**
How would you like this feature to work?

**Alternatives Considered**
Any alternative solutions you've considered.

**Additional Context**
Screenshots, mockups, or examples.

**Privacy Impact**
How does this feature affect user privacy?
```

### Feature Guidelines

- Align with our privacy-first approach
- Consider Nothing OS design aesthetics
- Think about battery and performance impact
- Ensure accessibility compliance

## 💻 Code Contributions

### Pull Request Process

1. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

2. **Make Your Changes**
   - Write clean, well-documented code
   - Follow coding standards
   - Add tests for new functionality
   - Update documentation if needed

3. **Test Your Changes**
   ```bash
   # Run unit tests
   ./gradlew test
   
   # Run lint checks
   ./gradlew lint
   
   # Test on device
   ./gradlew connectedAndroidTest
   ```

4. **Commit Your Changes**
   ```bash
   # Use conventional commits
   git commit -m "feat: add custom notification sounds"
   git commit -m "fix: resolve NFC detection issue"
   git commit -m "docs: update installation guide"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   # Create pull request on GitHub
   ```

### Commit Message Convention

We use [Conventional Commits](https://conventionalcommits.org/):

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `style:` Code style changes (formatting, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

### Pull Request Template

```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Code refactoring
- [ ] Performance improvement

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] Tested on multiple devices
- [ ] No breaking changes

## Screenshots
If applicable, add screenshots of UI changes.

## Privacy Impact
Describe any privacy implications of your changes.

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings or errors
```

## 🌍 Translations

Help us make NFC Manager available in more languages!

### Current Languages

- English (default)
- العربية (Arabic) - RTL support
- Español (Spanish)
- Français (French)
- Русский (Russian)
- 中文 (Chinese Simplified)
- हिन्दी (Hindi)
- Filipino

### Adding a New Language

1. **Create Language Files**
   ```
   android/app/src/main/res/values-{language_code}/strings.xml
   ```

2. **Translate Strings**
   - Copy from `values/strings.xml`
   - Translate all string values
   - Keep string keys unchanged
   - Consider cultural context

3. **Test Translation**
   - Test UI layout with translated text
   - Ensure text fits in UI components
   - Test RTL languages properly

4. **Update Documentation**
   - Add language to README.md
   - Update supported languages list

### Translation Guidelines

- **Accuracy**: Maintain technical accuracy
- **Context**: Consider UI context and space limitations
- **Consistency**: Use consistent terminology
- **Cultural**: Respect cultural differences
- **RTL**: Properly support right-to-left languages

## 🎨 Design Contributions

### Nothing OS Design System

Our design follows Nothing OS principles:

- **Colors**: Primary red (#EF4444), pure black/white
- **Typography**: Nothing Font family
- **Spacing**: 8px grid system
- **Animations**: 300ms interactions, custom bezier curves
- **Components**: Clean lines, minimal shadows

### Design Tools

- **Figma**: For UI mockups and prototypes
- **Android Studio**: Layout inspector and preview
- **Material Theme Builder**: For color schemes

### Design Process

1. **Research**: Study Nothing OS design patterns
2. **Wireframe**: Create low-fidelity layouts
3. **Mockup**: Design high-fidelity screens
4. **Prototype**: Interactive prototypes for complex flows
5. **Implement**: Work with developers for implementation

## 🧪 Testing

### Testing Strategy

- **Unit Tests**: Test individual components and functions
- **Integration Tests**: Test component interactions
- **UI Tests**: Test user interface and interactions
- **Manual Testing**: Real device testing scenarios

### Writing Tests

```kotlin
// Unit Test Example
@Test
fun `test NFC status detection`() {
    // Given
    val nfcAdapter = mockk<NfcAdapter>()
    every { nfcAdapter.isEnabled } returns true
    
    // When
    val isEnabled = NFCUtils.isNfcEnabled(context)
    
    // Then
    assertTrue(isEnabled)
}

// Compose UI Test Example
@Test
fun `test home screen displays NFC status`() {
    composeTestRule.setContent {
        HomeScreen(viewModel = mockViewModel)
    }
    
    composeTestRule
        .onNodeWithText("NFC STATUS")
        .assertIsDisplayed()
}
```

### Manual Testing Checklist

- [ ] NFC detection works correctly
- [ ] Background service functions properly
- [ ] Notifications display correctly
- [ ] Settings save and load properly
- [ ] Multi-language support works
- [ ] Dark/light themes work
- [ ] Accessibility features function
- [ ] Performance is acceptable

## 📚 Documentation

### Documentation Types

- **Code Documentation**: Inline comments and KDoc
- **API Documentation**: Generated from code comments
- **User Documentation**: Guides and tutorials
- **Developer Documentation**: Setup and architecture guides

### Writing Guidelines

- **Clear**: Use simple, clear language
- **Complete**: Cover all necessary information
- **Current**: Keep documentation up to date
- **Examples**: Include code examples and screenshots

## 🔍 Code Review Process

### For Contributors

- Be open to feedback and suggestions
- Respond promptly to review comments
- Make requested changes thoroughly
- Ask questions if something is unclear

### For Reviewers

- Be constructive and helpful
- Focus on code quality and standards
- Consider privacy and security implications
- Test changes when possible

### Review Checklist

- [ ] Code follows project conventions
- [ ] No privacy or security issues
- [ ] Performance impact considered
- [ ] Tests are adequate
- [ ] Documentation updated
- [ ] Accessibility maintained

## 🎉 Recognition

### Contributors

We recognize contributors in several ways:

- **README Credits**: Listed in project README
- **Release Notes**: Mentioned in version releases
- **Hall of Fame**: Special recognition page
- **Direct Communication**: Access to development team

### Contribution Levels

- **First-time Contributors**: Welcome package and guidance
- **Regular Contributors**: Enhanced recognition and access
- **Core Contributors**: Decision-making participation
- **Maintainers**: Full project access and responsibility

## 📞 Getting Help

### Communication Channels

- **GitHub Issues**: Technical discussions and bug reports
- **GitHub Discussions**: General questions and ideas
- **Email**: support@dxbmark.com for direct communication
- **Discord**: Community chat (link in README)

### Support Guidelines

- **Be Respectful**: Maintain professional and friendly communication
- **Be Patient**: Allow time for responses
- **Be Specific**: Provide detailed information
- **Search First**: Check existing issues and documentation

## ⚖️ Legal Guidelines

### Licensing

- All contributions are subject to Apache License 2.0
- Contributors retain copyright of their contributions
- Contributions must be original work or properly attributed
- No proprietary or copyrighted code without permission

### Code of Conduct

- **Respectful**: Treat all community members with respect
- **Inclusive**: Welcome contributors from all backgrounds
- **Professional**: Maintain professional communication
- **Constructive**: Provide helpful feedback and suggestions

## 🎯 Project Roadmap

### Current Focus (v1.0.x)

- Bug fixes and stability improvements
- Performance optimization
- Enhanced accessibility
- Additional language support

## 📋 Quick Reference

### Useful Commands

```bash
# Development
./gradlew clean build
./gradlew test
./gradlew lint
./gradlew assembleDebug

# Testing
./gradlew connectedAndroidTest
./gradlew testDebugUnitTest

# Code Quality
./gradlew ktlintCheck
./gradlew detekt
```

### Important Files

- `android/app/build.gradle` - App configuration
- `android/app/src/main/AndroidManifest.xml` - Permissions and components
- `android/app/src/main/kotlin/` - Source code
- `android/app/src/main/res/` - Resources and layouts

### Coding Checklist

- [ ] Code follows Kotlin conventions
- [ ] Proper error handling implemented
- [ ] Unit tests written
- [ ] Documentation updated
- [ ] Privacy considerations addressed
- [ ] Accessibility features maintained
- [ ] Performance impact considered

---

## 📞 Contact

For questions about contributing:

- **Email**: support@dxbmark.com
- **Subject**: "Contributing - NFC Manager"
- **Response Time**: 24-48 hours

---

**Built with ❤️ by Tariq Said - Nothing OS Inspired Design**

*Technical Support & Contact: support@dxbmark.com*

---

*Licensed under the Apache License, Version 2.0*  
*Copyright 2025 Tariq Said. All rights reserved.*