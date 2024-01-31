(ns jaxank.DepsUpdater
  (:gen-class))

(defn UpdateGitHubDeps [prefix depsEDN-file]
  ;read in current deps.edn file from depsEDN-file

  ;Find all instances of dependencies that are github libraries with supplied argument prefix wildcard (i.e. "jaxank/")
  ;    Note make prefix be caps insensitive, JAXANK for jaxanK should both work.

  ;Use information for each dependency to find the link and access Github online api to get latest version (note repos will have to be public I think, since no credentials here).
  ;   alternatively could use tools.deps find-latest-version tool... may be better... in fact it is worth looking into since there are other deps tools that we may want to add later

  ;for each dependency of pattern found, update the deps.edn map we read in and this spit it back out into the file and replace the file with the new file.

  ;Could try to find way to kill and restart the repl... may be some terminal command to facilitate, but probably not
  )

(defn -main
  "main function run at start... first argument should be deps.edn filepath "
  [& args]
  (UpdateGitHubDeps "JaxanK/" (first args))
  (println "JaxanK deps updated!")
  
  )
