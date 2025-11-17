# Rickroll on Test Failure Action

A GitHub Action that displays a rickroll message when tests fail.

## Description

When tests fail, this action displays:
- ASCII art with "NEVER GONNA GIVE YOU UP"
- Rick Astley lyrics
- Link to the music video
- Test failure details

## Usage

```yaml
- name: Run Tests
  id: test
  run: mvn test
  continue-on-error: true

- name: Rickroll on Failure
  uses: ./.github/actions/rickroll-on-failure
  with:
    test-result: ${{ steps.test.outcome }}
```

## Inputs

| Input | Description | Required |
|-------|-------------|----------|
| `test-result` | Result of the test step (success or failure) | Yes |

## Features

- Works with any test framework (Maven, Gradle, npm, pytest, etc.)
- Adds a notice annotation to the workflow run
- Displays commit information for debugging
- Compatible with GitHub Actions workflows

## Author

Created by Karim Hozaien for the GraphHopper project as part of a university assignment on testing with humor.

