(ns jaxank.DepsUpdater
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [jaxank.DepsFileHandler :as version-updater]
            [jaxank.DepsFileHandler-SHA :as sha-updater])
  (:gen-class))

(def cli-options
  [["-v" "--version" "Update version numbers instead of SHA keys"]
   ["-p" "--path PATH" "Path to the deps.edn file" :default "./deps.edn"]
   ["-n" "--namespace NS" "Namespace (GitHub owner) to filter dependencies by" :default "JaxanK"]])

(defn -main
  [& args]
  (let [{:keys [options args errors]} (parse-opts args cli-options)
        path (:path options)
        owner (:namespace options)]
  (cond
    (:version options)
    (do
      (version-updater/update-deps-file path owner)
      (println "Version numbers updated for" owner "in" (str path "!")))
    :else
    (do
      (sha-updater/update-git-deps-in-file path owner)
      (println "SHA keys updated for" owner "in" (str path "!"))))))
