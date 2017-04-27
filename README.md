# Miscellaneous utilities used by other repos

[![Travis CI Build Status][travis-badge]][travis-link]

  [travis-link]: https://travis-ci.org/plandes/clj-tools-misc
  [travis-badge]: https://travis-ci.org/plandes/clj-tools-misc.svg?branch=master

Simple small file containing various low (library) footprint dependencies.

This contains a library for helping with
* Spreadsheets (CSV and Excel)
* Zip files/streams


## Obtaining

In your `project.clj` file, add:

[![Clojars Project](https://clojars.org/com.zensols.tools/misc/latest-version.svg)](https://clojars.org/com.zensols.tools/misc/)


## Documentation

Additional [documentation](https://plandes.github.io/clj-tools-misc/codox/index.html).


## Usage

Create a spreadsheet with all column adjusted to largest size of largest cell
and make a stylized header.
```clojure
user=> (require '[clj-excel.core :as excel])
user=> (require '[zensols.util.spreadsheet :as ss])
user=> (-> (excel/build-workbook
            (excel/workbook-hssf)
            {"Animal Stats"
             (-> [["Animal" "Really Big Col" "Size"]
                  ["cat" 1 "small"]
                  ["dog" 0 "big"]]
                 (ss/headerize))})
           (ss/autosize-columns)
           (excel/save (res/resource-path :analysis-report "test.xls")))
user=> (-> (clojure.java.io/file "test.xls")
		   ss/excel-or-csv-by-columns
		   pprint)
(("Animal" "Really Big Col" "Size")
 ("cat" 1.0 "small")
 ("dog" 0.0 "big"))
```


## Building

To build from source, do the folling:

- Install [Leiningen](http://leiningen.org) (this is just a script)
- Install [GNU make](https://www.gnu.org/software/make/)
- Install [Git](https://git-scm.com)
- Download the source: `git clone https://github.com/clj-mkproj && cd clj-mkproj`
- Download the make include files:
```bash
mkdir ../clj-zenbuild && wget -O - https://api.github.com/repos/plandes/clj-zenbuild/tarball | tar zxfv - -C ../clj-zenbuild --strip-components 1
```
- Build the distribution binaries: `make dist`

Note that you can also build a single jar file with all the dependencies with: `make uber`


## Changelog

An extensive changelog is available [here](CHANGELOG.md).


## License

Copyright © 2017 Paul Landes

Apache License version 2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
