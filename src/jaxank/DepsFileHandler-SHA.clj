(ns jaxank.DepsFileHandler-SHA
  (:require [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as gh]))


(defn find-all-deps
  "Finds all Dependencies starting with \"io.github.\" matches until the next \"}\"."
  [content]
  (let [reg #"io\.github\.[^\}]+?\}"
        findings (re-seq reg content)]
    findings))

(defn parse-git-deps
  "Parses git dependencies and returns `full-name` and `sha`"
  [deps]
  (keep (fn [dep]
          (when-let [[_ full-name sha] (re-find #"(io\.github\.[^\s]+)\s+\{:git/sha\s+\"([^\"]+)\"" dep)]
            {:name full-name, :current-sha sha}))
        deps))

(defn matches-namespace
  "Checks if the given dependency name matches the namespace prefix."
  [dep-name namespace-prefix]
  (re-matches (re-pattern (str "^io\\.github\\." (str/replace namespace-prefix #"\." "\\.") "\\b.*")) dep-name))

(defn apply-git-sha-updates
  "Applies git SHA updates to the original content string based on a collection of git dependency matches."
  [content git-deps-matches]
  (reduce (fn [acc {:keys [name current-sha new-sha]}]
            ;; Construct the pattern to find the exact :git/sha entry for replacement.
            (let [pattern (re-pattern (str (java.util.regex.Pattern/quote name) "\\s+\\{:git/sha\\s+\"" (java.util.regex.Pattern/quote current-sha) "\"}"))
                  replacement (str name " {:git/sha \"" new-sha "\"}") ;; Construct the replacement string.
                  ]
              (str/replace acc pattern replacement))) ;; Perform the replacement.
          content
          git-deps-matches))

(defn process-dependency
  "Attempts to update the SHA for a single dependency and provides feedback, including in case of errors."
 [{:keys [name current-sha]}] 
  (let [repo-name (-> name (str/replace "io.github." "") (str/split #"\s") (first))]   
    (try
      (let [new-sha-full (gh/fetch-latest-commit-sha name)
            new-sha (if new-sha-full (subs new-sha-full 0 7))] ;; Shorten SHA strings for better readability.
        (cond
          (and new-sha (not= current-sha new-sha))
          (do
            (println "Updating" repo-name ": current SHA" current-sha "=> new SHA" new-sha)
            {:name name :current-sha current-sha :new-sha new-sha})

          :else
          (do
            (println repo-name "is already up to date with SHA" current-sha)
            nil))) ;; Return nil if no update is needed.
      (catch Exception e
        (println "Error updating" repo-name ": " (.getMessage e))
        nil)))) ;; Return nil on error.


(defn update-git-deps
  "Fetches new SHAs for git dependencies within a specific namespace and prepares updates with feedback."
  [content namespace-prefix]
  (let [all-deps (find-all-deps content)
        parsed-deps (parse-git-deps all-deps)]
    (->> parsed-deps
         (filter #(matches-namespace (:name %) namespace-prefix))
         (mapv #(process-dependency %)) ;; Mapv for eager evaluation.
         (remove nil?)))) ;; Filter out nil entries representing no-update or errors.



(defn update-git-deps-in-file
  "Integrates steps to fetch new SHAs, apply updates, and rewrite deps.edn content."
  [filename namespace-prefix]
  (let [content (slurp filename)
        git-deps-updates (update-git-deps content namespace-prefix)
        updated-content (apply-git-sha-updates content git-deps-updates)]
    (spit filename updated-content))) 
