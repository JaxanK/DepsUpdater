(ns jaxank.GitHubAPIHandler)

(defn mock-fetch-latest-version [repo]
  (let [mock-responses {"jaxank/crazy-cool-plugin" {:mvn/version "v0.2.0"}
                        "jaxank/magic-lib" {:mvn/version "v0.2.1"}}
        response (get mock-responses repo)]
    (when response
      (:mvn/version response))))

(defn fetch-latest-version [repo]
  ;; Placeholder for the actual API call logic
  ;; For now, it could return a mock response or simply nil
  (mock-fetch-latest-version repo))

