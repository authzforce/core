# Contribution Rules
1. No SNAPSHOT dependencies on "develop" and obviously "master" branches

# Releases  
1. Start a release: `$ mvn jgitflow:release-start`
2. Remove any SNAPSHOT dependency
3. Update the CHANGELOG.md
4. Finish a release: `$ mvn jgitflow:release-finish`

More info on jgitflow: http://jgitflow.bitbucket.org/
