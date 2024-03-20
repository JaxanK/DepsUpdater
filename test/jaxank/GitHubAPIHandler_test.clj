(ns jaxank.GitHubAPIHandler-test
  (:require [clojure.test :refer :all]
            [jaxank.GitHubAPIHandler :refer :all]))


(deftest get-owner-test (is (get-owner-repo-v "neovim/neovim {:mvn/version '0.0.1'}") "neovim/neovim"))
(deftest fetching (is (fetch-latest-version "xadvent/test {:mvn/version 0.0.1}") "v0.1.0"))

(run-tests)
