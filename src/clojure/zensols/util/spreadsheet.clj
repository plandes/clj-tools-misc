(ns ^{:doc "Utility package for transposing, prettying, and otherwise working
with spreadsheets.  This package is designed to work with (not
replace) [clj-excel](https://github.com/outpace/clj-excel)."
      :author "Paul Landes"}
    zensols.util.spreadsheet
  (:require [clojure.java.io :as io]
            [clojure.string :as s])
  (:require [clj-excel.core :as excel]
            [clojure.data.csv :as csv]))

(def ^:private header-cell {:font {:bold true}
                            :border [:thin]
                            :alignment :center
                            :foreground-color :grey-25-percent
                            :pattern :solid-foreground})

(def ^:private left-col-cell
  (merge header-cell {:alignment :right}))

(defn autosize-columns
  "Auto size the columns in an Excel workbook.

  * **cols** the number of columns to autosize in the workbook"
  ([wb]
   (autosize-columns wb (range 30)))
  ([wb cols]
   (doall
    (map (fn [sheet]
           (doall
            (map (fn [col]
                   (.autoSizeColumn sheet col))
                 cols)))
         (->> (range (.getNumberOfSheets wb))
              (map #(.getSheetAt wb %)))))
   wb))

(defn headerize
  "Add a header row the row data **rows**.  This makes the first
  row (or columns) the header row.

  * **top?** whether to make the top row a header row
  * **top?** whether to make the left (most) column a header row"
  [rows & {:keys [top? left?] :or {top? true left? false}}]
  (concat (->> rows
               first
               (map (fn [cell-val]
                      (if top?
                        (merge header-cell {:value cell-val})
                        cell-val)))
               list)
          (map (fn [row]
                 (concat (list (if left?
                                 (merge left-col-cell {:value (first row)})
                                 (first row)))
                         (rest row)))
               (rest rows))))

(defn sheet-by-columns
  "Transpose the sheet like a matrix (rows become columns and the converse).

  * **sheet** an instane of an HSSF excel
  file (see [clj-excel](https://github.com/outpace/clj-excel))"
  ([sheet]
   (sheet-by-columns sheet #(and % (excel/cell-value %))))
  ([sheet cell-fn]
   (let [beg-col (.getFirstRowNum sheet)
         end-col (.getLastRowNum sheet)
         row-range (range beg-col (+ 1 end-col))
         col-count (apply max (map (fn [i]
                                     (-> sheet (.getRow i) (.getLastCellNum)))
                                   row-range))]
     (map (fn [col-idx]
            (map (fn [row-idx]
                   (let [row (.getRow sheet row-idx)
                         col (if (< col-idx (.getLastCellNum row))
                               (excel/get-cell sheet row-idx col-idx))]
                     (cell-fn col)))
                 row-range))
          (range col-count)))))

(defn sheet-by-rows
  "Transpose the sheet like a matrix (rows become columns and the converse)."
  ([sheet]
   (sheet-by-rows sheet #(and % (excel/cell-value %))))
  ([sheet cell-fn]
   (let [beg-col (.getFirstRowNum sheet)
         end-col (.getLastRowNum sheet)
         row-range (range beg-col (+ 1 end-col))
         col-count (apply max (map (fn [i]
                                     (-> sheet (.getRow i) (.getLastCellNum)))
                                   row-range))]
     (map (fn [row-idx]
            (map (fn [col-idx]
                   (let [row (.getRow sheet row-idx)
                         col (if (< col-idx (.getLastCellNum row))
                               (excel/get-cell sheet row-idx col-idx))]
                     (cell-fn col)))
                 (range col-count)))
          row-range))))

(defn transpose
  "Transpose rows and columns."
  [rows]
  (let [row-count (count rows)
        col-count (apply max (map count rows))]
    (vec (map (fn [col-idx]
                (vec (map (fn [row-idx]
                            (let [row (nth rows row-idx)
                                  col (if (< col-idx (count row))
                                        (nth row col-idx))]
                              (if (> (count col) 0) col)))
                          (range row-count))))
              (range col-count)))))

(defn rows-to-maps-key
  "Default function to convert a string key from a spreadsheet to a key used
  in [[rows-to-maps]]."
  [key]
  (->> (s/replace key #"[._\s]" "-")
       s/lower-case
       keyword))

(defn rows-to-maps
  "Return a sequence of maps each with keys taken from the top header row.

  See [[rows-to-maps]].

  ```
  [[\"Animal\" \"Size\"]
  [\"fish\" 3]
  [\"elephant\" 7]]
  ```
  becomes:
  ```
  ({:Animal \"fish\", :Size 3} {:Animal \"elephant\", :Size 7})
  ```

  Keys
  ----
  * **:to-key-fn** converts header to keys; defaults
  to [[rows-to-maps-key]]."
  ([by-rows str-keys
    & {:keys [to-key-fn]
       :or {to-key-fn rows-to-maps-key}}]
   (let [header (->> by-rows first (map to-key-fn))]
     (->> (rest by-rows)
          (map (fn [row]
                 (zipmap header row)))))))

(defn rows-to-maps
  "Convert **by-rows** data (created by [[clojure.data.csv/read-csv]] for
  example) and return a list of maps each with the keys of the header.

  See [[rows-to-map]].

  Keys
  ----
  * **:to-key-fn** converts header to keys; defaults
  to [[clojure.core/keyword]]
  * **:string-keys** hash set of keys (that match **:to-key-fn**) that if
  provided are read as strings; all other values are parsed as numbers with
  `read-string`"
  [by-rows &
   {:keys [to-key-fn string-keys]
    :or {to-key-fn #(->> (s/replace % #"[. _]" "-")
                         s/lower-case keyword)}}]
  (letfn [(parse-kv [[k v]]
            (try
              ((if (contains? string-keys k)
                 identity read-string)
               v)
              (catch Exception e
                (let [msg (format "Couldn't parse numeric value <%s>" v)]
                  (throw (ex-info msg {:key k :value v}))))))
          (parse-nums [row]
            (zipmap (keys row)
                    (map parse-kv row)))]
    (let [header (->> by-rows first (map to-key-fn))]
      (->> (rest by-rows)
           (map (fn [row]
                  (zipmap header row)))
           (map (if string-keys
                  parse-nums
                  identity))))))

(defn csv-by-columns
  "Get CSV data by columns, which does a transpose on the data.

  See [[transpose]]."
  [in-file]
  (try
    (with-open [reader (io/reader in-file)]
      (-> reader
          (csv/read-csv :separator \tab)
          transpose
          doall))
    (catch Exception e
      (throw (ex-info (format "Couldn't parse %s" in-file)
                      {:in-file in-file} e)))))

(defn file-type
  "Return the type of the spreadsheet file (ex: excel vs csv).

  This uses the file name to determine this for now.  The function returns one
  of the following keywords:

  * **:excel:** Excel (.xls xlsx)
  * **:csv** comma delimited (.csv)
  * **:tsv** tab delimited (.tsv)
  * **:text** text (.txt)"
  [file]
  (let [types [[:excel #"\.xlsx?$"]
               [:csv #"\.csv$"]
               [:tsv #"\.tsv$"]
               [:text #"\.txt$"]]
        filename (if (string? file)
                   (io/file file)
                   (.getName file))]
    (->> types
         (some (fn [[type regex]]
                 (and (re-find regex filename) type)))
         (#(or % 'unknown)))))

(defmacro with-read-spreadsheet
  "Read a spreadsheet file and return the contents in a sequence of sequences.

  **exprs** is the special form:

  `[file data opts]`

  where:

  * **file** is the file to open and close during the evaluation of
  **forms** and **data** is any symbol used to refer to sequene of sequences
  * **type** is the type of the file as a key (see [[file-type]])

  **opts** is an optional map with keys:

  * **:file-type-fn** a function to detect the type of spreadsheet as a keyword
  and defaults to [[file-type]]

Example
-------

```
(let [file (io/file \"file1.csv\")]
  (with-read-spreadsheet [file rows type]
    (->> rows
         rows-to-maps
         (take 3)
         doall)))
```"
  {:style/indent 1}
  [exprs & forms]
  (let [[file- data- ftype- opts-] exprs]
    `(let [opts# ~opts-
           file-type-fn# (or (:file-type-fn opts#) file-type)
           reader# (gensym)
           ~ftype- (file-type-fn# ~file-)]
       (case ~ftype-
         :csv (with-open [reader# (io/reader ~file-)]
                (let [~data- (->> reader#
                                  csv/read-csv)]
                  ~@forms))
         :tsv (with-open [reader# (io/reader ~file-)]
                (let [~data- (-> reader#
                                 (csv/read-csv :separator \tab))]
                  ~@forms))
         :excel (let [~data- (-> (excel/workbook-hssf ~file-)
                                 (.getSheetAt 0)
                                 sheet-by-rows)]
                  ~@forms)
         :text (with-open [reader# (io/reader ~file-)]
                 (let [~data- (->> reader#
                                   line-seq
                                   (map list))]
                   ~@forms))))))
