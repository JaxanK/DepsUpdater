(ns jaxank.DepsUpdater
  (:require [clojure.string :as str]
            [jaxank.DepsFileHandler :as UpdateGitHubDeps])
  (:gen-class))


(defn -main
  "main function run at start... first argument should be deps.edn filepath "
  [& args]
  (let [path (first args)
        owner (if (str/blank? (second args)) "JaxanK" (second args))]
    (UpdateGitHubDeps/update-deps-file path owner)
    (println owner "deps updated!")))
