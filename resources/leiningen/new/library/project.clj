(defproject {{raw-name}} "0.0.0"
  :description "FIXME: write description"
  :url "https://github.com/{{repo-path}}"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"]]
  :plugins [[lein-cloverage "1.2.1"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]
            [lein-cljsbuild "1.1.8"]
            [lein-doo "0.1.11"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/clojurescript "1.10.773" :scope "provided"]]}}
  :doo {:build "test"}
  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test" "target/classes"]
             :compiler {:output-to "target/js/testable.js"
                        :output-dir "target/js/out"
                        :main {{namespace}}.test-runner
                        :optimizations :none
                        :pretty-print  true
                        :source-map true}}]}
  :deploy-repositories [["releases" :clojars]]
  :aliases {"update-readme-version" ["shell" "sed" "-i" {{{version-regex}}} "README.md"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
