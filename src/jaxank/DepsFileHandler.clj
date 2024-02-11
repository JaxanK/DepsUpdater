(ns jaxank.DepsFileHandler
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as github]))

;; ------- File Altercation -------
(defn read-deps-file-content [filename]
  (slurp filename))

(defn write-deps-file-content [filename content]
  (spit filename content))

;; ------- Basic Utility -------
(defn ensure-ends-with [s suffix]
  (if (str/ends-with? s suffix)
    s
    (str s suffix)))

;; ------- User Feedback -------
(defn printCheck [dep-name previous-version new-version]
  (let [pattern (re-pattern "[0-9.]")
        previous-formatted (str "v" (str/join (re-seq pattern previous-version)))]
    (if (= previous-formatted new-version)
      (println dep-name "is already up to date")
      (println "Updated" dep-name "from" previous-formatted "to" new-version))))

;; ------- Rewriting Content -------
(defn update-version-in-content [content namespace-prefix]
  (let [deps-pattern (re-pattern "[a-zA-Z0-9/-]+\\s+\\{:mvn/version\\s+\"[0-9.]+\"")
        deps-matches (re-seq deps-pattern content)]
    (reduce (fn [acc dep-match]
              (let [dep-name (first (re-find (re-pattern "([a-zA-Z0-9/-]+)") dep-match))]
                (if (str/starts-with? dep-name namespace-prefix)
                  (let [fetched-version (github/mock-fetch-latest-version dep-name)
                        new-version (if (str/starts-with? fetched-version "v")
                                      (subs fetched-version 1)  ; Strip "v" if present
                                      fetched-version)
                        updated-match (str/replace dep-match #":mvn/version\s+\"[^\"]+\"" (str ":mvn/version \"" new-version "\""))]
                    (printCheck dep-name dep-match fetched-version)
                    (str/replace acc dep-match updated-match))
                  acc)))
            content
            deps-matches)))

;; ------- Update Deps -------
(defn update-deps-file [filename namespace-prefix]
  (let [content (read-deps-file-content filename)
        updated-content (update-version-in-content content (ensure-ends-with namespace-prefix "/"))]
    (write-deps-file-content filename updated-content)
    (println namespace-prefix "dependencies updated in" filename)))

;; ------- Testing here for now -------
(update-deps-file "./__mocks__/mockDeps.edn" "jaxank")