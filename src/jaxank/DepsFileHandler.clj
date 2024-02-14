(ns jaxank.DepsFileHandler
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as gh]))



;; ------- Basic Utility -------
(defn ensure-str-ends-with [s suffix]
  (if (str/ends-with? s suffix)
    s
    (str s suffix)))

;; ------- User Feedback -------
(defn printCheck [dep-name previous-version new-version]
  (let [pattern  #"[0-9.]"
        previous-formatted (str "v" (str/join (re-seq pattern previous-version)))]
    (if (= previous-formatted new-version)
      (println dep-name "is already up to date")
      (println "Updated" dep-name "from" previous-formatted "to" new-version))))

;; ------- Rewriting Content -------
(defn update-version-in-content [content namespace-prefix]
  (let [deps-matches (re-seq #"[a-zA-Z0-9/-]+\s+\{:mvn/version\s+\"[0-9.]+\"" content)
        deps-matches (for [match deps-matches] {:dep-name    (re-find #"([a-zA-Z0-9/-]+)"          match)
                                                :dep-ver-str (re-find #":mvn/version\s+\"[^\"]+\"" match)
                                                :original-str match})
        deps-matches (filter #(str/starts-with? (:dep-name %) namespace-prefix) deps-matches)]
    (map #(assoc :new-str (let [match %
                                fetched-version (gh/fetch-latest-version (:dep-name match))
                                new-version (if (str/starts-with? fetched-version "v")
                                              (subs fetched-version 1)  ; Strip "v" if present
                                              fetched-version)]
                            (str/replace (:fullstr match) #":mvn/version\s+\"[^\"]+\"" (str ":mvn/version \"" new-version "\""))))
         deps-matches)))

;; ------- Update Deps -------
  (defn update-deps-file [filename namespace-prefix]
    (let [content (slurp filename)
          updated-content (update-version-in-content content (ensure-str-ends-with namespace-prefix "/"))]
      (spit filename updated-content)
      (println namespace-prefix "dependencies updated in" filename)))

;; ------- Testing here for now -------
  (update-deps-file "./__mocks__/mockDeps.edn" "jaxank")