Miscellaneous utilities used by other repos
===========================================
Simple small file containing various low (library) footprint dependencies.

This contains a library for helping with
* Spreadsheets (CSV and Excel)
* Zip files/streams

Obtaining
---------
In your `project.clj` file, add:

[![Clojars Project](https://clojars.org/com.zensols.tools/misc/latest-version.svg)](https://clojars.org/com.zensols.tools/misc/)

Documentation
-------------
Additional [documentation](https://plandes.github.io/clj-tools-misc/codox/index.html).

Usage
-----
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

Building
--------
All [leiningen](http://leiningen.org) tasks will work in this project.  For
additional build functionality (git tag convenience utility functionality)
clone the [Clojure build repo](https://github.com/plandes/clj-zenbuild) in the
same (parent of this file) directory as this project:
```bash
   cd ..
   git clone https://github.com/plandes/clj-zenbuild
```

License
--------
Copyright © 2016 Paul Landes

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
