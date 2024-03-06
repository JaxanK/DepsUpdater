(ns jaxank.GitHubAPIHandler
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(defn mock-fetch-latest-version [repo]
  (let [mock-responses {"jaxank/crazy-cool-plugin" {:mvn/version "v0.2.0"}
                        "jaxank/magic-lib" {:mvn/version "v0.2.1"}}
        response (get mock-responses repo)]
    (when response
      (:mvn/version response))))

(defn get-owner-repo
  "Dependency: owner/name {:mvn/version 'version number'}"
  [full-dependency]
  (let [split-string (first (clojure.string/split full-dependency #" "))]
    split-string))

(defn fetch-latest-version
  "Fetches the latest version of a GitHub repository based on a dependency string."
  [repo]
  (let [url (str "https://api.github.com/repos/" (get-owner-repo repo) "/releases/latest")
        response (client/get url {:headers {"Authorization" (str "Bearer " (System/getenv "GITHUB_TOKEN"))}
                                  :as :json})]
    (if (= 200 (:status response))
      (-> response :body :tag_name)
      (println "Failed to fetch latest version for" repo "with status code" (:status response)))))

; NOTE: This requires a system env variable named "GITHUB_TOKEN"
(fetch-latest-version "neovim/neovim {:mvn/version '0.0.1'}") 