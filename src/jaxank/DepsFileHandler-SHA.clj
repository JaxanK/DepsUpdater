(ns jaxank.DepsFileHandler-SHA
  (:require [clojure.string :as str]
            [jaxank.GitHubAPIHandler :as gh]))


(defn find-all-deps
  "Finds all Dependencies starting with \"io.github.\" matches until the next \"}\"."
  [content]
  (let [reg #"io\.github\.[^\}]+?\}"
        findings (re-seq reg content)]
    findings))

(defn make-namespace-check
  "Creates a Regex for matching the given namespace-prefix in a dependency string."
  [namespace-prefix]
  (let [prefix (re-pattern (str "io\\.github\\." (str/replace namespace-prefix #"\." "\\.")))]
    prefix))

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

(defn update-git-deps
  "Fetches new SHAs for git dependencies within a specific namespace, prepares updates."
  [content namespace-prefix]
  (let [namespace-check (make-namespace-check namespace-prefix)
        all-deps (find-all-deps content)
        filtered-deps (filter #(matches-namespace (:name %) namespace-prefix) (parse-git-deps all-deps))]
    (map (fn [{:keys [name current-sha]}] ;; Map to fetch new SHAs and prepare update info.
           (if-let [new-sha (subs (gh/fetch-latest-commit-sha name) 0 7)]
             {:name name :current-sha current-sha :new-sha new-sha}))
         filtered-deps)))



(defn update-git-deps-in-file
  "Integrates steps to fetch new SHAs, apply updates, and rewrite deps.edn content."
  [filename namespace-prefix]
  (let [content (slurp filename)
        git-deps-updates (update-git-deps content namespace-prefix)
        updated-content (apply-git-sha-updates content git-deps-updates)] ;; Apply SHA updates.
    (spit filename updated-content) ;; Write the updated content back to the file.
    (println "Updated git dependencies in" filename "for" namespace-prefix)))

;; ---- Testing ----
; (update-git-deps-in-file "./__mocks__/mockDeps.edn" "xadvent")