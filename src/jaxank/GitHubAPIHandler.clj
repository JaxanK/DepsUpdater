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
  "Gets `owner/repo` from a dependency string."
  [full-dependency]
  (first (clojure.string/split full-dependency #" ")))

(defn fetch-latest-version
  "Fetches the latest release version of a GitHub repository."
  [repo]
  (let [url (str "https://api.github.com/repos/" (get-owner-repo repo) "/releases/latest")
        response (client/get url {:as :json})] ;; Removed the Authorization header for unauthenticated requests
    (if (= 200 (:status response))
      (-> response :body :tag_name)
      (println "Failed to fetch latest version for" repo "with status code" (:status response)))))


;; (fetch-latest-version "neovim/neovim {:mvn/version '0.0.1'}") 