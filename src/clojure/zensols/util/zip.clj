(ns ^{:doc "Helper package for dealing with zip streams (not files)."
      :author "Paul Landes"}
  zensols.util.zip
  (:import [java.util.zip ZipInputStream]
           [java.io InputStream])
  (:require [clojure.java.io :as io]))

(defn entry-input-stream
  "Return an InputStream for entry (ZipEntry) from ZipInputStream zin."
  [zin entry]
  (let [len (.getSize entry)
        left (atom 0)]
    (proxy [InputStream] []
      (read
        ([]
         (if (>= (swap! left inc) len)
           -1
           (let [buf (byte-array 1)]
             (.read zin buf (- @len 1))
             (first buf))))
        ([^bytes b]
         (let [len (count b)]
           (if (>= (swap! left #(- %)) len)
             -1
             (.read zin b 0 len))))
        ([^bytes b off len]
         (if (>= (swap! left #(- %)) len)
           -1
           (.read zin b off len)))))))

(defmacro doentries
  "Iterate through zip entries.
  Form:
  (doentries [exprs] ...)

  Where exprs is [input-stream entry-input-stream entry], input-stream the
  source, entry-input-stream is the stream to be read from the entry and entry
  is the entry instance.

  Return value is a map (see below) with keys :result (optional)
  and :continue (default true), which indicates whether or not to continue
  parsing zip entries or bailing.

  When the return value is not a map, it is interpretered with the return value
  as the result and will continue."
  {:style/indent 1}
  [exprs & forms]
  (let [[in- ein- entry-] exprs]
    `(let [zin# (ZipInputStream. ~in-)]
       (loop [entry# (.getNextEntry zin#)
              ret# []]
         (if (nil? entry#)
           ret#
           (let [~entry- entry#
                 ~ein- (entry-input-stream zin# entry#)
                 res# (do ~@forms)
                 res# (if (map? res#) res# {:result res#})]
             (recur (if (or (not (contains? res# :continue))
                            (:continue res#))
                      (.getNextEntry zin#))
                    (conj ret# (:result res#)))))))))

(defn read-entries
  "Read all entries and return them as a map with keys :name as the name
  and :content as the content."
  [fin]
  (doentries [fin ein e]
    {:name (.getName e) :content (slurp (io/reader ein))}))
