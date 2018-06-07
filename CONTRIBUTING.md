# Contributing

## Breaking Changes

If you are introducing a breaking change, you must include a description of that change in your commit message. Your description will be included automatically in the next release's changelog and the library will be incremented a major version.

Example:

```bash
git commit -m "refactor(myCoolFunction): pass args as Map

BREAKING CHANGE:
myCoolFunction now only accepts a single Map typed argument.
"
```

## Commit Rules
In order to allow automated publishing of new library versions, this repository follows the [conventional commits](https://conventionalcommits.org) standard.

To make your life easier this repo is [commitizen-friendly](http://commitizen.github.io/cz-cli) and provides the npm run-script `commit`.

At a high level, this means your commits should be:
* present tense
* first line maximum of 100 characters
* message format of `$type($scope): $message`
