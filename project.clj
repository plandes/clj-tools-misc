(defproject com.zensols.tools/misc "0.1.0-SNAPSHOT"
  :description "Miscellaneous utilities"
  :url "https://github.com/plandes/clj-nlp-annotation"
  :license {:name "Apache License version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :plugins [[lein-codox "0.9.5"]
            [org.clojars.cvillecsteele/lein-git-version "1.0.3"]]
  :codox {:metadata {:doc/format :markdown}
          :project {:name "Miscellaneous utilities"}
          :output-path "target/doc/codox"}
  :source-paths ["src/clojure"]
  :javac-options ["-Xlint:unchecked"]
  :exclusions [org.slf4j/slf4j-log4j12
               ch.qos.logback/logback-classic]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; spreadsheet
                 [org.clojure/data.csv "0.1.2"]
                 [outpace/clj-excel "0.0.2"]]
  :profiles {:uberjar {:aot :all}})
