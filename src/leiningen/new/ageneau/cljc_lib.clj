(ns leiningen.new.ageneau.cljc-lib
  (:require [leiningen.new.templates :refer [renderer year date project-name
                                             ->files sanitize-ns name-to-path
                                             multi-segment sanitize]]
            [leiningen.core.project :as proj]
            [clojure.tools.cli :refer [parse-opts]]
            [leiningen.new.ageneau.interaction :as i]
            [leiningen.core.main :as main]
            [clojure.java.io :as io]
            [clojure.java.shell]
            [clojure.string :as str]))

(defn base-path [name]
  (-> (System/getProperty "leiningen.original.pwd")
      (io/file name) (.getPath)))

(defn lein-project []
  (-> "project.clj" base-path io/file str proj/read))

(defn git-config-get [key]
  (let [{:keys [exit out]} (clojure.java.shell/sh "git" "config" "--get" key)]
    (when (zero? exit)
      (-> out clojure.string/split-lines first))))

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


(def cli-options [["-n" "--no-git" "Do not initialise as git repository"]
                  ["-o" "--owner OWNER" "Github repository owner"]
                  ["-r" "--repo NAME" "Github repository name"]])

(defn ageneau.cljc-lib [name & args]
  (main/info "This template needs GitHub coordinates (REPO_OWNER/REPO_NAME) of the repo you'll be keeping this project in.")
  (let [group (-> (lein-project) :group)
        cli (parse-opts args cli-options)
        owner (or (get-in cli [:options :owner])
                  (git-config-get "github.user")
                  (i/ask-user "Enter REPO_OWNER: "))
        repo  (or (get-in cli [:options :repo]) (i/ask-user "Enter REPO_NAME: "))
        data  (prepare-data name owner repo)
        initialise-with-git? (-> cli :options :no-git not)]
    (->> data
         (prepare-files)
         (apply ->files))
    (when initialise-with-git?
      (clojure.java.shell/with-sh-dir (base-path (:name data))
        (clojure.java.shell/sh "git" "init")
        (clojure.java.shell/sh "git" "add" ".")
        (clojure.java.shell/sh "git" "commit" "-m" (clojure.string/join " " (concat ["lein" "new" group name] args)))))
    (main/info (str "Generated a project based on \"" group "\" template."))))
