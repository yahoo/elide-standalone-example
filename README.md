# Elide Example

An archetype Elide project for Heroku using Postgres DB.  

[![Build Status](https://cd.screwdriver.cd/pipelines/7925/badge)](https://cd.screwdriver.cd/pipelines/7925)

## Background

This project is the sample code for [Elide's Getting Started documentation](https://github.com/yahoo/elide/tree/master/elide-standalone)

## Install

To build and run:

1. mvn clean install
2. java -jar target/elide-heroku-example-1.0.0.jar 
3. Browse http://localhost:8080/

For API Versioning
1. Browse http://localhost:8080/?path=/v1
2. Browse http://localhost:8080/?path=/v2

To run from Heroku:

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/yahoo/elide-standalone-example)

## Usage

See [Elide's Getting Started documentation](https://github.com/yahoo/elide/tree/master/elide-standalone)

## Contribute
Please refer to the [CONTRIBUTING.md](CONTRIBUTING.md) file for information about how to get involved. We welcome issues, questions, and pull requests.

## License
This project is licensed under the terms of the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) open source license.
Please refer to [LICENSE](LICENSE.txt) for the full terms.
