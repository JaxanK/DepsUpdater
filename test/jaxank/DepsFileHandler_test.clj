(ns jaxank.DepsFileHandler-test
  (:require [clojure.test :refer :all]
            [jaxank.DepsFileHandler :refer :all]))
        

(deftest printing
  (testing "Printing of updates")
  (is (print-update-status "test" "mvn/version: 0.0.1" "v0.1.0") "Updated test from 0.0.1 to 0.1.0"))

(deftest parsing
  (testing "Dependency parsing and filtering for xadvent"
    (let [content (slurp "./__mocks__/mockDeps.edn")
          expected [{:dep-name "xadvent/test" 
                     :dep-ver-str "xadvent/test {:mvn/version \"0.0.1\""}] 
          result (parse-deps content "xadvent")]
      (is (= expected result)))))

(deftest apply-updates-test
  (testing "Updates dependency versions in content"
    (let [content "org.clojure/clojure {:mvn/version \"1.10.1\"}\ncom.fasterxml.jackson.core/jackson-databind {:mvn/version \"2.9.10\"}"
          deps-matches [{:dep-ver-str "org.clojure/clojure {:mvn/version \"1.10.1\"}" :new-version "1.10.2"}
                        {:dep-ver-str "com.fasterxml.jackson.core/jackson-databind {:mvn/version \"2.9.10\"}" :new-version "2.9.11"}]
          updated-content (apply-updates content deps-matches)
          expected "org.clojure/clojure {:mvn/version \"1.10.2\"}\ncom.fasterxml.jackson.core/jackson-databind {:mvn/version \"2.9.11\"}"]
      (is (= expected updated-content)))))


(run-tests)