(ns jaxank.DepsFileHandler
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as github]))

;; name-space like jaxank NEEDS to have a "/" apparently
(defn ensure-ends-with [s suffix]
  (if (str/ends-with? s suffix) 
    s 
    (str s suffix)))

(defn read-deps-file [filename]
  (with-open [rdr (io/reader filename)]
    (let [data (edn/read (java.io.PushbackReader. rdr))]
      data)))

(defn find-dependencies [deps-data namespace-prefix]
  (let [deps (get deps-data :deps)
        namespace-prefix (clojure.string/lower-case namespace-prefix)]
    (filter (fn [[k _]]
              (let [full-key-str (clojure.string/lower-case  (str k))]
                (str/starts-with? full-key-str namespace-prefix)))
            deps)))

(defn update-deps-file [namespace-prefix]
  (let [deps-file-path "./__mocks__/mockDeps.edn"
        namespace-prefix (if (str/ends-with? namespace-prefix "/")
                           namespace-prefix
                           (str namespace-prefix "/"))
        deps-data (read-deps-file deps-file-path)
        matched-deps (find-dependencies deps-data namespace-prefix)]
    (if (empty? matched-deps)
      (println (str "No dependencies found for namespace prefix: " namespace-prefix))
      (let [updated-deps (reduce (fn [acc [k _]]
                                   (let [dependency-name (str k) ; Already includes the namespace
                                         new-version (github/mock-fetch-latest-version dependency-name)]
                                     (println "Updating version for" k "to" new-version)
                                     (assoc-in acc [k :mvn/version] new-version)))
                                 (get deps-data :deps) matched-deps)]
        (spit deps-file-path (prn-str (assoc deps-data :deps updated-deps)))
        (println "Dependencies updated")))))

(update-deps-file "jaxank")