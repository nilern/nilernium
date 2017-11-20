(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure       "1.8.0"]
                  [org.clojure/clojurescript "1.9.946" :scope "test"]

                  [cljsjs/virtual-dom "2.1.1-0" :scope "test"]
                  [org.clojure/core.async "0.3.465"]
                  [org.clojure/core.match "0.3.0-alpha5"]

                  [adzerk/boot-cljs        "2.1.3"  :scope "test"]
                  [adzerk/boot-reload      "0.5.2"  :scope "test"]
                  [adzerk/boot-cljs-repl   "0.3.3"  :scope "test"]
                  [com.cemerick/piggieback "0.2.2"  :scope "test"]
                  [weasel                  "0.7.0"  :scope "test"]
                  [org.clojure/tools.nrepl "0.2.13" :scope "test"]

                  [pandeiro/boot-http "0.8.3"]])

(task-options!
  pom {:project 'nilernium
       :version "0.1.0"})

(require '[adzerk.boot-cljs      :refer [cljs]]
         '[adzerk.boot-reload    :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]

         '[pandeiro.boot-http :refer [serve]])

(deftask dev
  []
  (comp (watch)
        (reload :ids #{"js/main"})
        (cljs-repl :ids #{"js/main"})
        (cljs :ids #{"js/main"})
        (sift :to-asset #{#"^js/.*"})
        (target)
        (serve :dir "target")))
