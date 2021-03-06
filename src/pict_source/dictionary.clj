(ns pict-source.dictionary
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.core :refer :all]))

;;;;  Dictionary functions
;;;;  to operate with dictionaries data
;;;;  defined in source directory

(def source-dir "resources/dictionary")
(def dictionary-seq (file-seq (io/file source-dir)))

(defn only-json [files-seq]
  (filter #(and (.isFile %) (re-find #"\.json$" (.getName %))) files-seq))

(defn only-dir [files-seq]
  (filter #(.isDirectory %) files-seq))

(defn only-lang [lang files-seq]
  (filter #(-> % .getPath (string/split #"/") reverse (nth 2) (= lang)) files-seq))

(defn first-letter [word-file]
  (first (.getName word-file)))

(defn read-word [word-file]
  (parse-stream (io/reader word-file) true))

(defn parse-files [files-seq]
  (map #(into
          {:letter (string/capitalize (first-letter %))
           :word (string/capitalize (string/replace (string/replace (.getName %) #"\.json" "") "_" " "))}
          (read-word %)) files-seq))

(defn group-by-letter [words-seq]
  (group-by :letter words-seq))

(defn lang-seq [lang]
  (only-lang lang (only-json dictionary-seq)))

(defn lang-data [lang]
  (try
    (parse-stream (io/reader (io/file (str source-dir "/" lang ".json"))) true)
    (catch java.io.FileNotFoundException e (hash-map))))

(defn words-map [lang]
  (sort (group-by-letter (parse-files (lang-seq lang)))))

(defn langs-available []
  (map #(.getName %) (only-dir (.listFiles (io/file source-dir)))))

(defn langs-sorted-by-words-count []
  (reverse
    (sort-by :words-count
             (map #(hash-map
                     :lang %
                     :words-count (count (lang-seq %))
                     :description (:description (lang-data %)))
                  (langs-available)))))
