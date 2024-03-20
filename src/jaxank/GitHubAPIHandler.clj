(ns jaxank.GitHubAPIHandler 
  (:require [clojure.string :as str] 
            [clj-http.client :as client]
            [cheshire.core :as json]))


(defn get-owner-repo-v
  "Gets `owner/repo` from a dependency string."
  [full-dependency]
  (first (str/split full-dependency #" ")))
  
(defn get-owner-repo-s
  "Extracts the GitHub owner and repository from a Clojure dependency string."
  [full-dependency]
  (let [owner-repo (first (str/split (str/replace full-dependency "io.github." "") #"\s"))]
    owner-repo))

(defn fetch-latest-version
  "Fetches the latest release version of a GitHub repository."
  [repo]
  (let [url (str "https://api.github.com/repos/" (get-owner-repo-v repo) "/releases/latest")
        response (client/get url {:as :json})] ;; Removed the Authorization header for unauthenticated requests
    (if (= 200 (:status response))
      (-> response :body :tag_name)
      (println "Failed to fetch latest version for" repo "with status code" (:status response)))))

(defn fetch-latest-commit-sha
  "Fetches the SHA of the latest commit of a GitHub repository."
  [repo]
  (let [url (str "https://api.github.com/repos/" (get-owner-repo-s repo) "/commits/main") ;; Assuming you want the latest commit on the main branch
        token (System/getenv "GITHUB_TOKEN")
        headers (if token {"Authorization" (str "token " token)} {})
        response (client/get url {:headers headers :as :json})]
    (if (= 200 (:status response))
      (-> response :body :sha)
      (println "Failed to fetch latest commit SHA for" repo "with status code" (:status response)))))

