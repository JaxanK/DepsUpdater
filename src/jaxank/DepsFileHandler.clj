(ns jaxank.DepsFileHandler
  (:require [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as gh]))


;; ------- Basic Utility -------
(defn ensure-str-ends-with [s suffix]
  (if (str/ends-with? s suffix)
    s
    (str s suffix)))

;; ------- User Feedback -------
(defn print-update-status
  "Prints an update status message for a dependency."
  [dep-name previous-version new-version]
  (let [pattern  #"[0-9.]"
        previous-formatted (str/join (re-seq pattern previous-version))
        new-formatted      (str/join (re-seq pattern new-version))]
    (if (= previous-formatted new-version)
      (str dep-name " is already up to date")
      (str "Updated " dep-name " from " previous-formatted " to " new-version))))

;; ------- Dependency Parsing -------
(defn parse-deps
  "Parses the dependencies from a given content string, filtering them based on a `namespace prefix`, and transforms dependency names to lowercase to make the process case-insensitive."
  [content namespace-prefix]
  (let [deps-pattern #"[a-zA-Z0-9/-]+\s+\{:mvn/version\s+\"[0-9.]+\""]
    (->> (re-seq deps-pattern content)
         (map #(let [dep-name     (first (re-find #"([a-zA-Z0-9/-]+)" %))
                     dep-ver-str %]
                 {:dep-name (str/lower-case dep-name) :dep-ver-str dep-ver-str}))
         (filter #(str/starts-with? (str/lower-case (:dep-name %)) (str/lower-case namespace-prefix))))))

;; ------- Version Update -------
(defn update-dep-version 
  "Updates the version of a given dependency by fetching the latest version from a GitHub repository."
  [dep-match]
  (let [fetched-version (gh/fetch-latest-version (:dep-name dep-match))
        new-version (if (str/starts-with? fetched-version "v")
                      (subs fetched-version 1)  ;; Strips "v" if present
                      fetched-version)]
    (assoc dep-match :new-version new-version)))

;; ------- Applying Updates -------
(defn apply-updates 
  "Applies version updates to the original content string based on a collection of dependency matches."
  [content deps-matches]
  (reduce (fn [acc {:keys [dep-ver-str new-version]}]
            (let [new-str (str/replace dep-ver-str #":mvn/version\s+\"[^\"]+\"" (str ":mvn/version \"" new-version "\""))]
              (str/replace acc dep-ver-str new-str)))
          content
          deps-matches))

;; ------- Rewriting Content -------
(defn update-version-in-content
  "Integrates all steps to parse dependencies from content, update their versions, and apply these updates to the original content."
  [content namespace-prefix]
  (let [namespace-prefix (ensure-str-ends-with namespace-prefix "/")
        parsed-deps  (parse-deps content namespace-prefix)
        updated-deps (map update-dep-version parsed-deps)]
    (doseq [{:keys [dep-name dep-ver-str new-version]} updated-deps] (println (print-update-status (str dep-name) (str dep-ver-str) (str new-version))))
    (apply-updates content updated-deps)))

;; ------- Update Deps -------
(defn update-deps-file 
  "Reads dependency definitions from a file, updates the versions of dependencies matching a given namespace prefix, and writes the updated definitions back to the file."
  [filename namespace-prefix]
  (let [content (slurp filename)
        updated-content (update-version-in-content content namespace-prefix)]
    (spit filename updated-content)))

;; ------- Testing here for now -------
(update-deps-file "./__mocks__/mockDeps.edn" "NeOviM")
(update-deps-file "./__mocks__/mockDeps.edn" "xadvent")