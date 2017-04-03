(ns ^{:doc "Miscellaneous string utils."
      :author "Paul Landes"}
    zensols.util.string)

(def ^:dynamic *trunc-len*
  "Default truncation length for [[trunc]]."
  80)

(defn trunc
  "Truncate string `obj` at `len` characters adding ellipses if larger that a set
  length.  If `obj` isn't a string use `pr-str` to make it a string.

  See [[*trunc-len*]]."
  ([obj] (trunc obj *trunc-len*))
  ([obj len]
   (let [s (if (string? obj) obj (pr-str obj))
         slen (count s)
         trunc? (> slen len)
         maxlen (-> (if trunc? (min slen (- len 3))
                        (min slen len))
                    (max 0))]
     (str (subs s 0 maxlen) (if trunc? "...")))))
