(ns jaxank.GitHubAPIHandler)

(defn fetch-latest-version [repo]
  ;; Placeholder for the actual API call logic
  ;; For now, it could return a mock response or simply nil
  nil)

(defn mock-fetch-latest-version [repo]
  (let [mock-responses {"jaxank/crazy-cool-plugin" {:mvm/version "v0.2.0"}
                        "jaxank/magic-lib" {:mvm/version "v0.2.1"}}
        response (get mock-responses repo)]
    (when response
      (:mvm/version response))))
