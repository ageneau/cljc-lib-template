(ns leiningen.new.ageneau.cljc-lib
  (:require [leiningen.new.templates :refer [renderer year date project-name
                                             ->files sanitize-ns name-to-path
                                             multi-segment sanitize]]
            [leiningen.core.main :as main]
            [clojure.string :as str]))


(defn prepare-data [name owner repo]
  (let [namespace    (project-name name)
        escaped-name (-> name
                         (str/replace #"\." "\\\\.")
                         (str/replace #"/" "\\\\\\\\/"))]
    {:raw-name      name
     :name          (project-name name)
     :namespace     namespace
     :package       (sanitize namespace)
     :nested-dirs   (name-to-path namespace)
     :repo-path     (str owner "/" repo)
     :version-regex (pr-str (format "s/\\\\[%s \"[0-9.]*\"\\\\]/[%s \"${:version}\"]/" escaped-name escaped-name))
     :debug         (System/getenv "DEBUG")}))


(defn prepare-files
  "Generates arguments for ->files. Extracted for testing."
  [data]
  (let [render (renderer "library")]
    (main/debug "Template data:" data)
    (concat
      [data
       ["project.clj" (render "project.clj" data)]
       ["README.md" (render "README.md" data)]
       ["LICENSE" (render "LICENSE" data)]
       ["CHANGELOG.md" (render "CHANGELOG.md" data)]
       [".gitignore" (render "_gitignore" data)]
       [".travis.yml" (render "_travis.yml" data)]
       [".github/workflows/clojure.yml" (render "_github/workflows/clojure.yml" data)]
       ["src/{{nested-dirs}}/core.cljc" (render "src/_namespace_/core.cljc" data)]
       ["test/{{nested-dirs}}/core_test.cljc" (render "test/_namespace_/core_test.cljc" data)]
       ["test/{{nested-dirs}}/test_runner.cljs" (render "test/_namespace_/test_runner.cljs" data)]])))


(defn ask-user [prompt]
  (print prompt)
  (flush)
  (read-line))


(defn ageneau.cljc-lib [name]
  (main/info "This template needs GitHub coordinates (REPO_OWNER/REPO_NAME) of the repo you'll be keeping this project in.")
  (let [owner (ask-user "Enter REPO_OWNER: ")
        repo  (ask-user "Enter REPO_NAME: ")
        data  (prepare-data name owner repo)]
    (->> data
         (prepare-files)
         (apply ->files))
    (main/info "Generated a project based on \"cljc-lib\" template.")))
