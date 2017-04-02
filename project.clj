(defproject com.zensols.tools/misc "0.1.0-SNAPSHOT"
  :description "Miscellaneous utilities"
  :url "https://github.com/plandes/clj-tools-misc"
  :license {:name "Apache License version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :plugins [[lein-codox "0.10.3"]
            [org.clojars.cvillecsteele/lein-git-version "1.2.7"]]
  :codox {:metadata {:doc/format :markdown}
          :project {:name "Miscellaneous utilities"}
          :output-path "target/doc/codox"
          :source-uri "https://github.com/plandes/clj-tools-misc/blob/v{version}/{filepath}#L{line}"}
  :git-version {:root-ns "zensols.util"
                :path "src/clojure/zensols/util"
                :version-cmd "git describe --match v*.* --abbrev=4 --dirty=-dirty"}
  :source-paths ["src/clojure"]
  :javac-options ["-Xlint:unchecked"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; spreadsheet
                 [org.clojure/data.csv "0.1.2"]
                 [outpace/clj-excel "0.0.2"]]
  :profiles {:appassem {:aot :all}
             :snapshot {:git-version {:version-cmd "echo -snapshot"}}})
